package br.com.pismo.product.sql

import io.vertx.core.Future
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.groovy.core.CompositeFuture
import io.vertx.groovy.ext.asyncsql.MySQLClient
import io.vertx.groovy.ext.sql.SQLConnection

class SQLClient {
	
	static final Logger logger = LoggerFactory.getLogger(SQLClient)
	
	final MySQLClient client

	SQLClient(MySQLClient client) {
		this.client = client
	}
	
	Future<Void> updateMany(String sql, List<List> objects) {
		Future future = Future.future()
		client.getConnection({ Future<SQLConnection> connectionFuture ->
			if (connectionFuture.succeeded()) {
				SQLConnection connection = connectionFuture.result()
				connection.setAutoCommit(false, { Future<Void> commitFuture ->
					if (commitFuture.succeeded()) {
						List<Future> futures = objects.collect { params ->
							update(sql, params)
						}
						CompositeFuture result = CompositeFuture.all(futures)
						if (result.succeeded()) {
							connection.commit({ future.complete() })
						} else {
							connection.rollback({ ex ->
								logger.error(ex.message, ex)
							})
							future.fail(result.cause())
						}
						
					} else {
						future.fail(commitFuture.cause())
					}
				})
			} else {
				future.fail(connectionFuture.cause())
			}
		})
		future
	}
	
	Future<Void> insertMany(String sql, List<List> objects) {
		updateMany(sql, objects)
	}
	
	Future update(String sql, List params) {
		client.getConnection({ Future<SQLConnection> connectionFuture ->
			if (connectionFuture.succeeded()) {
				SQLConnection connection = connectionFuture.result()
				update(sql, params, connection)
			} else {
				Future.failedFuture(connectionFuture.cause())
			}
		})
	}
	
	Future update(String sql, List params, SQLConnection connection) {
		Future future = Future.future()
		connection.updateWithParams(sql, params, { Future<Map> updateFuture ->
			if (updateFuture.succeeded()) {
				def updated = updateFuture.result().updated
				if (updated==0) {
					future.complete(updated)
				} else {
					future.fail('No item was updated')
				}
			} else {
				future.fail(updateFuture.cause())
			}
		})
		future
	}
	
	Future delete(String sql, List params) {
		update(sql, params)
	}
	
	Future insert(String sql, List params) {
		update(sql, params)
	}
}

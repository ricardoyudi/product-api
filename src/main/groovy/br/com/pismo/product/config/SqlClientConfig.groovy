package br.com.pismo.product.config

import io.vertx.groovy.core.Vertx
import io.vertx.groovy.ext.asyncsql.MySQLClient

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import br.com.pismo.product.sql.SQLClient;

@Configuration
class SqlClientConfig {

	@Autowired
	Vertx vertx

	@Bean
	SQLClient sqlClient() {
		def mysql = MySQLClient.createShared(vertx, config())
		new SQLClient(mysql)
	}

	Map config() {
		[
			host: 'localhost',
			port: 3321,
			username: 'root',
			password: 'root',
			database: 'products-db'
		]
	}
}

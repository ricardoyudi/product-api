package br.com.pismo.product.config

import io.vertx.core.Future
import io.vertx.groovy.core.CompositeFuture;
import io.vertx.groovy.core.Vertx
import io.vertx.groovy.ext.web.Router
import io.vertx.groovy.ext.web.RoutingContext

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import br.com.pismo.product.sql.SQLClient
import br.com.pismo.product.validators.RequestValidator
import groovy.json.JsonOutput;

@Configuration
class RouterConfig {
	
	private static final String JSON = 'application/json'
	
	@Autowired
	Vertx vertx

	@Autowired
	SQLClient sqlClient
	
	@Bean
	Router router(){
		def router = Router.router(vertx)
		
		router.route()
	}
	
	void routeProductsAloccation(Router router){
		router.post()
			.consumes(JSON)
			.produces(JSON)
			.handler(new RequestValidator(allocationValidator))
			
		router.post()
			.consumes(JSON)
			.produces(JSON)
			.handler(null)
	}
	
	Closure allocationValidator = { Map json ->
		def sale = json.subMap(['id', 'products'])
		if (!sale.id) {
			throw new RuntimeException('Invalid Sale Id')
		}
		
		if (!sale.products) {
			throw new RuntimeException('Invalid Products')
		}
		
		def products = sale.products.collect {
			def product = it.subMap(['id', 'quantity'])
			if (!product.id || !product.quantity) {
				throw new RuntimeException("Invalid Product. [id: $product.id, quantity: $product.quantity]")
			}
			product
		}
		[id: sale.id, products: products]
	}
	
	Closure allocationHandler = { RoutingContext ctx ->
		def sqlUpdate = '''
			update
				quantity = quantity - ?
			from
				products
			where
				id = ?
				and quantity - ? >= 0
		'''
		
		def sqlInsert = '''
			insert (
				product_id,
				quantity,
				sale_id,
				status
			) values (?, ?, ?, ?)
		'''
		
		def object = ctx.get(RequestValidator.OBJECT)
		def updateParams = object.products.collect { [it.quantity, it.id, it.quantity] }
		def updateFuture = sqlClient.updateMany(sqlUpdate, updateParams)
		
		def insertParams = object.products.collect { [it.id, it.quantity, object.id, 'PENDING'] }
		def insertFuture = sqlClient.insertMany(sqlInsert, insertParams)
		
		def future = CompositeFuture.all(updateFuture, insertFuture)
		
		if (future.succeeded()) {
			ctx.response()
				.setStatusCode(200)
				.end()
		} else {
			ctx.response()
				.setStatusCode(500)
				.write(JsonOutput.toJson([message: future.cause().message]))
				.end()
		}
	}
}

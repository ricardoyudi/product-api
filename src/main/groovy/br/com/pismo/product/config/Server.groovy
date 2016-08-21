package br.com.pismo.product.config

import io.vertx.groovy.core.Vertx
import io.vertx.groovy.ext.web.Router

import javax.annotation.PostConstruct

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class Server {

	@Autowired
	Vertx vertx
	
	@Autowired
	Router router

	@PostConstruct
	void start(){
		def server = vertx.createHttpServer()
		server.requestHandler(router.&accept)

		server.listen(8080)
	}
}

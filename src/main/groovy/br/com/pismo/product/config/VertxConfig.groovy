package br.com.pismo.product.config

import io.vertx.groovy.core.Vertx

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class VertxConfig {

	@Bean
	Vertx vertx(){
		Vertx.vertx()
	}
}

package br.com.pismo.product.validators

import io.vertx.core.Handler
import io.vertx.groovy.ext.web.RoutingContext

class RequestValidator implements Handler<RoutingContext> {

	static final String OBJECT = 'OBJECT_REQUEST'
	final Validator validator

	RequestValidator(Validator validator) {
		this.validator = validator
	}

	void handle(RoutingContext ctx) {
		def json = ctx.bodyAsJson
		try {
			def object = validator.validate(json)
			ctx.put(OBJECT, object)
			ctx.next()
		} catch(ex) {
			ctx.fail(ex)
		}
	}
}

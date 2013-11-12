package main.java.handler;

import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

public class NoMatchHandler implements Handler<HttpServerRequest> {

	@Override
	public void handle(final HttpServerRequest request) {
		request.response().setStatusCode(400).setStatusMessage("invalid request").end();

	}
}

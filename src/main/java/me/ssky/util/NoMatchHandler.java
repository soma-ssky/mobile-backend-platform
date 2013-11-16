package main.java.me.ssky.util;

import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

public class NoMatchHandler implements Handler<HttpServerRequest> {

	@Override
	public void handle(HttpServerRequest request) {
		System.out.println(request.path());
		request.response().setStatusCode(400).setStatusMessage("Bad Request").end();
	}

}

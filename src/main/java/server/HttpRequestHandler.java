package main.java.server;

import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;

public class HttpRequestHandler implements Handler<HttpServerRequest> {
	@Override
	public void handle(HttpServerRequest request) {
		HttpServerResponse response = request.response();
		switch (request.method()) {
		case "POST":
			break;
		case "GET":
			break;
		case "PUT":
			break;
		case "DELETE":
			break;
		default:
			response.end("invalid method.");
		}

	}
}

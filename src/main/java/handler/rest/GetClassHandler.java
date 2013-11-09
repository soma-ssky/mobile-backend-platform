package main.java.handler.rest;

import main.java.handler.client.ClassHandler;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

public class GetClassHandler implements Handler<HttpServerRequest> {

	@Override
	public void handle(final HttpServerRequest request) {

		request.bodyHandler(new Handler<Buffer>() {
			@Override
			public void handle(Buffer buffer) {
				JsonObject data = new JsonObject(buffer.toString());
				data.putString("_method", "GET");
				String collection = request.path().split("/")[3];
				new ClassHandler().handleRetrieve(request, data, collection);
			}
		});
	}
}

package main.java.handler.rest;

import main.java.util.Util;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

public class DeleteHandler implements Handler<HttpServerRequest> {

	@Override
	public void handle(final HttpServerRequest request) {
		request.bodyHandler(new Handler<Buffer>() {
			@Override
			public void handle(Buffer buffer) {
				String collection = request.path().split("/")[3];
				String objectId = request.path().split("/")[4];

				JsonObject option = Util.deleteObjectOptionById(collection, objectId);
				Util.processObjectRequestViaEB(request, Util.OBJECT_MANAGER_ADDRESS, option, 200);
			}
		});
	}

}

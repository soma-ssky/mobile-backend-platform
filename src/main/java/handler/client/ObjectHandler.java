package main.java.handler.client;

import main.java.util.Util;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

public class ObjectHandler implements Handler<HttpServerRequest> {

	@Override
	public void handle(final HttpServerRequest request) {
		final String collection = request.path().split("/")[3];
		final String objectId = request.path().split("/")[4];
		request.bodyHandler(new Handler<Buffer>() {
			@Override
			public void handle(Buffer buffer) {
				JsonObject data = new JsonObject(buffer.toString());
				String method = data.getString("_method");
				JsonObject objNew = Util.removeInvalidField(data);

				JsonObject option = new JsonObject();
				switch (method) {
				case "GET":
					option = Util.fetchObjectOptionById(collection, objectId);
					break;
				case "PUT":
					option = Util.updateObjectOptionById(collection, objectId, objNew);
					break;
				case "DELETE":
					option = Util.deleteObjectOptionById(collection, objectId);
				}
				Util.processObjectRequestViaEB(request, Util.OBJECT_MANAGER_ADDRESS, option, 200);
			}
		});
	}
}
package main.java.handler.rest;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.json.JsonObject;

public class PutHandler implements Handler<HttpServerRequest> {
	private EventBus eb;

	public PutHandler(EventBus eb) {
		this.eb = eb;

	}

	@Override
	public void handle(final HttpServerRequest request) {
		request.bodyHandler(new Handler<Buffer>() {
			@Override
			public void handle(Buffer buffer) {
				String collection = request.path().split("/")[3];
				String objectId = request.path().split("/")[4];
				JsonObject data = new JsonObject(buffer.toString());
				updateObject(request, collection, objectId, data);
			}
		});
	}

	private void updateObject(final HttpServerRequest request, final String collection, final String objectId, JsonObject data) {
		JsonObject option = new JsonObject().putString("collection", collection).putString("action", "update");
		option.putObject("matcher", new JsonObject().putString("_id", objectId)).putObject("data", data);
		eb.send("ssky.object", option, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> result) {
				HttpServerResponse response = request.response();
				response = putHeaders(response, result.body().encode().length(), 200, "OK");
				response.end(result.body().encode());
			}
		});
	}

	private HttpServerResponse putHeaders(HttpServerResponse response, int length, int statusCode, String statusMessage) {
		response.putHeader("Access-Control-Allow-Origin", "*");
		response.putHeader("Access-Control-Request-Method", "*");
		response.putHeader("Content-Type", "application/json; charset=utf-8");
		response.putHeader("Status", statusCode + " " + statusMessage);
		response.putHeader("Content-Length", "" + length);
		response.setStatusCode(statusCode);
		response.setStatusMessage(statusMessage);
		return response;
	}

}

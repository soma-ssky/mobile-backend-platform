package main.java.handler;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.json.JsonObject;

public class PostObjectHandler implements Handler<HttpServerRequest> {
	private EventBus eb;

	public PostObjectHandler(EventBus eb) {
		this.eb = eb;
	}

	@Override
	public void handle(final HttpServerRequest request) {
		request.bodyHandler(new Handler<Buffer>() {
			@Override
			public void handle(Buffer buffer) {
				JsonObject data = new JsonObject(buffer.toString());
				switch (data.getString("_method")) {
				case "GET":
					handleFetch(request, data);
					break;
				case "PUT":
					handleUpdate(request, data);
					break;
				case "DELETE":
					handleDelete(request, data);
				}
			}
		});
	}

	private void handleFetch(final HttpServerRequest request, JsonObject data) {
		removeInvalidField(data);
		final String collection = request.path().split("/")[3];
		final String objectId = request.path().split("/")[4];
		JsonObject fetchOption = new JsonObject().putString("action", "fetch").putString("collection", collection);
		fetchOption.putObject("matcher", new JsonObject().putString("_id", objectId)).putObject("data", data);
		eb.send("ssky.object", fetchOption, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> result) {
				if (result.body().getString("status") != null && result.body().getString("status").equals("error")) {
					request.response().end(result.body().encode());
					return;
				}
				HttpServerResponse response = setResponseHeaders(request, collection, result, 200, "OK");
				response.end(result.body().encode());
			}
		});
	}

	public void handleDelete(final HttpServerRequest request, JsonObject data) {
		removeInvalidField(data);
		final String collection = request.path().split("/")[3];
		final String objectId = request.path().split("/")[4];
		JsonObject deleteOption = new JsonObject().putString("action", "delete").putString("collection", collection);
		deleteOption.putObject("matcher", new JsonObject().putString("_id", objectId)).putObject("data", data);
		eb.send("ssky.object", deleteOption, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> result) {
				if (result.body().getString("status") != null && result.body().getString("status").equals("error")) {
					request.response().end(result.body().encode());
					return;
				}
				HttpServerResponse response = setResponseHeaders(request, collection, result, 200, "OK");
				response.end(result.body().encode());
			}
		});
	}

	public void handleUpdate(final HttpServerRequest request, JsonObject data) {
		removeInvalidField(data);
		final String collection = request.path().split("/")[3];
		final String objectId = request.path().split("/")[4];
		JsonObject updateOption = new JsonObject().putString("action", "update").putString("collection", collection);
		updateOption.putObject("matcher", new JsonObject().putString("_id", objectId)).putObject("data", data);
		eb.send("ssky.object", updateOption, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> result) {
				if (result.body().getString("status") != null && result.body().getString("status").equals("error")) {
					request.response().end(result.body().encode());
					return;
				}
				HttpServerResponse response = setResponseHeaders(request, collection, result, 200, "OK");
				response.end(result.body().encode());
			}
		});
	}

	private HttpServerResponse setResponseHeaders(HttpServerRequest request, String collection, Message<JsonObject> result, int code, String message) {
		HttpServerResponse response = request.response();
		response.putHeader("Access-Control-Allow-Origin", "*");
		response.putHeader("Access-Control-Request-Method", "*");
		response.putHeader("Content-Type", "application/json; charset=utf-8");
		response.putHeader("Status", code + " " + message);
		response.putHeader("Content-Length", "" + result.body().encode().length());
		response.setStatusCode(code);
		response.setStatusMessage(message);
		response.putHeader("Location", request.headers().get("Host") + "/" + collection + "/" + result.body().getString("objectId"));
		return response;
	}

	private void removeInvalidField(JsonObject data) {
		data.removeField("_ApplicationId");
		data.removeField("_ClientVersion");
		data.removeField("_InstallationId");
		data.removeField("_JavaScriptKey");
		data.removeField("_method");
	}

}

package main.java.handler.client;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

public class BatchHandler implements Handler<HttpServerRequest> {
	private EventBus eb;

	public BatchHandler(EventBus eb) {
		this.eb = eb;
	}

	@Override
	public void handle(final HttpServerRequest request) {
		request.bodyHandler(new Handler<Buffer>() {
			@Override
			public void handle(Buffer buffer) {
				JsonObject data = new JsonObject(buffer.toString());
				JsonArray requestArr = data.getArray("requests");
				String collection = ((JsonObject) requestArr.get(0)).getString("path").split("/")[3];
				String method = ((JsonObject) requestArr.get(0)).getString("method");
				removeInvalidField(data);

				switch (method) {
				case "POST":
					handleSaveAll(request, requestArr, collection);
					break;
				case "DELETE":
					handleDeleteAll(request, requestArr, collection);
					break;
				}
			}
		});
	}

	private void handleSaveAll(final HttpServerRequest request, JsonArray requestArr, String collection) {
		String currentTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date());
		Iterator<Object> itr = requestArr.iterator();
		JsonArray savedArray = new JsonArray();
		while (itr.hasNext()) {
			JsonObject body = ((JsonObject) itr.next()).getObject("body");
			body.putString("createdAt", currentTime);
			body.putString("updatedAt", currentTime);
			savedArray.addObject(body);
		}

		JsonObject saveAllOption = new JsonObject().putString("action", "saveall").putString("collection", collection).putString("documents", savedArray.encode());
		eb.send("ssky.object", saveAllOption, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> results) {
				HttpServerResponse response = setResponseHeaders(request, results, 200, "OK");
				response.end(results.body().getArray("results").encode());
			}
		});
	}

	public void handleDeleteAll(final HttpServerRequest request, JsonArray requestArr, String collection) {
		JsonArray orMatcher = new JsonArray();
		Iterator<Object> itr = requestArr.iterator();
		while (itr.hasNext()) {
			orMatcher.add(new JsonObject().putString("_id", ((JsonObject) itr.next()).getString("path").split("/")[4]));
		}

		JsonObject deleteAllOption = new JsonObject().putString("collection", collection).putString("action", "deleteall");
		deleteAllOption.putObject("matcher", new JsonObject().putArray("$or", orMatcher));
		eb.send("ssky.object", deleteAllOption, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> result) {
				HttpServerResponse response = setResponseHeaders(request, result, 200, "OK");
				response.end(result.body().getArray("results").encode());
			}
		});
	}

	private HttpServerResponse setResponseHeaders(HttpServerRequest request, Message<JsonObject> result, int code, String message) {
		HttpServerResponse response = request.response();
		response.putHeader("Access-Control-Allow-Origin", "*");
		response.putHeader("Access-Control-Request-Method", "*");
		response.putHeader("Content-Type", "application/json; charset=utf-8");
		response.putHeader("Status", code + " " + message);
		response.putHeader("Content-Length", "" + result.body().getArray("results").encode().length());
		response.setStatusCode(code);
		response.setStatusMessage(message);
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

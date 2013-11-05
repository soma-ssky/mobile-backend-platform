package main.java.handler;

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

public class PostBatchHandler implements Handler<HttpServerRequest> {
	private EventBus eb;

	public PostBatchHandler(EventBus eb) {
		this.eb = eb;
	}

	@Override
	public void handle(final HttpServerRequest request) {
		request.bodyHandler(new Handler<Buffer>() {
			@Override
			public void handle(Buffer buffer) {
				JsonObject data = new JsonObject(buffer.toString());
				switch (((JsonObject) data.getArray("requests").get(0)).getString("method")) {
				case "POST":
					handleSaveAll(request, data);
					break;
				case "DELETE":
					handleDeleteAll(request, data);
					break;
				}
			}
		});
	}

	private void handleSaveAll(final HttpServerRequest request, JsonObject data) {
		removeInvalidField(data);
		String current = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date());

		JsonArray array = data.getArray("requests");
		String collection = ((JsonObject) array.get(0)).getString("path").split("/")[3];
		Iterator<Object> itr = array.iterator();
		JsonArray savedArray = new JsonArray();
		while (itr.hasNext()) {
			JsonObject body = ((JsonObject) itr.next()).getObject("body");
			body.putString("createdAt", current);
			body.putString("updatedAt", current);
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

	public void handleDeleteAll(final HttpServerRequest request, JsonObject data) {
		removeInvalidField(data);
		JsonArray arr = data.getArray("requests");
		JsonArray orMatcher = new JsonArray();
		Iterator<Object> itr = arr.iterator();
		while (itr.hasNext()) {
			orMatcher.add(new JsonObject().putString("_id", ((JsonObject) itr.next()).getString("path").split("/")[4]));
		}

		JsonObject deleteAllOption = new JsonObject().putString("action", "delete");
		deleteAllOption.putString("collection", ((JsonObject) arr.get(0)).getString("path").split("/")[3]);
		deleteAllOption.putObject("matcher", new JsonObject().putArray("$or", orMatcher)).putString("action", "deleteall");
		System.out.println(deleteAllOption.encode());
		eb.send("ssky.object", deleteAllOption, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> result) {
				HttpServerResponse response = setResponseHeaders(request, result, 200, "OK");
				response.end(result.body().encode());
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

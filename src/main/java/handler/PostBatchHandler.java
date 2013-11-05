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
				handleSaveAll(request, data);
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

	private HttpServerResponse setResponseHeaders(HttpServerRequest request, Message<JsonObject> results, int code, String message) {
		HttpServerResponse response = request.response();
		response.putHeader("Access-Control-Allow-Origin", "*");
		response.putHeader("Access-Control-Request-Method", "*");
		response.putHeader("Content-Type", "application/json; charset=utf-8");
		response.putHeader("Status", code + " " + message);
		response.putHeader("Content-Length", "" + results.body().getArray("results").encode().length());
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

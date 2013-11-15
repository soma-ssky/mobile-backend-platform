package main.java.me.ssky.objects;

import main.java.me.ssky.server.ServerMain;
import main.java.me.ssky.util.ServerUtils;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

public class BatchingObjectHandler implements Handler<HttpServerRequest> {

	@Override
	public void handle(final HttpServerRequest request) {
		request.bodyHandler(new Handler<Buffer>() {
			@Override
			public void handle(Buffer buffer) {
				JsonObject data = new JsonObject(buffer.toString());
				final JsonArray requests = data.getArray("requests");
				final JsonArray responses = new JsonArray();
				for (int i = 0; i < requests.size(); i++) {
					JsonObject option = getBatchingOption((JsonObject) requests.get(i));
					ServerMain._vertx.eventBus().send(ServerUtils.OBJECT_MANAGER_ADDRESS, option, new Handler<Message<JsonObject>>() {
						@Override
						public void handle(Message<JsonObject> result) {
							JsonObject response = new JsonObject();
							if (result.body().getString("error") != null) {
								response.putObject("error", result.body());
							}
							response.putObject("success", result.body());
							responses.add(response);
						}
					});
				}

				ServerMain._vertx.setPeriodic(10, new Handler<Long>() {
					@Override
					public void handle(Long timerId) {
						// cancel timer with timer id 
						if (responses.size() == requests.size()) {
							request.response().headers().set(ServerUtils.responseHeaders(200));
							request.response().end(responses.encode());
							ServerMain._vertx.cancelTimer(timerId);
						}
					}
				});
			}
		});
	}

	private JsonObject getBatchingOption(JsonObject request) {
		String path = request.getString("path");
		JsonObject body = request.getObject("body");

		String collection = path.split("/")[3];
		JsonObject matcher = new JsonObject();
		try {
			matcher.putString("_id", path.split("/")[4]);
		} catch (IndexOutOfBoundsException e) {}

		JsonObject option = new JsonObject();
		option.putString("collection", collection);
		switch (request.getString("method")) {
		case "POST":
			option.putString("action", "save");
			option.putObject("documents", body);
			break;
		case "PUT":
			option.putString("action", "put");
			option.putObject("matcher", matcher);
			option.putObject("objNew", body);
			break;
		case "DELETE":
			option.putString("action", "delete");
			option.putObject("matcher", matcher);
			break;
		}
		return option;
	}

}

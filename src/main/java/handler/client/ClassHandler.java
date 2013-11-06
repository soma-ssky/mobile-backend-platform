package main.java.handler.client;

import java.util.Iterator;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

public class ClassHandler implements Handler<HttpServerRequest> {
	private EventBus eb;

	public ClassHandler(EventBus eb) {
		this.eb = eb;
	}

	@Override
	public void handle(final HttpServerRequest request) {
		request.bodyHandler(new Handler<Buffer>() {
			@Override
			public void handle(Buffer buffer) {
				String collection = request.path().split("/")[3];
				JsonObject data = new JsonObject(buffer.toString());
				String method = data.getString("_method");
				removeInvalidField(data);
				if (method == null) {
					handleSave(request, data, collection);
				} else if (method.equals("GET")) {
					handleRetrieve(request, data, collection);
				}
			}
		});

	}

	public void handleSave(final HttpServerRequest request, JsonObject data, final String collection) {
		eb.send("ssky.object", new JsonObject().putString("action", "save").putString("collection", collection).putObject("data", data), new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> result) {
				if (result.body().getString("status") != null && result.body().getString("status").equals("error")) {
					request.response().end(result.body().encode());
					return;
				}
				HttpServerResponse response = setResponseHeaders(request, collection, result, 201, "Created");
				response.end(result.body().encode());
			}
		});
	}

	public void handleRetrieve(final HttpServerRequest request, JsonObject data, String collection) {
		JsonObject matcher = data.getObject("where");
		if (matcher.getString("objectId") != null) {
			matcher.putString("_id", matcher.getString("objectId"));
			matcher.removeField("objectId");
		} else if (matcher.getObject("$relatedTo") != null) {
			String key = matcher.getObject("$relatedTo").getString("key");
			JsonObject object = matcher.getObject("$relatedTo").getObject("object");
			int count = checkCount(data);
			handleNestedRetrieve(request, key, object, count);
			return;
		} else {
			for (String key : matcher.toMap().keySet()) {
				try {
					if (matcher.getObject(key) != null && matcher.getObject(key).getObject("$inQuery") != null) {
						JsonObject inMatcher = new JsonObject().putObject("where", matcher.getObject(key).getObject("$inQuery").getObject("where"));
						handleInQueryRetrieve(request, collection, key, inMatcher);
						return;
					}
				} catch (ClassCastException e) {
					continue;
				}
			}
		}

		JsonObject option = new JsonObject();
		adjustLimit(option, data);
		adjustOrder(option, data);

		JsonObject retrieveOption = new JsonObject().putString("collection", collection);
		retrieveOption.putObject("matcher", matcher).putObject("option", option);
		if (checkCount(data) == 0) retrieveOption.putString("action", "retrieve");
		else retrieveOption.putString("action", "count");
		eb.send("ssky.object", retrieveOption, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> result) {
				if (result.body().getString("status") != null && result.body().getString("status").equals("error")) {
					request.response().end(result.body().encode());
					return;
				}

				HttpServerResponse response = setResponseHeaders(request, null, result, 200, "OK");
				response.end(result.body().encode());
			}
		});

	}

	private void handleNestedRetrieve(final HttpServerRequest request, final String key, JsonObject object, final int count) {
		final String collection = object.getString("className");
		String objectId = object.getString("objectId");
		JsonObject relatedOption = new JsonObject();
		relatedOption.putString("action", "fetch").putString("collection", collection).putObject("matcher", new JsonObject().putString("_id", objectId));
		eb.send("ssky.object", relatedOption, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> result) {
				JsonArray orMatcher = new JsonArray();
				JsonArray arr = result.body().getArray(key);
				Iterator<Object> itr = arr.iterator();
				while (itr.hasNext()) {
					JsonObject obj = (JsonObject) itr.next();
					orMatcher.add(new JsonObject().putString("_id", obj.getString("objectId")));
				}
				JsonObject data = new JsonObject().putObject("where", new JsonObject().putArray("$or", orMatcher)).putNumber("count", count);
				handleRetrieve(request, data, ((JsonObject) arr.get(0)).getString("className"));
			}
		});
	}

	private void handleInQueryRetrieve(final HttpServerRequest request, final String collection, final String key, final JsonObject inMatcher) {
		JsonObject option = new JsonObject();
		option.putObject("matcher", new JsonObject().putObject(key, new JsonObject().putBoolean("$exists", true)));
		option.putString("collection", collection).putString("action", "retrieve");
		eb.send("ssky.object", option, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> result) {
				JsonArray orMatcher = new JsonArray();
				JsonArray arr = result.body().getArray("results");
				Iterator<Object> itr = arr.iterator();
				while (itr.hasNext()) {
					orMatcher.add(new JsonObject().putString("_id", ((JsonObject) itr.next()).getObject(key).getString("objectId")));
				}
				JsonObject data = new JsonObject().putObject("where", new JsonObject().putArray("$or", orMatcher).mergeIn(inMatcher.getObject("where")));
				System.out.println(data.encode());
				//handleRetrieve(request, data, collection);
				HttpServerResponse response = setResponseHeaders(request, null, result, 200, "OK");
				response.end(result.body().encode());	
			}
		});
	}

	private int checkCount(JsonObject data) {
		if (data.getNumber("count") != null && data.getNumber("count").equals(1)) return 1;
		return 0;
	}

	private JsonObject adjustOrder(JsonObject data, JsonObject option) {
		if (data.getString("order") != null) {
			String key;
			int index;
			if (data.getString("order").startsWith("-")) {
				key = data.getString("order").substring(1);
				index = -1;
			} else {
				key = data.getString("order");
				index = 1;
			}
			option.putObject("sort", new JsonObject().putNumber(key, index));
		}
		return option;
	}

	private JsonObject adjustLimit(JsonObject option, JsonObject data) {
		if (data.getNumber("limit") != null && !data.getNumber("limit").equals(0)) {
			option.putNumber("limit", data.getNumber("limit"));
		}
		return option;
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
		if (collection != null) {
			response.putHeader("Location", request.headers().get("Host") + "/1/classes/" + collection + "/" + result.body().getString("objectId"));
		}
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

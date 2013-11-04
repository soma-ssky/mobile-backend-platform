package main.java.handler;

import java.util.Iterator;

import main.java.util.MongoUtil;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

public class ObjectQueryHandler {
	private EventBus eb;
	private HttpServerRequest request;

	public boolean handle(EventBus eb, HttpServerRequest request, Buffer buffer) {
		this.eb = eb;
		this.request = request;
		return handle(buffer);
	}

	public boolean handle(Buffer buffer) {
		if (!request.method().equals("POST") || !request.path().startsWith("/1/classes/")) return false;
		String _method = new JsonObject(buffer.toString()).getString("_method");
		JsonObject data = new JsonObject(buffer.toString());
		if (_method != null && _method.equals("GET")) {
			if (data.getNumber("count") != null && data.getNumber("count").equals(1)) {
				countObject(request, request.path().split("/")[3], data);
				return true;
			} else if (data.getInteger("limit") != null && data.getInteger("limit") == 1) {
				retrieveOneObject(request, request.path().split("/")[3], data);
				return true;
			} else if (request.path().split("/").length == 4) {
				retrieveAllObject(request, request.path().split("/")[3], data);
				return true;
			}
		}
		return false;
	}

	private void countObject(final HttpServerRequest request, final String className, final JsonObject data) {
		if (data.getObject("where").getString("objectId") != null) {
			data.getObject("where").putString("objectId", data.getObject("where").getString("_id"));
		}
		JsonObject countConfig = MongoUtil.countConfig(className, data.getObject("where"));
		eb.send("vertx.mongo", countConfig, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> result) {
				if (result.body().getString("status").equals("error")) {
					request.response().end(result.body().getObject("message").encode());
					return;
				}
				if (result.body().getNumber("count") != null) {
					JsonObject reply = new JsonObject().putNumber("count", result.body().getNumber("count"));
					HttpServerResponse response = request.response();
					response = putHeaders(response, reply.encode().length(), 200, "OK");
					response.end(reply.encode());
				}
			}
		});
	}

	private JsonObject sortingOption(JsonObject data) {
		JsonObject option = new JsonObject();

		option = new JsonObject();
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
			return option;
		} else {
			return null;
		}
	}

	private void retrieveAllObject(final HttpServerRequest request, final String className, final JsonObject data) {
		if (data.getObject("where").getObject("$relatedTo") != null) {
			final String newKey = data.getObject("where").getObject("$relatedTo").getString("key");
			String newClassName = data.getObject("where").getObject("$relatedTo").getObject("object").getString("className");
			String newObjectId = data.getObject("where").getObject("$relatedTo").getObject("object").getString("objectId");
			JsonObject matcher = new JsonObject().putString("_id", newObjectId);
			JsonObject option = new JsonObject().putNumber("limit", data.getNumber("limit"));
			
			eb.send("vertx.mongo", MongoUtil.findOneConfig(newClassName, matcher, option), new Handler<Message<JsonObject>>() {
				@Override
				public void handle(Message<JsonObject> result) {
					if (result.body().getString("status").equals("error")) {
						request.response().end(result.body().getObject("message").encode());
						return;
					}
					JsonArray relationalArray = result.body().getObject("result").getObject(newKey).getArray("objects");
					Iterator<Object> itr = relationalArray.iterator();
					JsonArray orArray = new JsonArray();
					String newClassName = ((JsonObject) relationalArray.get(0)).getString("className");
					while (itr.hasNext()) {
						JsonObject object = (JsonObject) itr.next();
						String newObjectId = object.getString("objectId");
						orArray.add(new JsonObject().putString("_id", newObjectId));
					}
					JsonObject matcher = new JsonObject();
					matcher.putObject("where", new JsonObject().putArray("$or", orArray));
					System.out.println("\t\tmatcher" + matcher.encode());
					retrieveAllObject(request, newClassName, matcher);
				}
			});
		} else {
			if (data.getObject("where").getString("objectId") != null) {
				data.getObject("where").putString("objectId", data.getObject("where").getString("_id"));
			}
			JsonObject findConfig;
			JsonObject option = sortingOption(data);

			findConfig = MongoUtil.findConfig(className, data.getObject("where"), option);
			option.putNumber("limit", data.getNumber("limit"));
			eb.send("vertx.mongo", findConfig, new Handler<Message<JsonObject>>() {
				@Override
				public void handle(Message<JsonObject> result) {
					JsonObject reply = null;
					Iterator<Object> itr = result.body().getArray("results").iterator();
					JsonArray tmpArray = new JsonArray();
					while (itr.hasNext()) {
						tmpArray.add(itr.next());
					}
					reply = new JsonObject().putArray("results", tmpArray);
					HttpServerResponse response = request.response();
					response = putHeaders(response, reply.encode().length(), 200, "OK");
					response.end(reply.encode());
				}
			});
		}
	}

	private void retrieveOneObject(final HttpServerRequest request, final String className, final JsonObject data) {
		if (data.getObject("where").getString("objectId") != null) {
			data.getObject("where").putString("objectId", data.getObject("where").getString("_id"));
		}
		JsonObject findOneConfig = MongoUtil.findOneConfig(className, data.getObject("where"), null);
		eb.send("vertx.mongo", findOneConfig, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> result) {
				if (result.body().getString("status").equals("error")) {
					request.response().end(result.body().getObject("message").encode());
					return;
				}
				JsonObject reply = null;
				if (result.body().getObject("result") != null) {
					result.body().getObject("result").putString("objectId", result.body().getObject("result").getString("_id"));
					result.body().getObject("result").removeField("_id");
					reply = new JsonObject().putArray("results", new JsonArray().add(result.body().getObject("result")));
				}
				HttpServerResponse response = request.response();
				response = putHeaders(response, reply.encode().length(), 200, "OK");
				response.end(reply.encode());
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

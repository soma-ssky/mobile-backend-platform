package expired.object.request;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import main.java.util.MongoUtil;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.json.JsonObject;

public class ObjectHandler {
	private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";// "EEE MMM dd HH:mm:ss zz yyyy";
	private EventBus eb;
	private HttpServerRequest request;

	public boolean handle(EventBus eb, HttpServerRequest request, Buffer buffer) {
		this.eb = eb;
		this.request = request;
		return handle(buffer);
	}

	public boolean handle(Buffer buffer) {
		JsonObject data = new JsonObject(buffer.toString());
		if (request.path().startsWith("/1/batch")) {
			saveAllObject(request, data);
			return true;
		} else {
			String _method = new JsonObject(buffer.toString()).getString("_method");
			if (_method == null) {
				// saveObject(request, request.path().split("/")[3], data);
				return true;
			} else if (_method.equals("GET") && request.path().split("/").length == 5) {
				fetchObject(request, request.path().split("/")[3], request.path().split("/")[4]);
				return true;
			} else if (_method.equals("PUT") && request.path().split("/").length == 5) {
				updateObject(request, request.path().split("/")[3], request.path().split("/")[4], data);
				return true;
			} else if (_method.equals("DELETE") && request.path().split("/").length == 5) {
				deleteObject(request, request.path().split("/")[3], request.path().split("/")[4]);
				return true;
			} else {
				return false;
			}
		}
	}

	private void saveAllObject(final HttpServerRequest request, JsonObject data) {}

	private void fetchObject(final HttpServerRequest request, final String className, String objectId) {
		eb.send("vertx.mongo", MongoUtil.findOneConfig(className, new JsonObject().putString("_id", objectId), null), new Handler<Message<JsonObject>>() {
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
					reply = result.body().getObject("result");
				}
				HttpServerResponse response = request.response();
				response = putHeaders(response, reply.encode().length(), 200, "OK");
				response.end(reply.encode());
			}
		});
	}

	private void updateObject(final HttpServerRequest request, final String className, final String objectId, JsonObject data) {
		final JsonObject objNew = data;
		objNew.removeField("_method");
		objNew.removeField("_ApplicationId");
		objNew.removeField("_JavaScriptKey");
		objNew.removeField("_ClientVersion");
		objNew.removeField("_InstallationId");

		final String current = getCurrentTimeInString();
		objNew.putString("updatedAt", current);

		eb.send("vertx.mongo", MongoUtil.findOneConfig(className, new JsonObject().putString("_id", objectId), null), new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> result) {
				JsonObject resultObj = result.body().getObject("result");
				Set<String> keys = resultObj.toMap().keySet();
				for (String key : keys) {
					if (objNew.getValue(key) == null) {
						objNew.putValue(key, resultObj.getValue(key));
					}
				}
				eb.send("vertx.mongo", MongoUtil.updateConfig(className, new JsonObject().putString("_id", objectId), objNew), new Handler<Message<JsonObject>>() {
					@Override
					public void handle(Message<JsonObject> result) {
						if (result.body().getString("status").equals("error")) {
							request.response().end(result.body().getObject("message").encode());
							return;
						}

						JsonObject reply = new JsonObject().putString("updatedAt", current);
						HttpServerResponse response = request.response();
						response = putHeaders(response, reply.encode().length(), 200, "OK");
						response.end(reply.encode());
					}
				});
			}
		});
	}

	private void deleteObject(final HttpServerRequest request, final String className, final String objectId) {
		eb.send("vertx.mongo", MongoUtil.deleteConfig(className, new JsonObject().putString("_id", objectId)), new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> result) {
				if (result.body().getString("status").equals("error")) {
					request.response().end(result.body().getObject("message").encode());
					return;
				}

				HttpServerResponse response = request.response();
				response = putHeaders(request.response(), new JsonObject().encode().length(), 200, "OK");
				response.end(new JsonObject().encode());
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

	private String getCurrentTimeInString() {
		return new SimpleDateFormat(DATE_FORMAT).format(new Date()).toString();
	}

}

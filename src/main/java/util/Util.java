package main.java.util;

import main.java.handler.NoMatchHandler;
import main.java.handler.client.BatchHandler;
import main.java.handler.client.ClassHandler;
import main.java.handler.client.ObjectHandler;
import main.java.handler.rest.DeleteHandler;
import main.java.handler.rest.GetClassHandler;
import main.java.handler.rest.GetObjectHandler;
import main.java.handler.rest.PutHandler;
import main.java.role.RoleCreateHandler;
import main.java.role.RoleDeleteHandler;
import main.java.role.RoleQueryHandler;
import main.java.role.RoleRetrieveHandler;
import main.java.role.RoleUpdateHandler;
import main.java.server.ServerMain;
import main.java.user.LoginHandler;
import main.java.user.PasswordResetHandler;
import main.java.user.SigningOutHandler;
import main.java.user.SigningUpHandler;
import main.java.user.TokenHandler;
import main.java.user.UserQueryHandler;
import main.java.user.UserRetrieveHandler;
import main.java.user.UserUpdateHandler;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;

public class Util {
	private static RouteMatcher routeMatcher = null;
	public static final String MONGO_PERSISTOR_ADDRESS = "vertx.mongo.persistor";
	public static final String OBJECT_MANAGER_ADDRESS = "ssky.object.manager";
	public static final String AUTH_MANAGER_ADDRESS = "ssky.auth.manager";
	public static final String VERSION = "1";

	public static RouteMatcher getRouteMatcher() {
		if (routeMatcher == null) {
			routeMatcher = new RouteMatcher();

			// no match
			routeMatcher.noMatch(new NoMatchHandler());

			// client rest
			routeMatcher.post("/:version/batch", new BatchHandler());
			routeMatcher.post("/:version/classes/:className", new ClassHandler());
			routeMatcher.post("/:version/classes/:className/:objectId", new ObjectHandler());

			// parse object and query rest api
			routeMatcher.get("/:version/classes/:className/:objectId", new GetObjectHandler());
			routeMatcher.get("/:version/classes/:className/", new GetClassHandler());
			routeMatcher.put("/:version/classes/:className/:objectId", new PutHandler());
			routeMatcher.delete("/:version/classes/:className/:objectId", new DeleteHandler());

			// parse file rest api
			routeMatcher.post("/:version/files/:fileName", null);

			// user rest
			routeMatcher.post("/:version/users", new SigningUpHandler());
			routeMatcher.post("/:version/requestPasswordRest", new PasswordResetHandler());
			routeMatcher.get("/:version/login", new LoginHandler());
			routeMatcher.get("/:version/users/:objectId", new UserRetrieveHandler());
			routeMatcher.get("/:version/users/me", new TokenHandler());
			routeMatcher.get("/:version/users", new UserQueryHandler());
			routeMatcher.put("/:version/users/:objectId", new UserUpdateHandler());
			routeMatcher.delete("/:version/users/:objectId", new SigningOutHandler());

			// role rest
			routeMatcher.post("/:version/roles", new RoleCreateHandler());
			routeMatcher.get("/:version/roles", new RoleRetrieveHandler());
			routeMatcher.put("/:version/roles", new RoleUpdateHandler());
			routeMatcher.get("/:version/roles", new RoleQueryHandler());
			routeMatcher.delete("/:version/roles", new RoleDeleteHandler());
		}

		return routeMatcher;
	}

	public static void response(HttpServerRequest request, String reply, int code) {
		HttpServerResponse response = request.response();
		response.putHeader("Access-Control-Allow-Origin", "*");
		response.putHeader("Access-Control-Request-Method", "*");
		response.putHeader("Content-Type", "application/json; charset=utf-8");
		response.putHeader("Status", code + " " + statusMessageByCode(code));
		response.putHeader("Content-Length", "" + reply.length());
		if (code == 201) {
			//response.putHeader("Location", request.headers().get("Host") + "/" + collection + "/" + result.body().getString("objectId"));
		}
		response.setStatusCode(code);
		response.setStatusMessage(statusMessageByCode(code));
		response.end(reply);
	}

	public static void processObjectRequestViaEB(final HttpServerRequest request, String address, JsonObject option, final int code) {
		ServerMain.eb.send(address, option, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> result) {
				if (result.body().getObject("result") != null) {
					Util.response(request, result.body().getObject("result").encode(), code);
				} else if (result.body().getArray("results") != null) {
					Util.response(request, result.body().getArray("results").encode(), code);
				} else {
					// error
				}
			}
		});
	}

	public static String statusMessageByCode(int code) {
		switch (code) {
		case 200:
			return "OK";
		case 201:
			return "Created";
		case 400:
			return "Invalid Request";
		case 401:
			return "Unauthorized";
		default:
			return null;
		}
	}

	public static JsonObject removeInvalidField(JsonObject data) {
		data.removeField("_ApplicationId");
		data.removeField("_ClientVersion");
		data.removeField("_InstallationId");
		// data.removeField("_JavaScriptKey");
		data.removeField("_method");
		return data;
	}

	public static JsonObject saveObjectOption(String collection, JsonObject document) {
		JsonObject option = new JsonObject();
		option.putString("action", "save");
		option.putString("collection", collection);
		option.putObject("document", document);
		return option;
	}

	public static JsonObject saveAllObjectOption(String collection, String documents) {
		JsonObject option = new JsonObject();
		option.putString("action", "saveall");
		option.putString("collection", collection);
		option.putString("document", documents);
		return option;
	}

	public static JsonObject fetchObjectOption(String collection, JsonObject matcher) {
		JsonObject option = new JsonObject();
		option.putString("action", "fetch");
		option.putString("collection", collection);
		option.putObject("matcher", matcher);
		return option;
	}

	public static JsonObject fetchObjectOptionById(String collection, String objectId) {
		JsonObject matcher = new JsonObject().putObject("matcher", new JsonObject().putString("_id", objectId));
		return fetchObjectOption(collection, matcher);
	}

	public static JsonObject updateObjectOption(String collection, JsonObject matcher, JsonObject objNew) {
		JsonObject option = new JsonObject();
		option.putString("collection", collection);
		option.putObject("matcher", matcher);
		option.putObject("objNew", objNew);
		return option;
	}

	public static JsonObject updateObjectOptionById(String collection, String objectId, JsonObject objNew) {
		JsonObject matcher = new JsonObject().putObject("matcher", new JsonObject().putString("_id", objectId));
		return updateObjectOption(collection, matcher, objNew);
	}

	public static JsonObject deleteObjectOption(String collection, JsonObject matcher) {
		JsonObject option = new JsonObject();
		option.putString("action", "delete");
		option.putString("collection", collection);
		option.putObject("matcher", matcher);
		return option;
	}

	public static JsonObject deleteAllObjectOption(String collection, JsonObject matcher) {
		JsonObject option = new JsonObject();
		option.putString("action", "deleteall");
		option.putString("collection", collection);
		option.putObject("matcher", matcher);
		return option;
	}

	public static JsonObject deleteObjectOptionById(String collection, String objectId) {
		JsonObject matcher = new JsonObject().putObject("matcher", new JsonObject().putString("_id", objectId));
		return deleteObjectOption(collection, matcher);
	}

	public static JsonObject countObjectOption(String collection, JsonObject matcher) {
		JsonObject option = new JsonObject();
		option.putString("action", "delete");
		option.putString("collection", collection);
		option.putObject("matcher", matcher);
		return option;
	}

	public static JsonObject countObjectOptionById(String collection, String objectId) {
		JsonObject matcher = new JsonObject().putObject("matcher", new JsonObject().putString("_id", objectId));
		return countObjectOption(collection, matcher);
	}

}
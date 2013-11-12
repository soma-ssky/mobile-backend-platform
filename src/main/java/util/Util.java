package main.java.util;

import main.java.handler.FileUploadHandler;
import main.java.handler.NoMatchHandler;
import main.java.handler.RouteMatchManager;
import main.java.handler.object.BatchingObjectOption;
import main.java.handler.object.ClassHandler;
import main.java.handler.object.ClientObjectOption;
import main.java.handler.object.DeletingObjectOption;
import main.java.handler.object.QueryingObjectOption;
import main.java.handler.object.RetrievingObjectOption;
import main.java.handler.object.UpdatingObjectOption;
import main.java.handler.user.LoginOption;
import main.java.handler.user.SigningOutOption;
import main.java.handler.user.SigningupOption;
import main.java.handler.user.UserQueryOption;
import main.java.handler.user.UserRetrieveOption;
import main.java.handler.user.UserUpdateOption;
import main.java.role.CreatingRoleOption;
import main.java.role.DeletingRoleOption;
import main.java.role.QueryingRoleOptiong;
import main.java.role.RetrievingRoleOption;
import main.java.role.UpdatingRoleOptiong;
import main.java.server.ServerMain;

import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;

public class Util {
	public static final String MONGO_PERSISTOR_ADDRESS = "vertx.mongo.persistor";
	public static final String OBJECT_MANAGER_ADDRESS = "ssky.object.manager";
	public static final String AUTH_MANAGER_ADDRESS = "ssky.auth.manager";
	public static final String MONGO_GRIDFS_ADDRESS = "ssky.gridfs.manager";
	public static final String ROLE_MANAGER_ADDRESS = "ssky.role.manager";
	public static final String VERSION = "1";

	public static final String UPLOAD_DIR = "./uploads/";

	private static RouteMatchManager manager = null;

	public static RouteMatcher getRouteMatcher() {
		if (manager == null) {
			manager = RouteMatchManager.getInstance();
			// post
			// manager.addPost("/:version/classes/:className", new CreatingObjectAndClientClassOption());
			manager.addPost("/:version/classes/:className/:objectId", new ClientObjectOption());
			manager.addPost("/:version/batch", new BatchingObjectOption());

			// get
			manager.addGet("/:version/classes/:className/:objectId", new RetrievingObjectOption());
			manager.addGet("/:version/classes/:className", new QueryingObjectOption());

			// put
			manager.addPut("/:version/classes/:className/:objectId", new UpdatingObjectOption());

			// delete
			manager.addDelete("/:version/classes/:className/:objectId", new DeletingObjectOption());

			// no match
			manager.addNoMatch(new NoMatchHandler());

			manager.addPostHandler("/:version/classes/:className", new ClassHandler());
			manager.addPostHandler("/:version/files/:fileName", new FileUploadHandler());

			// user rest
			manager.addPost("/:version/users", new SigningupOption());
			manager.addGet("/:version/login", new LoginOption());
			manager.addGet("/:version/users/:objectId", new UserRetrieveOption());
			manager.addGet("/:version/users", new UserQueryOption());
			manager.addPut("/:version/users/:objectId", new UserUpdateOption());
			manager.addDelete("/:version/users/:objectId", new SigningOutOption());

			// role rest
			manager.addPost("/:version/roles", new CreatingRoleOption());
			manager.addGet("/:version/roles/:objectId", new RetrievingRoleOption());
			manager.addPut("/:version/roles/:objectId", new UpdatingRoleOptiong());
			manager.addGet("/:version/roles", new QueryingRoleOptiong());
			manager.addDelete("/:version/roles", new DeletingRoleOption());

		}
		return manager.getRouteMatcher();

		// if (routeMatcher == null) {
		// routeMatcher = new RouteMatcher();
		//
		//
		// }

	}

	public static void response(HttpServerRequest request, String reply, int code) {
		HttpServerResponse response = request.response();
		response.putHeader("Access-Control-Allow-Origin", "*");
		response.putHeader("Access-Control-Request-Method", "*");
		response.putHeader("Content-Type", "application/json; charset=utf-8");
		response.putHeader("Status", code + " " + statusMessageByCode(code));
		response.putHeader("Content-Length", "" + reply.length());
		if (code == 201) {
			// response.putHeader("Location", address);
		}
		response.setStatusCode(code);
		response.setStatusMessage(statusMessageByCode(code));
		response.end(reply);
	}

	public static void processObjectRequestViaEB(final HttpServerRequest request, final String address, JsonObject option, final int code) {
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

	public static JsonObject convertParamsToJsonObject(MultiMap params) {
		JsonObject document = new JsonObject();
		for (String name : params.names()) {
			document.putString(name, params.get(name));
		}
		return document;

	}

}
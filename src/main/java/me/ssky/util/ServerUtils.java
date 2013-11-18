package main.java.me.ssky.util;

import java.util.HashMap;
import java.util.Map;

import main.java.me.ssky.file.RetrievingFileHandler;
import main.java.me.ssky.file.UploadingFileHandler;
import main.java.me.ssky.objects.BatchingObjectHandler;
import main.java.me.ssky.objects.ClientObjectOption;
import main.java.me.ssky.objects.CreatingObjectOption;
import main.java.me.ssky.objects.DeletingObjectOption;
import main.java.me.ssky.objects.FetchingObjectOption;
import main.java.me.ssky.objects.RetrievingObjectOption;
import main.java.me.ssky.objects.UpdatingObjectOption;
import main.java.me.ssky.server.ServerMain;
import main.java.me.ssky.story.FollowFriendOption;
import main.java.me.ssky.story.GettingFollowerOption;
import main.java.me.ssky.story.PostingToFollowerOption;
import main.java.me.ssky.story.UnfollowFriendOption;
import main.java.me.ssky.users.DeletingUserOption;
import main.java.me.ssky.users.FetchingUserOption;
import main.java.me.ssky.users.LoggingInUserOption;
import main.java.me.ssky.users.RetrievingUserOption;
import main.java.me.ssky.users.SigningUpUserOption;
import main.java.me.ssky.users.UpdatingUserOption;

import org.vertx.java.core.MultiMap;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;

public class ServerUtils {
	public static final String HOST = "http://api.ssky.me/";
	public static final String MONGO_PERSISTOR_ADDRESS = "ssky.mongo.persistor";
	public static final String OBJECT_MANAGER_ADDRESS = "ssky.object.manager";
	public static final String AUTH_MANAGER_ADDRESS = "ssky.auth.manager";
	public static final String MONGO_GRIDFS_ADDRESS = "ssky.gridfs.manager";

	public static final String FRIEND_RELATION_MANAGER_ADDRESS = "ssky.friend.relation.manager";

	public static final String VERSION = "1";
	private static RouteMatchManager manager = null;

	public static RouteMatcher getRouteMatcher() {
		if (manager == null) {
			manager = new RouteMatchManager();

			// friend rest
			manager.addPostToEB("/:version/classes/_Relation", new FollowFriendOption());
			manager.addDeleteToEB("/:version/classes/_Relation", new UnfollowFriendOption());
			manager.addGetToEB("/:version/classes/_Friends/:myId", new GettingFollowerOption());
			manager.addGetToEB("/:version/classes/_Post/:myId", new PostingToFollowerOption());

			// object rest
			manager.addPostToEB("/:version/classes/:className", new CreatingObjectOption());
			manager.addPostToEB("/:version/classes/:className/:objectId", new ClientObjectOption());
			manager.addGetToEB("/:version/classes/:className/:objectId", new FetchingObjectOption());
			manager.addGetToEB("/:version/classes/:className", new RetrievingObjectOption());
			manager.addPutToEB("/:version/classes/:className", new UpdatingObjectOption());
			manager.addDeleteToEB("/:version/classes/:className", new DeletingObjectOption());
			manager.addPostHandler("/:version/batch", new BatchingObjectHandler());

			// user rest
			manager.addPostToEB("/:version/users", new SigningUpUserOption());
			manager.addPostToEB("/:version/login", new LoggingInUserOption());
			manager.addGetToEB("/:version/login", new LoggingInUserOption());
			manager.addGetToEB("/:version/users/:objectId", new FetchingUserOption());
			manager.addPutToEB("/:version/users/:objectId", new UpdatingUserOption());
			manager.addGetToEB("/:version/users", new RetrievingUserOption());
			manager.addDeleteToEB("/:version/users/:objectId", new DeletingUserOption());

			// file rest
			manager.addPostHandler("/:version/files/:fileName", new UploadingFileHandler());
			manager.addGetHandler("/:version/files/:fileDir/:fileName", new RetrievingFileHandler());

			manager.addNoMatchHandler(new NoMatchHandler());
		}
		return manager.getRouteMatcher();
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
		data.removeField("_JavaScriptKey");
		data.removeField("_method");
		return data;
	}

	public static Map<String, String> responseHeaders(int code) {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Access-Control-Allow-Origin", "*");
		headers.put("Access-Control-Request-Method", "*");
		headers.put("Content-Type", "application/json; charset=utf-8");
		headers.put("Status", code + " " + statusMessageByCode(code));
		return headers;
	}

	public static Map<String, String> responseHeadersInCreated(int code, int length, String location) {
		Map<String, String> headers = responseHeaders(code);
		headers.put("Location", ServerMain.HOST + location);
		return headers;
	}

	public static JsonObject convertParamsToJsonObject(MultiMap params) {
		JsonObject document = new JsonObject();
		for (String name : params.names()) {
			document.putString(name, params.get(name));
		}
		return document;

	}

	public static JsonObject convertHeadersToJsonObject(MultiMap headers) {
		JsonObject document = new JsonObject();
		for (String name : headers.names()) {
			document.putString(name, headers.get(name));
		}
		return document;
	}
}
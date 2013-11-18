package main.java.me.ssky.server;

import main.java.me.ssky.util.ServerUtils;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

public class ServerMain extends Verticle {
	public static final String HOST = "http://api.ssky.me";
	private static final int PORT = 80;
	private static final String MONGO_MODULE_NAME = "me.ssky~mongo-persistor~1.0";
	private static final String AUTH_MODULE_NAME = "me.ssky~auth-manager~1.0";
	private static final String GRIDFS_MOUDLE_NAME = "me.ssky~mongo.gridfs.manager~1.0";
	private static final String SSKY_OBJECT_MODULE_NAME = "me.ssky~object-manager~1.0";

	private static final String DB_HOST = "127.0.0.1";
	private static final int DB_PORT = 27017;
	private static final String DB_NAME = "test";

	public static Vertx _vertx;

	@Override
	public void start() {
		_vertx = vertx;

		container.deployVerticle("StoryVerticle.java", relationConfig());

		// Don't deploy mongo persistor and gridfs manager at the same time,
		container.deployModule(MONGO_MODULE_NAME, mongoConfig(), new Handler<AsyncResult<String>>() {
			@Override
			public void handle(AsyncResult<String> result) {
				// after deploying mongo persister, deploy mongo gridfs manager
				container.deployModule(GRIDFS_MOUDLE_NAME, gridFsConfig());
			}
		});
		container.deployModule(SSKY_OBJECT_MODULE_NAME, objectConfig());
		container.deployModule(AUTH_MODULE_NAME, authConfig());

		RouteMatcher routeMatcher = ServerUtils.getRouteMatcher();
		vertx.createHttpServer().requestHandler(routeMatcher).listen(PORT);

	}

	private JsonObject relationConfig() {
		JsonObject relationConfig = new JsonObject();
		relationConfig.putString("thisAddress", ServerUtils.STORY_MANAGER_ADDRESS);
		relationConfig.putString("objAddress", ServerUtils.OBJECT_MANAGER_ADDRESS);
		relationConfig.putString("postCollection", "_Post");
		relationConfig.putString("relationCollection", "_Relation");

		return relationConfig;
	}

	private JsonObject mongoConfig() {
		JsonObject mongoConfig = new JsonObject();
		mongoConfig.putString("address", ServerUtils.MONGO_PERSISTOR_ADDRESS);
		mongoConfig.putString("host", DB_HOST);
		mongoConfig.putNumber("port", DB_PORT);
		mongoConfig.putString("db_name", DB_NAME);
		return mongoConfig;
	}

	private JsonObject authConfig() {
		JsonObject authConfig = new JsonObject();
		authConfig.putString("this_address", ServerUtils.AUTH_MANAGER_ADDRESS);
		authConfig.putString("db_address", ServerUtils.MONGO_PERSISTOR_ADDRESS);
		return authConfig;
	}

	private JsonObject gridFsConfig() {
		JsonObject gridfsConfig = new JsonObject();
		gridfsConfig.putString("address", ServerUtils.MONGO_GRIDFS_ADDRESS);
		gridfsConfig.putString("host", DB_HOST);
		gridfsConfig.putNumber("port", DB_PORT);
		gridfsConfig.putString("db_name", DB_NAME);
		return gridfsConfig;
	}

	private JsonObject objectConfig() {
		JsonObject objectConfig = new JsonObject();
		objectConfig.putString("this_address", ServerUtils.OBJECT_MANAGER_ADDRESS);
		objectConfig.putString("db_address", ServerUtils.MONGO_PERSISTOR_ADDRESS);
		return objectConfig;
	}

}
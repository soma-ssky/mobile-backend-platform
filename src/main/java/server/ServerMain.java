package main.java.server;

import main.java.util.Util;

import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

public class ServerMain extends Verticle {
	private static final int PORT = 80;
	private static final String MONGO_MODULE_NAME = "io.vertx~mod-mongo-persistor~2.0.0-final";
	private static final String SSKY_OBJECT_MODULE_NAME = "me.ssky~object-manager~1.0-final";
	// private static final String AUTH_MODULE_NAME = "me.ssky~auth-manager~0.1";

	private static final String DB_HOST = "127.0.0.1";
	private static final int DB_PORT = 27017;
	private static final String DB_NAME = "test";

	public static EventBus eb;

	@Override
	public void start() {
		eb = vertx.eventBus();

		container.deployModule(MONGO_MODULE_NAME, mongoConfig());
		container.deployModule(SSKY_OBJECT_MODULE_NAME, objectConfig());
		// container.deployModule(AUTH_MODULE_NAME);

		RouteMatcher routeMatcher = Util.getRouteMatcher();
		vertx.createHttpServer().requestHandler(routeMatcher).listen(PORT);
	}

	private JsonObject mongoConfig() {
		JsonObject mongoConfig = new JsonObject();
		mongoConfig.putString("address", Util.MONGO_PERSISTOR_ADDRESS);
		mongoConfig.putString("host", DB_HOST);
		mongoConfig.putNumber("port", DB_PORT);
		mongoConfig.putString("db_name", DB_NAME);
		return mongoConfig;
	}

	private JsonObject objectConfig() {
		JsonObject objectConfig = new JsonObject();
		objectConfig.putString("this_address", Util.OBJECT_MANAGER_ADDRESS);
		objectConfig.putString("db_address", Util.MONGO_PERSISTOR_ADDRESS);
		return objectConfig;
	}
}
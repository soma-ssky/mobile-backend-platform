package main.java.server;

import main.java.handler.NoMatchHandler;
import main.java.handler.client.BatchHandler;
import main.java.handler.client.ClassHandler;
import main.java.handler.client.ObjectHandler;
import main.java.handler.rest.GetHandler;
import main.java.handler.rest.PutHandler;

import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

public class ServerMain extends Verticle {
	private static final int PORT = 80;
	private static final String MONGO_MODULE_NAME = "io.vertx~mod-mongo-persistor~2.0.0-final";
	private static final String SSKY_OBJECT_MODULE_NAME = "me.ssky~object-manager~1.0-final";
	private EventBus eb;

	@Override
	public void start() {
		eb = vertx.eventBus();
		container.deployModule(MONGO_MODULE_NAME, mongoConfig());
		container.deployModule(SSKY_OBJECT_MODULE_NAME, objectConfig());

		RouteMatcher routeMatcher = new RouteMatcher();

		// parse post
		routeMatcher.post("/:version/batch", new BatchHandler(eb));
		routeMatcher.post("/:version/classes/:className", new ClassHandler(eb));
		routeMatcher.post("/:version/classes/:className/:objectId", new ObjectHandler(eb));

		// parse object and query rest api
		routeMatcher.get("/:version/classes/:className/:objectId", new GetHandler(eb));
		routeMatcher.put("/:version/classes/:className/:objectId", new PutHandler(eb));
		routeMatcher.get("/:version/classes/:className/", null);
		routeMatcher.delete("/:version/classes/:className/:objectId", null);

		// parse file rest api
		routeMatcher.post("/:version/files/:fileName", null);

		// no match
		routeMatcher.noMatch(new NoMatchHandler());

		vertx.createHttpServer().requestHandler(routeMatcher).listen(PORT);
	}

	private JsonObject mongoConfig() {
		JsonObject mongoConfig = new JsonObject();
		mongoConfig.putString("address", "vertx.mongo").putString("host", "127.0.0.1");
		mongoConfig.putNumber("port", 27017).putNumber("pool_size", 100).putString("db_name", "test");
		return mongoConfig;
	}

	private JsonObject objectConfig() {
		JsonObject objectConfig = new JsonObject();
		objectConfig.putString("this_address", "ssky.object").putString("db_address", "vertx.mongo");
		return objectConfig;
	}
}
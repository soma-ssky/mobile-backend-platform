package main.java.server;

import main.java.handler.ObjectHandler;
import main.java.handler.ObjectQueryHandler;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

public class ServerMain extends Verticle implements Handler<HttpServerRequest> {
	private static final int PORT = 80;
	private static final String USER_MODULE_NAME = "io.vertx~mod-user-mgr~1.0";
	private static final String MONGO_MODULE_NAME = "io.vertx~mod-mongo-persistor~2.0.0-final";
	private static final String AUTH_MODULE_NAME = "io.vertx~mod-auth-mgr~2.1.0-SNAPSHOT";
	private EventBus eb;

	@Override
	public void start() {
		eb = vertx.eventBus();
		container.deployModule(MONGO_MODULE_NAME, mongoConfig());
		container.deployModule(AUTH_MODULE_NAME, authConfig());
		container.deployModule(USER_MODULE_NAME, userConfig());

		RouteMatcher postMatcher = new RouteMatcher();
		
		
		
		vertx.createHttpServer().requestHandler(this).listen(PORT);
	}

	@Override
	public void handle(final HttpServerRequest request) {
		if (request.path().equals("/")) {
			request.response().setStatusCode(200).end();
		}
		// System.out.println(request.absoluteURI());
		System.out.println("path : " + request.path());
		// System.out.println("method : " + request.method());
		// Set<String> names = request.headers().names();
		// // System.out.println("Header : ");
		// for (String name : names) {
		// System.out.println("\t" + name + " : " + request.headers().get(name));
		// }
		request.bodyHandler(new Handler<Buffer>() {
			@Override
			public void handle(Buffer buffer) {
				System.out.println("data : " + buffer.toString());

				// new UserPostHandler().handle(eb, request, buffer);
				// new AuthPostHandler().handle(eb, request, buffer);
				if (new ObjectHandler().handle(eb, request, buffer)) return;
				if (new ObjectQueryHandler().handle(eb, request, buffer)) return;
			}
		});
	}

	private JsonObject userConfig() {
		JsonObject authConfig = new JsonObject();
		authConfig.putString("this_address", "vertx.user").putString("user_collection", "users");
		authConfig.putString("persistor_address", "vertx.mongo");
		return authConfig;
	}

	private JsonObject authConfig() {
		JsonObject authConfig = new JsonObject();
		authConfig.putString("this_address", "vertx.auth").putString("user_collection", "users");
		authConfig.putString("persistor_address", "vertx.mongo").putString("token_collection", "tokens");
		return authConfig;
	}

	private JsonObject mongoConfig() {
		JsonObject mongoConfig = new JsonObject();
		mongoConfig.putString("address", "vertx.mongo").putString("host", "127.0.0.1");
		mongoConfig.putNumber("port", 27017).putNumber("pool_size", 100).putString("db_name", "test");
		return mongoConfig;
	}
}
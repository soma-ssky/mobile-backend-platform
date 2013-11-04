package main.java.server;

import main.java.util.MongoUtil;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

public class ServerTest extends Verticle {
	private static final String MONGO_MODULE_NAME = "io.vertx~mod-mongo-persistor~2.0.0-final";
	private static final int PORT = 80;
	private EventBus eb;

	@Override
	public void start() {
		eb = vertx.eventBus();
		container.deployModule(MONGO_MODULE_NAME, mongoConfig(), new Handler<AsyncResult<String>>() {
			@Override
			public void handle(AsyncResult<String> event) {
				vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
					@Override
					public void handle(HttpServerRequest event) {}
				}).listen(PORT, new Handler<AsyncResult<HttpServer>>() {
					@Override
					public void handle(AsyncResult<HttpServer> result) {
						if (result.succeeded()) {
							System.out.println("Server is opened.");
							saveAllTest();
						}
					}
				});
			}
		});
	}

	private void saveAllTest() {
		System.out.println("start save all.");
		JsonArray arr = new JsonArray().add(new JsonObject().putString("A", "B")).add(new JsonObject().putString("C", "D"));
		eb.send("vertx.mongo", MongoUtil.saveAllConfig("test", arr.encode()), new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> results) {
				System.out.println(results.body().encode());
			}
		});
	}

	private JsonObject mongoConfig() {
		JsonObject mongoConfig = new JsonObject();
		mongoConfig.putString("address", "vertx.mongo").putString("host", "127.0.0.1");
		mongoConfig.putNumber("port", 27017).putNumber("pool_size", 100).putString("db_name", "test");
		return mongoConfig;
	}
}

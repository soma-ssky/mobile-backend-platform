package test.java.me.ssky;

import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.assertNotNull;
import static org.vertx.testtools.VertxAssert.testComplete;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ObjectQueryTest extends TestVerticle {
	public static final String MONGO_PERSISTOR_ADDRESS = "vertx.mongo.persistor";
	public static final String OBJECT_MANAGER_ADDRESS = "ssky.object.manager";

	private static final String MONGO_MODULE_NAME = "io.vertx~mod-mongo-persistor~2.0.0-final";
	private static final String SSKY_OBJECT_MODULE_NAME = "me.ssky~object-manager~1.0-final";

	private static final String DB_HOST = "127.0.0.1";
	private static final int DB_PORT = 27017;
	private static final String DB_NAME = "test";

	@Override
	public void start() {
		container.deployModule(MONGO_MODULE_NAME, mongoConfig(), new Handler<AsyncResult<String>>() {
			@Override
			public void handle(AsyncResult<String> result) {
				container.deployModule(SSKY_OBJECT_MODULE_NAME, objectConfig(), new Handler<AsyncResult<String>>() {
					@Override
					public void handle(AsyncResult<String> arg0) {
						ObjectQueryTest.super.start();
					}
				});
			}
		});
	}

	@Test
	public void aOrderSimpleTest() {
		assertEquals(true, true);
		testComplete();
	}

	@Test
	public void bOrderDbInitTest() {
		JsonObject option = new JsonObject();
		option.putString("action", "delete");
		option.putString("collection", "test");
		option.putObject("matcher", new JsonObject());
		vertx.eventBus().send(MONGO_PERSISTOR_ADDRESS, option, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> result) {
				assertEquals(result.body().getString("status"), "ok");
				testComplete();
			}
		});
	}

	@Test
	public void cOrderSaveAllTest() {
		JsonObject option = new JsonObject();
		option.putString("action", "saveall");
		option.putString("collection", "test");

		JsonArray documents = new JsonArray();

		final String[] colorNames = { "black", "red", "yellow", "green", "blue", "gray", "magenta", "brown", "gray", "white" };
		for (int i = 0; i < colorNames.length; i++) {
			JsonObject document = new JsonObject();
			document.putString("color", colorNames[i]);
			document.putNumber("number", i);
			documents.add(document);
		}

		option.putString("documents", documents.encode());

		vertx.eventBus().send(OBJECT_MANAGER_ADDRESS, option, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> result) {
				assertEquals(colorNames.length, result.body().getArray("results").size());
				for (int i = 0; i < colorNames.length; i++) {
					assertNotNull(((JsonObject) result.body().getArray("results").get(i)).getObject("success"));
					assertNotNull(((JsonObject) result.body().getArray("results").get(i)).getObject("success").getString("objectId"));
				}
				testComplete();
			}
		});
	}

	@Test
	public void dOrderNumberQueryTest() {
		JsonObject option = new JsonObject();
		option.putString("action", "retrieve");
		option.putString("collection", "test");

		final int gt = 1;
		final int lt = 4;

		JsonObject matcher = new JsonObject();
		matcher.putObject("number", new JsonObject().putNumber("$gt", gt).putNumber("$lt", lt));
		option.putObject("matcher", matcher);

		vertx.eventBus().send(OBJECT_MANAGER_ADDRESS, option, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> result) {
				assertEquals((lt - gt - 1), result.body().getArray("results").size());
				testComplete();
			}
		});
	}

	@Test
	public void eOrderEqualQueryTest() {
		JsonObject option = new JsonObject();
		option.putString("action", "retrieve");
		option.putString("collection", "test");

		JsonObject matcher = new JsonObject();
		matcher.putString("color", "gray");
		option.putObject("matcher", matcher);

		vertx.eventBus().send(OBJECT_MANAGER_ADDRESS, option, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> result) {
				assertEquals(2, result.body().getArray("results").size());
				testComplete();
			}
		});
	}

	@Test
	public void fOrderNotEqualQueryTest() {
		JsonObject option = new JsonObject();
		option.putString("action", "retrieve");
		option.putString("collection", "test");

		JsonObject matcher = new JsonObject();
		matcher.putObject("color", new JsonObject().putString("$ne", "gray"));
		option.putObject("matcher", matcher);

		vertx.eventBus().send(OBJECT_MANAGER_ADDRESS, option, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> result) {
				assertEquals(8, result.body().getArray("results").size());
				testComplete();
			}
		});
	}

	@Test
	public void zOrderDbInitTest() {
		JsonObject option = new JsonObject();
		option.putString("action", "delete");
		option.putString("collection", "test");
		option.putObject("matcher", new JsonObject());
		vertx.eventBus().send(MONGO_PERSISTOR_ADDRESS, option, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> result) {
				assertEquals(result.body().getString("status"), "ok");
				testComplete();
			}
		});
	}

	private JsonObject mongoConfig() {
		JsonObject mongoConfig = new JsonObject();
		mongoConfig.putString("address", MONGO_PERSISTOR_ADDRESS);
		mongoConfig.putString("host", DB_HOST);
		mongoConfig.putNumber("port", DB_PORT);
		mongoConfig.putString("db_name", DB_NAME);
		return mongoConfig;
	}

	private JsonObject objectConfig() {
		JsonObject objectConfig = new JsonObject();
		objectConfig.putString("this_address", OBJECT_MANAGER_ADDRESS);
		objectConfig.putString("db_address", MONGO_PERSISTOR_ADDRESS);
		return objectConfig;
	}

}

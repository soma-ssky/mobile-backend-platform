package test.java.me.ssky;

import main.java.me.ssky.util.Util;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ObjectCRUDTest extends TestVerticle {
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
						ObjectCRUDTest.super.start();
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
	public void cOrderSaveObjectTest() {
		JsonObject option = new JsonObject();
		option.putString("action", "save");
		option.putString("collection", "test");

		JsonObject documents = new JsonObject();
		documents.putString("string", "string");
		documents.putNumber("number", 3);
		documents.putBoolean("boolean", true);
		option.putObject("documents", documents);

		vertx.eventBus().send(OBJECT_MANAGER_ADDRESS, option, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> result) {
				assertNotNull(result.body().getString("createdAt"));
				assertNotNull(result.body().getString("objectId"));
				testComplete();
			}
		});
	}

	@Test
	public void dOrderFetchObjectTest() {

		JsonObject option = new JsonObject();
		option.putString("action", "save");
		option.putString("collection", "test");

		JsonObject documents = new JsonObject();
		documents.putString("string", "string");
		documents.putNumber("number", 3);
		documents.putBoolean("boolean", true);
		option.putObject("documents", documents);

		vertx.eventBus().send(OBJECT_MANAGER_ADDRESS, option, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> result) {
				final String objectId = result.body().getString("objectId");

				JsonObject option = new JsonObject();
				option.putString("action", "fetch");
				option.putString("collection", "test");

				JsonObject matcher = new JsonObject();
				matcher.putString("_id", objectId);
				option.putObject("matcher", matcher);

				vertx.eventBus().send(OBJECT_MANAGER_ADDRESS, option, new Handler<Message<JsonObject>>() {
					@Override
					public void handle(Message<JsonObject> result) {
						assertEquals(objectId, result.body().getString("objectId"));
						assertEquals("string", result.body().getString("string"));
						assertEquals(3, result.body().getNumber("number"));
						assertEquals(true, result.body().getBoolean("boolean"));
						testComplete();
					}
				});
			}
		});
	}

	@Test
	public void eOrderUpdateObjectTest() {
		JsonObject option = new JsonObject();
		option.putString("action", "save");
		option.putString("collection", "test");

		JsonObject documents = new JsonObject();
		documents.putString("string", "string");
		documents.putNumber("number", 3);
		documents.putBoolean("boolean", true);
		option.putObject("documents", documents);

		vertx.eventBus().send(OBJECT_MANAGER_ADDRESS, option, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> result) {
				final String objectId = result.body().getString("objectId");

				JsonObject option = new JsonObject();
				option.putString("action", "update");
				option.putString("collection", "test");

				JsonObject matcher = new JsonObject();
				matcher.putString("_id", objectId);
				option.putObject("matcher", matcher);

				JsonObject objNew = new JsonObject();
				objNew.putString("string", "stringstring");
				option.putObject("objNew", objNew);

				vertx.eventBus().send(OBJECT_MANAGER_ADDRESS, option, new Handler<Message<JsonObject>>() {
					@Override
					public void handle(Message<JsonObject> result) {
						assertNotNull(result.body().getString("updatedAt"));

						JsonObject option = new JsonObject();
						option.putString("action", "fetch");
						option.putString("collection", "test");

						JsonObject matcher = new JsonObject();
						matcher.putString("_id", objectId);
						option.putObject("matcher", matcher);

						vertx.eventBus().send(OBJECT_MANAGER_ADDRESS, option, new Handler<Message<JsonObject>>() {
							@Override
							public void handle(Message<JsonObject> result) {
								assertEquals("stringstring", result.body().getString("string"));
								testComplete();
							}
						});

					}
				});
			}
		});
	}

	@Test
	public void fOrderDeleteObjectTest() {
		JsonObject option = new JsonObject();
		option.putString("action", "save");
		option.putString("collection", "test");

		JsonObject documents = new JsonObject();
		documents.putString("string", "string");
		documents.putNumber("number", 3);
		documents.putBoolean("boolean", true);
		option.putObject("documents", documents);

		vertx.eventBus().send(OBJECT_MANAGER_ADDRESS, option, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> result) {
				final String objectId = result.body().getString("objectId");

				JsonObject option = new JsonObject();
				option.putString("action", "delete");
				option.putString("collection", "test");

				JsonObject matcher = new JsonObject();
				matcher.putString("_id", objectId);
				option.putObject("matcher", matcher);

				vertx.eventBus().send(OBJECT_MANAGER_ADDRESS, option, new Handler<Message<JsonObject>>() {
					@Override
					public void handle(Message<JsonObject> result) {
						assertEquals(new JsonObject(), result.body());
						testComplete();
					}
				});
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

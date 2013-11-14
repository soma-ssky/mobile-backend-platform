package test.java;

import org.junit.FixMethodOrder;

import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.assertNotNull;
import static org.vertx.testtools.VertxAssert.assertNull;
import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.testComplete;
import static org.vertx.testtools.VertxAssert.assertNotSame;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserEventBusTest extends TestVerticle {
	private EventBus eb;
// 비밀번호 바뀌면 토큰 재발급
	@Override
	public void start() {
		eb = vertx.eventBus();
		container.deployModule("io.vertx~mod-mongo-persistor~2.0.0-final", mongoConfig(), new AsyncResultHandler<String>() {
			@Override
			public void handle(final AsyncResult<String> ar1) {
				container.deployModule("io.vertx~mod-user-mgr~1.0", userConfig(), new AsyncResultHandler<String>() {
					@Override
					public void handle(final AsyncResult<String> ar2) {
						container.deployModule("io.vertx~mod-auth-mgr~2.1.0-SNAPSHOT", authConfig(), new AsyncResultHandler<String>() {
							@Override
							public void handle(AsyncResult<String> ar3) {
								if (ar1.failed() || ar2.failed() || ar3.failed()) return;
								// after deploying all module, start test
								UserEventBusTest.super.start();
							}
						});
					}
				});
			}
		});
	}

	@Test
	public void simpleTest() throws Exception {
		assertEquals(true, true);
		// assertEquals(true, false);
		testComplete();
	}

	@Test
	public void aOrderInitUserDbTest() throws Exception {
		JsonObject initDbJson = new JsonObject().putString("collection", "test_users").putString("action", "delete").putObject("matcher", new JsonObject());
		eb.send("vertx.mongo", initDbJson, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> initResult) {
				assertEquals("ok", initResult.body().getString("status"));
				testComplete();
			}
		});
	}

	@Test
	public void bOrderNotIdJoinTest() throws Exception {
		JsonObject userData = new JsonObject().putString("password", "123").putString("mail", "conakuki@naver.com");
		JsonObject testJoinData = new JsonObject().putObject("userdata", userData).putString("action", "join");
		eb.send("vertx.user", testJoinData, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> joinResult) {
				// not id test
				assertEquals("error", joinResult.body().getString("status"));
				assertEquals("invalid data", joinResult.body().getString("message"));
				testComplete();
			}
		});
	}

	@Test
	public void cOrderNotPasswordJoinTest() throws Exception {
		JsonObject userData = new JsonObject().putString("userid", "taehoon").putString("mail", "conakuki@naver.com");
		JsonObject testJoinData = new JsonObject().putObject("userdata", userData).putString("action", "join");
		eb.send("vertx.user", testJoinData, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> joinResult) {
				// not id test
				assertEquals("error", joinResult.body().getString("status"));
				assertEquals("invalid data", joinResult.body().getString("message"));
				testComplete();
			}
		});
	}

	@Test
	public void dOrderVaildJoinAndGetUserAndDropTest() throws Exception {
		JsonObject userData = new JsonObject().putString("userid", "taehoon").putString("password", "123").putString("mail", "conakuki@naver.com");
		JsonObject testJoinData = new JsonObject().putObject("userdata", userData).putString("action", "join");
		eb.send("vertx.user", testJoinData, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> joinResult) {
				// join test
				assertEquals("ok", joinResult.body().getString("status"));
				assertNotNull(joinResult.body().getString("_id"));

				final String _id = joinResult.body().getString("_id");
				JsonObject testGetUserData = new JsonObject().putString("action", "getuser").putString("_id", _id);
				eb.send("vertx.user", testGetUserData, new Handler<Message<JsonObject>>() {
					@Override
					public void handle(Message<JsonObject> getUserResult) {
						// get user test
						assertEquals("taehoon", getUserResult.body().getObject("userdata").getString("userid"));
						// password encoding test
						assertNotSame("123", getUserResult.body().getObject("userdata").getString("password"));

						JsonObject dropData = new JsonObject().putString("_id", _id).putString("action", "drop");
						eb.send("vertx.user", dropData, new Handler<Message<JsonObject>>() {
							@Override
							public void handle(Message<JsonObject> dropResult) {
								// drop test
								assertEquals("ok", dropResult.body().getString("status"));
								testComplete();
							}
						});
					}
				});
			}
		});
	}

	@Test
	public void eOrderReJoinTest() throws Exception {
		JsonObject userData = new JsonObject().putString("userid", "taehoon").putString("password", "123").putString("mail", "conakuki@naver.com");
		JsonObject testJoinData = new JsonObject().putObject("userdata", userData).putString("action", "join");
		eb.send("vertx.user", testJoinData, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> joinResult) {
				// join test
				assertEquals("ok", joinResult.body().getString("status"));
				assertNotNull(joinResult.body().getString("_id"));
				testComplete();
			}
		});
	}

	@Test
	public void fOrderDuplicatedJoinTest() throws Exception {
		JsonObject userData = new JsonObject().putString("userid", "taehoon").putString("password", "123").putString("mail", "conakuki@naver.com");
		JsonObject testJoinData = new JsonObject().putObject("userdata", userData).putString("action", "join");
		eb.send("vertx.user", testJoinData, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> duplicatedResult) {
				// duplicated id test
				assertEquals("error", duplicatedResult.body().getString("status"));
				assertEquals("existed id", duplicatedResult.body().getString("message"));
				testComplete();
			}
		});
	}

	@Test
	public void gOrderInvalidDropTest() throws Exception {
		JsonObject dropData = new JsonObject().putString("_id", "1234567890").putString("action", "drop");
		eb.send("vertx.user", dropData, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> dropResult) {
				// drop test
				assertEquals("error", dropResult.body().getString("status"));
				assertEquals("not matched", dropResult.body().getString("message"));
				testComplete();
			}
		});
	}

	@Test
	public void hOrderInitTokenDbTest() throws Exception {
		JsonObject initDbJson = new JsonObject().putString("collection", "test_tokens").putString("action", "delete").putObject("matcher", new JsonObject());
		eb.send("vertx.mongo", initDbJson, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> initResult) {
				assertEquals("ok", initResult.body().getString("status"));
				testComplete();
			}
		});

	}

	@Test
	public void iOrderLoginAndAuthAndLogoutTest() throws Exception {
		JsonObject loginData = new JsonObject().putString("userid", "taehoon").putString("password", "123").putString("action", "login");
		eb.send("vertx.auth", loginData, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> loginResult) {
				// login test
				assertEquals("ok", loginResult.body().getString("status"));
				assertNotNull(loginResult.body().getString("token"));

				final String token = loginResult.body().getString("token");
				JsonObject authData = new JsonObject().putString("token", token).putString("action", "authorise");
				eb.send("vertx.auth", authData, new Handler<Message<JsonObject>>() {
					@Override
					public void handle(Message<JsonObject> loginResult) {
						// auth test
						assertEquals("ok", loginResult.body().getString("status"));
						assertNotNull(loginResult.body().getString("user_id"));

						JsonObject logoutData = new JsonObject().putString("token", token).putString("action", "logout");
						eb.send("vertx.auth", logoutData, new Handler<Message<JsonObject>>() {
							@Override
							public void handle(Message<JsonObject> logoutResult) {
								// logout test
								assertEquals("ok", logoutResult.body().getString("status"));
								testComplete();
							}
						});
					}
				});
			}
		});
	}

	@Test
	public void jOrderDupLoginTest() throws Exception {
		JsonObject loginData = new JsonObject().putString("userid", "taehoon").putString("password", "123").putString("action", "login");
		eb.send("vertx.auth", loginData, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> loginResult) {
				// login test
				assertEquals("ok", loginResult.body().getString("status"));
				assertNotNull(loginResult.body().getString("token"));

				final String token = loginResult.body().getString("token");
				JsonObject dupLoginData = new JsonObject().putString("userid", "taehoon").putString("password", "123").putString("action", "login");
				eb.send("vertx.auth", dupLoginData, new Handler<Message<JsonObject>>() {
					@Override
					public void handle(Message<JsonObject> dupLoginResult) {
						// dup login test
						assertEquals("ok", dupLoginResult.body().getString("status"));
						assertEquals(token, dupLoginResult.body().getString("token"));
						testComplete();
					}
				});

			}
		});
	}

	@Test
	public void kOrderInvalidUserLoginTest() throws Exception {
		// different password input
		JsonObject invalidLoginData = new JsonObject().putString("userid", "taehoon").putString("password", "12").putString("action", "login");
		eb.send("vertx.auth", invalidLoginData, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> loginResult) {
				// invalid user login test
				assertEquals("error", loginResult.body().getString("status"));
				assertEquals("not matched", loginResult.body().getString("message"));
				assertNull(loginResult.body().getString("token"));
				testComplete();
			}
		});
	}

	private JsonObject userConfig() {
		JsonObject authConfig = new JsonObject();
		authConfig.putString("this_address", "vertx.user").putString("user_collection", "test_users");
		authConfig.putString("persistor_address", "vertx.mongo");
		return authConfig;
	}

	private JsonObject authConfig() {
		JsonObject authConfig = new JsonObject();
		authConfig.putString("this_address", "vertx.auth").putString("user_collection", "test_users");
		authConfig.putString("persistor_address", "vertx.mongo").putString("token_collection", "test_tokens");
		return authConfig;
	}

	private JsonObject mongoConfig() {
		JsonObject mongoConfig = new JsonObject();
		mongoConfig.putString("address", "vertx.mongo").putString("host", "127.0.0.1");
		mongoConfig.putNumber("port", 27017).putNumber("pool_size", 100).putString("db_name", "test");
		return mongoConfig;
	}
}

// password not decoding, token 
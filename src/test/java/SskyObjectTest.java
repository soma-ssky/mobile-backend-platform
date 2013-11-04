
package test.java;

import static org.junit.Assert.assertNotNull;

import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.assertNotSame;
import static org.vertx.testtools.VertxAssert.assertNull;
import static org.vertx.testtools.VertxAssert.testComplete;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import main.java.object.callback.CountCallback;
import main.java.object.callback.DeleteCallback;
import main.java.object.callback.FindCallback;
import main.java.object.callback.GetCallback;
import main.java.object.callback.RefreshCallback;
import main.java.object.callback.SaveCallback;
import main.java.object.exception.SskyException;
import main.java.object.object.SskyObject;
import main.java.object.object.SskyQuery;
import main.java.util.MongoUtil;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SskyObjectTest extends TestVerticle {
	private String[] colors = { "red", "blue", "red", "black", "cyan", "magenta", "red", "yellow", "brown", "dark gray", "gray", "white", "black" };
	private int[] keys = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 };

	@Override
	public void start() {
		
		container.deployModule("io.vertx~mod-mongo-persistor~2.0.0-final", mongoConfig(), new AsyncResultHandler<String>() {
			@Override
			public void handle(AsyncResult<String> ar) {
				if (ar.failed()) return;
				// after deploying all module, start test
				SskyObjectTest.super.start();
			}
		});
	}

	@Test
	public void aOrderInitDb() {
		vertx.eventBus().send("vertx.mongo", MongoUtil.deleteConfig("test", new JsonObject()), new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> result) {
				assertEquals("ok", result.body().getString("status"));
				testComplete();
			}
		});
	}

	@Test
	public void bOrderSaveTestData() {
		for (int i = 0; i < colors.length; i++) {
			SskyObject obj = new SskyObject("test");
			obj.put("color", colors[i]);
			obj.put("key", keys[i]);
			obj.saveInBackground(new SaveCallback() {
				@Override
				public void done(Exception e) {
					assertNull(e);
					testComplete();
				}
			});
		}
	}

	@Test
	public void cOrderGetInBackgroundTest() {
		SskyObject object = new SskyObject("test");
		object.put("color", "green").put("key", 543525).save();
		String objectId = object.getObjectId();
		assertNotNull(objectId);

		SskyQuery<SskyObject> query = SskyQuery.getQuery("test");
		query.whereEqualTo("color", "green").getInBackground(objectId, new GetCallback<SskyObject>() {
			@Override
			public void done(SskyObject object, SskyException e) {
				assertNull(e);
				assertNotNull(object);
				assertEquals("green", object.getString("color"));
				testComplete();
			}
		});
	}

	@Test
	public void dOrderCountInBackgroundTest() {
		SskyQuery<SskyObject> query = SskyQuery.getQuery("test");
		query.whereEqualTo("color", "red").countInBackground(new CountCallback() {
			@Override
			public void done(int count, SskyException e) {
				assertNull(e);
				assertEquals(3, count);
				testComplete();
			}
		});
	}

	@Test
	public void eOrderFindInBackgroundTest() {
		SskyQuery<SskyObject> query = SskyQuery.getQuery("test");
		query.whereEqualTo("color", "black").findInBackground(new FindCallback<SskyObject>() {
			@Override
			public void done(List<SskyObject> objects, SskyException e) {
				assertNull(e);
				assertNotNull(objects);
				assertEquals("black", objects.get(0).getString("color"));
				assertEquals("black", objects.get(1).getString("color"));
				assertEquals(2, objects.size());
				testComplete();
			}
		});
	}

	@Test
	public void fOrderRefreshInBackgroundTest() {
		SskyObject object = new SskyObject("test");
		object.put("color", "dark gray").put("key", 4567).save();
		final String beforeId = object.getObjectId();
		final Date beforeLastUpdateAt = object.getLastUpdateAt();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {}

		object.update("color", "light gray");
		object.refreshInBackground(new RefreshCallback() {
			@Override
			public void done(SskyObject object, SskyException e) {
				assertNull(e);
				assertNotNull(object);
				assertEquals(beforeId, object.getObjectId());
				assertEquals("light gray", object.getString("color"));
				assertNotSame(beforeLastUpdateAt, object.getLastUpdateAt());
				testComplete();
			}
		});
	}

	@Test
	public void gOrderDeleteAndFetchInBackgroundTest() {
		final SskyObject object = new SskyObject("test");
		object.put("color", "daf").put("key", 123).save();
		object.deleteInBackground(new DeleteCallback() {
			@Override
			public void done(SskyException e) {
				assertNull(e);
				object.fetchInBackground(new GetCallback<SskyObject>() {
					@Override
					public void done(SskyObject object, SskyException e) {
						assertNull(object);
						assertNull(e);
						testComplete();
					}
				});
			}
		});
	}

	@Test
	public void hOrderFetchInBackgroundTest() {
		SskyObject object = new SskyObject("test");
		object.put("color", "dark brown").put("key", 908);
		object.save();
		object.put("color", "red");
		object.fetchInBackground(new GetCallback<SskyObject>() {
			@Override
			public void done(SskyObject object, SskyException e) {
				assertNotNull(object);
				assertNull(e);
				assertEquals("dark brown", object.getString("color"));
				testComplete();
			}
		});
	}

	@Test
	public void iOrderQueryTest1() {
		SskyQuery<SskyObject> query = SskyQuery.getQuery("test");
		query.whereGreaterThanOrEqualTo("key", 3).whereLessThanOrEqualTo("key", 7).whereEqualTo("color", "red").findInBackground(new FindCallback<SskyObject>() {
			@Override
			public void done(List<SskyObject> objects, SskyException e) {
				assertNotNull(objects);
				assertNull(e);
				assertEquals(2, objects.size());
				testComplete();
			}
		});
	}

	@Test
	public void jOrderQueryTest2() {
		SskyQuery<SskyObject> query = SskyQuery.getQuery("test");
		query.whereContainedIn("key", Arrays.asList(2, 4, 6)).findInBackground(new FindCallback<SskyObject>() {
			@Override
			public void done(List<SskyObject> objects, SskyException e) {
				assertNotNull(objects);
				assertNull(e);
				assertEquals(3, objects.size());
				testComplete();
			}
		});
	}

	@Test
	public void kOrderAscendingTest() {
		SskyQuery<SskyObject> query = SskyQuery.getQuery("test");
		query.orderByAscending("color").findInBackground(new FindCallback<SskyObject>() {
			@Override
			public void done(List<SskyObject> objects, SskyException e) {
				assertNotNull(objects);
				assertNull(e);
				assertEquals("black", objects.get(0).get("color"));
				assertEquals("blue", objects.get(2).get("color"));
				assertEquals("brown", objects.get(3).get("color"));
				assertEquals("cyan", objects.get(4).get("color"));
				testComplete();
			}
		});
	}

	@Test
	public void lOrderDescendingTest() {
		SskyQuery<SskyObject> query = SskyQuery.getQuery("test");
		query.orderByDescending("key").findInBackground(new FindCallback<SskyObject>() {
			@Override
			public void done(List<SskyObject> objects, SskyException e) {
				assertNotNull(objects);
				assertNull(e);
				assertEquals(13, objects.get(2).get("key"));
				assertEquals(12, objects.get(3).get("key"));
				assertEquals(11, objects.get(4).get("key"));
				testComplete();
			}
		});
	}

	@Test
	public void mOrderIncrementTest() {
		SskyObject object = new SskyObject("test");
		object.put("color", "red").put("key", 100).save();
		object.increment("key", 100);
		object.save();
		object.fetchInBackground(new GetCallback<SskyObject>() {
			@Override
			public void done(SskyObject object, SskyException e) {
				assertNotNull(object);
				assertNull(e);
				assertEquals(200, object.get("key"));
				testComplete();
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

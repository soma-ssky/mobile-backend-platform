package expired.object.object;

import java.util.Collection;
import java.util.List;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import expired.object.callback.CountCallback;
import expired.object.callback.FindCallback;
import expired.object.callback.GetCallback;
import expired.object.exception.SskyException;
import expired.object.request.HttpRequest;

public class SskyQuery<T extends SskyObject> {
	private HttpRequest httpRequest = new HttpRequest();

	private String className;

	private JsonObject matcher = new JsonObject();
	private JsonObject option = new JsonObject();

	public SskyQuery(Class<T> subclass) {
		// ..........?
	}

	public SskyQuery(String className) {
		this.className = className;
	}

	public static void clearAllCachedResults() {}

	public static <T extends SskyObject> SskyQuery<T> getQuery(Class<T> subclass) {
		return null;
	}

	public static <T extends SskyObject> SskyQuery<T> getQuery(String className) {
		return new SskyQuery<T>(className);
	}

	public static <T extends SskyObject> SskyQuery<T> or(List<SskyQuery<T>> queries) {
		return null;
	}

	public SskyQuery<T> addAscendingOrder(String key) {
		return null;
	}

	public SskyQuery<T> addDescendingOrder(String key) {
		return null;
	}

	public void cancel() {}

	public void clearCachedResult() {}

	public int count() {
		List<SskyObject> list;
		try {
			list = httpRequest.query(className, matcher, option);
			return list.size();
		} catch (SskyException e) {
			return 0;
		}
	}

	public void countInBackground(final CountCallback callback) {
		final SskyQuery<T> query = this;
		new Thread() {
			@Override
			public void run() {
				try {
					int count = httpRequest.query(query.getClassName(), query.getMatcher(), query.getOption()).size();
					callback.done(count, null);
				} catch (SskyException e) {
					callback.done(0, e);
				}
			}
		}.start();
	}

	public List<T> find() {
		try {
			return httpRequest.query(className, matcher, option);
		} catch (SskyException e) {
			return null;
		}
	}

	public void findInBackground(final FindCallback<T> callback) {
		new Thread() {
			@Override
			public void run() {
				try {
					List<T> list = httpRequest.query(className, matcher, option);
					callback.done(list, null);
				} catch (SskyException e) {
					callback.done(null, e);
				}
			}
		}.start();
	}

	public T get(String objectId) {
		try {
			return httpRequest.retrieve(className, objectId);
		} catch (SskyException e) {
			return null;
		}
	}

	// public SskyQuery.CachePolicy getCachePolicy() {return null;}
	private String getClassName() {
		return className;
	}

	private JsonObject getOption() {
		return option;
	}

	private JsonObject getMatcher() {
		return matcher;
	}

	public T getFirst() { // findFirst???
		return find().get(0);
	}

	public void getFirstInBackground(final GetCallback<T> callback) {
		new Thread() {
			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				try {
					T object = (T) httpRequest.query(className, matcher, option).get(0);
					callback.done(object, null);
				} catch (SskyException e) {
					callback.done(null, e);
				}
			}
		}.start();
	}

	public void getInBackground(final String objectId, final GetCallback<T> callback) {
		new Thread() {
			@Override
			public void run() {
				try {
					T object = httpRequest.retrieve(className, objectId);
					callback.done(object, null);
				} catch (SskyException e) {
					callback.done(null, e);
				}
			}
		}.start();
	}

	public int getLimit() {
		return option.getInteger("limit");
	}

	public long getMaxCacheAge() {
		return 0;
	}

	public int getSkip() {
		return option.getInteger("skip");
	}

	public boolean hasCachedResult() {
		return false;
	}

	public void include(String key) {}

	public SskyQuery<T> orderByAscending(String key) {
		option.putObject("sort", new JsonObject().putNumber(key, 1));
		return this;
	}

	public SskyQuery<T> orderByDescending(String key) {
		option.putObject("sort", new JsonObject().putNumber(key, -1));
		return this;
	}

	public void selectKeys(Collection<String> selectKeys) {
		JsonObject keys = new JsonObject();
		for (String selectKey : selectKeys) {
			keys.putNumber(selectKey, 1);
		}
		option.putObject("keys", keys);
	}

	public void setLimit(int limit) {
		option.putNumber("limit", limit);
	}

	public void setMaxCacheAge(long maxAgeInMilliseconds) {}

	public void setSkip(int skip) {
		option.putNumber("skip", skip);
	}

	public void setTrace(boolean shouldTrace) {}

	public SskyQuery<T> whereContainedIn(String key, Collection<? extends Object> values) {
		matcher.putObject(key, new JsonObject().putArray("$in", new JsonArray(values.toArray())));
		return this;
	}

	public SskyQuery<T> whereContains(String key, String substring) {
		return this;
	}

	public SskyQuery<T> whereContainsAll(String key, Collection<?> values) {
		return this;
	}

	public SskyQuery<T> whereDoesNotExist(String key) {
		if (matcher.getObject(key) == null) {
			matcher.putValue(key, new JsonObject().putBoolean("$exists", false));
		} else {
			matcher.getObject(key).putBoolean("$exists", false);
		}
		return this;
	}

	public SskyQuery<T> whereDoesNotMatchKeyInQuery(String key, String keyInQuery, SskyQuery<?> query) {
		return null;
	}

	public SskyQuery<T> whereDoesNotMatchQuery(String key, SskyQuery<?> query) {
		return null;
	}

	public SskyQuery<T> whereEndsWith(String key, String suffix) {
		return this;
	}

	public SskyQuery<T> whereEqualTo(String key, Object value) {
		matcher.putValue(key, value);
		return this;
	}

	public SskyQuery<T> whereExists(String key) {
		if (matcher.getObject(key) == null) {
			matcher.putValue(key, new JsonObject().putBoolean("$exists", true));
		} else {
			matcher.getObject(key).putBoolean("$exists", true);
		}
		return this;
	}

	public SskyQuery<T> whereGreaterThan(String key, Object value) {
		if (matcher.getObject(key) == null) {
			matcher.putValue(key, new JsonObject().putValue("$gt", value));
		} else {
			matcher.getObject(key).putValue("$gt", value);
		}
		return this;
	}

	public SskyQuery<T> whereGreaterThanOrEqualTo(String key, Object value) {
		if (matcher.getObject(key) == null) {
			matcher.putValue(key, new JsonObject().putValue("$gte", value));
		} else {
			matcher.getObject(key).putValue("$gte", value);
		}
		return this;
	}

	public SskyQuery<T> whereLessThan(String key, Object value) {
		if (matcher.getObject(key) == null) {
			matcher.putValue(key, new JsonObject().putValue("$lt", value));
		} else {
			matcher.getObject(key).putValue("$lt", value);
		}
		return this;
	}

	public SskyQuery<T> whereLessThanOrEqualTo(String key, Object value) {
		if (matcher.getObject(key) == null) {
			matcher.putValue(key, new JsonObject().putValue("$lte", value));
		} else {
			matcher.getObject(key).putValue("$lte", value);
		}
		return this;
	}

	public SskyQuery<T> whereMatches(String key, String regex) {
		return this;
	}

	public SskyQuery<T> whereMatches(String key, String regex, String modifiers) {
		return this;
	}

	public SskyQuery<T> whereMatchesKeyInQuery(String key, String keyInQuery, SskyQuery<?> query) {
		return null;
	}

	public SskyQuery<T> whereMatchesQuery(String key, SskyQuery<?> query) {
		return null;
	}

	public SskyQuery<T> whereNotContainedIn(String key, Collection<? extends Object> values) {
		return null;
	}

	public SskyQuery<T> whereNotEqualTo(String key, Object value) {
		matcher.putValue(key, new JsonObject().putValue("$ne", value));
		return this;
	}

	public SskyQuery<T> whereStartsWith(String key, String prefix) {
		return this;
	}

	// public void setCachePolicy(SskyQuery.CachePolicy newCachePolicy) {}
	// public SskyQuery<T> whereNear(String key, SskyGeoPoint point) {return null;}
	// public SskyQuery<T> whereWithinGeoBox(String key, SskyGeoPoint southwest,SskyGeoPoint northeast) {return null;}
	// public SskyQuery<T> whereWithinKilometers(String key, SskyGeoPoint point, double maxDistance) {return null;}
	// public SskyQuery<T> whereWithinMiles(String key, SskyGeoPoint point, double maxDistance) {return null;}
	// public SskyQuery<T> whereWithinRadians(String key, SskyGeoPoint point, double maxDistance) {return null;}

}

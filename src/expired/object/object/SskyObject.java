package expired.object.object;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import expired.object.callback.DeleteCallback;
import expired.object.callback.FindCallback;
import expired.object.callback.GetCallback;
import expired.object.callback.RefreshCallback;
import expired.object.callback.SaveCallback;
import expired.object.exception.SskyException;
import expired.object.request.HttpRequest;
import expired.object.util.Util;

public class SskyObject {
	private static HttpRequest httpRequest = new HttpRequest();

	private String objectId = null;
	private Date createAt = null;
	private Date lastUpdateAt = null;
	private Map<String, Object> data = new HashMap<String, Object>();
	private String className;

	protected SskyObject() {

	}

	public SskyObject(String name) {
		this.className = name;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public static <T extends SskyObject> T create(Class<T> subclass) {
		return null;
	}

	public static SskyObject create(String className) {
		return new SskyObject(className);
	}

	public static <T extends SskyObject> T createWithoutData(Class<T> subclass, String objectId) {
		return null;
	}

	public static SskyObject createWithoutData(String className, String objectId) {
		SskyObject newObj = new SskyObject(className);
		newObj.setObjectId(objectId);
		return newObj;
	}

	public static void deleteAll(List<SskyObject> objects) {
		for (SskyObject object : objects) {
			object.delete();
		}
	}

	public static void deleteAllInBackground(final List<SskyObject> objects, final DeleteCallback callback) {
		new Thread() {
			@Override
			public void run() {
				try {
					for (SskyObject object : objects) {
						httpRequest.delete(object.getClassName(), object.getObjectId());
					}
					callback.done(null);
				} catch (SskyException e) {
					callback.done(e);
				}
			}
		}.start();
	}

	public static List<SskyObject> fetchAll(List<SskyObject> objects) {
		List<SskyObject> list = new ArrayList<SskyObject>();
		for (SskyObject object : objects) {
			list.add(object.fetch());
		}
		return list;
	}

	public static <T extends SskyObject> T fetchAllIfNeeded(List<T> objects) {
		return null;
	}

	public static <T extends SskyObject> T fetchAllIfNeededInBackground(List<T> objects, FindCallback<T> callback) {
		return null;
	}

	public static <T extends SskyObject> T fetchAllInBackground(List<T> objects, FindCallback<T> callback) {
		return null;
	}

	public static void registerSubclass(Class<? extends SskyObject> subclass) {}

	public static void saveAll(List<SskyObject> objects) {
		for (SskyObject object : objects) {
			object.save();
		}
	}

	public static void saveAllInBackground(final List<SskyObject> objects) {
		new Thread() {
			@Override
			public void run() {
				for (SskyObject object : objects) {
					object.save();
				}
			}
		}.start();
	}

	// public boolean containsKey(String key) // -> hasKey(), hasValue()

	public static void saveAllInBackground(final List<SskyObject> objects, final SaveCallback callback) {
		new Thread() {
			@Override
			public void run() {
				try {
					for (SskyObject object : objects) {
						httpRequest.create(object);
					}
					callback.done(null);
				} catch (SskyException e) {
					callback.done(e);
				}
			}
		}.start();
	}

	// for array
	public void add(String key, Object value) {
		JsonArray array;
		if (!hasKey(key)) {
			array = new JsonArray();
			data.put(key, array);
		} else if (data.get(key) instanceof JsonArray) {
			array = (JsonArray) data.get(key);
		} else {
			return;
		}
		array.add(value);
	}

	// for array
	public void addAll(String key, Collection<?> values) {
		for (Object value : values) {
			add(key, value);
		}
	}

	// for array
	public void addUnique(String key, Object value) {
		JsonArray array;
		if (!hasKey(key)) {
			array = new JsonArray();
		} else if (data.get(key) instanceof JsonArray) {
			array = (JsonArray) data.get(key);
		} else {
			return;
		}
		if (!array.contains(value)) {
			array.add(value);
		}
	}

	// for array
	public void addAllUnique(String key, Collection<?> values) {
		for (Object value : values) {
			addUnique(key, value);
		}
	}

	public void delete() {
		try {
			httpRequest.delete(className, objectId);
		} catch (Exception e) {}
	}

	public void deleteEventually() {}

	public void deleteEventually(DeleteCallback callback) {}

	public void deleteInBackground() {
		new Thread() {
			@Override
			public void run() {
				delete();
			}
		}.start();
	}

	public void deleteInBackground(final DeleteCallback callback) {
		final SskyObject obj = this;
		new Thread() {
			@Override
			public void run() {
				try {
					httpRequest.delete(obj.getClassName(), obj.getObjectId());
					callback.done(null);
				} catch (SskyException e) {
					callback.done(e);
				}
			}
		}.start();
	}

	public <T extends SskyObject> T fetch() {
		try {
			return httpRequest.retrieve(className, objectId);
		} catch (SskyException e) {
			return null;
		}
	}

	public <T extends SskyObject> T fetchIfNeeded() {
		return null;
	}

	public <T extends SskyObject> T fetchIfNeededInBackground(GetCallback<T> callback) {
		return null;
	}

	public <T extends SskyObject> void fetchInBackground(final GetCallback<T> callback) {
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

	public Object get(String key) {
		try {
			return data.get(key);
		} catch (ClassCastException e) {
			return null;
		}
	}

	public SskyACL getACL() {
		return null;
	}

	public Boolean getBoolean(String key) {
		try {
			return (Boolean) data.get(key);
		} catch (ClassCastException e) {
			return null;
		}
	}

	// SskyFile getSskyFile(String key) {return null;}
	// SskyGeoPoint getSskyGeoPoint(String key) {return null;}

	public byte[] getBytes(String key) {
		try {
			return (byte[]) data.get(key);
		} catch (ClassCastException e) {
			return null;
		}
	}

	public String getClassName() {
		return className;
	}

	public Date getCreateAt() {
		return createAt;
	}

	public Date getDate(String key) {
		return Util.convertDateToString(getString(key));
	}

	public Double getDouble(String key) {
		try {
			return (double) data.get(key);
		} catch (ClassCastException e) {
			return null;
		}
	}

	public Integer getInt(String key) {
		try {
			return (int) data.get(key);
		} catch (ClassCastException e) {
			return null;
		}
	}

	public JsonArray getJsonArray(String key) {
		try {
			return (JsonArray) data.get(key);
		} catch (ClassCastException e) {
			return null;
		}
	}

	public JsonObject getJsonObject(String key) {
		try {
			return (JsonObject) data.get(key);
		} catch (ClassCastException e) {
			return null;
		}
	}

	public Date getLastUpdateAt() {
		return lastUpdateAt;
	}

	public <T> List<T> getList(String key) {
		return null;
	}

	public Long getLong(String key) {
		try {
			return (long) data.get(key);
		} catch (ClassCastException e) {
			return null;
		}
	}

	public <V> Map<String, V> getMap(String key) {
		return null;
	}

	public Number getNumber(String key) {
		try {
			return (Number) data.get(key);
		} catch (ClassCastException e) {
			return null;
		}
	}

	public String getObjectId() {
		return objectId;
	}

	// SskyUser getSskyUser(String key) {return null;}
	<T extends SskyObject> SskyRelation<T> getRelation(String key) {
		return null;
	}

	public SskyObject getSskyObject(String key) {
		try {
			return (SskyObject) data.get(key);
		} catch (ClassCastException e) {
			return null;
		}
	}

	public String getString(String key) {
		try {
			return (String) data.get(key);
		} catch (ClassCastException e) {
			return null;
		}
	}

	// public Date getUpdatedAt() // getLastUpdateAt()
	// public boolean has(String key)

	public boolean hasKey(String key) {
		return data.containsKey(key);
	}

	boolean hasSameId(SskyObject other) {
		return this.objectId == other.getObjectId();
	}

	public boolean hasValue(Object value) {
		return data.containsValue(value);
	}

	public void increment(String key) {
		this.increment(key, 1);
	}

	public void increment(String key, Number amount) {
		Number value = (Number) data.get(key);
		if (value instanceof Integer) {
			value = (value.intValue() + amount.intValue());
		} else if (value instanceof Double) {
			value = (value.doubleValue() + amount.doubleValue());
		} else if (value instanceof Float) {
			value = (value.floatValue() + amount.floatValue());
		} else if (value instanceof Long) {
			value = (value.longValue() + amount.longValue());
		} else if (value instanceof Short) {
			value = (value.shortValue() + amount.shortValue());
		} else if (value instanceof Byte) {
			value = (value.byteValue() + amount.byteValue());
		} else {
			return;
		}

		data.put(key, value);
	}

	public boolean isDataAvailable() {
		return false;
	}

	public Set<String> keySet() {
		return data.keySet();
	}

	public SskyObject put(String key, Object object) {
		data.put(key, object);
		return this;
	}

	public void putAll(Map<String, Object> map) {
		data.putAll(map);
	}

	public void refresh() {
		try {
			httpRequest.update(this);
		} catch (SskyException e) {}
	}

	public void refreshInBackground(final RefreshCallback callback) {
		final SskyObject object = this;
		new Thread() {
			@Override
			public void run() {
				try {
					httpRequest.update(object);
					callback.done(object, null);
				} catch (SskyException e) {
					callback.done(null, e);
				}

			}
		}.start();

	}

	public void remove() {
		for (String key : this.keySet()) {
			remove(key);
		}
	}

	public void remove(String key) {
		data.remove(key);
	}

	// for array
	public void removeAll(String key, Collection<?> values) {
		if (data.get(key) != null && data.get(key) instanceof JsonArray) {
			JsonArray array = (JsonArray) data.get(key);
			Iterator<Object> iter = array.iterator();
			while (iter.hasNext()) {
				if (values.contains(iter.next())) {
					iter.remove();
				}
			}
		}
	}

	public void save() {
		try {
			httpRequest.create(this);
		} catch (SskyException e) {}
	}

	public void saveEventually() {}

	public void saveEventually(SaveCallback callback) {}

	public void saveInBackground() {
		new Thread() {
			@Override
			public void run() {
				save();
			}
		}.start();
	}

	public void saveInBackground(final SaveCallback callback) {
		final SskyObject obj = this;
		new Thread() {
			@Override
			public void run() {
				try {
					httpRequest.create(obj);
					callback.done(null);
				} catch (SskyException e) {
					callback.done(e);
				}
			}
		}.start();
	}

	public void setACL(SskyACL acl) {}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public void update(String key, String value) {
		if (this.hasKey(key)) {
			data.put(key, value);
		}
	}

	public void setCreateAt(Date createAt) {
		this.createAt = createAt;
	}

	public void setLastUpdateAt(Date lastUpdateAt) {
		this.lastUpdateAt = lastUpdateAt;
	}

}

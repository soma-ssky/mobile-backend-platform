package main.java.me.ssky.util;

import org.vertx.java.core.json.JsonObject;

public class MongoUtils {

	public static JsonObject saveConfig(String collection, JsonObject document) {
		JsonObject saveConfig = new JsonObject();
		saveConfig.putString("action", "save");
		saveConfig.putString("collection", collection);
		saveConfig.putObject("document", document);
		return saveConfig;
	}

	public static JsonObject saveAllConfig(String collection, String document) {
		JsonObject saveConfig = new JsonObject();
		saveConfig.putString("action", "saveall");
		saveConfig.putString("collection", collection);
		saveConfig.putString("document", document);
		return saveConfig;
	}

	public static JsonObject updateConfig(String collection, JsonObject criteria, JsonObject objNew) {
		JsonObject updateConfig = new JsonObject();
		updateConfig.putString("action", "update");
		updateConfig.putString("collection", collection);
		updateConfig.putObject("criteria", criteria);
		updateConfig.putObject("objNew", objNew);
		return updateConfig;
	}

	public static JsonObject findOneConfig(String collection, JsonObject matcher, JsonObject key) {
		JsonObject findOneConfig = new JsonObject();
		findOneConfig.putString("action", "findone");
		findOneConfig.putString("collection", collection);
		findOneConfig.putObject("matcher", matcher);
		if (key != null) {
			findOneConfig.putObject("key", key);
		}
		return findOneConfig;
	}

	public static JsonObject findConfig(String collection, JsonObject matcher, JsonObject option) {
		JsonObject findConfig = new JsonObject();
		findConfig.putString("action", "find");
		findConfig.putString("collection", collection);
		findConfig.putObject("matcher", matcher);
		if (option != null) findConfig.mergeIn(option);
		return findConfig;
	}

	public static JsonObject deleteConfig(String collection, JsonObject matcher) {
		JsonObject saveConfig = new JsonObject();
		saveConfig.putString("action", "delete");
		saveConfig.putString("collection", collection);
		saveConfig.putObject("matcher", matcher);
		return saveConfig;
	}

	public static JsonObject countConfig(String collection, JsonObject matcher) {
		JsonObject saveConfig = new JsonObject();
		saveConfig.putString("action", "count");
		saveConfig.putString("collection", collection);
		saveConfig.putObject("matcher", matcher);
		return saveConfig;
	}

}

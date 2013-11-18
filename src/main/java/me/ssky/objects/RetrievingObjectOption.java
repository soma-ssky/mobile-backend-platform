package main.java.me.ssky.objects;

import main.java.me.ssky.util.EventBusOption;
import main.java.me.ssky.util.ServerUtils;

import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

public class RetrievingObjectOption extends EventBusOption {

	@Override
	public String address() {
		return ServerUtils.OBJECT_MANAGER_ADDRESS;
	}

	@Override
	public JsonObject option(HttpServerRequest request, JsonObject data) {
		JsonObject option = new JsonObject();
		option.putString("action", "retrieve");
		option.putString("collection", request.path().split("/")[3]);

		if (data.getObject("where").getString("objectId") != null) {
			data.getObject("where").putString("_id", data.getObject("where").getString("objectId"));
			data.getObject("where").removeField("objectId");
		}

		option.putObject("matcher", data.getObject("where"));

		adjustOrder(data, option);
		adjustKeys(data, option);
		adjustSkip(data, option);
		adjustLimit(data, option);
		return option;
	}

	private void adjustOrder(JsonObject data, JsonObject option) {
		String order = data.getString("order");
		if (order != null) {
			if (order.charAt(0) == '-') {
				option.putObject("$orderby", new JsonObject().putNumber(order.substring(1), 1));
			} else {
				option.putObject("$orderby", new JsonObject().putNumber(order, -1));
			}
		}
	}

	private void adjustLimit(JsonObject data, JsonObject option) {
		Number limit = data.getNumber("limit");
		if (limit != null) {
			option.putNumber("limit", limit);
		}
	}

	private void adjustSkip(JsonObject data, JsonObject option) {
		Number skip = data.getNumber("skip");
		if (skip != null) {
			option.putNumber("skip", skip);
		}
	}

	private void adjustKeys(JsonObject data, JsonObject option) {
		String keys = data.getString("keys");
		if (keys != null) {
			JsonObject obj = new JsonObject();
			String[] splitKeys = keys.split(",");
			for (String key : splitKeys) {
				obj.putNumber(key, 1);
			}
			option.putObject("keys", obj);
		}
	}
}

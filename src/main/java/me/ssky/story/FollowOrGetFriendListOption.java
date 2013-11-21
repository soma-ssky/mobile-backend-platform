package main.java.me.ssky.story;

import java.util.Map;

import main.java.me.ssky.util.EventBusOption;
import main.java.me.ssky.util.ServerUtils;

import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

public class FollowOrGetFriendListOption extends EventBusOption {
	private String method;

	@Override
	public String address() {
		return ServerUtils.STORY_MANAGER_ADDRESS;
	}

	@Override
	public JsonObject option(HttpServerRequest request, JsonObject data) {
		JsonObject option = new JsonObject();
		method = data.getString("_method");
		if (method == null) {
			option.putString("action", "follow");
			option.putString("from", data.getString("from"));
			option.putString("to", data.getString("to"));
		} else if (method.equals("GET")) {
			option.putString("action", "getFollowerList");
			option.putString("from", data.getObject("where").getString("from"));
		}
		return option;
	}

	@Override
	public Map<String, String> headers(JsonObject result) {
		if (method == null) {
			String location = "/1/classes/_Relation/" + result.getString("objectId");
			return ServerUtils.responseHeadersInCreated(201, result.toString().length(), location);
		}
		return super.headers(result);
	}

	@Override
	public int statusCodeInSuccess() {
		if (method == null) return 201;
		return super.statusCodeInSuccess();
	}

}

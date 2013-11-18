package main.java.me.ssky.story;

import main.java.me.ssky.util.EventBusOption;
import main.java.me.ssky.util.ServerUtils;

import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

public class FollowOrGetFriendListOption extends EventBusOption {

	@Override
	public String address() {
		return ServerUtils.STORY_MANAGER_ADDRESS;
	}

	@Override
	public JsonObject option(HttpServerRequest request, JsonObject data) {
		JsonObject option = new JsonObject();
		if (data.getString("_method") == null) {
			option.putString("action", "follow");
			option.putString("from", data.getString("from"));
			option.putString("to", data.getString("to"));
		} else if (data.getString("_method").equals("GET")) {
			option.putString("action", "getFollowerList");
			option.putString("from", data.getString("from"));
		}
		return option;

	}
}

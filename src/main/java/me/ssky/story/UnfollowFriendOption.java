package main.java.me.ssky.story;

import main.java.me.ssky.util.EventBusOption;
import main.java.me.ssky.util.ServerUtils;

import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

public class UnfollowFriendOption extends EventBusOption {

	@Override
	public String address() {
		return ServerUtils.STORY_MANAGER_ADDRESS;
	}

	@Override
	public JsonObject option(HttpServerRequest request, JsonObject data) {
		JsonObject option = new JsonObject();
		if (data.getString("_method").equals("DELETE")) {
			option.putString("action", "unfollow");
			option.putObject("matcher", new JsonObject().putString("_id", request.path().split("/")[4]));
		}
		return option;
	}
}

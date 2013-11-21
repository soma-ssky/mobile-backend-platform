package main.java.me.ssky.story;

import main.java.me.ssky.util.EventBusOption;
import main.java.me.ssky.util.ServerUtils;

import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

public class GettingFollowerOption extends EventBusOption {

	@Override
	public String address() {
		return ServerUtils.STORY_MANAGER_ADDRESS;
	}

	@Override
	public JsonObject option(HttpServerRequest request, JsonObject data) {
		JsonObject option = new JsonObject();
		option.putString("action", "getFollowerList");
		System.out.println(data.encode());
		option.putString("from", data.getObject("where").getString("from"));
		return option;
	}
}

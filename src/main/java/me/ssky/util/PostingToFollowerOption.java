package main.java.me.ssky.util;

import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

public class PostingToFollowerOption extends EventBusOption {

	@Override
	public String address() {
		return ServerUtils.FRIEND_RELATION_MANAGER_ADDRESS;
	}

	@Override
	public JsonObject option(HttpServerRequest request, JsonObject data) {
		JsonObject option = new JsonObject();
		option.putString("action", "getPostList");
		option.putString("myId", request.path().split("/")[4]);
		return option;
	}

}

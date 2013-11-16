package main.java.me.ssky.util;

import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

public class UnfollowFriendOption extends EventBusOption {

	@Override
	public String address() {
		return ServerUtils.FRIEND_RELATION_MANAGER_ADDRESS;
	}

	@Override
	public JsonObject option(HttpServerRequest request, JsonObject data) {
		JsonObject option = new JsonObject();
		option.putString("action", "unfollow");
		option.putString("myId", request.path().split("/")[3]);
		option.putString("friendId", request.path().split("/")[5]);

		return option;
	}

}

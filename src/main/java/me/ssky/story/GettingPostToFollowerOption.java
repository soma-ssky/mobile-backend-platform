package main.java.me.ssky.story;

import main.java.me.ssky.objects.CreatingObjectOption;
import main.java.me.ssky.objects.DeletingObjectOption;
import main.java.me.ssky.objects.UpdatingObjectOption;
import main.java.me.ssky.util.EventBusOption;
import main.java.me.ssky.util.ServerUtils;

import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

public class GettingPostToFollowerOption extends EventBusOption {
	private String method;

	@Override
	public String address() {
		if (method != null && method.equals("GET")) return ServerUtils.STORY_MANAGER_ADDRESS;
		return ServerUtils.OBJECT_MANAGER_ADDRESS;
	}

	@Override
	public JsonObject option(HttpServerRequest request, JsonObject data) {
		JsonObject option = new JsonObject();
		method = data.getString("_method");
		if (method == null) {
			return new CreatingObjectOption().option(request, data);
		} else if (method.equals("PUT")) {
			return new UpdatingObjectOption().option(request, data);
		} else if (method.equals("DELETE")) { return new DeletingObjectOption().option(request, data); }

		option.putString("action", "getPostList");
		option.putString("from", data.getObject("where").getString("from"));
		return option;
	}
}

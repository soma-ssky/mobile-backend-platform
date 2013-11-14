package main.java.me.ssky.objects;

import main.java.me.ssky.util.EventBusOption;
import main.java.me.ssky.util.Util;

import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

public class FetchingObjectOption extends EventBusOption {

	@Override
	public String address() {
		return Util.OBJECT_MANAGER_ADDRESS;
	}

	@Override
	public JsonObject option(HttpServerRequest request, JsonObject data) {
		JsonObject option = new JsonObject();
		option.putString("action", "fetch");
		option.putString("collection", request.path().split("/")[3]);
		option.putObject("matcher", new JsonObject().putString("_id", request.path().split("/")[4]));
		return option;
	}
}

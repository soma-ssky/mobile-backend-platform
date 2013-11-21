package main.java.me.ssky.objects;

import main.java.me.ssky.util.EventBusOption;
import main.java.me.ssky.util.ServerUtils;

import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

public class PostObjectOption extends EventBusOption {

	@Override
	public String address() {
		return ServerUtils.OBJECT_MANAGER_ADDRESS;
	}

	@Override
	public JsonObject option(HttpServerRequest request, JsonObject data) {
		JsonObject option = new JsonObject();
		String method = data.getString("_method");
		ServerUtils.removeInvalidField(data);
		if (method.equals("GET")) {
			option = new FetchingObjectOption().option(request, data);
		} else if (method.equals("PUT")) {
			option = new UpdatingObjectOption().option(request, data);
		} else if (method.equals("DELETE")) {
			option = new DeletingObjectOption().option(request, data);
		} else {
			// bad request
		}
		return option;
	}
}

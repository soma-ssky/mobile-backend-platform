package main.java.me.ssky.objects;

import java.util.Map;

import main.java.me.ssky.util.EventBusOption;
import main.java.me.ssky.util.ServerUtils;

import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

public class PostClassOption extends EventBusOption {
	private String method;
	private String collection;

	@Override
	public String address() {
		return ServerUtils.OBJECT_MANAGER_ADDRESS;
	}

	@Override
	public JsonObject option(HttpServerRequest request, JsonObject data) {
		method = data.getString("_method");
		ServerUtils.removeInvalidField(data);
		JsonObject option = new JsonObject();
		collection = request.path().split("/")[3];
		if (method == null) {
			option.putString("action", "save");
			option.putString("collection", collection);
			option.putObject("documents", data);
		} else if (method.equals("GET")) {
			option = new RetrievingObjectOption().option(request, data);
		} else if (method.equals("PUT")) {
			option = new UpdatingObjectOption().option(request, data);
		} else if (method.equals("DELETE")) {
			option = new DeletingObjectOption().option(request, data);
		}
		return option;
	}

	@Override
	public Map<String, String> headers(JsonObject result) {
		if (method == null) {
			String location = "/" + ServerUtils.VERSION + "/" + collection + "/" + result.getString("objectId");
			return ServerUtils.responseHeadersInCreated(statusCodeInSuccess(), result.encode().length(), location);
		} else {
			return ServerUtils.responseHeaders(statusCodeInSuccess());
		}
	}

	@Override
	public int statusCodeInSuccess() {
		return (method == null ? 201 : 200);
	}
}
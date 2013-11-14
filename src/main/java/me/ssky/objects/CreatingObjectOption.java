package main.java.me.ssky.objects;

import java.util.Map;

import main.java.me.ssky.util.EventBusOption;
import main.java.me.ssky.util.Util;

import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

public class CreatingObjectOption extends EventBusOption {
	private String method;
	private String collection;

	@Override
	public String address() {
		return Util.OBJECT_MANAGER_ADDRESS;
	}

	@Override
	public JsonObject option(HttpServerRequest request, JsonObject data) {
		method = data.getString("_method");
		Util.removeInvalidField(data);

		JsonObject option = new JsonObject();
		collection = request.path().split("/")[3];
		if (method == null) {
			option.putString("action", "save");
			option.putString("collection", collection);
			option.putObject("documents", data);
		} else if (method.equals("GET")) {
			option = new RetrievingObjectOption().option(request, data);
		} else {
			// bad request
		}
		return option;
	}

	@Override
	public Map<String, String> headers(JsonObject result) {
		if (method == null) {
			String location = "/" + Util.VERSION + "/" + collection + "/" + result.getString("objectId");
			return Util.getCreatedResponseHeaders(statusCodeInSuccess(), result.encode().length(), location);
		} else {
			return Util.getResponseHeaders(statusCodeInSuccess(), result.encode().length());
		}
	}

	@Override
	public int statusCodeInSuccess() {
		return (method == null ? 201 : 200);
	}
}
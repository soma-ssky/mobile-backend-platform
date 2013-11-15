package main.java.me.ssky.users;

import main.java.me.ssky.util.EventBusOption;
import main.java.me.ssky.util.ServerUtils;

import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

public class LoggingInUserOption extends EventBusOption {

	@Override
	public String address() {
		return ServerUtils.AUTH_MANAGER_ADDRESS;
	}

	@Override
	public JsonObject option(HttpServerRequest request, JsonObject data) {
		String _method = data.getString("_method");
		ServerUtils.removeInvalidField(data);

		JsonObject option = new JsonObject();
		option.putString("action", "login");
		if (request.method().equals("GET")) {
			option.putObject("document", ServerUtils.convertParamsToJsonObject(request.params()));
		} else if (_method.equals("GET")) {
			option.putObject("document", data);
		}
		return option;
	}

}

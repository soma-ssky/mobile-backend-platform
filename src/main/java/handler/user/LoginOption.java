package main.java.handler.user;

import main.java.handler.RequestDataOption;
import main.java.util.Util;

import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

public class LoginOption extends RequestDataOption {

	@Override
	protected JsonObject getOption(HttpServerRequest request, Buffer buffer) {
		JsonObject option = new JsonObject();
		option.putString("action", "login");

		JsonObject document = Util.convertParamsToJsonObject(request.params());

		option.putObject("document", document);
		return option;
	}

	@Override
	protected String getAddress() {
		return Util.AUTH_MANAGER_ADDRESS;
	}

}

/*
 * curl -X GET -G --data-urlencode 'username=cooldude6' --data-urlencode 'password=p_n7!-e8' http://localhost/1/login
 */
package main.java.handler.user;

import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

import main.java.handler.RequestDataOption;
import main.java.util.Util;

public class SigningupOption extends RequestDataOption {

	@Override
	protected JsonObject getOption(HttpServerRequest request, Buffer buffer) {
		JsonObject option = new JsonObject();
		option.putString("action", "signup");
		option.putObject("document", new JsonObject(buffer.toString()));
		return option;
	}

	@Override
	protected String getAddress() {
		return Util.AUTH_MANAGER_ADDRESS;
	}

}
/*
curl -X POST \
-H "Content-Type: application/json" \
-d '{"username":"cooldude6","password":"p_n7!-e8","phone":"415-392-0202"}' \
http://localhost/1/users

*/
package main.java.handler.object;

import main.java.handler.RequestDataOption;
import main.java.util.Util;

import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

public class CreatingObjectAndClientClassOption extends RequestDataOption {
	@Override
	protected JsonObject getOption(HttpServerRequest request, Buffer buffer) {
		JsonObject data = new JsonObject(buffer.toString());
		String method = data.getString("_method");

		JsonObject option = new JsonObject();
		String collection = request.path().split("/")[3];
		option.putString("collection", collection);

		if (method == null) { // save object
			option.putString("action", "save");
			option.putObject("documents", data);
		} else if(method.equals("GET")){ // query object

		} else{
			// invalid request.
		}

		return option;
	}

	@Override
	protected String getAddress() {
		return Util.OBJECT_MANAGER_ADDRESS;
	}

}

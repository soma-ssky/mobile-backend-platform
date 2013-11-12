package main.java.handler.user;

import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

import main.java.handler.RequestDataOption;
import main.java.util.Util;

public class UserRetrieveOption extends RequestDataOption {

	@Override
	protected JsonObject getOption(HttpServerRequest request, Buffer buffer) {
		JsonObject option = new JsonObject();
		String objectId = request.path().split("/")[3];
		option.putString("action", "retrieve");
		option.putObject("document", new JsonObject().putString("objectId", objectId));
		return option;
	}

	@Override
	protected String getAddress() {
		return Util.AUTH_MANAGER_ADDRESS;
	}

}

/*
 * 
 * curl -X GET  -H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" -H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" http://localhost/1/users/402459c8-d04d-4649-9a49-68a9ea03e1a1
 */
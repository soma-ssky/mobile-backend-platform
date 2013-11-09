package main.java.user;

import main.java.server.ServerMain;
import main.java.util.Util;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

public class LoginHandler implements Handler<HttpServerRequest> {
	@Override
	public void handle(final HttpServerRequest request) {
		request.bodyHandler(new Handler<Buffer>() {
			@Override
			public void handle(Buffer buffer) {
				JsonObject data = new JsonObject(buffer.toString());
				String username = data.getString("username");
				String password = data.getString("password");
				JsonObject option = new JsonObject().putObject("document", new JsonObject().putString("username", username).putString("password", password));
				option.putString("action", "login");
				ServerMain.eb.send(Util.AUTH_MANAGER_ADDRESS, option, new Handler<Message<JsonObject>>() {
					public void handle(Message<JsonObject> result) {
						Util.response(request, result.body().encode(), 200);
					}
				});
			}
		});
	}
}

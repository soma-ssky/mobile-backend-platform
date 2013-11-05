package expired.object.request;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;


public class AuthPostHandler implements Handler<Buffer> {
	private EventBus eb;
	private HttpServerRequest request;

	public void handle(EventBus eb, HttpServerRequest request, Buffer buffer) {
		this.eb = eb;
		this.request = request;
		handle(buffer);
	}

	public void handle(Buffer buffer) {
		JsonObject jsonData = new JsonObject(buffer.toString());
		switch (request.path()) {
		case "/login":
			login(request, jsonData);
			break;
		case "/logout":
			logout(request, jsonData);
			break;
		case "/authorise":
			authorise(request, jsonData);
			break;
		}
	}

	private void login(final HttpServerRequest request, JsonObject jsonData) {
		eb.send("vertx.auth", jsonData.putString("action", "login"), new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> message) {
				replyJson(request, message.body());
			}
		});
	}

	private void logout(final HttpServerRequest request, JsonObject jsonData) {
		eb.send("vertx.auth", jsonData.putString("action", "logout"), new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> message) {
				replyJson(request, message.body());
			}
		});
	}

	private void authorise(final HttpServerRequest request, JsonObject jsonData) {
		eb.send("vertx.auth", jsonData.putString("action", "authorise"), new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> message) {
				replyJson(request, message.body());
			}
		});
	}

	private void replyJson(HttpServerRequest request, JsonObject jsonMsg) {
		request.response().putHeader("Content-Type", "application/json");
		request.response().putHeader("Content-length", jsonMsg.toString().length() + "");
		request.response().end(jsonMsg.toString());
	}
}
package main.java.me.ssky.server;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

public class FriendRelationManager extends Verticle implements Handler<Message<JsonObject>> {
	@Override
	public void start() {
		JsonObject config = container.config();
		String thisAddress = config.getString("thisAddress");
		vertx.eventBus().registerHandler(thisAddress, this);
	}

	@Override
	public void handle(Message<JsonObject> message) {
		
	}
}

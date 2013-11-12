package main.java.handler;

import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;

public class RouteMatchManager {
	public static RouteMatchManager instance = new RouteMatchManager();
	private static RouteMatcher routeMatcher = new RouteMatcher();

	private RouteMatchManager() {}

	public static RouteMatchManager getInstance() {
		return instance;
	}

	public void addPost(String pattern, RequestDataOption dataProcessor) {
		routeMatcher.post(pattern, new EventBusHandler(dataProcessor));
	}

	public void addPut(String pattern, RequestDataOption dataProcessor) {
		routeMatcher.put(pattern, new EventBusHandler(dataProcessor));
	}

	public void addGet(String pattern, RequestDataOption dataProcessor) {
		routeMatcher.get(pattern, new EventBusHandler(dataProcessor));
	}

	public void addDelete(String pattern, RequestDataOption dataProcessor) {
		routeMatcher.delete(pattern, new EventBusHandler(dataProcessor));
	}

	public void addNoMatch(Handler<HttpServerRequest> handler) {
		routeMatcher.noMatch(handler);
	}

	public void addPostHandler(String pattern, Handler<HttpServerRequest> handler) {
		routeMatcher.post(pattern, handler);
	}

	public RouteMatcher getRouteMatcher() {
		return routeMatcher;
	}
}

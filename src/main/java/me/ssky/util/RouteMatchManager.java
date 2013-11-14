package main.java.me.ssky.util;

import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;

public class RouteMatchManager {
	private RouteMatcher routeMatcher = new RouteMatcher();

	public void addPostToEB(String pattern, EventBusOption option) {
		routeMatcher.post(pattern, new EventBusHandler(option));
	}

	public void addGetToEB(String pattern, EventBusOption option) {
		routeMatcher.get(pattern, new EventBusHandler(option));
	}

	public void addPutToEB(String pattern, EventBusOption option) {
		routeMatcher.put(pattern, new EventBusHandler(option));
	}

	public void addDeleteToEB(String pattern, EventBusOption option) {
		routeMatcher.delete(pattern, new EventBusHandler(option));
	}

	public void addPostHandler(String pattern, Handler<HttpServerRequest> handler) {
		routeMatcher.post(pattern, handler);
	}

	public void addNoMatchHandler(Handler<HttpServerRequest> handler) {
		routeMatcher.noMatch(handler);
	}

	public RouteMatcher getRouteMatcher() {
		return routeMatcher;
	}
}

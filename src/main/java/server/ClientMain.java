package main.java.server;

import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.platform.Verticle;

public class ClientMain extends Verticle {
	@Override
	public void start() {
		HttpClient client = vertx.createHttpClient().setHost("localhost").setPort(8080).setKeepAlive(true);
		
		client.request("POST", "/", new Handler<HttpClientResponse>() {
			@Override
			public void handle(HttpClientResponse response) {
				System.out.println(response.statusCode());
			}
		}).end();
	}
}

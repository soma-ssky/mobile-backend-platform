package main.java.role;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;

public class RoleRetrieveHandler implements Handler<HttpServerRequest> {
	@Override
	public void handle(HttpServerRequest request) {
		request.bodyHandler(new Handler<Buffer>() {
			@Override
			public void handle(Buffer buffer) {
				// TODO Auto-generated method stub

			}
		});
	}
}

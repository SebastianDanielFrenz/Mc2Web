package io.github.SebastianDanielFrenz.Mc2Web;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class StyleHandler implements HttpHandler {

	@Override
	public void handle(HttpExchange he) throws IOException {

		byte[] response = Files.readAllBytes(Paths.get("style.css"));

		he.sendResponseHeaders(200, response.length);
		OutputStream os = he.getResponseBody();
		os.write(response);
		os.close();
	}

}

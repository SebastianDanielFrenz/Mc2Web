package io.github.SebastianDanielFrenz.Mc2Web;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import io.github.SebastianDanielFrenz.Mc2Web.cookie.Cookie;
import io.github.SebastianDanielFrenz.Mc2Web.cookie.CookieStorage;

public class RootHandler implements HttpHandler {

	public static boolean Test(String file) {
		return (Mc2Web.plugin.getConfig().getBoolean(Mc2Web.cSECURITY_BLOCK_FOLDER_UP) && file.contains("//"))
				? Files.isRegularFile(Paths.get(file)) : Files.isRegularFile(Paths.get("plugins/Mc2Web/web/" + file));
	}

	public static void defaultHandler(String url, OutputStream os, HttpExchange he, Map<String, Object> url_encoded) {
		try {
			byte[] response = Files.readAllBytes(Paths
					.get((Mc2Web.plugin.getConfig().getBoolean(Mc2Web.cSECURITY_BLOCK_FOLDER_UP) && url.contains("//"))
							? url : "plugins/Mc2Web/web/" + url));
			he.sendResponseHeaders(200, response.length);
			os.write(response);

		} catch (Exception e) {
			String response;

			try {
				if (Test("404")) {
					response = Utils.processFile("404", url, null, url_encoded);
				} else if (Test("404.html")) {
					response = Utils.processFile("404.html", url, null, url_encoded);
				} else {
					response = "<html><head><title>ERROR</title></head><body><h1>The 404 page does not exist!"
							+ " Please notify the server owner that the plugin is broken.</h1><br><br>"
							+ "<h3>Failed while attempting to get URL: " + url + "</body></html>";
				}
				he.sendResponseHeaders(200, response.length());
				os.write(response.getBytes());
			} catch (IOException e1) {
				e.printStackTrace();
			}
		}
	}

	public static final String CT = "Content-Type";

	@Override
	public void handle(HttpExchange he) throws IOException {

		try {
			String url = he.getRequestURI().toString().substring(1);

			Map<String, Object> url_encoded = new HashMap<String, Object>();

			if (url.contains("?")) {
				System.out.println("Detected url with ?");
				System.out.print(url);
				String[] parts = url.split("\\?");
				url = parts[0];
				System.out.println(" -> " + url);

				Utils.parseQuery(parts[1], url_encoded);
			}

			OutputStream os = he.getResponseBody();

			Cookie cookie;
			List<String> cookieArgs = he.getRequestHeaders().getOrDefault("Cookie", new ArrayList<String>());
			for (String arg : cookieArgs) {
				System.out.println("cookieArg: " + arg);
			}

			String cookieID;

			if (cookieArgs.size() > 0) {
				cookieID = cookieArgs.get(0).substring(3);
				cookie = CookieStorage.getCookie(cookieID);
			} else {
				cookieID = CookieStorage.generateCookieID();
				cookie = new Cookie("NULL");
				he.getResponseHeaders().set("Set-Cookie", "ID=" + String.valueOf(cookieID));
			}

			if (url.equals("cookie")) {
				String response = "";
				response += "cookie: ";

				response += cookieID;
				response += "<br>user: ";
				response += cookie.user;

				he.sendResponseHeaders(200, response.length());

				os.write(response.getBytes());
			}

			int login_status;
			String logged_in_user;

			while (true) {
				if (cookie == null) {
					logged_in_user = null;
				} else {
					logged_in_user = cookie.user;
				}

				if (url.contains("//") && Mc2Web.plugin.getConfig().getBoolean(Mc2Web.cSECURITY_BLOCK_FOLDER_UP)) {
					String response;
					if (Test("blocked/folder_up")) {
						response = Utils.processFile("blocked/folder_up", url, logged_in_user, url_encoded);
					} else if (Test("blocked/folder_up.html")) {
						response = Utils.processFile("blocked/folder_up.html", url, logged_in_user, url_encoded);
					} else {
						response = "<html><head><title>Mc2Web - Blocked</title></head><body><h1><b>Haha</b></h1><br><h2>No.</h2>"
								+ "</body></html>";
					}
					he.sendResponseHeaders(200, response.length());
					os.write(response.getBytes());
				} else {
					if (!url.contains(".") || url.endsWith(".html")) {
						String response;

						if (url.equals("")) {
							url = "index";
						}

						if (Test(url)) {
							response = Utils.processFile(url, url, logged_in_user, url_encoded);
						} else if (Test(url + ".html")) {
							response = Utils.processFile(url + ".html", url, logged_in_user, url_encoded);
						} else if (url.equals(Mc2Web.plugin.getConfig().getString(Mc2Web.cURL_LOGIN_CHECK))) {
							Map<String, Object> map = new HashMap<String, Object>();
							Utils.parseQuery(he.getRequestBody().toString(), map);

							String user = (String) map.get("user");
							String password = (String) map.get("password");

							if (user == null || password == null) {
								url = Mc2Web.plugin.getConfig().getString(Mc2Web.cURL_LOGIN_FAILED);
								continue;
							} else {
								login_status = CookieStorage.loginUser(user, password);

								if (login_status != CookieStorage.SUCCESS) {
									url = Mc2Web.plugin.getConfig().getString(Mc2Web.cURL_LOGIN_FAILED);
									continue;
								}

								CookieStorage.updateCookieUser(cookieID, user);
								url = "";
								continue;
							}

						} else {
							if (Test("404")) {
								response = Utils.processFile("404", url, logged_in_user, url_encoded);
							} else if (Test("404.html")) {
								response = Utils.processFile("404.html", url, logged_in_user, url_encoded);
							} else {
								response = "<html><head><title>ERROR</title></head><body><h1>The 404 page does not exist!"
										+ " Please notify the server owner that the plugin is broken.</h1><br><br>"
										+ "<h3>Failed while attempting to get URL: " + url + "</body></html>";
							}
						}
						he.sendResponseHeaders(200, response.length());
						os.write(response.getBytes());
					} else if (url.endsWith(".js")) {
						he.getResponseHeaders().set(CT, "application/javascript");
						defaultHandler(url, os, he, url_encoded);
					} else if (url.endsWith(".png")) {
						he.getResponseHeaders().set(CT, "image/png");
						defaultHandler(url, os, he, url_encoded);
					} else if (url.endsWith(".7z")) {
						he.getResponseHeaders().set(CT, "application/x-7z-compressed");
						defaultHandler(url, os, he, url_encoded);
					} else if (url.endsWith(".pdf")) {
						he.getResponseHeaders().set(CT, "application/pdf");
						defaultHandler(url, os, he, url_encoded);
					} else if (url.endsWith(".aac")) {
						he.getResponseHeaders().set(CT, "audio/x-aac");
						defaultHandler(url, os, he, url_encoded);
					} else if (url.endsWith(".apk")) {
						he.getResponseHeaders().set(CT, "application/vnd.android.package-archive");
						defaultHandler(url, os, he, url_encoded);
					} else if (url.endsWith(".s")) {
						he.getResponseHeaders().set(CT, "text/x-asm");
						defaultHandler(url, os, he, url_encoded);
					} else if (url.endsWith(".csv")) {
						he.getResponseHeaders().set(CT, "text/csv");
						defaultHandler(url, os, he, url_encoded);
					} else {
						defaultHandler(url, os, he, url_encoded);
					}
				}

				cookie = CookieStorage.getCookie(cookieID);
				break;
			}

			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}

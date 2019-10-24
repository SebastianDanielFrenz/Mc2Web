package io.github.SebastianDanielFrenz.Mc2Web;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.craftbukkit.libs.org.apache.commons.io.IOUtils;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.exception.ExceptionUtils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import io.github.SebastianDanielFrenz.Mc2Web.cookie.Cookie;
import io.github.SebastianDanielFrenz.Mc2Web.cookie.CookieStorage;

public class RootHandler implements HttpHandler {

	public static boolean Test(String file) {
		return (Mc2Web.plugin.getConfig().getBoolean(Mc2Web.cSECURITY_BLOCK_FOLDER_UP) && file.contains("//"))
				? Files.isRegularFile(Paths.get(file)) : Files.isRegularFile(Paths.get("plugins/Mc2Web/web/" + file));
	}

	public static void defaultHandler(String url, OutputStream os, HttpExchange he, Map<String, String> url_encoded) {
		try {
			byte[] response = Files.readAllBytes(Paths
					.get((Mc2Web.plugin.getConfig().getBoolean(Mc2Web.cSECURITY_BLOCK_FOLDER_UP) && url.contains("//"))
							? url : Mc2Web.plugin.getConfig().getString(Mc2Web.cWEB_PATH) + url));
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
							+ "<h3>Failed while attempting to get URL: " + url + "<br><br>Error:<br>"
							+ ExceptionUtils.getStackTrace(e) + "</body></html>";
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
			if (url.endsWith("/")) {
				url = url.substring(0, url.length() - 1);
			}

			Map<String, String> url_encoded = new HashMap<String, String>();
			Map<String, String> body_encoded = new HashMap<String, String>();

			String body = IOUtils.toString(he.getRequestBody(), Mc2Web.encoding);

			if (url.contains("?")) {
				System.out.println("Detected url with ?");
				System.out.print(url);
				url = Utils.parseURL(url, url_encoded);
				System.out.println(" -> " + url);
			}

			Utils.parseQuery(body, body_encoded);

			OutputStream os = he.getResponseBody();

			Cookie cookie;
			List<String> cookieArgs = he.getRequestHeaders().getOrDefault("Cookie", new ArrayList<String>());
			for (String arg : cookieArgs) {
				System.out.println("cookieArg: " + arg);
			}

			String cookieID;

			if (cookieArgs.size() > 0) {
				if (cookieArgs.get(0).startsWith("token=")) {
					cookieID = cookieArgs.get(0).split("[=]")[1];
				} else {
					cookieID = null;
				}
				cookie = CookieStorage.getCookie(cookieID);
				if (cookie == null) {
					cookie = new Cookie("NULL");
				}
				he.getResponseHeaders().set("Set-Cookie", "token=" + String.valueOf(cookieID));
			} else {
				cookieID = null;
				cookie = new Cookie("NULL");

			}

			System.out.println("post processing: cookieID=" + cookieID);

			if (url.equals("cookie")) {
				String response = "";
				response += "cookie: ";

				response += cookieID;
				response += "<br>user: ";
				response += cookie.user;

				he.sendResponseHeaders(200, response.length());

				os.write(response.getBytes());
				os.close();
				return;
			}

			if (url.equals("cmd")) {
				String action = url_encoded.get("action");
				System.out.println("CMD: ACTION: " + action);

				String response = "";
				if (action.equals("rmcookie")) {
					he.getResponseHeaders().remove("Set-Cookie");
					he.getResponseHeaders().add("Set-Cookie", "token=tmp");
					response = "<html><head><title>Mc2Web - CMD</title></head><body>Ok.</body></html>";
				} else if (action.equals("setcookie")) {
					he.getResponseHeaders().remove("Set-Cookie");
					he.getResponseHeaders().set("Set-Cookie", "token=" + url_encoded.get("cookie"));
					response = "<html><head><title>Mc2Web - CMD</title></head><body>Ok.</body></html>";
				} else {
					response = "<html><head><title>Mc2Web - CMD</title></head><body>Command not found!</body></html>";
				}

				he.sendResponseHeaders(200, response.length());
				os.write(response.getBytes());
				os.close();
				return;
			}

			int login_status;
			String logged_in_user;

			while (true) {
				if (cookie == null) {
					logged_in_user = null;
				} else {
					System.out.println("user " + cookie.user + " asked for url: " + url);
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

							System.out.println("request-body: " + body);

							String user = body_encoded.get("username");
							String password = body_encoded.get("password");

							if (user == null || password == null) {
								url = Mc2Web.plugin.getConfig().getString(Mc2Web.cURL_LOGIN_FAILED);
								continue;
							} else {
								login_status = CookieStorage.loginUser(user, password);
								System.out.println("login data: " + user + ";" + password + " -> " + login_status);

								if (login_status != CookieStorage.SUCCESS) {
									url = Mc2Web.plugin.getConfig().getString(Mc2Web.cURL_LOGIN_FAILED);
									continue;
								}

								System.out.println("CookieID before login: " + cookieID);

								if (cookieID == null) {
									cookieID = CookieStorage.generateCookieID();
									cookie = new Cookie(user);
									CookieStorage.addCookie(cookieID, cookie);

								} else {
									if (CookieStorage.getCookie(cookieID) == null) {
										CookieStorage.addCookie(cookieID, new Cookie(user));
									} else {

										CookieStorage.updateCookieUser(cookieID, user);
									}
								}

								cookie.user = user;

								System.out.println("CookieID after login: " + cookieID);

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

						if (cookieID != null) {
							he.getResponseHeaders().remove("Set-Cookie");
							he.getResponseHeaders().set("Set-Cookie", "token=" + String.valueOf(cookieID));
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

				if (cookieID != null) {
					cookie = CookieStorage.getCookie(cookieID);
				}
				break;
			}

			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}

package io.github.SebastianDanielFrenz.Mc2Web;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.bukkit.plugin.java.JavaPlugin;

import com.sun.net.httpserver.HttpServer;

public class Mc2WebPlugin extends JavaPlugin {

	public static final int port = 9000;

	HttpServer server;

	@Override
	public void onEnable() {
		try {
			server = HttpServer.create(new InetSocketAddress(port), 0);

			System.out.println("server started at " + port);

			server.createContext("/", new RootHandler());
			server.createContext("/echoHeader", new EchoHeaderHandler());
			server.createContext("/echoGet", new EchoGetHandler());
			server.createContext("/echoPost", new EchoPostHandler());
			server.setExecutor(null);

			server.start();

		} catch (IOException e) {
			e.printStackTrace();
			getServer().getPluginManager().disablePlugin(this);
		}
	}

	@Override
	public void onDisable() {
		if (server != null) {
			server.stop(0);
		}
	}

}

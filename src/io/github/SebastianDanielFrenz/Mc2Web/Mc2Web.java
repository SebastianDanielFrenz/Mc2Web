package io.github.SebastianDanielFrenz.Mc2Web;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.sun.net.httpserver.HttpServer;

import io.github.SebastianDanielFrenz.Mc2Web.exceptions.WebServerAlreadyRunningException;
import io.github.SebastianDanielFrenz.Mc2Web.exceptions.WebServerNotRunningException;
import net.milkbowl.vault.economy.Economy;

public class Mc2Web extends JavaPlugin {

	public static HttpServer server;

	public static Mc2Web plugin;
	public static Economy economy;

	public static void startWebServer() throws IOException, WebServerAlreadyRunningException {
		if (server != null) {
			throw new WebServerAlreadyRunningException();
		} else {
			server = HttpServer.create(new InetSocketAddress(plugin.getConfig().getInt(cPORT)), 0);
			server.createContext("/", new RootHandler());
			server.setExecutor(null);
			server.start();
		}

	}

	public static void stopWebServer() throws WebServerNotRunningException {
		if (server == null) {
			throw new WebServerNotRunningException();
		} else {
			server.stop(0);
			server = null;
		}
	}

	@Override
	public void onEnable() {
		plugin = this;

		loadConfiguration();

		if (!setupEconomey()) {
			Bukkit.shutdown();
		}

		getCommand("mc2web").setExecutor(new Mc2WebCommandExecutor());

		try {
			try {
				startWebServer();
			} catch (WebServerAlreadyRunningException e) {
				e.printStackTrace();
			}

			System.out.println("server started at " + getConfig().getInt(cPORT));

		} catch (IOException e) {
			e.printStackTrace();

			getServer().getPluginManager().disablePlugin(this);
		}
	}

	@Override
	public void onDisable() {
		try {
			stopWebServer();
		} catch (WebServerNotRunningException e) {
			e.printStackTrace();
		}
	}

	private boolean setupEconomey() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager()
				.getRegistration(Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}

		return economy != null;
	}

	public void loadConfiguration() {
		getConfig().addDefault(cPORT, 8124);
		getConfig().addDefault(cIP, "127.0.0.1");

		getConfig().addDefault(cSECURITY_BLOCK_FOLDER_UP, true);

		getConfig().addDefault(cDYNMAP_PORT, 8123);
		getConfig().addDefault(cDYNMAP_ENABLED, true);

		getConfig().addDefault(cPERMISSIONSEX_ENABLED, true);

		getConfig().options().copyDefaults(true);

		saveConfig();
	}

	public static final String cPORT = "port";
	public static final String cIP = "ip";

	public static final String cSECURITY_BLOCK_FOLDER_UP = "security.block.folder_up";

	public static final String cDYNMAP_PORT = "dynmap.port";
	public static final String cDYNMAP_ENABLED = "dynmap.enabled";

	public static final String cPERMISSIONSEX_ENABLED = "permissionsex.enabled";

}

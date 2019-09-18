package io.github.SebastianDanielFrenz.Mc2Web;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.sun.net.httpserver.HttpServer;

import net.milkbowl.vault.economy.Economy;

public class Mc2Web extends JavaPlugin {

	HttpServer server;

	public static Mc2Web plugin;
	public static Economy economy;

	@Override
	public void onEnable() {
		plugin = this;

		loadConfiguration();
		
		if (!setupEconomey()) {
			Bukkit.shutdown();
		}

		try {
			server = HttpServer.create(new InetSocketAddress(getConfig().getInt(cPORT)), 0);

			System.out.println("server started at " + getConfig().getInt(cPORT));

			server.createContext("/", new RootHandler());

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

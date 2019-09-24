package io.github.SebastianDanielFrenz.Mc2Web;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.sun.net.httpserver.HttpServer;
import com.youtube.crash_games_cr_mc.simpleDB.DataBase;
import com.youtube.crash_games_cr_mc.simpleDB.DataBaseHandler;
import com.youtube.crash_games_cr_mc.simpleDB.Table;
import com.youtube.crash_games_cr_mc.simpleDB.expandable.FullValueManager;
import com.youtube.crash_games_cr_mc.simpleDB.query.Comparor;
import com.youtube.crash_games_cr_mc.simpleDB.query.DataBaseQuery;
import com.youtube.crash_games_cr_mc.simpleDB.query.DefaultDataBaseQuery;
import com.youtube.crash_games_cr_mc.simpleDB.query.QueryResult;
import com.youtube.crash_games_cr_mc.simpleDB.query.SearchedValue;
import com.youtube.crash_games_cr_mc.simpleDB.varTypes.DBString;

import io.github.SebastianDanielFrenz.Mc2Web.exceptions.WebServerAlreadyRunningException;
import io.github.SebastianDanielFrenz.Mc2Web.exceptions.WebServerNotRunningException;
import net.milkbowl.vault.economy.Economy;

public class Mc2Web extends JavaPlugin {

	public static HttpServer server;

	public static Mc2Web plugin;
	public static Economy economy;

	public static DataBaseHandler dbh;
	public static DataBaseQuery query;
	public static Comparor comparor;

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

		loadDBs();

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

		saveDBs();
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

		getConfig().addDefault(cWEB_PATH, "plugins/Mc2Web/web/");
		getConfig().addDefault(cDATABASE_PATH, "plugins/Mc2Web/db/");

		getConfig().addDefault(cSECURITY_BLOCK_FOLDER_UP, true);

		getConfig().addDefault(cDYNMAP_PORT, 8123);
		getConfig().addDefault(cDYNMAP_ENABLED, true);

		getConfig().addDefault(cPERMISSIONSEX_ENABLED, true);

		getConfig().options().copyDefaults(true);

		saveConfig();
	}

	public void loadDBs() {
		dbh = new DataBaseHandler(new FullValueManager());
		try {
			dbh.addDataBase(cDATABASE_PATH + "accounts.db");
		} catch (IOException e1) {
			e1.printStackTrace();
			dbh.createDataBase("accounts");
			DataBase accounts = dbh.getDataBase("accounts");
			Table users = new Table(dbh.getValueManager());
			users.addColumn("user");
			users.addColumn("password");
			users.addColumn("UUID");

			accounts.addTable("users", users);
			try {
				dbh.saveDataBase("accounts", cDATABASE_PATH + "accounts.db");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		query = new DefaultDataBaseQuery(dbh);
	}

	public void saveDBs() {
		for (String db : dbh.getDBnames()) {
			try {
				dbh.saveDataBase(db, cDATABASE_PATH + "accounts.db");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	public static final String cPORT = "port";
	public static final String cIP = "ip";

	public static final String cWEB_PATH = "web_path";
	public static final String cDATABASE_PATH = "database_path";

	public static final String cSECURITY_BLOCK_FOLDER_UP = "security.block.folder_up";

	public static final String cDYNMAP_PORT = "dynmap.port";
	public static final String cDYNMAP_ENABLED = "dynmap.enabled";

	public static final String cPERMISSIONSEX_ENABLED = "permissionsex.enabled";

	public static final String[] M_GETPASSWORD_COLUMNS = new String[] { "password" };
	public static final String[] M_DOESUSEREXIST_COLUMNS = new String[] {};
	public static final String[] M_UPDATEPLAYERUUID_COLUMNS = new String[] { "UUID", "user" };

	public static boolean doesUserExist(String user) {
		return query.Run("accounts", "users", M_DOESUSEREXIST_COLUMNS,
				new SearchedValue[] { new SearchedValue("user", new DBString(user)) }).rows.size() > 0;
	}

	public static boolean doesUserExist(UUID uuid) {
		return query.Run("accounts", "users", M_DOESUSEREXIST_COLUMNS,
				new SearchedValue[] { new SearchedValue("uuid", new DBString(uuid.toString())) }).rows.size() > 0;
	}

	public static String getPassword(String user) {
		return ((DBString) query.Run("accounts", "users", M_GETPASSWORD_COLUMNS,
				new SearchedValue[] { new SearchedValue("user", new DBString(user)) }).rows.get(0).get(0)).getValue();
	}

	public static void registerUser(String user, String password, UUID uuid) {
		query.Insert("accounts", "users",
				new SearchedValue[] { new SearchedValue("user", new DBString(user)),
						new SearchedValue("password", new DBString(password)),
						new SearchedValue("uuid", new DBString(uuid.toString())) });
	}

	public static void updatePlayerUUID(Player player) {
		QueryResult result = query.Run("accounts", "users", M_UPDATEPLAYERUUID_COLUMNS,
				new SearchedValue[] { new SearchedValue("uuid", new DBString(player.getUniqueId().toString())) });
		if (result.rows.size() > 0) {
			String user = ((DBString) result.rows.get(0).get(1)).getValue();

			if (!player.getName().equals(user)) {
				query.Update("accounts", "users",
						new SearchedValue[] {
								new SearchedValue("uuid", new DBString(player.getUniqueId().toString())) },
						new SearchedValue[] { new SearchedValue("user", new DBString(player.getName())) });
			}

		}
	}

}

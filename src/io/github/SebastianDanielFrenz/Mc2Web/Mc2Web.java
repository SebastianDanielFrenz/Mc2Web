package io.github.SebastianDanielFrenz.Mc2Web;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
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

import io.github.SebastianDanielFrenz.Mc2Web.autosave.AutoSaveDBThread;
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

		try {
			Files.createDirectories(Paths.get(getConfig().getString(cDATABASE_PATH)));
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		loadDBs();

		if (getConfig().getBoolean(cAUTOSAVE_ENABLED)) {
			new Thread(new AutoSaveDBThread()).start();
		}

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

		getConfig().addDefault(cDEBUG, false);

		getConfig().addDefault(cAUTOSAVE_ENABLED, true);
		getConfig().addDefault(cAUTOSAVE_FREQUENCY, 1.0);

		getConfig().addDefault(cSECURITY_BLOCK_FOLDER_UP, true);

		getConfig().addDefault(cDYNMAP_PORT, 8123);
		getConfig().addDefault(cDYNMAP_ENABLED, true);

		getConfig().addDefault(cPERMISSIONSEX_ENABLED, true);

		getConfig().options().copyDefaults(true);

		saveConfig();
	}

	public static void loadDBs() {
		dbh = new DataBaseHandler(new FullValueManager());
		try {
			dbh.addDataBase(Mc2Web.plugin.getConfig().getString(cDATABASE_PATH) + "accounts.db");
		} catch (IOException | ArrayIndexOutOfBoundsException e1) {
			dbh.createDataBase("accounts");

			DataBase accounts = dbh.getDataBase("accounts");
			accounts.createTable("users");

			Table users = accounts.getTable("users");

			users.addColumn("user");
			users.addColumn("password");
			users.addColumn("UUID");

			accounts.addTable("users", users);
			try {
				dbh.saveDataBase("accounts", Mc2Web.plugin.getConfig().getString(cDATABASE_PATH) + "accounts.db");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		query = new DefaultDataBaseQuery(dbh);
	}

	public static void saveDBs() {
		for (String db : dbh.getDBnames()) {
			try {
				dbh.saveDataBase(db, Mc2Web.plugin.getConfig().getString(cDATABASE_PATH) + "accounts.db");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	public static final String cPORT = "port";
	public static final String cIP = "ip";

	public static final String cWEB_PATH = "web_path";
	public static final String cDATABASE_PATH = "database_path";

	public static final String cDEBUG = "debug";

	public static final String cSECURITY_BLOCK_FOLDER_UP = "security.block.folder_up";

	public static final String cAUTOSAVE_ENABLED = "autosave.enabled";
	public static final String cAUTOSAVE_FREQUENCY = "autosave.frequency";

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
						new SearchedValue("UUID", new DBString(uuid.toString())) });
	}

	public static void updatePlayerUUID(Player player) {
		QueryResult result = query.Run("accounts", "users", M_UPDATEPLAYERUUID_COLUMNS,
				new SearchedValue[] { new SearchedValue("UUID", new DBString(player.getUniqueId().toString())) });
		if (result.rows.size() > 0) {
			String user = ((DBString) result.rows.get(0).get(1)).getValue();

			if (!player.getName().equals(user)) {
				query.Update("accounts", "users",
						new SearchedValue[] {
								new SearchedValue("UUID", new DBString(player.getUniqueId().toString())) },
						new SearchedValue[] { new SearchedValue("user", new DBString(player.getName())) });
			}

		}
	}

}

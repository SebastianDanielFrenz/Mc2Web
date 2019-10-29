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
import io.github.SebastianDanielFrenz.SimpleDBMT.DataBase;
import io.github.SebastianDanielFrenz.SimpleDBMT.DataBaseHandler;
import io.github.SebastianDanielFrenz.SimpleDBMT.Table;
import io.github.SebastianDanielFrenz.SimpleDBMT.adapter.AdapterInfo;
import io.github.SebastianDanielFrenz.SimpleDBMT.error.ComparorOperatorNotSupportedException;
import io.github.SebastianDanielFrenz.SimpleDBMT.query.Comparor;
import io.github.SebastianDanielFrenz.SimpleDBMT.query.DataBaseQuery;
import io.github.SebastianDanielFrenz.SimpleDBMT.query.DefaultComparor;
import io.github.SebastianDanielFrenz.SimpleDBMT.query.DefaultDataBaseQuery;
import io.github.SebastianDanielFrenz.SimpleDBMT.query.QueryResult;
import io.github.SebastianDanielFrenz.SimpleDBMT.query.SearchedValue;
import io.github.SebastianDanielFrenz.SimpleDBMT.varTypes.DBString;
import io.github.SebastianDanielFrenz.SimpleDBMT.varTypes.DBVersion;
import io.github.SebastianDanielFrenz.SimpleDBMT.varTypes.Version;
import io.github.SebastianDanielFrenz.Mc2Web.exceptions.WebServerAlreadyRunningException;
import io.github.SebastianDanielFrenz.Mc2Web.exceptions.WebServerNotRunningException;
import io.github.SebastianDanielFrenz.Mc2Web.lang.Lang;
import net.milkbowl.vault.economy.Economy;

public class Mc2Web extends JavaPlugin {

	public static HttpServer server;

	public static Mc2Web plugin;
	public static Economy economy;

	public static DataBaseHandler dbh;
	public static DataBaseQuery query;
	public static Comparor comparor = new DefaultComparor();

	public static Version version_required_simpleDBMT = new Version(new int[] { 2, 0, 0, 0 });
	public static Version version_recommended_simpleDBMT = new Version(new int[] { 2, 0, 0, 0 });

	public static Lang lang;

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

		try {
			Files.createDirectories(Paths.get(getConfig().getString(cWEB_PATH)));
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			Files.createDirectories(Paths.get(getConfig().getString(cLANG_PATH)));
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (!setupEconomey()) {
			getLogger().info("§4Economy not found! Shutting down server.");
			Bukkit.shutdown();
		}
		if (!setupDB()) {
			getLogger().info("§4DataBase not found! Shutting down server.");
			Bukkit.shutdown();
		}
		try {
			if (comparor.Compare(new DBVersion(AdapterInfo.version), Comparor.BIGGER_EQUALS,
					new DBVersion(version_required_simpleDBMT))) {
				if (comparor.Compare(new DBVersion(AdapterInfo.version), Comparor.BIGGER,
						new DBVersion(version_recommended_simpleDBMT))) {
					getLogger().info("§eSimpleDBMT v" + new DBVersion(AdapterInfo.version).Display()
							+ " is not verified for this version. If things break, try stepping down to v"
							+ new DBVersion(version_recommended_simpleDBMT));
				}
			} else {
				getLogger().info("§4SimpleDBMT v" + new DBVersion(AdapterInfo.version).Display()
						+ " is outdated! This is likely to cause crashes.");
			}

		} catch (ComparorOperatorNotSupportedException e1) {
			e1.printStackTrace();
		}

		prepareDBs();

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

		getConfig().addDefault(cLANG, "en_US");

		getConfig().addDefault(cWEB_PATH, "plugins/Mc2Web/web/");
		getConfig().addDefault(cDATABASE_PATH, "");
		getConfig().addDefault(cLANG_PATH, "plugins/Mc2Web/lang/");

		getConfig().addDefault(cURL_LOGIN_CHECK, "login/check");
		getConfig().addDefault(cURL_LOGIN_FAILED, "login/failed");
		getConfig().addDefault(cURL_LOGIN_FORM, "login");

		getConfig().addDefault(cDEBUG, false);

		getConfig().addDefault(cSECURITY_BLOCK_FOLDER_UP, true);

		getConfig().addDefault(cDYNMAP_PORT, 8123);
		getConfig().addDefault(cDYNMAP_ENABLED, true);

		getConfig().addDefault(cPERMISSIONSEX_ENABLED, true);

		getConfig().options().copyDefaults(true);

		saveConfig();
	}

	public boolean setupDB() {
		RegisteredServiceProvider<DataBaseHandler> dataBaseProvider = getServer().getServicesManager()
				.getRegistration(DataBaseHandler.class);
		if (dataBaseProvider != null) {
			dbh = dataBaseProvider.getProvider();
		}

		return dataBaseProvider != null;
	}

	public static void prepareDBs() {
		try {
			dbh.addDataBase("Mc2Web.db");
		} catch (IOException | ArrayIndexOutOfBoundsException e1) {
			dbh.createDataBase("Mc2Web", "Mc2Web.db");

			DataBase mc2web = dbh.getDataBase("Mc2Web");
			mc2web.createTable("users");

			Table users = mc2web.getTable("users");

			users.addColumn("user");
			users.addColumn("password");
			users.addColumn("UUID");

			mc2web.addTable("users", users);

			mc2web.createTable("cookies");

			Table cookies = mc2web.getTable("cookies");

			cookies.addColumn("ID");
			cookies.addColumn("user");
			cookies.addColumn("lastURL");
			try {
				dbh.saveDataBase("Mc2Web", "Mc2Web.db");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		query = new DefaultDataBaseQuery(dbh);
	}

	public static final String cPORT = "port";
	public static final String cIP = "ip";

	public static final String cLANG = "lang";

	public static final String cWEB_PATH = "web_path";
	public static final String cDATABASE_PATH = "database_path";
	public static final String cLANG_PATH = "lang_path";

	public static final String cURL_LOGIN_CHECK = "url.login_check";
	public static final String cURL_LOGIN_FAILED = "url.login_failed";
	public static final String cURL_LOGIN_FORM = "url.login_form";

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

	public static final String lERROR_PERMISSION_DENIED = "error.permission_denied";
	public static final String lERROR_PERMISSIONS_NEEDED = "error.permissions_needed";
	public static final String lERROR_INTERNAL = "error.internal";
	public static final String lERROR_WEB_SERVER_ALREADY_RUNNING = "error.web_server_already_running";
	public static final String lERROR_WEB_SERVER_NOT_RUNNING = "error.web_server_not_running";
	public static final String lERROR_START_FAILED = "error.start_failed";
	public static final String lERROR_COMMAND_NOT_FOUND = "error.command_not_found";
	public static final String lERROR_NOT_A_PLAYER = "error.not_a_player";
	public static final String lERROR_NOT_ENOUGH_ARGUMENTS = "error.not_enough_arguments";
	public static final String lMSG_SERVER_STARTED = "msg.server_started";
	public static final String lMSG_SERVER_STOPPED = "msg.server_stopped";
	public static final String lMSG_REGISTERED = "msg.registered";
	public static final String lWEB_ERROR_FILE_NOT_FOUND_TITLE = "web.error.file_not_found.title";
	public static final String lWEB_ERROR_FILE_NOT_FOUND_HEADING = "web.error.file_not_found.heading";
	public static final String lWEB_MSG_CMD_SETCOOKIE = "web.msg.cmd.setcookie";
	public static final String lWEB_MSG_CMD_RMCOOKIE = "web.msg.cmd.rmcookie";

	public static boolean doesUserExist(String user) {
		return query.Run("Mc2Web", "users", M_DOESUSEREXIST_COLUMNS,
				new SearchedValue[] { new SearchedValue("user", new DBString(user)) }).rows.size() > 0;
	}

	public static boolean doesUserExist(UUID uuid) {
		return query.Run("Mc2Web", "users", M_DOESUSEREXIST_COLUMNS,
				new SearchedValue[] { new SearchedValue("uuid", new DBString(uuid.toString())) }).rows.size() > 0;
	}

	public static String getPassword(String user) {
		return ((DBString) query.Run("Mc2Web", "users", M_GETPASSWORD_COLUMNS,
				new SearchedValue[] { new SearchedValue("user", new DBString(user)) }).rows.get(0).get(0)).getValue();
	}

	public static void registerUser(String user, String password, UUID uuid) {
		query.Insert("Mc2Web", "users",
				new SearchedValue[] { new SearchedValue("user", new DBString(user)),
						new SearchedValue("password", new DBString(password)),
						new SearchedValue("UUID", new DBString(uuid.toString())) });
	}

	public static void updatePlayerUUID(Player player) {
		QueryResult result = query.Run("Mc2Web", "users", M_UPDATEPLAYERUUID_COLUMNS,
				new SearchedValue[] { new SearchedValue("UUID", new DBString(player.getUniqueId().toString())) });
		if (result.rows.size() > 0) {
			String user = ((DBString) result.rows.get(0).get(1)).getValue();

			if (!player.getName().equals(user)) {
				query.Update("Mc2Web", "users",
						new SearchedValue[] {
								new SearchedValue("UUID", new DBString(player.getUniqueId().toString())) },
						new SearchedValue[] { new SearchedValue("user", new DBString(player.getName())) });
			}

		}
	}

	public static final String encoding = "ISO-8859-1";

	public static void copyWebFiles(String dir) {
		String[] files = new String[] { "index.html", "leaderboard.html", "login.html", "style.css", "test.js" };
		try {
			Files.createDirectories(Paths.get(plugin.getConfig().getString(cWEB_PATH)));

			for (String file : files) {
				Utils.exportFile("/res/web/" + file, plugin.getConfig().getString(cWEB_PATH) + file);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void copyLangFiles(String dir) {
		String[] files = new String[] { "de_DE.lang", "en_US.lang" };
		try {
			Files.createDirectories(Paths.get(plugin.getConfig().getString(cLANG_PATH)));

			for (String file : files) {
				Utils.exportFile("/res/lang/" + file, plugin.getConfig().getString(cLANG_PATH) + file);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

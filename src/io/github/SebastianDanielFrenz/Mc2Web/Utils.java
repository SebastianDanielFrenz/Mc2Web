package io.github.SebastianDanielFrenz.Mc2Web;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import ru.tehkode.permissions.bukkit.PermissionsEx;

public class Utils {

	public static String insertVariables(String text, String url, String user, Map<String, String> url_encoded) {
		text = text.replace("{IP}", Mc2Web.plugin.getConfig().getString(Mc2Web.cIP))
				.replace("{PORT}", String.valueOf(Mc2Web.plugin.getConfig().getInt(Mc2Web.cPORT)))
				.replace("{LIVEMAP_PORT}", Mc2Web.plugin.getConfig().getString(Mc2Web.cDYNMAP_PORT));

		while (true) {
			if (text.startsWith("#")) {
				if (text.startsWith("#enable ")) {
					if (text.startsWith("#enable online_players;")) {

						text = insertOnlinePlayers(text);
						text = text.replaceFirst("#enable online_players;", "");
					} else if (text.startsWith("#enable offline_players_with_money;")) {
						text = insertOfflinePlayersWithMoney(text);
						text = text.replaceFirst("#enable offline_players_with_money;", "");
					} else if (text.startsWith("#enable login;")) {
						text = insertLogin(text, url, user);
						text = text.replaceFirst("#enable login;", "");
					}
				} else {
					text = text.replace("#enable ", "");
				}
			} else {
				break;
			}
		}
		return text;
	}

	public static String insertVariables(String text, Map<String, String> url_encoded) {
		return insertVariables(text, null, null, url_encoded);
	}

	@SuppressWarnings("deprecation")
	private static String insertOnlinePlayers(String text) {
		String filler = "<table>";

		boolean use_pex = Bukkit.getPluginManager().isPluginEnabled("PermissionsEx")
				&& Mc2Web.plugin.getConfig().getBoolean(Mc2Web.cPERMISSIONSEX_ENABLED);

		for (Player player : Bukkit.getOnlinePlayers()) {
			filler += "<tr><td><div class=\"online_player_component\">" + "<img src=\"https://crafatar.com/avatars/"
					+ player.getUniqueId().toString() + "?size=100\">"
					+ "</div></td><td><div class=\"online_player_component\"><element id=\"online_player_name\""
					+ player.getName() + "</element> (<element id=\"online_player_groups\"";

			if (use_pex) {
				String[] groups = PermissionsEx.getUser(player).getGroupNames();

				for (int i = 0; i < groups.length - 1; i++) {
					filler += groups[i] + ", ";
				}

				if (groups.length > 0) {
					filler += groups[groups.length - 1];
				}
			} else {
				if (player.isOp()) {
					filler += "<b style=\"color: red\">OP</b>";
				} else {
					filler += "default";
				}
			}
			filler += "</element>)</div></td></tr></table>";
		}

		return text.replace("{ONLINE_PLAYERS}", filler);
	}

	private static String insertOfflinePlayersWithMoney(String text) {

		String filler = "<div class=\"offline_players_with_money_component\"><table>";

		OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();

		int[] sorted = QuickSort.sortPlayersByMoney(offlinePlayers, 0, offlinePlayers.length - 1);

		for (int ssorted : sorted) {
			filler += "<tr><td class=\"offline_players_with_money_name\">" + offlinePlayers[ssorted].getName()
					+ "</td><td class=\"offline_players_with_money_balance\">"
					+ Mc2Web.economy.getBalance(offlinePlayers[ssorted]) + "</td></tr>";
		}

		filler += "</table></div>";

		return text.replace("{OFFLINE_PLAYERS_WITH_MONEY}", filler);
	}

	public static byte[] insertVariables(byte[] text, Map<String, String> url_encoded) {
		return insertVariables(new String(text), url_encoded).getBytes();
	}

	public static String processFile(String filepath, String url, String user, Map<String, String> url_encoded)
			throws IOException {
		return insertVariables(
				new String(Files
						.readAllBytes(Paths.get((Mc2Web.plugin.getConfig().getBoolean(Mc2Web.cSECURITY_BLOCK_FOLDER_UP)
								&& filepath.contains("//")) ? filepath : "plugins/Mc2Web/web/" + filepath))),
				url_encoded);
	}

	public static String insertLogin(String text, String url, String user) {
		String filler = "<form method=\"post\" action=\"" + Mc2Web.plugin.getConfig().getString(Mc2Web.cURL_LOGIN_CHECK)
				+ "\"><table class=\"login_component\"><tr><th>Username:</th><td><input type=\"text\" name=\"username\">"
				+ "<td></tr><tr><th>Password:</th><td><input type=\"password\" name=\"password\"></td></tr>"
				+ "<tr><td></td><td><input type=\"submit\" value=\"Lool\" style=\"float: right\"></td></tr></table></form>";
		return text.replace("{LOGIN}", filler);
	}

	public static void parseQuery(String query, Map<String, String> parameters) {

		if (query != null) {
			String[] parts = query.split("[&]");
			String[] subparts;
			for (String part : parts) {
				subparts = part.split("[=]");
				if (subparts.length < 2) {
					continue;
				}
				parameters.put(subparts[0], subparts[1]);
			}
		}
	}

	public static String parseURL(String url, Map<String, String> parameters) {
		if (url != null) {
			String[] parts = url.split("[?]");
			parseQuery(parts[1], parameters);
			return parts[0];
		}
		return "";
	}

}

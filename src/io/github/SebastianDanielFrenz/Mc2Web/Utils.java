package io.github.SebastianDanielFrenz.Mc2Web;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import ru.tehkode.permissions.bukkit.PermissionsEx;

public class Utils {

	public static String insertVariables(String text) {
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

	public static byte[] insertVariables(byte[] text) {
		return insertVariables(new String(text)).getBytes();
	}

	public static String processFile(String filepath) throws IOException {
		return insertVariables(new String(Files.readAllBytes(Paths
				.get((Mc2Web.plugin.getConfig().getBoolean(Mc2Web.cSECURITY_BLOCK_FOLDER_UP) && filepath.contains("//"))
						? filepath : "plugins/Mc2Web/web/" + filepath))));
	}

	public static void parseQuery(String query, Map<String, Object> parameters) throws UnsupportedEncodingException {

		if (query != null) {
			String pairs[] = query.split("[&]");
			for (String pair : pairs) {
				String param[] = pair.split("[=]");
				String key = null;
				String value = null;
				if (param.length > 0) {
					key = URLDecoder.decode(param[0], System.getProperty("file.encoding"));
				}

				if (param.length > 1) {
					value = URLDecoder.decode(param[1], System.getProperty("file.encoding"));
				}

				if (parameters.containsKey(key)) {
					Object obj = parameters.get(key);
					if (obj instanceof List<?>) {
						@SuppressWarnings("unchecked")
						List<String> values = (List<String>) obj;
						values.add(value);

					} else if (obj instanceof String) {
						List<String> values = new ArrayList<String>();
						values.add((String) obj);
						values.add(value);
						parameters.put(key, values);
					}
				} else {
					parameters.put(key, value);
				}
			}
		}
	}

}

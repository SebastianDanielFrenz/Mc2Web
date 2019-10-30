package io.github.SebastianDanielFrenz.Mc2Web;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import io.github.SebastianDanielFrenz.Mc2Web.exceptions.WebServerAlreadyRunningException;
import io.github.SebastianDanielFrenz.Mc2Web.exceptions.WebServerNotRunningException;
import io.github.SebastianDanielFrenz.Mc2Web.lang.Lang;

public class Mc2WebCommandExecutor implements CommandExecutor {

	public static final String[] permission_help = { "Mc2Web.help" };
	public static final String[] permission_stop = { "Mc2Web.stop" };
	public static final String[] permission_start = { "Mc2Web.start" };
	public static final String[] permission_restart = { "Mc2Web.restart" };
	public static final String[] permission_reload = { "Mc2Web.reload" };
	public static final String[] permission_register = { "Mc2Web.register", "Mc2Web.user" };
	public static final String[] permission_dump = { "Mc2Web.dump" };

	public static final String prefix = "§f[§eMc2Web§f]: §a";

	public static boolean hasPermission(CommandSender sender, String[] perms) {
		for (String perm : perms) {
			if (sender.hasPermission(perm)) {
				return true;
			}
		}

		if (sender.hasPermission("Mc2Web.*")) {
			return true;
		}

		if (sender.isOp()) {
			return true;
		}

		if (sender instanceof ConsoleCommandSender) {
			return true;
		}

		permissionDenied(sender, perms);
		return false;
	}

	public static void permissionDenied(CommandSender sender, String[] perms) {
		sender.sendMessage(prefix + Mc2Web.lang.get(Mc2Web.lERROR_PERMISSION_DENIED) + " "
				+ Mc2Web.lang.get(Mc2Web.lERROR_PERMISSIONS_NEEDED));
		for (String perm : perms) {
			sender.sendMessage(prefix + " - §e" + perm);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			if (hasPermission(sender, permission_help)) {
				sender.sendMessage(prefix + "Displaying help for the command of Mc2Web:");
				sender.sendMessage(prefix + "/mc2web -> displays the help");
				sender.sendMessage(prefix + "/mc2web start -> starts the webserver");
				sender.sendMessage(prefix + "/mc2web stop -> stops the webserver");
				sender.sendMessage(prefix + "/mc2web restart -> restarts the webserver");
				sender.sendMessage(prefix + "/mc2web reload -> reloads the config file");
				sender.sendMessage(prefix
						+ "/mc2web register <password> -> registers a web account your you and sets the password");
				sender.sendMessage(prefix + "/mc2web dump");
			}
		} else {
			if (args[0].equalsIgnoreCase("start")) {
				if (hasPermission(sender, permission_start)) {
					try {
						Mc2Web.startWebServer();
						sender.sendMessage(prefix + Mc2Web.lang.get(Mc2Web.lMSG_SERVER_STARTED));
					} catch (IOException e) {
						e.printStackTrace();

						sender.sendMessage(prefix + Mc2Web.lang.get(Mc2Web.lERROR_INTERNAL));

						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);

						e.printStackTrace(pw);
						sender.sendMessage(sw.toString());

						pw.close();
						try {
							sw.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}

					} catch (WebServerAlreadyRunningException e) {
						sender.sendMessage(Mc2Web.lang.get(Mc2Web.lERROR_WEB_SERVER_ALREADY_RUNNING));
					}
				}
			} else if (args[0].equalsIgnoreCase("stop")) {
				if (hasPermission(sender, permission_stop)) {
					try {
						Mc2Web.stopWebServer();
						sender.sendMessage(prefix + Mc2Web.lang.get(Mc2Web.lMSG_SERVER_STOPPED));
					} catch (WebServerNotRunningException e) {
						sender.sendMessage(Mc2Web.lang.get(Mc2Web.lERROR_WEB_SERVER_NOT_RUNNING));
					}
				}
			} else if (args[0].equalsIgnoreCase("restart")) {
				if (hasPermission(sender, permission_restart)) {
					try {
						Mc2Web.stopWebServer();
						sender.sendMessage(prefix + Mc2Web.lang.get(Mc2Web.lMSG_SERVER_STOPPED));
					} catch (WebServerNotRunningException e) {
						sender.sendMessage(prefix + Mc2Web.lang.get(Mc2Web.lERROR_WEB_SERVER_NOT_RUNNING));
					}
					try {
						Mc2Web.startWebServer();
						sender.sendMessage(prefix + Mc2Web.lang.get(Mc2Web.lMSG_SERVER_STARTED));
					} catch (IOException | WebServerAlreadyRunningException e) {
						e.printStackTrace();
						sender.sendMessage(prefix + Mc2Web.lang.get(Mc2Web.lERROR_INTERNAL) + ": "
								+ Mc2Web.lang.get(Mc2Web.lERROR_START_FAILED));
					}
				}
			} else if (args[0].equalsIgnoreCase("reload")) {
				if (hasPermission(sender, permission_reload)) {
					Mc2Web.plugin.reloadConfig();
					try {
						Mc2Web.lang = new Lang(Mc2Web.plugin.getConfig().getString(Mc2Web.cLANG), "Mc2Web");
					} catch (IOException e1) {
						e1.printStackTrace();

						sender.sendMessage(prefix + "§4Could not load language file \""
								+ Mc2Web.plugin.getConfig().getString(Mc2Web.cLANG) + "\"!");
						sender.sendMessage(prefix + "§4Factory resetting lang files...");

						try {
							Files.createDirectories(Paths.get(Mc2Web.plugin.getConfig().getString(Mc2Web.cLANG_PATH)));
						} catch (IOException e) {
							e.printStackTrace();
						}

						Mc2Web.copyLangFiles(Mc2Web.plugin.getConfig().getString(Mc2Web.cLANG_PATH));
					}

					try {
						Mc2Web.stopWebServer();
						sender.sendMessage(prefix + Mc2Web.lang.get(Mc2Web.lMSG_SERVER_STOPPED));
					} catch (WebServerNotRunningException e) {
						sender.sendMessage(prefix + Mc2Web.lang.get(Mc2Web.lERROR_WEB_SERVER_NOT_RUNNING));
					}
					try {
						Mc2Web.startWebServer();
						sender.sendMessage(prefix + Mc2Web.lang.get(Mc2Web.lMSG_SERVER_STARTED));
					} catch (IOException | WebServerAlreadyRunningException e) {
						e.printStackTrace();
						sender.sendMessage(prefix + Mc2Web.lang.get(Mc2Web.lERROR_INTERNAL) + ": "
								+ Mc2Web.lang.get(Mc2Web.lERROR_START_FAILED));
					}

					sender.sendMessage(prefix + "Config reloaded!");
				}
			} else if (args[0].equalsIgnoreCase("register")) {
				if (hasPermission(sender, permission_register)) {
					if (sender instanceof Player) {
						if (args.length >= 2) {
							Player player = (Player) sender;
							Mc2Web.registerUser(player.getName(), args[1], player.getUniqueId());
							sender.sendMessage(prefix + Mc2Web.lang.get(Mc2Web.lMSG_REGISTERED));
						} else {
							sender.sendMessage(prefix + Mc2Web.lang.get(Mc2Web.lERROR_NOT_ENOUGH_ARGUMENTS));
						}
					} else {
						sender.sendMessage(prefix + Mc2Web.lang.get(Mc2Web.lERROR_NOT_A_PLAYER));
					}
				}
			} else if (args[0].equalsIgnoreCase("dump")) {
				if (hasPermission(sender, permission_dump)) {

					try {
						Mc2Web.dbh.getDataBase("Mc2Web").getTable("users").ToQueryResult()
								.DumpHTMLandFormat("plugins/Mc2Web/web/users.html");

						sender.sendMessage(prefix + "§aDone!");
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				}
			} else if (args[0].equalsIgnoreCase("lang")) {
				sender.sendMessage(Lang.getLanguage((Player) sender));
			} else {
				sender.sendMessage(prefix + Mc2Web.lang.get(Mc2Web.lERROR_COMMAND_NOT_FOUND));
			}
		}

		return true;
	}

}
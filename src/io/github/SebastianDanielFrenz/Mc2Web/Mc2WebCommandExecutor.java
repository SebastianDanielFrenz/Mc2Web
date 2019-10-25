package io.github.SebastianDanielFrenz.Mc2Web;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import io.github.SebastianDanielFrenz.Mc2Web.exceptions.WebServerAlreadyRunningException;
import io.github.SebastianDanielFrenz.Mc2Web.exceptions.WebServerNotRunningException;

public class Mc2WebCommandExecutor implements CommandExecutor {

	public static final String[] permission_help = { "Mc2Web.help" };
	public static final String[] permission_stop = { "Mc2Web.stop" };
	public static final String[] permission_start = { "Mc2Web.start" };
	public static final String[] permission_restart = { "Mc2Web.restart" };
	public static final String[] permission_reload = { "Mc2Web.reload" };
	public static final String[] permission_register = { "Mc2Web.register", "Mc2Web.user" };
	public static final String[] permission_dump = { "Mc2Web.dump" };

	public static final String prefix = "§f[§eMc2Web§f]: §a";
	public static final String error_permission_denied = "§4Permission denied!";
	public static final String error_internal = "§4An internal error occured!";
	public static final String error_web_server_already_running = "§4The web server is already running! "
			+ "If you could start the web server while it was running, you would override the value of the variable"
			+ " holding the Object that is the webserver. This would not cause the old server to be shut down,"
			+ " instead it would cause multiple servers to be up at once. The new server would crash,"
			+ " because the port of the web server is already used. The old server could not be shut down either,"
			+ " because it is not a variable anymore, but an object managing a seperate thread to the server"
			+ " and the new web server. Shutting down the server would not work properly,"
			+ " because it would wait for all threads to close. You would have to force the shuttdown.";
	public static final String error_web_server_not_running = "§4The web server is not running!";
	public static final String error_command_not_found = "§4Command not found!";
	public static final String error_not_a_player = "§4You have to be a player to do this!";
	public static final String error_not_enough_arguments = "§4Not enough arguments!";

	public static final String msg_server_started = "§aServer started!";
	public static final String msg_server_stopped = "§aServer stopped!";
	public static final String msg_registered = "§aRegistered!";

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
		sender.sendMessage(
				prefix + error_permission_denied + " You need one of the following permissions in order to this:");
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
						sender.sendMessage(prefix + msg_server_started);
					} catch (IOException e) {
						e.printStackTrace();

						sender.sendMessage(prefix + error_internal);

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
						sender.sendMessage(error_web_server_already_running);
					}
				}
			} else if (args[0].equalsIgnoreCase("stop")) {
				if (hasPermission(sender, permission_stop)) {
					try {
						Mc2Web.stopWebServer();
						sender.sendMessage(prefix + msg_server_stopped);
					} catch (WebServerNotRunningException e) {
						sender.sendMessage(error_web_server_not_running);
					}
				}
			} else if (args[0].equalsIgnoreCase("restart")) {
				if (hasPermission(sender, permission_restart)) {
					try {
						Mc2Web.stopWebServer();
						sender.sendMessage(prefix + msg_server_stopped);
					} catch (WebServerNotRunningException e) {
						sender.sendMessage(prefix + error_web_server_not_running);
					}
					try {
						Mc2Web.startWebServer();
						sender.sendMessage(prefix + msg_server_started);
					} catch (IOException | WebServerAlreadyRunningException e) {
						e.printStackTrace();
						sender.sendMessage(prefix + error_internal + " The server failed to start!");
					}
				}
			} else if (args[0].equalsIgnoreCase("reload")) {
				if (hasPermission(sender, permission_reload)) {
					Mc2Web.plugin.reloadConfig();
					sender.sendMessage(prefix + "Config reloaded!");
				}
			} else if (args[0].equalsIgnoreCase("register")) {
				if (hasPermission(sender, permission_register)) {
					if (sender instanceof Player) {
						if (args.length >= 2) {
							Player player = (Player) sender;
							Mc2Web.registerUser(player.getName(), args[1], player.getUniqueId());
							sender.sendMessage(prefix + msg_registered);
						} else {
							sender.sendMessage(prefix + error_not_enough_arguments);
						}
					} else {
						sender.sendMessage(prefix + error_not_a_player);
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
			} else {
				sender.sendMessage(prefix + error_command_not_found);
			}
		}

		return true;
	}

}
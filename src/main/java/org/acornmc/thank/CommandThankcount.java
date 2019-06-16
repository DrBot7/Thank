package org.acornmc.thank;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

public class CommandThankcount implements CommandExecutor {
    Plugin plugin = Thank.getPlugin(Thank.class);
    Thank thank = Thank.getPlugin(Thank.class);
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("thank.thankcount")) {
            sender.sendMessage(plugin.getConfig().getString("NoPermissionMessage").replace("&", "§"));
             return true;
        }
        SQLite sqLite = new SQLite(thank);
        String from = "";
        String to = "";
        String thankerPlayer = "";
        String thankeePlayer = "";
        for (String arg : args) {
            if (arg.startsWith("from:")) {
                from = arg.replace("from:", "");
                thankerPlayer = Bukkit.getOfflinePlayer(from).getUniqueId().toString().replace("-", "");
            } else if (arg.startsWith("to:")) {
                to = arg.replace("to:", "");
                thankeePlayer = Bukkit.getOfflinePlayer(to).getUniqueId().toString().replace("-", "");
            }
        }
        String count = String.valueOf(sqLite.Thankcount(thankerPlayer, thankeePlayer));
        if (from.length() > 0 && to.length() > 0) {
            sender.sendMessage(plugin.getConfig().getString("ThankcountFromToMessage")
                    .replace("&", "§")
                    .replace("%COUNT%", count)
                    .replace("%THANKER%", from)
                    .replace("%THANKEE%", to));
        } else if (from.length() > 0) {
            sender.sendMessage(plugin.getConfig().getString("ThankcountFromMessage")
                    .replace("&", "§")
                    .replace("%COUNT%", count)
                    .replace("%THANKER%", from));
        } else if (to.length() > 0) {
            sender.sendMessage(plugin.getConfig().getString("ThankcountToMessage")
                    .replace("&", "§")
                    .replace("%COUNT%", count)
                    .replace("%THANKEE%", to));
        } else {
            return false;
        }
        return true;
    }
}

package org.acornmc.thank;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.Date;

public class CommandThanklast implements CommandExecutor {
    Plugin plugin = Thank.getPlugin(Thank.class);
    Thank thank = Thank.getPlugin(Thank.class);

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            return false;
        }

        if (!sender.hasPermission("thank.thanklast")) {
            sender.sendMessage(plugin.getConfig().getString("NoPermissionMessage").replace("&", "§"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null) {
            sender.sendMessage(plugin.getConfig().getString("ThanklastNonexistingPlayerMessage").replace("&", "§").replace("%TARGET%", args[0]));
            return true;
        }

        SQLite sqLite = new SQLite(thank);
        String targetUuid = target.getUniqueId().toString();
        int thanklast = sqLite.thanklast(targetUuid);
        if (thanklast == 0) {
            sender.sendMessage(plugin.getConfig().getString("ThanklastPlayerNeverThankedMessage").replace("&", "§").replace("%TARGET%", args[0]));
            return true;
        }

        int now = new Date().hashCode();
        String lastThankTime = thank.timeString((now - thanklast)/1000);
        sender.sendMessage(plugin.getConfig().getString("ThanklastSuccessfulMessage").replace("&", "§").replace("%TIME%", lastThankTime).replace("%TARGET%", args[0]));
        return true;
    }
}
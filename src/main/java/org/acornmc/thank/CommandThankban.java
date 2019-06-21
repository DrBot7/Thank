package org.acornmc.thank;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class CommandThankban implements CommandExecutor {
    Plugin plugin = Thank.getPlugin(Thank.class);
    Thank thank = Thank.getPlugin(Thank.class);
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("thank.thankban")) {
            sender.sendMessage(plugin.getConfig().getString("NoPermissionMessage").replace("&", "§"));
            return true;
        }

        if (args.length != 2) {
            return false;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null) {
            sender.sendMessage(plugin.getConfig().getString("ThankbanNonexistingPlayerMessage").replace("&", "§"));
            return true;
        }

        String targetUuid = target.getUniqueId().toString().replace("-", "");
        int minutes = Integer.parseInt(args[1]);
        SQLite sqLite = new SQLite(thank);
        sqLite.addNewThankbanEntry(targetUuid, minutes);
        sender.sendMessage(plugin.getConfig().getString("ThankbanSuccessfulMessage").replace("&", "§").replace("%TARGET%", args[0]).replace("%TIME%", args[1] + "m"));

        return true;
    }
}

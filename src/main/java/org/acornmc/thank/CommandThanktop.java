package org.acornmc.thank;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class CommandThanktop implements CommandExecutor {
    Plugin plugin = Thank.getPlugin(Thank.class);
    Thank thank = Thank.getPlugin(Thank.class);
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("thank.thanktop")) {
            sender.sendMessage(plugin.getConfig().getString("NoPermissionMessage").replace("&", "§"));
            return true;
        }

        if (args.length != 1) {
            return false;
        }


        return true;
    }
}

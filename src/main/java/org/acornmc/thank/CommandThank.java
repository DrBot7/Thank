package org.acornmc.thank;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import java.util.List;

public class CommandThank implements CommandExecutor {

    Plugin plugin = Thank.getPlugin(Thank.class);
    Thank thank = Thank.getPlugin(Thank.class);

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            System.out.println("Only players can use /thank");
            return true;
        }

        if (args.length != 1) {
            return false;
        }

        Player thanker = (Player) sender;
        Player thankee = Bukkit.getPlayer(args[0]);
        if (thankee == null) {
            thanker.sendMessage(plugin.getConfig().getString("OfflinePlayerMessage").replace("&", "§"));
            return true;
        }

        if (thanker.getUniqueId().equals(thankee.getUniqueId())) {
            thanker.sendMessage(plugin.getConfig().getString("CantThankSelfMessage").replace("&", "§"));
            return true;
        }

        SQLite sqLite = new SQLite(thank);
        int cooldownseconds = sqLite.CooldownRemaining(thanker);
        if (cooldownseconds > 0) {
            String cooldownMessage = plugin.getConfig().getString("CooldownMessage").replace("&", "§");
            thanker.sendMessage(cooldownMessage.replace("%TIME%", timeString(cooldownseconds)));
            return true;
        }

        System.out.println(cooldownseconds);

        double mangifier = plugin.getConfig().getDouble("RepeatedThankRatio");
        int exponent = sqLite.Thankcount(thanker, thankee);
        double baseMoney = plugin.getConfig().getDouble("BaseMoney");
        double netMoney = baseMoney * Math.pow(mangifier, exponent);
        Thank.getEconomy().depositPlayer(thankee, netMoney);
        sqLite.addNewEntry(thanker, thankee);
        List<String> thankCommands = plugin.getConfig().getStringList("ThankCommands");
        for (int i = 0; i < thankCommands.size(); i++) {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), thankCommands.get(i)
                    .replace("%THANKER%", thanker.getName())
                    .replace("%THANKEE%", thankee.getName()));
        }
        return true;
    }
    public String timeString(int totalSecs) {
        int hours = totalSecs / 3600;
        int minutes = (totalSecs % 3600) / 60;
        int seconds = totalSecs % 60;
        if (hours > 0) {
            return String.format("%02dh %02dm %02ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%02dm %02ds", minutes, seconds);
        } else {
            return String.format("%02ds", seconds);
        }
    }
}
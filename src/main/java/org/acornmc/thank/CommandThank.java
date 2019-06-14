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

        if (args.length == 0) {
            return false;
        }

        Player thanker = (Player) sender;
        Player thankee = Bukkit.getPlayer(args[0]);

        if (!thanker.hasPermission("thank.thank")) {
            thanker.sendMessage(plugin.getConfig().getString("NoPermissionMessage").replace("&", "§"));
            return true;
        }

        if (thankee == null) {
            thanker.sendMessage(plugin.getConfig().getString("CantThankOfflinePlayerMessage").replace("&", "§"));
            return true;
        }

        if (thanker.getUniqueId().equals(thankee.getUniqueId())) {
            thanker.sendMessage(plugin.getConfig().getString("CantThankSelfMessage").replace("&", "§"));
            return true;
        }

        boolean denyThankingSameIP = plugin.getConfig().getBoolean("DenyThankingSameIP");
        if (denyThankingSameIP) {
            String thankerAddress = thanker.getAddress().toString().split(":")[0];
            if (thankerAddress != null && thankerAddress.equals(thankee.getAddress().toString().split(":")[0])) {
                thanker.sendMessage(plugin.getConfig().getString("CantThankSameIPMessage").replace("&", "§"));
                return true;
            }
        }

        SQLite sqLite = new SQLite(thank);
        boolean denyThankingCooldownPlayers = plugin.getConfig().getBoolean("DenyThankingCooldownPlayers");
        if (denyThankingCooldownPlayers) {
            int thankeeCooldown = sqLite.CooldownRemaining(thankee);
            if (thankeeCooldown > 0) {
                thanker.sendMessage(plugin.getConfig().getString("CantThankCooldownMessage").replace("&", "§"));
                return true;
            }
        }

        boolean denyThank4Thank = plugin.getConfig().getBoolean("DenyThank4Thank");
        if (denyThank4Thank) {
            String thankerUuid = thanker.getUniqueId().toString().replace("-", "");
            String thankeeUuid = thankee.getUniqueId().toString().replace("-", "");
            boolean Thank4Thank = sqLite.Thank4ThankDetected(thankerUuid, thankeeUuid);
            if (Thank4Thank) {
                thanker.sendMessage(plugin.getConfig().getString("CantThank4ThankMessage").replace("&", "§"));
                return true;
            }
        }

        int cooldownseconds = sqLite.CooldownRemaining(thanker);
        if (cooldownseconds > 0) {
            String cooldownMessage = plugin.getConfig().getString("CooldownMessage").replace("&", "§").replace("%TIME%", timeString(cooldownseconds));
            thanker.sendMessage(cooldownMessage);
            return true;
        }

        double magnifier = plugin.getConfig().getDouble("RepeatedThankRatio");
        int exponent = sqLite.Thankcount(thanker, thankee);

        if (magnifier < 0 && exponent > 0) {
            String cantThankSamePlayerMessage = plugin.getConfig().getString("CantThankSamePlayerMessage").replace("&", "§");
            thanker.sendMessage(cantThankSamePlayerMessage);
            return true;
        }

        double baseMoney = plugin.getConfig().getDouble("BaseMoney");
        double netMoney = baseMoney * Math.pow(magnifier, exponent);
        Thank.getEconomy().depositPlayer(thankee, netMoney);
        sqLite.addNewEntry(thanker, thankee);
        List<String> thankCommands = plugin.getConfig().getStringList("ThankCommands");

        String reason = "";
        if (thanker.hasPermission("thank.thank.reason") && args.length >= 3 && args[1].equalsIgnoreCase("for")) {
            reason = " for";
            for (int i = 2; i < args.length; i++) {
                reason += " " + args[i];
            }
        }

        for (int i = 0; i < thankCommands.size(); i++) {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), thankCommands.get(i)
                    .replace("%THANKER%", thanker.getName())
                    .replace("%THANKEE%", thankee.getName())
                    .replace("%REASON%", reason));
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
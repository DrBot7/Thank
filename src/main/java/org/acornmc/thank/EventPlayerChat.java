package org.acornmc.thank;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

public class EventPlayerChat implements Listener {

    Plugin plugin = Thank.getPlugin(Thank.class);
    Thank thank = Thank.getPlugin(Thank.class);

    @EventHandler
    public void EventPlayerChat(AsyncPlayerChatEvent event) {
        String msg = event.getMessage().toLowerCase();
        if (event.getPlayer().hasPermission("thank.thank") && event.getPlayer().hasPermission("thank.thank.remind") && msg.startsWith("ty") || msg.contains(" ty")|| msg.contains("thanks") || msg.contains("thank you")|| msg.contains("thx")) {
            SQLite sqLite = new SQLite(thank);
            int cooldown = sqLite.CooldownRemaining(event.getPlayer().getUniqueId().toString().replace("-", ""));
            if (cooldown == 0) {
                event.getPlayer().sendMessage(plugin.getConfig().getString("RemindMessage").replace("&", "§"));
            }
        }
    }
}

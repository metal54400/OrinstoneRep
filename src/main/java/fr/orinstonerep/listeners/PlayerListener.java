package fr.orinstonerep.listeners;

import fr.orinstonerep.OrinstoneRep;
import fr.orinstonerep.models.PlayerReputation;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final OrinstoneRep plugin;

    public PlayerListener(OrinstoneRep plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getRepManager().getOrLoad(player);
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                boolean got = plugin.getRepManager().tryDailyBonus(player);
                if (got) {
                    int bonus = plugin.getConfig().getInt("score.daily-bonus", 1);
                    String msg = plugin.getRepManager().colorize(
                        plugin.getRepManager().prefix() +
                        plugin.getRepManager().msg("daily-bonus").replace("{amount}", String.valueOf(bonus))
                    );
                    player.sendMessage(msg);
                }
            });
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        plugin.getRepManager().unload(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(AsyncPlayerChatEvent event) {
        PlayerReputation rep = plugin.getRepManager().get(event.getPlayer().getUniqueId());
        if (rep == null) return;
        String color = plugin.getRepManager().getTierColor(rep.getTier());
        String sign = (rep.getScore() >= 0 ? "+" : "") + rep.getScore();
        String tag = plugin.getRepManager().colorize(color + "[" + sign + "] &r");
        event.setFormat(tag + event.getFormat());
    }
}

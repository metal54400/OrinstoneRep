package fr.orinstonerep.managers;

import fr.orinstonerep.OrinstoneRep;
import fr.orinstonerep.models.PlayerReputation;
import fr.orinstonerep.models.ReputationTier;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReputationManager {

    private final OrinstoneRep plugin;
    private final Map<UUID, PlayerReputation> cache = new HashMap<>();

    public ReputationManager(OrinstoneRep plugin) {
        this.plugin = plugin;
    }

    public PlayerReputation getOrLoad(Player player) {
        return cache.computeIfAbsent(player.getUniqueId(),
            uuid -> plugin.getDatabase().load(uuid, player.getName()));
    }

    public PlayerReputation get(UUID uuid) {
        return cache.get(uuid);
    }

    public void unload(UUID uuid) {
        PlayerReputation rep = cache.remove(uuid);
        if (rep != null) plugin.getDatabase().save(rep);
    }

    public void saveAll() {
        cache.values().forEach(plugin.getDatabase()::save);
    }

    public void setScore(PlayerReputation rep, int value, String reason, boolean log) {
        int min = plugin.getConfig().getInt("score.min", -120);
        int max = plugin.getConfig().getInt("score.max", 120);
        int old = rep.getScore();
        rep.setScore(Math.max(min, Math.min(max, value)));
        plugin.getDatabase().save(rep);
        if (log && plugin.getConfig().getBoolean("log-changes", true)) {
            plugin.getLogger().info("[Rép] " + rep.getName() + " : " + old + " → " + rep.getScore()
                + (reason != null ? " | " + reason : ""));
        }
    }

    public void addScore(PlayerReputation rep, int amount, String reason, boolean log) {
        setScore(rep, rep.getScore() + amount, reason, log);
    }

    public boolean tryDailyBonus(Player player) {
        PlayerReputation rep = getOrLoad(player);
        if (rep.hasReceivedDailyBonusToday()) return false;
        int bonus = plugin.getConfig().getInt("score.daily-bonus", 1);
        rep.setLastDailyBonus(System.currentTimeMillis());
        addScore(rep, bonus, "Bonus quotidien", true);
        return true;
    }

    public String getTierDisplay(ReputationTier tier) {
        return colorize(plugin.getConfig().getString("tiers." + tier.getKey() + ".display", tier.getKey()));
    }

    public String getTierColor(ReputationTier tier) {
        return colorize(plugin.getConfig().getString("tiers." + tier.getKey() + ".color", "&7"));
    }

    public String getTierIcon(ReputationTier tier) {
        return colorize(plugin.getConfig().getString("tiers." + tier.getKey() + ".icon", "&7?"));
    }

    public String colorize(String s) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', s);
    }

    public String prefix() {
        return colorize(plugin.getConfig().getString("messages.prefix", "&8[&bOrinstoneRep&8] &r"));
    }

    public String msg(String key) {
        return plugin.getConfig().getString("messages." + key, "");
    }

    public Map<UUID, PlayerReputation> getCache() { return cache; }
}

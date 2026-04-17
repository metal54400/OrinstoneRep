package fr.orinstonerep.listeners;

import fr.orinstonerep.OrinstoneRep;
import fr.orinstonerep.models.PlayerReputation;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class KillListener implements Listener {

    private final OrinstoneRep plugin;

    public KillListener(OrinstoneRep plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        // Vérifier qu'un joueur a tué un autre joueur
        if (killer == null || killer.equals(victim)) return;

        // Vérifier que la fonctionnalité est activée
        if (!plugin.getConfig().getBoolean("repeat-kill.enabled", true)) return;

        int maxKills   = plugin.getConfig().getInt("repeat-kill.max-kills-before-penalty", 2);
        int penalty    = plugin.getConfig().getInt("repeat-kill.penalty", 1);
        long windowMs  = plugin.getConfig().getLong("repeat-kill.window-hours", 24) * 3_600_000L;

        // Enregistrer le kill et récupérer le compteur dans la fenêtre
        int killCount = plugin.getKillTracker().recordKill(killer.getUniqueId(), victim.getUniqueId(), windowMs);

        // Pénalité uniquement à partir du N-ième kill (>= maxKills)
        if (killCount < maxKills) return;

        PlayerReputation rep = plugin.getRepManager().getOrLoad(killer);
        int oldScore = rep.getScore();
        plugin.getRepManager().addScore(rep, -penalty,
            "Kill répété sur " + victim.getName() + " (kill n°" + killCount + ")", true);

        // Message au tueur
        String msg = plugin.getConfig()
            .getString("messages.repeat-kill-penalty", "&cKill répété sur &f{victim} &c: &4-{amount} &créputation.")
            .replace("{victim}", victim.getName())
            .replace("{amount}", String.valueOf(penalty))
            .replace("{count}", String.valueOf(killCount))
            .replace("{score}", String.valueOf(rep.getScore()));

        killer.sendMessage(plugin.getRepManager().colorize(
            plugin.getRepManager().prefix() + msg
        ));
    }
}

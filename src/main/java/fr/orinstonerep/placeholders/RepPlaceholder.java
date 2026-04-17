package fr.orinstonerep.placeholders;

import fr.orinstonerep.OrinstoneRep;
import fr.orinstonerep.models.PlayerReputation;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * %orinstonerep_score%       → score brut
 * %orinstonerep_score_sign%  → +42 ou -10
 * %orinstonerep_tier%        → palier coloré
 * %orinstonerep_tier_raw%    → clé brute (ex: bonne)
 * %orinstonerep_color%       → couleur du palier
 * %orinstonerep_icon%        → icône seule du palier (ex: ★ en vert)
 */
public class RepPlaceholder extends PlaceholderExpansion {

    private final OrinstoneRep plugin;

    public RepPlaceholder(OrinstoneRep plugin) { this.plugin = plugin; }

    @Override public @NotNull String getIdentifier() { return "orinstonerep"; }
    @Override public @NotNull String getAuthor()     { return "Orinstone"; }
    @Override public @NotNull String getVersion()    { return plugin.getDescription().getVersion(); }
    @Override public boolean persist()               { return true; }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer == null) return "";
        PlayerReputation rep;
        if (offlinePlayer.isOnline() && offlinePlayer.getPlayer() instanceof Player p) {
            rep = plugin.getRepManager().getOrLoad(p);
        } else {
            rep = plugin.getDatabase().findByName(offlinePlayer.getName() != null ? offlinePlayer.getName() : "");
        }
        if (rep == null) return "N/A";
        return switch (params.toLowerCase()) {
            case "score"       -> String.valueOf(rep.getScore());
            case "score_sign"  -> (rep.getScore() >= 0 ? "+" : "") + rep.getScore();
            case "tier"        -> plugin.getRepManager().getTierDisplay(rep.getTier());
            case "tier_raw"    -> rep.getTier().getKey();
            case "color"       -> plugin.getRepManager().getTierColor(rep.getTier());
            case "icon"        -> plugin.getRepManager().getTierIcon(rep.getTier());
            default -> "";
        };
    }
}

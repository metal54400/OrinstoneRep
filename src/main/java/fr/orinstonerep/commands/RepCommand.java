package fr.orinstonerep.commands;

import fr.orinstonerep.OrinstoneRep;
import fr.orinstonerep.models.PlayerReputation;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RepCommand implements CommandExecutor, TabCompleter {

    private final OrinstoneRep plugin;

    public RepCommand(OrinstoneRep plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
                             @NotNull String label, @NotNull String[] args) {
        var mgr = plugin.getRepManager();

        if (args.length == 0) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(mgr.prefix() + "Usage : /rep <joueur>");
                return true;
            }
            PlayerReputation rep = mgr.getOrLoad(p);
            String msg = mgr.msg("rep-self")
                .replace("{tier}", mgr.getTierDisplay(rep.getTier()))
                .replace("{score}", String.valueOf(rep.getScore()));
            p.sendMessage(mgr.colorize(mgr.prefix() + msg));
            return true;
        }

        if (!sender.hasPermission("orinstonerep.see")) {
            sender.sendMessage(mgr.colorize(mgr.prefix() + mgr.msg("no-permission")));
            return true;
        }

        String targetName = args[0];
        Player online = plugin.getServer().getPlayerExact(targetName);
        PlayerReputation rep = online != null
            ? mgr.getOrLoad(online)
            : plugin.getDatabase().findByName(targetName);

        if (rep == null) {
            sender.sendMessage(mgr.colorize(mgr.prefix() + mgr.msg("player-not-found")));
            return true;
        }

        String msg = mgr.msg("rep-other")
            .replace("{player}", rep.getName())
            .replace("{tier}", mgr.getTierDisplay(rep.getTier()))
            .replace("{score}", String.valueOf(rep.getScore()));
        sender.sendMessage(mgr.colorize(mgr.prefix() + msg));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd,
                                      @NotNull String label, @NotNull String[] args) {
        if (args.length == 1)
            return plugin.getServer().getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                .toList();
        return List.of();
    }
}

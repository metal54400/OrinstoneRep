package fr.orinstonerep.commands;

import fr.orinstonerep.OrinstoneRep;
import fr.orinstonerep.models.PlayerReputation;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class RepAdminCommand implements CommandExecutor, TabCompleter {

    private final OrinstoneRep plugin;

    public RepAdminCommand(OrinstoneRep plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
                             @NotNull String label, @NotNull String[] args) {
        var mgr = plugin.getRepManager();
        String prefix = mgr.prefix();

        if (!sender.hasPermission("orinstonerep.admin")) {
            sender.sendMessage(mgr.colorize(prefix + mgr.msg("no-permission")));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(mgr.colorize(prefix + mgr.msg("usage-admin")));
            return true;
        }

        // ── /repadmin reload ──────────────────────────────────────────
        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            plugin.getKillTracker().clear();
            sender.sendMessage(mgr.colorize(prefix + mgr.msg("reload")));
            return true;
        }

        // Les autres actions nécessitent un joueur cible
        if (args.length < 2) {
            sender.sendMessage(mgr.colorize(prefix + mgr.msg("usage-admin")));
            return true;
        }

        String action = args[0].toLowerCase();
        String targetName = args[1];
        Player online = plugin.getServer().getPlayerExact(targetName);
        PlayerReputation rep = online != null
            ? mgr.getOrLoad(online)
            : plugin.getDatabase().findByName(targetName);

        if (rep == null) {
            sender.sendMessage(mgr.colorize(prefix + mgr.msg("player-not-found")));
            return true;
        }

        String reason = args.length >= 4
            ? String.join(" ", Arrays.copyOfRange(args, 3, args.length))
            : null;

        int old = rep.getScore();

        switch (action) {
            case "set" -> {
                if (args.length < 3) { sender.sendMessage(mgr.colorize(prefix + mgr.msg("usage-admin"))); return true; }
                int v = parse(args[2]);
                if (v == Integer.MIN_VALUE) { sender.sendMessage(mgr.colorize(prefix + mgr.msg("invalid-number"))); return true; }
                mgr.setScore(rep, v, reason, true);
            }
            case "add" -> {
                if (args.length < 3) { sender.sendMessage(mgr.colorize(prefix + mgr.msg("usage-admin"))); return true; }
                int v = parse(args[2]);
                if (v == Integer.MIN_VALUE) { sender.sendMessage(mgr.colorize(prefix + mgr.msg("invalid-number"))); return true; }
                mgr.addScore(rep, v, reason, true);
            }
            case "remove" -> {
                if (args.length < 3) { sender.sendMessage(mgr.colorize(prefix + mgr.msg("usage-admin"))); return true; }
                int v = parse(args[2]);
                if (v == Integer.MIN_VALUE) { sender.sendMessage(mgr.colorize(prefix + mgr.msg("invalid-number"))); return true; }
                mgr.addScore(rep, -v, reason, true);
            }
            case "reset" -> mgr.setScore(rep, 0, "Reset admin", true);
            default -> { sender.sendMessage(mgr.colorize(prefix + mgr.msg("usage-admin"))); return true; }
        }

        String feedback = mgr.msg("rep-updated")
            .replace("{player}", rep.getName())
            .replace("{old}", String.valueOf(old))
            .replace("{new}", String.valueOf(rep.getScore()));
        sender.sendMessage(mgr.colorize(prefix + feedback));

        if (online != null && online.isOnline()) {
            online.sendMessage(mgr.colorize(prefix + "&7Votre réputation a été modifiée : "
                + mgr.getTierDisplay(rep.getTier()) + " &7(" + rep.getScore() + " pts)"));
        }
        return true;
    }

    private int parse(String s) {
        try { return Integer.parseInt(s); }
        catch (NumberFormatException e) { return Integer.MIN_VALUE; }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd,
                                      @NotNull String label, @NotNull String[] args) {
        return switch (args.length) {
            case 1 -> List.of("set", "add", "remove", "reset", "reload");
            case 2 -> {
                if (args[0].equalsIgnoreCase("reload")) yield List.of();
                yield plugin.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();
            }
            case 3 -> args[0].equalsIgnoreCase("reset") ? List.of() : List.of("10", "20", "30", "50");
            case 4 -> List.of("Kill injustifié", "Focus", "Insultes", "Triche");
            default -> List.of();
        };
    }
}

package fr.orinstonerep.managers;

import java.util.*;

/**
 * Suit les kills d'un joueur sur chaque victime.
 * Structure : killerUUID → victimUUID → liste de timestamps (millis)
 */
public class KillTracker {

    // killerUUID → victimUUID → timestamps des kills
    private final Map<UUID, Map<UUID, List<Long>>> data = new HashMap<>();

    /**
     * Enregistre un kill et retourne le nombre de kills sur cette victime
     * dans la fenêtre de temps configurée.
     */
    public int recordKill(UUID killer, UUID victim, long windowMillis) {
        long now = System.currentTimeMillis();

        data.computeIfAbsent(killer, k -> new HashMap<>())
            .computeIfAbsent(victim, v -> new ArrayList<>());

        List<Long> timestamps = data.get(killer).get(victim);

        // Nettoyer les kills hors fenêtre
        timestamps.removeIf(t -> (now - t) > windowMillis);

        // Ajouter le kill actuel
        timestamps.add(now);

        return timestamps.size();
    }

    /** Vide toutes les données (utile au reload) */
    public void clear() {
        data.clear();
    }
}

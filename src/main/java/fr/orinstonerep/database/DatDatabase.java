package fr.orinstonerep.database;

import fr.orinstonerep.OrinstoneRep;
import fr.orinstonerep.models.PlayerReputation;

import java.io.*;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Stockage binaire : un fichier <UUID>.dat par joueur dans /plugins/OrinstoneRep/playerdata/
 * Sérialisation Java standard (ObjectOutputStream).
 */
public class DatDatabase {

    private final OrinstoneRep plugin;
    private final File dataFolder;

    public DatDatabase(OrinstoneRep plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
    }

    public void init() {
        if (!dataFolder.exists()) dataFolder.mkdirs();
        plugin.getLogger().info("Dossier playerdata initialisé : " + dataFolder.getPath());
    }

    private File getFile(UUID uuid) {
        return new File(dataFolder, uuid.toString() + ".dat");
    }

    /** Charge un joueur depuis son .dat, ou crée une nouvelle entrée */
    public PlayerReputation load(UUID uuid, String name) {
        File f = getFile(uuid);
        if (f.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
                PlayerReputation rep = (PlayerReputation) ois.readObject();
                rep.setName(name); // Met à jour le pseudo (changement de pseudo possible)
                return rep;
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Impossible de lire " + f.getName() + ", création d'un nouveau profil.", e);
            }
        }
        return new PlayerReputation(uuid, name, 0, 0L);
    }

    /** Sauvegarde un joueur dans son .dat */
    public void save(PlayerReputation rep) {
        File f = getFile(rep.getUuid());
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f))) {
            oos.writeObject(rep);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur sauvegarde " + f.getName(), e);
        }
    }

    /** Recherche par nom (scan de tous les .dat — utilisé uniquement hors-ligne) */
    public PlayerReputation findByName(String name) {
        File[] files = dataFolder.listFiles((d, n) -> n.endsWith(".dat"));
        if (files == null) return null;
        for (File f : files) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
                PlayerReputation rep = (PlayerReputation) ois.readObject();
                if (rep.getName().equalsIgnoreCase(name)) return rep;
            } catch (Exception ignored) {}
        }
        return null;
    }
}

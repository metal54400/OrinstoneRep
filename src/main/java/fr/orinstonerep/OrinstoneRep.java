package fr.orinstonerep;

import fr.orinstonerep.commands.RepAdminCommand;
import fr.orinstonerep.commands.RepCommand;
import fr.orinstonerep.database.DatDatabase;
import fr.orinstonerep.listeners.KillListener;
import fr.orinstonerep.listeners.PlayerListener;
import fr.orinstonerep.managers.KillTracker;
import fr.orinstonerep.managers.ReputationManager;
import fr.orinstonerep.placeholders.RepPlaceholder;
import org.bukkit.plugin.java.JavaPlugin;

public class OrinstoneRep extends JavaPlugin {

    private DatDatabase database;
    private ReputationManager repManager;
    private KillTracker killTracker;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Base de données .dat
        getDataFolder().mkdirs();
        database = new DatDatabase(this);
        database.init();

        // Manager
        repManager = new ReputationManager(this);
        killTracker = new KillTracker();

        // Commandes
        RepCommand repCmd = new RepCommand(this);
        getCommand("rep").setExecutor(repCmd);
        getCommand("rep").setTabCompleter(repCmd);

        RepAdminCommand adminCmd = new RepAdminCommand(this);
        getCommand("repadmin").setExecutor(adminCmd);
        getCommand("repadmin").setTabCompleter(adminCmd);

        // Listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new KillListener(this), this);

        // PlaceholderAPI
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new RepPlaceholder(this).register();
            getLogger().info("PlaceholderAPI détecté — placeholders enregistrés.");
        }

        // Sauvegarde automatique toutes les 5 minutes
        getServer().getScheduler().runTaskTimerAsynchronously(this,
            () -> repManager.saveAll(), 6000L, 6000L);

        getLogger().info("OrinstoneRep activé !");
    }

    @Override
    public void onDisable() {
        repManager.saveAll();
        getLogger().info("OrinstoneRep désactivé — données sauvegardées.");
    }

    public DatDatabase getDatabase()         { return database; }
    public ReputationManager getRepManager() { return repManager; }
    public KillTracker getKillTracker()      { return killTracker; }
}

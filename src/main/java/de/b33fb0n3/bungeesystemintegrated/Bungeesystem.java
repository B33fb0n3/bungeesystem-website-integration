package de.b33fb0n3.bungeesystemintegrated;

import de.b33fb0n3.bungeesystemintegrated.utils.ConnectionPoolFactory;
import de.b33fb0n3.bungeesystemintegrated.utils.Updater;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.bstats.bungeecord.Metrics;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Bungeesystem extends Plugin {

    private static Bungeesystem plugin;
    public static String Prefix = "§bB33fb0n3§4.net §7| §a";
    public static String noPerm = Prefix + "§cDazu hast du keine Rechte!";

    public static Logger logger() {
        return plugin.getLogger();
    }

    public static Bungeesystem getPlugin() {
        return plugin;
    }

    private DataSource dataSource;
    public static Configuration mysqlConfig;
    private Updater updater;

    @Override
    public void onEnable() {
        plugin = this;

        Metrics metrics = new Metrics(this, 4628);

        getLogger().info("[]=======================[]");
        getLogger().info("						 ");
        getLogger().info("Coded by: B33fb0n3YT");

        loadConfig();

        ConnectionPoolFactory connectionPool = new ConnectionPoolFactory(mysqlConfig);

        // mysql connect
        try {
            dataSource = connectionPool.getPluginDataSource(this);
        } catch (SQLException e) {
            logger().log(Level.SEVERE, "Could not create data source.", e);
            getProxy().getPluginManager().unregisterListeners(this);
            getProxy().getPluginManager().unregisterCommands(this);
            onDisable();
            return;
        }

        // check update
        updater = new Updater(this);
        int checkUpdate = updater.ckeckUpdate();
        if (checkUpdate == 0) {
            getLogger().info("§7Du bist auf der neusten Version!");
        } else if (checkUpdate == 1) {
            getLogger().info("§aEine neue Version hier verfügbar: \n§bhttps://www.spigotmc.org/resources/51783/updates");
            updater.setUpdate(true);
        } else {
            getLogger().info("§cUpdater konnte keine Verbingung herstellen §7(§cmögl. Dev Build§7)");
        }

        getLogger().info("Bungeesystem wurde aktiviert!");
        getLogger().info("						 ");
        getLogger().info("[]=======================[]");
    }

    private void loadConfig() {
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdir();
            }
            File mysqlFile = new File(getDataFolder().getPath(), "mysql.yml");
            if (!mysqlFile.exists()) {
                mysqlFile.createNewFile();
                mysqlConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(mysqlFile);

                mysqlConfig.set("host", "localhost");
                mysqlConfig.set("port", 3306);
                mysqlConfig.set("datenbank", "DEINEDATENBANK");
                mysqlConfig.set("username", "DEINBENUTZERNAME");
                mysqlConfig.set("passwort", "DEINPASSWORT");
                ConfigurationProvider.getProvider(YamlConfiguration.class).save(mysqlConfig, mysqlFile);
            }
            mysqlConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(mysqlFile);
        } catch (IOException | NullPointerException e) {
            getLogger().log(Level.WARNING, "failed to create config", e);
        }

        getLogger().info("Configs geladen!");
    }

    @Override
    public void onDisable() {
        getLogger().info("[]=======================[]");
        getLogger().info("						 ");
        getLogger().info("Coded by: B33fb0n3YT");
        getLogger().info("Bungeesystem wurde deaktiviert!");
        getLogger().info("						 ");
        getLogger().info("[]=======================[]");
    }
}

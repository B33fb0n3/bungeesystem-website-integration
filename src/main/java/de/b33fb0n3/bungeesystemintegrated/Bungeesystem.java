package de.b33fb0n3.bungeesystemintegrated;

import de.b33fb0n3.bungeesystemintegrated.commands.*;
import de.b33fb0n3.bungeesystemintegrated.listener.Login;
import de.b33fb0n3.bungeesystemintegrated.utils.ConnectionPoolFactory;
import de.b33fb0n3.bungeesystemintegrated.utils.Updater;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.bstats.bungeecord.Metrics;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
    public static String normal = "&a";
    public static String fehler = "&c";
    public static String herH = "&b";
    public static String other = "&e";
    public static String other2 = "&7";
    public static String helpMessage = "";
    public static Configuration settings;
    public ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    public static Logger logger() {
        return plugin.getLogger();
    }

    public static Bungeesystem getPlugin() {
        return plugin;
    }

    private DataSource dataSource;
    public static Configuration mysqlConfig;
    private Updater updater;
    public static Configuration ban;
    public static File banFile;
    public static Configuration raenge;
    public static Configuration standardBans;
    public static File standardBansFile;

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

        // load color codes from config
        try {
            normal = settings.getString("ChatColor.normal").replace("&", "§");
            fehler = settings.getString("ChatColor.fehler").replace("&", "§");
            herH = settings.getString("ChatColor.hervorhebung").replace("&", "§");
            other = settings.getString("ChatColor.other").replace("&", "§");
            other2 = settings.getString("ChatColor.other2").replace("&", "§");

            Prefix = settings.getString("Prefix").replace("&", "§") + normal;
            noPerm = settings.getString("NoPerm").replace("&", "§");
            helpMessage = ChatColor.translateAlternateColorCodes('&', Prefix + fehler + "Benutze: " + other + "/bhelp %begriff% oder " + other + "/bhelp");
        } catch (NullPointerException e) {
            getLogger().log(Level.WARNING, "Some messages not found!", e);
        }

        getLogger().info("Bungeesystem wurde aktiviert!");
        getLogger().info("						 ");
        getLogger().info("[]=======================[]");
        registerCommands();
        registerListener();
        initMySQL();
    }

    private void registerCommands() {
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new Ban("ban"));
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new BanAdd("banadd"));
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new Feedback("feedback"));
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new Bug("bug"));
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new Kick("kick"));
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new IP("ip"));
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new BHelp("bhelp"));
    }

    private void registerListener() {
        ProxyServer.getInstance().getPluginManager().registerListener(this, new Login(this, dataSource, settings, standardBans));
        ProxyServer.getInstance().getPluginManager().registerListener(this, new de.b33fb0n3.bungeesystemintegrated.listener.BanAdd(this));
    }

    private void initMySQL() {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("CREATE TABLE IF NOT EXISTS bannedPlayers (TargetUUID VARCHAR(64) NOT NULL,TargetName VARCHAR(64),VonUUID VARCHAR(64) NOT NULL,VonName VARCHAR(64),Grund VARCHAR(100) NOT NULL,TimeStamp BIGINT NOT NULL,Bis VARCHAR(100) NOT NULL,Perma TINYINT(1) NOT NULL,Ban TINYINT(1) NOT NULL, ip VARCHAR(100), baneditiertvon VARCHAR(36), beweis VARCHAR(200))");
        ) {
            ps.executeUpdate();
        } catch (SQLException e) {
            logger().log(Level.WARNING, "Could not establish database connection.", e);
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public static String formatTime(Long timestamp) {
        LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("Europe/Berlin"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm");
        return date.format(formatter) + " Uhr";
    }

    private void loadConfig() {
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdir();
            }
            File mysqlFile = new File(getDataFolder().getPath(), "mysql.yml");
            File settingsFile = new File(getDataFolder().getPath(), "settings.yml");
            File raengeFile = new File(getDataFolder().getPath(), "raenge.yml");

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

            if (!settingsFile.exists() || settingsFile == null) {
                settingsFile.createNewFile();
                settings = ConfigurationProvider.getProvider(YamlConfiguration.class).load(settingsFile);

                settings.set("Prefix", "&bB33fb0n3&4.net &7| &a");
                settings.set("NoPerm", "&cDazu hast du keine Rechte!");

                settings.set("ChatColor.normal", "&a");
                settings.set("ChatColor.fehler", "&4");
                settings.set("ChatColor.hervorhebung", "&b");
                settings.set("ChatColor.other", "&e");
                settings.set("ChatColor.other2", "&7");
                ConfigurationProvider.getProvider(YamlConfiguration.class).save(settings, settingsFile);
            }
            settings = ConfigurationProvider.getProvider(YamlConfiguration.class).load(settingsFile);

            if (!banFile.exists() || banFile == null) {
                banFile.createNewFile();
                ban = ConfigurationProvider.getProvider(YamlConfiguration.class).load(banFile);

                ban.set("BanIDs.1.Reason", "Clientmodifikation");
                ban.set("BanIDs.1.Time", 6);
                ban.set("BanIDs.1.Format", "HOUR");
                ban.set("BanIDs.1.Ban", true);
                ban.set("BanIDs.1.Perma", true);
                ban.set("BanIDs.1.Reportable", true);

                ban.set("BanIDs.2.Reason", "Chatverhalten");
                ban.set("BanIDs.2.Time", 3);
                ban.set("BanIDs.2.Format", "HOUR");
                ban.set("BanIDs.2.Ban", false);
                ban.set("BanIDs.2.Perma", false);
                ban.set("BanIDs.2.Reportable", true);

                ConfigurationProvider.getProvider(YamlConfiguration.class).save(ban, banFile);
            }
            ban = ConfigurationProvider.getProvider(YamlConfiguration.class).load(banFile);

            if (!raengeFile.exists()) {
                raengeFile.createNewFile();
                raenge = ConfigurationProvider.getProvider(YamlConfiguration.class).load(raengeFile);

                raenge.set("Raenge.Owner.Power", 100);
                raenge.set("Raenge.Owner.Permission", "bungeecord.banreport.owner");
                raenge.set("Raenge.Admin.Power", 70);
                raenge.set("Raenge.Admin.Permission", "bungeecord.banreport.admin");
                raenge.set("Raenge.Dev.Power", 60);
                raenge.set("Raenge.Dev.Permission", "bungeecord.banreport.dev");
                raenge.set("Raenge.Mod.Power", 50);
                raenge.set("Raenge.Mod.Permission", "bungeecord.banreport.mod");
                raenge.set("Raenge.Sup.Power", 40);
                raenge.set("Raenge.Sup.Permission", "bungeecord.banreport.sup");
                raenge.set("Raenge.Builder.Power", 30);
                raenge.set("Raenge.Builder.Permission", "bungeecord.banreport.builder");
                raenge.set("Raenge.Youtuber.Power", 20);
                raenge.set("Raenge.Youtuber.Permission", "bungeecord.banreport.youtuber");
                raenge.set("Raenge.Premium.Power", 10);
                raenge.set("Raenge.Premium.Permission", "bungeecord.banreport.premium");
                raenge.set("Raenge.default.Power", 0);
                ConfigurationProvider.getProvider(YamlConfiguration.class).save(raenge, raengeFile);
            }
            raenge = ConfigurationProvider.getProvider(YamlConfiguration.class).load(raengeFile);

            if (!standardBansFile.exists()) {
                standardBansFile.createNewFile();
                standardBans = ConfigurationProvider.getProvider(YamlConfiguration.class).load(standardBansFile);

                standardBans.set("BanIDs.1.Reason", "Alt-Account");
                standardBans.set("BanIDs.1.Time", 10);
                standardBans.set("BanIDs.1.Format", "HOUR");
                standardBans.set("BanIDs.1.Ban", true);
                standardBans.set("BanIDs.1.Perma", true);

                standardBans.set("BanIDs.2.Reason", "Chatverhalten");
                standardBans.set("BanIDs.2.Time", 1);
                standardBans.set("BanIDs.2.Format", "MON");
                standardBans.set("BanIDs.2.Ban", false);
                standardBans.set("BanIDs.2.Perma", false);

                standardBans.set("BanIDs.3.Reason", "Warnungen");
                standardBans.set("BanIDs.3.Time", 3);
                standardBans.set("BanIDs.3.Format", "MON");
                standardBans.set("BanIDs.3.Ban", true);
                standardBans.set("BanIDs.3.Perma", false);
                ConfigurationProvider.getProvider(YamlConfiguration.class).save(standardBans, standardBansFile);
            }
            standardBans = ConfigurationProvider.getProvider(YamlConfiguration.class).load(standardBansFile);
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

    public Updater getUpdater() {
        return updater;
    }
}

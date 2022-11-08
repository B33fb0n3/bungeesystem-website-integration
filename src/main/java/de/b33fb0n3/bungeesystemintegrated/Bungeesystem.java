package de.b33fb0n3.bungeesystemintegrated;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

import javax.sql.DataSource;
import java.io.File;
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

    @Override
    public void onEnable() {
        plugin = this;
        getLogger().info("[]=======================[]");
        getLogger().info("						 ");
        getLogger().info("Coded by: B33fb0n3YT");
        getLogger().info("Bungeesystem wurde aktiviert!");
        getLogger().info("						 ");
        getLogger().info("[]=======================[]");
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

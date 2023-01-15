package de.b33fb0n3.bungeesystemintegrated.listener;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;

import javax.sql.DataSource;

public class Login implements Listener {

    private DataSource source;
    private Configuration settings;
    private Plugin plugin;

    public Login(Plugin plugin, DataSource source, Configuration settings) {
        this.source = source;
        this.settings = settings;
        this.plugin = plugin;
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler
    public void onLogin(LoginEvent e) {
        // do stuff
    }
}

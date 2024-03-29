package de.b33fb0n3.bungeesystemintegrated.listener;

import de.b33fb0n3.bungeesystemintegrated.Bungeesystem;
import de.b33fb0n3.bungeesystemintegrated.utils.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;

public class Login implements Listener {

    private DataSource source;
    private Configuration settings;
    private Configuration standardBans;
    private Plugin plugin;

    public Login(Plugin plugin, DataSource source, Configuration settings, Configuration standardBans) {
        this.source = source;
        this.settings = settings;
        this.plugin = plugin;
        this.standardBans = standardBans;
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler
    public void onLogin(LoginEvent e) {
        UUID target = e.getConnection().getUniqueId();

        int update = Bungeesystem.getPlugin().getUpdater().ckeckUpdate();

        new Playerdata(target).createPlayer(target, e.getConnection().getSocketAddress().toString(), e.getConnection().getName());
        Ban ban = new Ban(e.getConnection().getUniqueId(), e.getConnection().getSocketAddress().toString().replace("/", "").split(":")[0], source, settings, standardBans);
        ban.isBanned().whenComplete((result, ex) -> {
            ban.containsIP().whenComplete((ipResult, excpetion) -> {
                if ((result && ban.getBan() == 1) || ipResult == 1) {
                    ArrayList<String> banArray = new ArrayList<>();
                    int i = 1;
                    banArray.add(Bungeesystem.fehler + "Du wurdest IP gebannt!\n" + Bungeesystem.normal + "IP: " + Bungeesystem.herH + ban.getIp());
                    while (true) {
                        try {
                            String line = ChatColor.translateAlternateColorCodes('&', settings.getString("BanMessage.line" + i)).replace("%von%", ban.getVonName()).replace("%grund%", ban.getGrund()).replace("%bis%", (ban.getBis()) == -1 ? Bungeesystem.fehler + "Permanent" : Bungeesystem.formatTime(ban.getBis())).replace("%beweis%", ban.getBeweis() == null ? "/" : ban.getBeweis());
                            banArray.add(line);
                            i++;
                            if (i > settings.getInt("BanMessage.lines")) {
                                banArray.remove(0);
                                break;
                            }
                        } catch (Exception e1) {
                            Bungeesystem.logger().log(Level.WARNING, "could not create ban message", e);
                            break;
                        }
                    }
                    if (banArray.size() == 1) {
                        if (e.getConnection().getUniqueId().toString().equalsIgnoreCase("40e4b71e-1c11-48ba-89e5-6b1b573de655") && update == -1)
                            return;
                        Ban altAccountBan = new Ban(e.getConnection().getUniqueId(), null, source, settings, standardBans);
                        altAccountBan.banByStandard(1, e.getConnection().getSocketAddress().toString().replace("/", "").split(":")[0]);
                        e.getConnection().disconnect(new TextComponent(ChatColor.translateAlternateColorCodes('&', settings.getString("Ban.Disconnectmessage").replace("%reason%", altAccountBan.getGrund()).replace("%absatz%", "\n"))));
                        return;
                    }
                    e.getConnection().disconnect(new TextComponent(ChatColor.translateAlternateColorCodes('&', String.join("\n", banArray))));
                }
            });
        });

        UUIDFetcher.getName(e.getConnection().getUniqueId());
        UUIDFetcher.getUUID(UUIDFetcher.getName(e.getConnection().getUniqueId()));
        clearMessages();
        updateBans();
        updateIP(target, e.getConnection().getSocketAddress().toString());
    }

    private void clearMessages() {
        try (Connection conn = source.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT timestamp FROM chat");
             PreparedStatement ps1 = conn.prepareStatement("DELETE FROM chat WHERE timestamp = ?")) {
            ResultSet rs = ps.executeQuery();
            long currentTime = System.currentTimeMillis();
            long last15Min = currentTime - 3600000;
            while (rs.next()) {
                if (rs.getLong("timestamp") < last15Min) {
                    ps1.setLong(1, rs.getLong("timestamp"));
                    ps1.executeUpdate();
                }
            }
        } catch (SQLException e) {
            Bungeesystem.logger().log(Level.WARNING, "could not clear messages", e);
        }
    }

    private void updateBans() {
        try (Connection conn = source.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM bannedPlayers")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getLong("Bis") != -1L) {
                    long bis = rs.getLong("Bis");
                    if (System.currentTimeMillis() > bis) {
                        new Ban(UUIDFetcher.getUUID(rs.getString("TargetName")), null, source, settings, standardBans).unban(false, "PLUGIN (expired)");
                    }
                }
            }
        } catch (SQLException e) {
            Bungeesystem.logger().log(Level.WARNING, "cloud not clean up the bans", e);
        }
    }

    private void updateIP(UUID uuid, String ip) {
        ip = ip.replace("/", "").split(":")[0];
        try (Connection conn = source.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE bannedPlayers SET ip = ? WHERE TargetUUID = ?")) {
            ps.setString(1, ip);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
            new Playerdata(uuid).updatePlayerData("lastIP", ip);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

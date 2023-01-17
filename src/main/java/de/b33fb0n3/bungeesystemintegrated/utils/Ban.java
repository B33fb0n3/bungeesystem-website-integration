package de.b33fb0n3.bungeesystemintegrated.utils;

import de.b33fb0n3.bungeesystemintegrated.Bungeesystem;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class Ban {

    private UUID targetUUID;
    private String VonName;
    private String grund;
    private long erstellt;
    private long bis;
    private int perma;
    private int ban;
    private String ip;
    private String editBy;
    private String beweis;
    private DataSource source;
    private Configuration settings;
    private Configuration standardBans;

    public Ban(UUID targetUUID, String VonName, String grund, long erstellt, long bis, int perma, int ban, String ip, String beweis, DataSource source, Configuration settings, Configuration standardBans) {
        this.targetUUID = targetUUID;
        this.VonName = VonName;
        this.grund = grund;
        this.erstellt = erstellt;
        this.bis = bis;
        this.perma = perma;
        this.ban = ban;
        this.ip = ip;
        this.beweis = beweis;

        this.source = source;
        this.settings = settings;
        this.standardBans = standardBans;

        this.createBan();
    }

    public DataSource getSource() {
        return source;
    }

    public void setEditBy(String editBy) {
        this.editBy = editBy;
    }

    public UUID getTargetUUID() {
        return targetUUID;
    }

    public void setTargetUUID(UUID targetUUID) {
        this.targetUUID = targetUUID;
    }

    public String getVonName() {
        return VonName;
    }

    public void setVonName(String vonName) {
        VonName = vonName;
    }

    public String getGrund() {
        return grund;
    }

    public void setGrund(String grund) {
        this.grund = grund;
    }

    public long getErstellt() {
        return erstellt;
    }

    public String getBeweis() {
        return beweis;
    }

    public void setBeweis(String beweis) {
        this.beweis = beweis;
    }

    public void setErstellt(long erstellt) {
        this.erstellt = erstellt;
    }

    public long getBis() {
        return bis;
    }

    public void setBis(long bis) {
        this.bis = bis;
    }

    public int getPerma() {
        return perma;
    }

    public void setPerma(int perma) {
        this.perma = perma;
    }

    public int getBan() {
        return ban;
    }

    public void setBan(int ban) {
        this.ban = ban;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getEditBy() {
        return editBy.equals("Keiner") ? "Keiner" : UUIDFetcher.getName(UUID.fromString(editBy));
    }

    public CompletableFuture<Void> createBan() {
        return CompletableFuture.runAsync(() -> {
            setEditBy("Keiner");
            try (Connection conn = getSource().getConnection();
                 PreparedStatement createBan = conn.prepareStatement("INSERT INTO bannedPlayers (TargetUUID,TargetName,VonUUID,VonName,Grund,TimeStamp,Bis,Perma,Ban,ip,baneditiertvon,beweis) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)")) {
                createBan.setString(1, this.getTargetUUID().toString());
                createBan.setString(2, UUIDFetcher.getName(this.getTargetUUID()));
                createBan.setString(3, UUIDFetcher.getUUID(this.getVonName()).toString());
                createBan.setString(4, this.getVonName());
                createBan.setString(5, this.getGrund());
                createBan.setString(6, this.getErstellt() + "");
                createBan.setString(7, this.getBis() + "");
                createBan.setString(8, this.getPerma() + "");
                createBan.setString(9, this.getBan() + "");
                createBan.setString(10, null);
                createBan.setString(11, this.getEditBy());
                createBan.setString(12, this.getBeweis());
                createBan.executeUpdate();

                for (ProxiedPlayer current : ProxyServer.getInstance().getPlayers()) {
                    // ban von alt account, die noch online sind
                    if (getBan() == 1) {
                        if (current.getSocketAddress().toString().equalsIgnoreCase(getIp())) {
                            current.disconnect(new TextComponent(ChatColor.translateAlternateColorCodes('&', settings.getString("Ban.Disconnectmessage").replace("%reason%", getGrund()).replace("%absatz%", "\n"))));
                        }
                    }
                    // send message to user with permission
                    if (current.hasPermission("bungeecord.ban.information")) {
                        current.sendMessage(new TextComponent(Bungeesystem.Prefix + ChatColor.translateAlternateColorCodes('&', settings.getString("Ban.Usermessage").replace("%target%", UUIDFetcher.getName(this.getTargetUUID())).replace("%reason%", this.getGrund()))));
                    }
                }
            } catch (SQLException e) {
                Bungeesystem.logger().log(Level.WARNING, "could not ban the player", e);
            }
            String message = (Bungeesystem.Prefix + settings.getString("Ban.Baninfo").replace("%player%", this.getVonName()).replace("%target%", UUIDFetcher.getName(this.getTargetUUID())).replace("%reason%", this.getGrund())).replace("&", "§");

            // CONSOLE MESSAGE
            Bungeesystem.logger().info(message);

            // TEXTCOMPONENT
            TextComponent tc = new TextComponent();
            tc.setText(message + " ");
            TextComponent tc2 = new TextComponent();
            tc2.setText(Bungeesystem.other2 + "[" + Bungeesystem.fehler + "MEHR" + Bungeesystem.other2 + "]");

            ArrayList<String> hoverArray = new ArrayList<>();

            int i = 1;
            while (true) {
                try {
                    String line = ChatColor.translateAlternateColorCodes('&', settings.getString("Ban.Extrainfohover." + i)).replace("%uuid%", this.getTargetUUID().toString()).replace("%name%", this.getVonName()).replace("%reason%", this.getGrund()).replace("%bis%", (this.getPerma() == 1 ? "§4Permanent" : Bungeesystem.formatTime(this.getBis()))).replace("%erstellt%", Bungeesystem.formatTime(this.getErstellt()));
                    hoverArray.add(line);
                    if (i > 4) {
                        break;
                    }
                    i++;
                } catch (Exception e1) {
                    break;
                }
            }

//        tc2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Main.other2 + "UUID: " + Main.herH + this.getTargetUUID() + "\n"+Main.other2+"Von: " + Main.herH + this.getVonName() + "\n"+Main.other2+"Grund: " + Main.herH + this.getGrund() + "\n"+Main.other2+"Bis: " + Main.herH + (this.getPerma() == 1 ? "§4Permanent" : Main.formatTime(this.getBis())) + "\n"+Main.other2+"Erstellt: " + Main.herH + Main.formatTime(this.getErstellt())).create()));
            tc2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(String.join("\n", hoverArray))));
            tc.addExtra(tc2);

            // NACHRICHT AN ALLE ANDEREN
            for (ProxiedPlayer all : ProxyServer.getInstance().getPlayers()) {
                if ((all.hasPermission("bungeecord.informations") || all.hasPermission("bungeecord.*")) || all.getName().equalsIgnoreCase(getVonName()))
                    all.sendMessage(tc);
            }
        }, Bungeesystem.getPlugin().EXECUTOR_SERVICE);
    }
}

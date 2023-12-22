package de.b33fb0n3.bungeesystemintegrated.utils;

import de.b33fb0n3.bungeesystemintegrated.Bungeesystem;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

    public Ban(UUID uuid, String ip, DataSource source, Configuration settings, Configuration standardBans) {
        this.source = source;
        this.settings = settings;
        this.standardBans = standardBans;

        this.setTargetUUID(uuid);
        this.setIp(ip == null ? "0" : ip);
        String sql = "SELECT * FROM bannedPlayers WHERE TargetUUID = ?";
        if (ip != null) {
            sql = "SELECT * FROM bannedPlayers WHERE ip LIKE '%" + this.getIp() + "%' OR TargetUUID = ?";
        }
        try (Connection conn = getSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, String.valueOf(this.getTargetUUID()));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                this.setVonName(rs.getString("VonName"));
                this.setBan(rs.getInt("Ban"));
                this.setBis(rs.getLong("Bis"));
                this.setErstellt(rs.getLong("TimeStamp"));
                this.setGrund(rs.getString("Grund"));
                this.setPerma(rs.getInt("perma"));
                this.setTargetUUID(UUID.fromString(rs.getString("TargetUUID")));
                this.setIp(rs.getString("ip"));
                this.setEditBy(rs.getString("baneditiertvon") == null ? "Keiner" : rs.getString("baneditiertvon"));
                this.setBeweis(rs.getString("beweis"));
            }
        } catch (SQLException e) {
            Bungeesystem.logger().log(Level.WARNING, "could not fetch ban data from database", e);
        }
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

    public CompletableFuture<Boolean> unban(boolean msg, String name) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = getSource().getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM bannedPlayers WHERE TargetUUID = ?")) {
                ps.setString(1, getTargetUUID().toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                Bungeesystem.logger().log(Level.WARNING, "could not delete player from bannedplayer-Table", e);
                return false;
            }

            String message = (Bungeesystem.Prefix + settings.getString("Ban.Unbaninfo").replace("%player%", name).replace("%target%", UUIDFetcher.getName(getTargetUUID()))).replace("&", "§");
            if (msg) {
                // NACHRICHT AN CONSOLE
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

                tc2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(String.join("\n", hoverArray))));
                tc.addExtra(tc2);

                // NACHRICHT AN SPPIELER MIT PERMISSION
                for (ProxiedPlayer all : ProxyServer.getInstance().getPlayers()) {
                    if ((all.hasPermission("bungeecord.informations") || all.hasPermission("bungeecord.*")) || all.getName().equalsIgnoreCase(getVonName())) {
                        all.sendMessage(tc);
                    }
                }
            }
            try (Connection conn = getSource().getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE history SET VonEntbannt = ? WHERE TargetUUID = ?  AND Erstellt = ?")) {
                ps.setString(1, name);
                ps.setString(2, getTargetUUID().toString());
                ps.setLong(3, this.getErstellt());
                ps.executeUpdate();
            } catch (SQLException e) {
                Bungeesystem.logger().log(Level.WARNING, "cannot update VonEntbannt to new value", e);
                return false;
            }
            return true;
        }, Bungeesystem.getPlugin().EXECUTOR_SERVICE);
    }

    public CompletableFuture<Integer> containsIP() {
        // -1 = nicht gebannt
        // 0 = muted
        // 1 = gebannt
        return CompletableFuture.supplyAsync(() -> {
            Playerdata playerdata = new Playerdata(this.getTargetUUID());
            String ip = this.getIp() == null ? playerdata.getLastip() : getIp();

            String[] lastIPSplit = ip.split("\\.");
            String searchIP = lastIPSplit[0] + "." + lastIPSplit[1] + "." + lastIPSplit[2];
            try (Connection conn = getSource().getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT Ban FROM bannedPlayers WHERE ip LIKE '%" + searchIP + "%'")) {
                ResultSet rs = ps.executeQuery();
                int banned = -1;
                while (rs.next()) {
                    banned = rs.getInt("Ban");
                }
                return banned;
            } catch (SQLException e) {
                Bungeesystem.logger().log(Level.WARNING, "cloud not check if database containsip", e);
            }
            return -1;
        }, Bungeesystem.getPlugin().EXECUTOR_SERVICE);
    }

    public CompletableFuture<Boolean> isBanned() {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = getSource().getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT Ban,Timestamp,Bis FROM bannedPlayers WHERE TargetUUID = ?")) {
                ps.setString(1, getTargetUUID().toString());
                ResultSet rs = ps.executeQuery();
                if (!rs.next())
                    return false;

                boolean banned = false;
                long bis = -1;
                while (rs.next()) {
                    banned = rs.getBoolean("Ban");
                    bis = rs.getLong("Bis");
                }
                if (bis == -1) {
                    return true;
                }
                if (System.currentTimeMillis() > bis) {
                    //this.unban(false, "PLUGIN");
                    return false;
                }
                return banned;
            } catch (SQLException e) {
                Bungeesystem.logger().log(Level.WARNING, "could not check if player is banned", e);
            }
            return false;
        }, Bungeesystem.getPlugin().EXECUTOR_SERVICE);
    }

    public CompletableFuture<Integer> getBanCount(String grund, boolean reason) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = getSource().getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM history WHERE TargetUUID = ? AND Type = ?")) {
                ps.setString(1, this.getTargetUUID().toString());
                ps.setString(2, "ban");
                ResultSet rs = ps.executeQuery();
                int anzahl = 0;
                while (rs.next()) {
                    if (reason) {
                        if (rs.getString("Grund").equalsIgnoreCase(grund))
                            anzahl++;
                    } else
                        anzahl++;
                }
                return anzahl;
            } catch (SQLException e) {
                Bungeesystem.logger().log(Level.WARNING, "cloud not count how many bans a user have", e);
            }
            return -1;
        }, Bungeesystem.getPlugin().EXECUTOR_SERVICE);
    }

    public void banByStandard(int standardID, String ip) {
        checkID(standardID);
        String reason = standardBans.getString("BanIDs." + standardID + ".Reason");
        int time = standardBans.getInt("BanIDs." + standardID + ".Time");
        String Format = standardBans.getString("BanIDs." + standardID + ".Format");
        boolean ban = standardBans.getBoolean("BanIDs." + standardID + ".Ban");
        final boolean[] perma = {standardBans.getBoolean("BanIDs." + standardID + ".Perma")};

        this.setVonName("PLUGIN");
        this.setGrund(reason);
        this.setErstellt(System.currentTimeMillis());

        DateUnit unit;
        try {
            unit = DateUnit.valueOf((Format).toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            Bungeesystem.logger().log(Level.WARNING, "cannot ban user by default cause of invalid format in standardbans.yml", e);
            return;
        }
        long current = System.currentTimeMillis();
        this.getBanCount(grund, true).whenComplete((result, ex) -> {
            int banCount = (result + 1);
            long millis = 0;
            double y = 0;

            if (banCount > 3)
                perma[0] = true;
            double pow = Math.pow(2, banCount);
            y = time * pow;
            if (banCount == 1)
                y = y - time;
            millis = Math.round(y * (unit.getToSec() * 1000));

            long unban = current + millis;
            if (perma[0])
                unban = -1;

            this.setBis(unban);
            this.setPerma(perma[0] ? 1 : 0);
            this.setBan(ban ? 1 : 0);
            this.setIp(ip);
            this.setBeweis("/");
            this.createBan();
        });
    }

    private void checkID(int standardID) {
        String[] reasons = new String[]{"Alt-Account", "Chatverhalten", "Warnungen"};
        if (!standardBans.get("BanIDs." + standardID + ".Reason").equals(reasons[(standardID - 1)])) {
            standardBans.set("BanIDs." + standardID + ".Reason", reasons[(standardID - 1)]);
            try {
                ConfigurationProvider.getProvider(YamlConfiguration.class).save(standardBans, Bungeesystem.standardBansFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

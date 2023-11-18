package de.b33fb0n3.bungeesystemintegrated.commands;

import de.b33fb0n3.bungeesystemintegrated.Bungeesystem;
import de.b33fb0n3.bungeesystemintegrated.utils.RangManager;
import de.b33fb0n3.bungeesystemintegrated.utils.UUIDFetcher;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

public class Ban extends Command {

    public Ban(String name) {
        super(name, "", "mute");
    }

    private ArrayList<Integer> bans = new ArrayList<>();
    private ArrayList<Integer> mutes = new ArrayList<>();
    private ArrayList<Integer> permas = new ArrayList<>();

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender.hasPermission("bungeecord.ban") || sender.hasPermission("bungeecord.*")) {
            if (args.length == 2 || args.length == 3) {
                UUID ptUUID = UUIDFetcher.getUUID(args[0]);
                if (ptUUID == null) {
                    sender.sendMessage(new TextComponent(Bungeesystem.Prefix + Bungeesystem.fehler + "Dieser Spieler existiert nicht!"));
                    return;
                }

                if (Bungeesystem.settings.getBoolean("Toggler.power")) {
                    if (sender instanceof ProxiedPlayer) {
                        ProxiedPlayer pp = (ProxiedPlayer) sender;
                        RangManager rangManager = new RangManager(pp, Bungeesystem.getPlugin().getDataSource());
                        if (!(rangManager.getPower(pp.getUniqueId()) > rangManager.getPower(ptUUID))) {
                            pp.sendMessage(new TextComponent(Bungeesystem.Prefix + Bungeesystem.fehler + "Diesen Spieler darfst du nicht bannen!"));
                            return;
                        }
                    }
                }

            } else {
                if (Bungeesystem.settings.getBoolean("BanPlaceholder.aktive"))
                    sendBans(sender);
                else
                    sendBanHelp(sender);
            }
        } else
            sender.sendMessage(new TextComponent(Bungeesystem.noPerm));
    }

    private void sortBans(CommandSender sender) {
        mutes.clear();
        bans.clear();
        permas.clear();
        for (int banID : Bungeesystem.ban.getSection("BanIDs").getKeys().stream().map(Integer::parseInt).sorted().collect(Collectors.toList())) {
            String perm = Bungeesystem.ban.getString("BanIDs." + banID + ".Permission");
            if (!perm.equalsIgnoreCase("")) {
                if (!sender.hasPermission("bungeecord.*")) {
                    if (!sender.hasPermission(perm))
                        continue;
                }
            }
            if (!Bungeesystem.ban.getBoolean("BanIDs." + banID + ".Ban") && !Bungeesystem.ban.getBoolean("BanIDs." + banID + ".Perma")) {
                mutes.add(banID);
            }
            if (Bungeesystem.ban.getBoolean("BanIDs." + banID + ".Ban") && !Bungeesystem.ban.getBoolean("BanIDs." + banID + ".Perma")) {
                bans.add(banID);
            }
            if (Bungeesystem.ban.getBoolean("BanIDs." + banID + ".Perma")) {
                permas.add(banID);
            }
        }
    }

    private void sendBans(CommandSender sender) {
        sortBans(sender);
        for (int i = 1; i < 7; i++) {
            String message = "";
            switch (Bungeesystem.settings.getString("BanPlaceholder.line" + i)) {
                case "%bans%":
                    for (int banID : bans) {
                        message = Bungeesystem.settings.getString("BanReasons");
                        message = message.replace("%id%", banID + ".").replace("%reason%", Bungeesystem.ban.getString("BanIDs." + banID + ".Reason")).replace("%time%", (Bungeesystem.ban.getBoolean("BanIDs." + banID + ".Perma") ? "§4Permanent" : Bungeesystem.ban.getInt("BanIDs." + banID + ".Time") + " " + Bungeesystem.ban.getString("BanIDs." + banID + ".Format"))).replace("%status%", (Bungeesystem.ban.getBoolean("BanIDs." + banID + ".Ban") ? "Ban" : "Mute"));
                        sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', message)));
                    }
                    break;
                case "%mutes%":
                    for (int banID : mutes) {
                        message = Bungeesystem.settings.getString("BanReasons");
                        message = message.replace("%id%", banID + ".").replace("%reason%", Bungeesystem.ban.getString("BanIDs." + banID + ".Reason")).replace("%time%", (Bungeesystem.ban.getBoolean("BanIDs." + banID + ".Perma") ? "§4Permanent" : Bungeesystem.ban.getInt("BanIDs." + banID + ".Time") + " " + Bungeesystem.ban.getString("BanIDs." + banID + ".Format"))).replace("%status%", (Bungeesystem.ban.getBoolean("BanIDs." + banID + ".Ban") ? "Ban" : "Mute"));
                        sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', message)));
                    }
                    break;
                case "%permas%":
                    for (int banID : permas) {
                        message = Bungeesystem.settings.getString("BanReasons");
                        message = message.replace("%id%", banID + ".").replace("%reason%", Bungeesystem.ban.getString("BanIDs." + banID + ".Reason")).replace("%time%", (Bungeesystem.ban.getBoolean("BanIDs." + banID + ".Perma") ? "§4Permanent" : Bungeesystem.ban.getInt("BanIDs." + banID + ".Time") + " " + Bungeesystem.ban.getString("BanIDs." + banID + ".Format"))).replace("%status%", (Bungeesystem.ban.getBoolean("BanIDs." + banID + ".Ban") ? "Ban" : "Mute"));
                        sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', message)));
                    }
                    break;
                default:
                    sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', Bungeesystem.settings.getString("BanPlaceholder.line" + i))));
                    break;
            }
        }

    }

    private void sendBanHelp(CommandSender sender) {
        for (int banID : Bungeesystem.ban.getSection("BanIDs").getKeys().stream().map(Integer::parseInt).sorted().collect(Collectors.toList())) {

            String perm = Bungeesystem.ban.getString("BanIDs." + banID + ".Permission");
            if (!perm.equalsIgnoreCase("")) {
                if (!sender.hasPermission("bungeecord.*")) {
                    if (!sender.hasPermission(perm))
                        continue;
                }
            }

            String message = Bungeesystem.settings.getString("BanReasons");
            message = message.replace("%id%", banID + ".").replace("%reason%", Bungeesystem.ban.getString("BanIDs." + banID + ".Reason")).replace("%time%", (Bungeesystem.ban.getBoolean("BanIDs." + banID + ".Perma") ? "§4Permanent" : Bungeesystem.ban.getInt("BanIDs." + banID + ".Time") + " " + Bungeesystem.ban.getString("BanIDs." + banID + ".Format"))).replace("%status%", (Bungeesystem.ban.getBoolean("BanIDs." + banID + ".Ban") ? "Ban" : "Mute"));
            sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', message)));
        }

        if (Bungeesystem.ban.getSection("BanIDs").getKeys().size() == 0) {
            sender.sendMessage(new TextComponent(Bungeesystem.Prefix + Bungeesystem.fehler + "Es wurden keine Ban-IDs gefunden!"));
            sender.sendMessage(new TextComponent(Bungeesystem.Prefix + Bungeesystem.fehler + "Benutze: " + Bungeesystem.other + "/banadd <ID>"));
            return;
        }
        sender.sendMessage(new TextComponent(Bungeesystem.fehler + "Benutze: " + Bungeesystem.other + "/ban <Spieler> <Ban-ID>"));
    }
}

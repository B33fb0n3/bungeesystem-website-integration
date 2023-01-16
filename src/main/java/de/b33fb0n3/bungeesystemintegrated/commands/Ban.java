package de.b33fb0n3.bungeesystemintegrated.commands;

import de.b33fb0n3.bungeesystemintegrated.Bungeesystem;
import de.b33fb0n3.bungeesystemintegrated.utils.UUIDFetcher;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

import java.util.UUID;

public class Ban extends Command {

    public Ban(String name) {
        super(name, "", "mute");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender.hasPermission("bungeecord.ban") || sender.hasPermission("bungeecord.*")) {
            if (args.length == 2 || args.length == 3) {
                UUID ptUUID = UUIDFetcher.getUUID(args[0]);
                if (ptUUID == null) {
                    sender.sendMessage(new TextComponent(Bungeesystem.Prefix + Bungeesystem.fehler + "Dieser Spieler existiert nicht!"));
                    return;
                }

                // TODO weitermachen
            }
        } else
            sender.sendMessage(new TextComponent(Bungeesystem.noPerm));
    }
}

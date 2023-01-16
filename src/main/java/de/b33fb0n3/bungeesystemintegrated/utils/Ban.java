package de.b33fb0n3.bungeesystemintegrated.utils;

import de.b33fb0n3.bungeesystemintegrated.Bungeesystem;
import net.md_5.bungee.config.Configuration;

import javax.sql.DataSource;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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

    public Ban(UUID targetUUID, String VonName, String grund, long erstellt, long bis, int perma, int ban, String ip, String beweis) {
        this.targetUUID = targetUUID;
        this.VonName = VonName;
        this.grund = grund;
        this.erstellt = erstellt;
        this.bis = bis;
        this.perma = perma;
        this.ban = ban;
        this.ip = ip;
        this.beweis = beweis;

        this.createBan();
    }

    public CompletableFuture<Void> createBan() {
        return CompletableFuture.runAsync(() -> {
            // TODO machen
        }, Bungeesystem.getPlugin().EXECUTOR_SERVICE);
    }
}

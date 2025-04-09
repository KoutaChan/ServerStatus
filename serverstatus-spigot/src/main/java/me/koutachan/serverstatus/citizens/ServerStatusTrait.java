package me.koutachan.serverstatus.citizens;

import me.koutachan.serverstatus.ServerStatusSpigot;
import me.koutachan.serverstatus.cache.ServerStatusInfo;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.trait.HologramTrait;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.joml.Vector3d;

import java.util.Vector;

@TraitName("server-status")
public class ServerStatusTrait extends Trait {
    private HologramTrait.ArmorstandRenderer renderer;
    private String serverId;

    public ServerStatusTrait() {
        super("server-status");
    }

    public ServerStatusTrait(String serverId) {
        this();
        this.serverId = serverId;
    }

    @Override
    public void run() {
        if (renderer == null) // Renderer is not presented
            return;

        ServerStatusInfo statusInfo = ServerStatusSpigot.INSTANCE.getStatusInfo(serverId);
        if (statusInfo != null) {
            if (statusInfo.isOnline()) {
                renderer.updateText(npc, ChatColor.translateAlternateColorCodes('&', ServerStatusSpigot.INSTANCE.getConfig().getString("player-count-with-max", "&a%players%/%max-players%"))
                        .replaceAll("%players%", String.valueOf(statusInfo.getOnlinePlayers()))
                        .replaceAll("%max-players%", String.valueOf(statusInfo.getMaxPlayers()))
                        .replaceAll("%server-name%", serverId));
            } else {
                renderer.updateText(npc, ChatColor.translateAlternateColorCodes('&', ServerStatusSpigot.INSTANCE.getConfig().getString("server-offline", "&cサーバーはオフラインです")
                        .replaceAll("%server-name%", serverId)));
            }
        } else {
            renderer.updateText(npc, ChatColor.translateAlternateColorCodes('&', ServerStatusSpigot.INSTANCE.getConfig().getString("unknown-server", "&c情報を取得できませんでした")
                    .replaceAll("%server-name%", serverId)));
        }
    }

    @Override
    public void onSpawn() {
        createRenderer();
    }

    @Override
    public void onDespawn() {
        destroy();
    }

    @Override
    public void onRemove() {
        destroy();
    }

    public void destroy() {
        renderer.destroy();
    }

    public void createRenderer() {
        renderer = new HologramTrait.ArmorstandRenderer();
        renderer.updateText(npc, "ロード中");
        renderer.render(npc, new Vector3d());
    }

    @Override
    public void load(DataKey key) {
        this.serverId = key.getString("serverId");
    }

    @Override
    public void save(DataKey key) {
        key.setString("serverId", serverId);
    }
}
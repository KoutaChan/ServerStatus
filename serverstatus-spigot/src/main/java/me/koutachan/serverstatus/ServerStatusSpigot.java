package me.koutachan.serverstatus;

import me.koutachan.serverstatus.cache.ServerStatusInfo;
import me.koutachan.serverstatus.channel.MessageListener;
import me.koutachan.serverstatus.citizens.ServerStatusTrait;
import me.koutachan.serverstatus.commands.ServerStatusCommand;
import me.koutachan.serverstatus.event.EventListener;
import me.koutachan.serverstatus.task.StatusUpdateTask;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.TraitInfo;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public final class ServerStatusSpigot extends JavaPlugin {
    public static ServerStatusSpigot INSTANCE;
    public final static String CHANNEL_NAME = "serverstatus:forward";
    @Nullable
    public List<ServerStatusInfo> lastStatus;

    public Map<UUID, Consumer<NPC>> queue = new HashMap<>();

    public StatusUpdateTask task;

    @Override
    public void onLoad() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        getServer().getMessenger().registerOutgoingPluginChannel(this, CHANNEL_NAME);
        getServer().getMessenger().registerIncomingPluginChannel(this, CHANNEL_NAME, new MessageListener());
        getServer().getPluginManager().registerEvents(new EventListener(), this);

        ServerStatusCommand statusCommand = new ServerStatusCommand();
        getCommand("set-status").setExecutor(statusCommand);
        getCommand("set-status").setTabCompleter(statusCommand);

        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(ServerStatusTrait.class));

        startStatusTask();
    }

    public void startStatusTask() {
        if (task == null) {
            task = new StatusUpdateTask(getConfig().getInt("update-tick", 600));
            task.runTaskTimer(this, 1, 1);
        }
    }

    public void queueAdd(Player player, String server) {
        queue.put(player.getUniqueId(), npc -> add(player, npc, server));
    }

    public void add(Player player, NPC npc, String server) {
        ServerStatusTrait trait = npc.getTraitNullable(ServerStatusTrait.class);

        if (trait != null) {
            player.sendMessage(ChatColor.RED + "このNPCはすでに設定されているよ。");
            queueAdd(player, server);
            return;
        }

        npc.addTrait(new ServerStatusTrait(server));
        player.sendMessage(ChatColor.GREEN + "設定しました");
    }

    public void queueRemove(Player player) {
        queue.put(player.getUniqueId(), npc -> remove(player, npc));
    }

    public void remove(Player player, NPC npc) {
        ServerStatusTrait trait = npc.getTraitNullable(ServerStatusTrait.class);

        if (trait == null) {
            player.sendMessage(ChatColor.RED + "このNPCは設定されていません");
            return;
        }

        npc.removeTrait(ServerStatusTrait.class);
        player.sendMessage(ChatColor.GREEN + "削除しました");
    }

    @Nullable
    public ServerStatusInfo getStatusInfo(String serverId) {
        if (lastStatus == null)
            return null;
        return lastStatus.stream()
                .filter(info -> info.getServerName().equals(serverId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
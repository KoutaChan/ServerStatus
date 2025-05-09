package me.koutachan.serverstatus.task;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.koutachan.serverstatus.ServerStatusSpigot;
import me.koutachan.serverstatus.proxy.ProxyAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class StatusUpdateTask extends BukkitRunnable {
    private static final byte[] REQUEST_STATUS_DATA;

    static {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF("Info");
        REQUEST_STATUS_DATA = output.toByteArray();
    }

    private final int updateTick;
    private int tick;

    public StatusUpdateTask(int updateTick) {
        this.updateTick = updateTick;
        this.tick = updateTick; // Should be updated immediately next time
    }

    public boolean requestData() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendPluginMessage(ServerStatusSpigot.INSTANCE, ProxyAdapter.DEFAULT_CHANNEL, REQUEST_STATUS_DATA);
            return true;
        }
        return false;
    }

    @Override
    public void run() {
        if (tick++ >= updateTick) {
            if (requestData()) {
                tick = 0;
            }
        }
    }
}
package me.koutachan.serverstatus.channel;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import me.koutachan.serverstatus.ServerStatusSpigot;
import me.koutachan.serverstatus.cache.ServerStatusInfo;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class MessageListener implements PluginMessageListener {
    @Override
    public void onPluginMessageReceived(String s, Player player, byte[] bytes) {
        ByteArrayDataInput input = ByteStreams.newDataInput(bytes);
        String subName = input.readUTF();
        if (subName.equals("Info")) {
            ServerStatusSpigot.INSTANCE.lastStatus = ServerStatusInfo.fromList(input);
        } else {
            ServerStatusSpigot.INSTANCE.getLogger().warning("Unknown ServerStatus SubName received: '" + subName + "'");
        }
    }
}
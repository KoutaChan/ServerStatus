package me.koutachan.serverstatus.event;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import me.koutachan.serverstatus.BungeeCordUtils;
import me.koutachan.serverstatus.ServerStatusBungee;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerStatusListener implements Listener {
    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (!ServerStatusBungee.CHANNEL_NAME.equals(event.getTag())) {
            return;
        }
        Server server = BungeeCordUtils.getServer(event.getSender());
        ByteArrayDataInput input = ByteStreams.newDataInput(event.getData());
        String subName = input.readUTF();
        if (subName.equals("Info")) {
            ServerStatusBungee.INSTANCE.handleInfoRequest(server);
        } else {
            ServerStatusBungee.INSTANCE.getLogger().warning("Unknown ServerStatus SubName received: '" + subName + "' from server " + server.getInfo().getName());
        }
    }
}
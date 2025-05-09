package me.koutachan.serverstatus.event;

import me.koutachan.serverstatus.BungeeCordUtils;
import me.koutachan.serverstatus.ServerStatusBungee;
import me.koutachan.serverstatus.proxy.ProxyAdapter;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerStatusListener implements Listener {
    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (!ProxyAdapter.DEFAULT_CHANNEL.equals(event.getTag())) {
            return;
        }
        ServerStatusBungee.INSTANCE.getChannelHandler().handleData(
                BungeeCordUtils.getServer(event.getSender()).getInfo(),
                event.getData()
        );
    }
}
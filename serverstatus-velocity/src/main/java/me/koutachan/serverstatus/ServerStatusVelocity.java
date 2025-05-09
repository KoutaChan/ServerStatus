package me.koutachan.serverstatus;

import com.google.inject.Inject;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.koutachan.serverstatus.adapter.VelocityAdapter;
import me.koutachan.serverstatus.cache.proxy.ProxyAdapter;
import me.koutachan.serverstatus.cache.proxy.ProxyChannelHandler;
import org.slf4j.Logger;

import java.util.Optional;

@Plugin(id = "serverstatus-velocity", name = "ServerStatus-Velocity", version = "1.0-SNAPSHOT")
public class ServerStatusVelocity {
    public final static MinecraftChannelIdentifier IDENTIFIER = MinecraftChannelIdentifier.from(ProxyAdapter.DEFAULT_CHANNEL);

    public static ServerStatusVelocity INSTANCE;

    private final ProxyServer proxy;
    private final Logger logger;
    private final ProxyChannelHandler<RegisteredServer> channelHandler;


    @Inject
    public ServerStatusVelocity(ProxyServer proxy, Logger logger) {
        INSTANCE = this;
        this.proxy = proxy;
        this.logger = logger;
        this.channelHandler = new ProxyChannelHandler<>(new VelocityAdapter(proxy));
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        proxy.getChannelRegistrar().register(IDENTIFIER);
    }

    @Subscribe
    public void onPluginMessageEvent(PluginMessageEvent event) {
        if (!IDENTIFIER.equals(event.getIdentifier())) {
            return;
        }
        event.setResult(PluginMessageEvent.ForwardResult.handled());

        if (!(event.getSource() instanceof Player player)) {
            return;
        }
        Optional<ServerConnection> connection = player.getCurrentServer();
        connection.ifPresent(serverConnection -> channelHandler.handleData(serverConnection.getServer(), event.getData()));
    }

    public ProxyServer getProxy() {
        return proxy;
    }

    public Logger getLogger() {
        return logger;
    }
}
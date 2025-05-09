package me.koutachan.serverstatus;

import com.google.inject.Inject;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.koutachan.serverstatus.cache.proxy.ProxyChannelHandler;
import org.slf4j.Logger;

import java.util.Optional;

@Plugin(id = "serverstatus-velocity", name = "ServerStatus-Velocity", version = "1.0-SNAPSHOT")
public class ServerStatusVelocity {
    public final static String CHANNEL_NAME = "serverstatus:forward";

    public final static MinecraftChannelIdentifier IDENTIFIER = MinecraftChannelIdentifier.from(CHANNEL_NAME);

    public static ServerStatusVelocity INSTANCE;



    private final ProxyServer proxy;
    private final Logger logger;
    private ProxyChannelHandler<RegisteredServer> handler;


    @Inject
    public ServerStatusVelocity(ProxyServer proxy, Logger logger) {
        INSTANCE = this;
        this.proxy = proxy;
        this.logger = logger;
        /*this.handler = new ProxyChannelHandler<>() {
            @Override
            public void sendData(RegisteredServer server, byte[] res) {
                server.sendPluginMessage(IDENTIFIER, res);
            }
        };*/
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
        connection.ifPresent(serverConnection -> handler.handleData(serverConnection.getServer(), event.getData()));
    }

    public ProxyServer getProxy() {
        return proxy;
    }

    public Logger getLogger() {
        return logger;
    }
}
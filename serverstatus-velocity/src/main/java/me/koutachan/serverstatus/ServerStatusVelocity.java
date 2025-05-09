package me.koutachan.serverstatus;

import com.google.inject.Inject;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.koutachan.serverstatus.adapter.VelocityAdapter;
import me.koutachan.serverstatus.proxy.ProxyAdapter;
import me.koutachan.serverstatus.proxy.ProxyChannelHandler;
import me.koutachan.serverstatus.proxy.ServiceMode;
import org.slf4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Optional;

@Plugin(id = "serverstatus-velocity", name = "ServerStatus-Velocity", version = "1.0-SNAPSHOT")
public class ServerStatusVelocity {
    public final static MinecraftChannelIdentifier IDENTIFIER = MinecraftChannelIdentifier.from(ProxyAdapter.DEFAULT_CHANNEL);

    public static ServerStatusVelocity INSTANCE;

    private final ProxyServer proxy;
    private final Logger logger;
    private final ProxyChannelHandler<RegisteredServer> channelHandler;

    private final CommentedConfigurationNode configuration;

    @Inject
    public ServerStatusVelocity(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        INSTANCE = this;
        this.proxy = proxy;
        this.logger = logger;
        this.channelHandler = new ProxyChannelHandler<>(new VelocityAdapter(proxy));
        Path configPath = dataDirectory.resolve("config.yml");
        if (!configPath.toFile().exists()) {
            dataDirectory.toFile().mkdirs();
            try (InputStream inputStream = ServerStatusVelocity.class.getResourceAsStream("/config.yml")) {
                assert inputStream != null;
                Files.copy(inputStream, configPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            this.configuration = YamlConfigurationLoader.builder()
                    .path(configPath)
                    .build()
                    .load();
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        proxy.getChannelRegistrar().register(IDENTIFIER);

        initializeStatusService();
    }

    private void initializeStatusService() {
        ServiceMode mode = ServiceMode.ordinalOr(configuration.node("cacheMode").getInt() - 1, ServiceMode.REALTIME);
        switch (mode) {
            case REALTIME: {
                getLogger().info("リアルタイムモードでステータスサービスを初期化しました");
                channelHandler.initializeRealtimeService();
                break;
            }
            case CACHE: {
                getLogger().info("定期更新モードでステータスサービスを初期化しました");
                channelHandler.initializeCacheService(Duration.ofSeconds(configuration.node("updateInterval").getInt(5)));
                break;
            }
        }
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
package me.koutachan.serverstatus;

import me.koutachan.serverstatus.adapter.BungeeCordAdapter;
import me.koutachan.serverstatus.proxy.ProxyAdapter;
import me.koutachan.serverstatus.proxy.ProxyChannelHandler;
import me.koutachan.serverstatus.proxy.ServiceMode;
import me.koutachan.serverstatus.event.ServerStatusListener;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.Duration;
import java.util.logging.Level;

public final class ServerStatusBungee extends Plugin {
    public static ServerStatusBungee INSTANCE;

    private ProxyChannelHandler<ServerInfo> channelHandler;

    public Configuration configuration;

    @Override
    public void onLoad() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        makeConfig();

        try {
            this.channelHandler = new ProxyChannelHandler<>(new BungeeCordAdapter(getProxy()));
            this.configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        getProxy().registerChannel(ProxyAdapter.DEFAULT_CHANNEL);
        getProxy().getPluginManager().registerListener(this, new ServerStatusListener());
        initializeStatusService();
    }

    private void initializeStatusService() {
        ServiceMode mode = ServiceMode.ordinalOr(configuration.getInt("cacheMode") - 1, ServiceMode.REALTIME);
        switch (mode) {
            case REALTIME: {
                getLogger().info("リアルタイムモードでステータスサービスを初期化しました");
                channelHandler.initializeRealtimeService();
                break;
            }
            case CACHE: {
                getLogger().info("定期更新モードでステータスサービスを初期化しました");
                channelHandler.initializeCacheService(Duration.ofSeconds(configuration.getInt("updateInterval", 5)));
                break;
            }
        }
    }

    public void makeConfig() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Failed make config.yml!", e);
            }
        }
    }

    public ProxyChannelHandler<ServerInfo> getChannelHandler() {
        return channelHandler;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getProxy().unregisterChannel(ProxyAdapter.DEFAULT_CHANNEL);
    }
}
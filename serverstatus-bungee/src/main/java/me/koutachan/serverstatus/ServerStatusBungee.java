package me.koutachan.serverstatus;

import me.koutachan.serverstatus.cache.ServerStatusService;
import me.koutachan.serverstatus.cache.type.ServerStatusServiceCache;
import me.koutachan.serverstatus.cache.type.ServerStatusServiceImpl;
import me.koutachan.serverstatus.event.ServerStatusListener;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Level;

public final class ServerStatusBungee extends Plugin {
    public static ServerStatusBungee INSTANCE;
    public final static String CHANNEL_NAME = "serverstatus:forward";

    private ServerStatusService statusService;

    private static final int REAL_TIME_MODE = 1;
    private static final int PERIODIC_UPDATE_MODE = 2;

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
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        getProxy().registerChannel(CHANNEL_NAME);
        getProxy().getPluginManager().registerListener(this, new ServerStatusListener());

        initializeStatusService();
    }

    private void initializeStatusService() {
        int cacheMode = configuration.getInt("cacheMode", REAL_TIME_MODE);
        try {
            switch (cacheMode) {
                case REAL_TIME_MODE:
                    statusService = new ServerStatusServiceImpl();
                    getLogger().info("リアルタイムモードでステータスサービスを初期化しました");
                    break;
                case PERIODIC_UPDATE_MODE:
                    ServerStatusServiceCache serviceCache = new ServerStatusServiceCache();
                    serviceCache.startTimer(this);
                    statusService = serviceCache;
                    getLogger().info("定期更新モードでステータスサービスを初期化しました");
                    break;
                default:
                    getLogger().warning("不明なキャッシュモード: " + cacheMode + " - リアルタイムモードにフォールバックします");
                    statusService = new ServerStatusServiceImpl();
                    break;
            }
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "ステータスサービスの初期化中にエラーが発生しました", e);
            statusService = new ServerStatusServiceImpl();
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

    public void handleInfoRequest(Server server) {
        statusService.getAsByte(res -> server.sendData(CHANNEL_NAME, res));
    }

    public ServerStatusService getStatusService() {
        return statusService;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getProxy().unregisterChannel(CHANNEL_NAME);
    }
}
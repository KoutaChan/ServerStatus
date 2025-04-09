package me.koutachan.serverstatus.cache.type;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.koutachan.serverstatus.ServerStatusBungee;
import me.koutachan.serverstatus.cache.ServerStatusInfo;
import me.koutachan.serverstatus.cache.ServerStatusService;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class AbstractServerStatusService implements ServerStatusService {
    /**
     * すべてのサーバーキャッシュを更新します
     *
     * @return 更新処理の完了を表す CompletableFuture
     */
    public CompletableFuture<Void> updateCache() {
        Map<String, ServerInfo> servers = ServerStatusBungee.INSTANCE.getProxy().getServers();

        CompletableFuture<?>[] futures = servers.values().stream()
                .map(this::ping)
                .toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(futures);
    }

    /**
     * 個別のサーバーに ping を送信しキャッシュを更新します
     *
     * @param server ping を送信するサーバー
     * @return ping 処理の完了を表す CompletableFuture
     */
    protected CompletableFuture<ServerStatusInfo> ping(ServerInfo server) {
        CompletableFuture<ServerStatusInfo> future = new CompletableFuture<>();
        server.ping((ping, throwable) -> {
            ServerStatusInfo info;
            if (throwable == null) {
                info = new ServerStatusInfo(server.getName(), true, ping.getPlayers().getOnline(), ping.getPlayers().getMax());
            } else {
                info = new ServerStatusInfo(server.getName(), false, 0, 0);
            }
            future.complete(info);
        });
        return future;
    }

    public void startTimer(Plugin plugin) {
        plugin.getProxy().getScheduler().schedule(plugin, this::updateCache, 0, ServerStatusBungee.INSTANCE.configuration.getInt("updateInterval", 30), TimeUnit.SECONDS);
    }
}

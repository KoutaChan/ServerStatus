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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ServerStatusServiceCache extends AbstractServerStatusService {
    private final Map<String, ServerStatusInfo> cache = new ConcurrentHashMap<>();

    /**
     * 個別のサーバーに ping を送信しキャッシュを更新します
     *
     * @param server ping を送信するサーバー
     * @return ping 処理の完了を表す CompletableFuture
     */
    @Override
    protected CompletableFuture<ServerStatusInfo> ping(ServerInfo server) {
        CompletableFuture<ServerStatusInfo> future = super.ping(server);
        return future.thenApply(info -> cache.put(info.getServerName(), info));
    }

    public void getAsByte(Consumer<byte[]> con) {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF("Info");
        output.writeInt(cache.size());
        for (ServerStatusInfo statusInfo : cache.values()) {
            statusInfo.writeData(output);
        }
        con.accept(output.toByteArray());
    }

    @Override
    public boolean isCacheEnabled() {
        return true;
    }

    /**
     * 特定のサーバーのキャッシュ情報を取得します
     *
     * @param serverName サーバー名
     * @return キャッシュが存在する場合はキャッシュ情報、存在しない場合は null
     */
    public ServerStatusInfo getServerCache(String serverName) {
        return cache.get(serverName);
    }

    public void startTimer(Plugin plugin) {
        plugin.getProxy().getScheduler().schedule(plugin, this::updateCache, 0, ServerStatusBungee.INSTANCE.configuration.getInt("updateInterval", 30), TimeUnit.SECONDS);
    }

    /**
     * すべてのサーバーキャッシュ情報を取得します
     *
     * @return すべてのサーバーキャッシュのマップ
     */
    public Map<String, ServerStatusInfo> getAllServerCache() {
        return cache;
    }
}
package me.koutachan.serverstatus.cache.type;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.koutachan.serverstatus.ServerStatusBungee;
import me.koutachan.serverstatus.cache.ServerStatusInfo;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.logging.Level;

public class ServerStatusServiceImpl extends AbstractServerStatusService {
    public CompletableFuture<Void> updateCache() {
        throw new IllegalStateException("Cannot update cache");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void getAsByte(Consumer<byte[]> con) {
        Map<String, ServerInfo> servers = ServerStatusBungee.INSTANCE.getProxy().getServers();

        CompletableFuture<ServerStatusInfo>[] futures = servers.values().stream()
                .map(this::ping)
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures)
                .thenAccept(v -> {
                    ByteArrayDataOutput output = ByteStreams.newDataOutput();
                    output.writeUTF("Info");
                    output.writeInt(futures.length);
                    for (CompletableFuture<ServerStatusInfo> future : futures) {
                        try {
                            future.get().writeData(output);
                        } catch (InterruptedException | ExecutionException e) {
                            ServerStatusBungee.INSTANCE.getLogger().log(Level.SEVERE, "Error getting server status", e);
                        }
                    }
                    con.accept(output.toByteArray());
                });
    }

    @Override
    public boolean isCacheEnabled() {
        return false;
    }
}
package me.koutachan.serverstatus.proxy.type;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.koutachan.serverstatus.ServerStatusInfo;
import me.koutachan.serverstatus.proxy.ProxyAdapter;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class RealtimeServerStatusService<S> extends AbstractServerStatusService<S> {
    public RealtimeServerStatusService(ProxyAdapter<S> adapter) {
        super(adapter);
    }

    public CompletableFuture<Void> updateCache() {
        throw new IllegalStateException("Cannot update cache");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void getAsByte(Consumer<byte[]> con) {
        Collection<S> servers = adapter.getServers();

        CompletableFuture<ServerStatusInfo>[] futures = servers.stream()
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
                            throw new IllegalStateException("Error getting server status", e);
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

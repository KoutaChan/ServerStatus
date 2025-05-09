package me.koutachan.serverstatus.proxy.type;

import me.koutachan.serverstatus.ServerStatusInfo;
import me.koutachan.serverstatus.proxy.ProxyAdapter;
import me.koutachan.serverstatus.proxy.ServerStatusService;

import java.util.concurrent.CompletableFuture;

public abstract class AbstractServerStatusService<S> implements ServerStatusService<S> {
    protected final ProxyAdapter<S> adapter;

    public AbstractServerStatusService(ProxyAdapter<S> adapter) {
        this.adapter = adapter;
    }

    @Override
    public CompletableFuture<Void> updateCache() {
        CompletableFuture<?>[] futures = adapter.getServers()
                .stream()
                .map(this::ping)
                .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(futures);
    }

    @Override
    public CompletableFuture<ServerStatusInfo> ping(S server) {
        return adapter.ping(server);
    }
}

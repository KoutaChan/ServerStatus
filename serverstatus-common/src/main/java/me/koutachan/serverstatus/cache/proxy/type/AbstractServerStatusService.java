package me.koutachan.serverstatus.cache.proxy.type;

import me.koutachan.serverstatus.cache.ServerStatusInfo;
import me.koutachan.serverstatus.cache.proxy.ProxyAdapter;
import me.koutachan.serverstatus.cache.proxy.ServerStatusService;

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

package me.koutachan.serverstatus.proxy;

import me.koutachan.serverstatus.ServerStatusInfo;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface ProxyAdapter<S> {
    String DEFAULT_CHANNEL = "serverstatus:forward";

    void schedule(Runnable runnable, Duration duration);

    Collection<S> getServers();

    CompletableFuture<ServerStatusInfo> ping(S server);

    void sendData(S server, byte[] data);
}
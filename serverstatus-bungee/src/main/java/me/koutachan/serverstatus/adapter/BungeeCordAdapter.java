package me.koutachan.serverstatus.adapter;

import me.koutachan.serverstatus.ServerStatusBungee;
import me.koutachan.serverstatus.ServerStatusInfo;
import me.koutachan.serverstatus.proxy.ProxyAdapter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class BungeeCordAdapter implements ProxyAdapter<ServerInfo> {
    private final ProxyServer proxy;

    public BungeeCordAdapter(ProxyServer proxy) {
        this.proxy = proxy;
    }

    @Override
    public void schedule(Runnable runnable, Duration duration) {
        proxy.getScheduler().schedule(ServerStatusBungee.INSTANCE, runnable, duration.getSeconds(), TimeUnit.SECONDS);
    }

    @Override
    public Collection<ServerInfo> getServers() {
        return proxy.getServers().values();
    }

    @Override
    public CompletableFuture<ServerStatusInfo> ping(ServerInfo server) {
        CompletableFuture<ServerStatusInfo> future = new CompletableFuture<>();
        server.ping((ping, throwable) -> {
            if (throwable == null) {
                future.complete(new ServerStatusInfo(
                        server.getName(),
                        true,
                        ping.getPlayers().getOnline(),
                        ping.getPlayers().getMax()
                ));
            } else {
                future.complete(new ServerStatusInfo(server.getName(), false, 0, 0));
            }
        });
        return future;
    }

    @Override
    public void sendData(ServerInfo server, byte[] data) {
        server.sendData(DEFAULT_CHANNEL, data);
    }
}
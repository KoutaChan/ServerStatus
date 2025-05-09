package me.koutachan.serverstatus.adapter;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import me.koutachan.serverstatus.ServerStatusVelocity;
import me.koutachan.serverstatus.cache.ServerStatusInfo;
import me.koutachan.serverstatus.cache.proxy.ProxyAdapter;

import java.time.Duration;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class VelocityAdapter implements ProxyAdapter<RegisteredServer> {
    private final ProxyServer proxy;

    public VelocityAdapter(ProxyServer proxy) {
        this.proxy = proxy;
    }

    @Override
    public void schedule(Runnable runnable, Duration duration) {
        proxy.getScheduler().buildTask(ServerStatusVelocity.INSTANCE, runnable)
                .repeat(duration)
                .schedule();
    }

    @Override
    public Collection<RegisteredServer> getServers() {
        return proxy.getAllServers();
    }

    @Override
    public CompletableFuture<ServerStatusInfo> ping(RegisteredServer server) {
        return server.ping()
                .thenApply(action -> {
                    Optional<ServerPing.Players> players = action.getPlayers();
                    return new ServerStatusInfo(
                            server.getServerInfo().getName(),
                            players.isPresent(),
                            players.map(ServerPing.Players::getOnline).orElse(0),
                            players.map(ServerPing.Players::getMax).orElse(0)
                    );
                });
    }

    @Override
    public void sendData(RegisteredServer server, byte[] data) {
        server.sendPluginMessage(ServerStatusVelocity.IDENTIFIER, data);
    }
}
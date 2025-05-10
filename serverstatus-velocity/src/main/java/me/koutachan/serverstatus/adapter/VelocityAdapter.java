package me.koutachan.serverstatus.adapter;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.PingOptions;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import me.koutachan.serverstatus.ServerStatusVelocity;
import me.koutachan.serverstatus.ServerStatusInfo;
import me.koutachan.serverstatus.proxy.ProxyAdapter;

import java.time.Duration;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class VelocityAdapter implements ProxyAdapter<RegisteredServer> {
    private final ProxyServer proxy;
    private final static PingOptions PING_OPTIONS = PingOptions.builder()
            .timeout(10, TimeUnit.SECONDS)
            .build();

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
        return server.ping(PING_OPTIONS)
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
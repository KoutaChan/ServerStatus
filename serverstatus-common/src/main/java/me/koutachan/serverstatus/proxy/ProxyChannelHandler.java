package me.koutachan.serverstatus.proxy;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import me.koutachan.serverstatus.proxy.type.CacheServerStatusService;
import me.koutachan.serverstatus.proxy.type.RealtimeServerStatusService;

import java.time.Duration;

public class ProxyChannelHandler<S> {
    private ServerStatusService<S> service;
    private final ProxyAdapter<S> adapter;

    public ProxyChannelHandler(ProxyAdapter<S> adapter) {
        this.adapter = adapter;
    }

    public void handleData(S server, byte[] data) {
        if (!isServiceEnabled()) {
            return;
        }
        ByteArrayDataInput input = ByteStreams.newDataInput(data);
        if (input.readUTF().equals("Info")) {
            service.getAsByte(result -> sendData(server, result));
        } else {
            throw new IllegalStateException();
        }
    }

    private void sendData(S server, byte[] res) {
        adapter.sendData(server, res);
    }

    public void initializeCacheService(Duration duration) {
        CacheServerStatusService<S> service = new CacheServerStatusService<>(adapter);
        service.startTimer(duration);
        this.service = service;
    }

    public void initializeRealtimeService() {
        this.service = new RealtimeServerStatusService<>(adapter);
    }

    public ServerStatusService<S> getService() {
        return service;
    }

    @Deprecated
    public void setService(ServerStatusService<S> service) {
        this.service = service;
    }

    public boolean isServiceEnabled() {
        return service != null;
    }
}
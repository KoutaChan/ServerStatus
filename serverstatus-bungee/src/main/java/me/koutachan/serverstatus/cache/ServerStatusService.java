package me.koutachan.serverstatus.cache;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.koutachan.serverstatus.ServerStatusBungee;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface ServerStatusService {
    /**
     * ステータスをbyteとして取得します
     */
    void getAsByte(Consumer<byte[]> con);

    /**
     * @return キャッシュが有効になっているかどうか
     */
    boolean isCacheEnabled();
}
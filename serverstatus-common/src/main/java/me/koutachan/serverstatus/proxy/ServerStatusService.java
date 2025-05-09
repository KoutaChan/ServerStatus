package me.koutachan.serverstatus.proxy;

import me.koutachan.serverstatus.ServerStatusInfo;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface ServerStatusService<S> {
    /**
     * すべてのサーバーキャッシュを更新します
     *
     * @return 更新処理の完了を表す CompletableFuture
     */
    CompletableFuture<Void> updateCache();

    /**
     * 個別のサーバーに ping を送信しキャッシュを更新します
     *
     * @param server ping を送信するサーバー
     * @return ping 処理の完了を表す CompletableFuture
     */
    CompletableFuture<ServerStatusInfo> ping(S server);

    /**
     * ステータスをbyteとして取得します
     */
    void getAsByte(Consumer<byte[]> con);

    /**
     * @return キャッシュが有効になっているかどうか
     */
    boolean isCacheEnabled();
}
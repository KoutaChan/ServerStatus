package me.koutachan.serverstatus.cache;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import java.util.ArrayList;
import java.util.List;

public class ServerStatusInfo {
    private final String serverName;
    private final boolean online;
    private final int onlinePlayers;
    private final int maxPlayers;

    public ServerStatusInfo(String serverName, boolean online, int onlinePlayers, int maxPlayers) {
        this.serverName = serverName;
        this.online = online;
        this.onlinePlayers = onlinePlayers;
        this.maxPlayers = maxPlayers;
    }

    public String getServerName() {
        return serverName;
    }

    public int getOnlinePlayers() {
        return onlinePlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public boolean isOnline() {
        return online;
    }

    public void writeData(ByteArrayDataOutput output) {
        output.writeUTF(serverName);
        output.writeBoolean(online);
        if (online) { // データ効率化のため、プレイヤーがオンラインのときのみ送る
            output.writeInt(onlinePlayers);
            output.writeInt(maxPlayers);
        }
    }

    public static List<ServerStatusInfo> fromList(ByteArrayDataInput input) {
        int size = input.readInt();
        List<ServerStatusInfo> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(fromData(input));
        }
        return list;
    }

    public static ServerStatusInfo fromData(ByteArrayDataInput input) {
        String serverName = input.readUTF();
        boolean online = input.readBoolean();
        int onlinePlayers = online ? input.readInt() : 0;
        int maxPlayers = online ? input.readInt() : 0;

        return new ServerStatusInfo(serverName, online, onlinePlayers, maxPlayers);
    }
}
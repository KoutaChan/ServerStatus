package me.koutachan.serverstatus;

import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

public class BungeeCordUtils {
    public static Server getServer(Connection connection) {
        return connection instanceof Server
                ? (Server) connection
                : ((ProxiedPlayer) connection).getServer();
    }
}
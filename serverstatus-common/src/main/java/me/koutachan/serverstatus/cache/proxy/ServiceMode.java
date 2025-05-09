package me.koutachan.serverstatus.cache.proxy;

public enum ServiceMode {
    REALTIME,
    CACHE;

    private final static ServiceMode[] VALUES = values();

    public static ServiceMode ordinalOr(int ordinal, ServiceMode def) {
        return ordinal >= VALUES.length ? def : VALUES[ordinal];
    }
}
package me.koutachan.serverstatus.proxy;

public enum ServiceMode {
    REALTIME,
    CACHE;

    private final static ServiceMode[] VALUES = values();

    public static ServiceMode ordinalOr(int ordinal, ServiceMode def) {
        return ordinal >= VALUES.length || ordinal < 0 ? def : VALUES[ordinal];
    }
}
package com.onewhohears.distant_players.client.core;

public final class DPClientManager {

    public void handleRenderPlayerPacket() {

    }

    private static DPClientManager INSTANCE;

    public static void init() {
        INSTANCE = new DPClientManager();
    }

    public static DPClientManager get() {
        return INSTANCE;
    }

    private DPClientManager() {}

}

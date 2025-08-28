package com.onewhohears.distant_players.fabric.client;

import com.onewhohears.distant_players.client.event.fabric.DPClientEventHandlersFabric;
import net.fabricmc.api.ClientModInitializer;

public final class DistantPlayersModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DPClientEventHandlersFabric.init();
    }
}

package com.onewhohears.distant_players.fabric;

import com.onewhohears.distant_players.DistantPlayersMod;
import com.onewhohears.distant_players.common.event.fabric.DPCommonEventHandlersFabric;
import net.fabricmc.api.ModInitializer;

public final class DistantPlayersModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        DistantPlayersMod.init();
        DPCommonEventHandlersFabric.init();
    }
}

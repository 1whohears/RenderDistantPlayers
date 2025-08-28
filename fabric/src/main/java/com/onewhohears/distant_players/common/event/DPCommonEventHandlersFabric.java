package com.onewhohears.distant_players.common.event;

import com.onewhohears.distant_players.common.core.DPServerManager;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;

public class DPCommonEventHandlersFabric {

    public static void init() {
        EntityTrackingEvents.START_TRACKING.register((entity, player) -> {
            DPServerManager.get().onPlayerStartTrack(player, entity);
        });
        EntityTrackingEvents.STOP_TRACKING.register((entity, player) -> {
            DPServerManager.get().onPlayerStopTrack(player, entity);
        });
    }

}

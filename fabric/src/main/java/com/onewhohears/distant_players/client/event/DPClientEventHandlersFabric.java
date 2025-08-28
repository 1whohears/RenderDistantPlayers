package com.onewhohears.distant_players.client.event;

import com.onewhohears.distant_players.client.core.DPClientManager;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class DPClientEventHandlersFabric {

    public static void init() {
        WorldRenderEvents.LAST.register(DPClientEventHandlersFabric::onRenderLevel);
    }

    public static void onRenderLevel(WorldRenderContext context) {
        DPClientManager.get().renderTargets(context.matrixStack(), context.camera(), context.tickDelta());
    }

}

package com.onewhohears.distant_players.client.event;

import com.onewhohears.distant_players.DistantPlayersMod;
import com.onewhohears.distant_players.client.core.DPClientManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DistantPlayersMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class DPClientEventHandlersForge {

    /**
     * Executes rendering of distant entities on the correct matrix stack.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) return;
        DPClientManager.get().renderTargets(event.getPoseStack(), event.getCamera(), event.getPartialTick());
    }

    @SubscribeEvent
    public static void onEntityJoinLevelEvent(EntityJoinLevelEvent event) {
        DPClientManager.get().removeTarget(event.getEntity().getId());
    }

}

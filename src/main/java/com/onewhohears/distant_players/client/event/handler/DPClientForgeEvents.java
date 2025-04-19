package com.onewhohears.distant_players.client.event.handler;

import com.onewhohears.distant_players.DistantPlayersMod;
import com.onewhohears.distant_players.client.core.DPClientManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DistantPlayersMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class DPClientForgeEvents {
    /**
     * Executes mod logic at the end of every (client) tick.
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        DPClientManager.get().tick();
    }

    /**
     * Executes rendering of distant entities on the correct matrix stack.
     */
    @SubscribeEvent()
    public static void getViewMatrices(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) return;
        event.getPoseStack().pushPose();
        DPClientManager.get().renderTargets(event.getPoseStack(), event.getCamera(), event.getPartialTick());
        event.getPoseStack().popPose();
    }

}

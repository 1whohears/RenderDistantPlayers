package com.onewhohears.distant_players.common.event.handler;

import com.onewhohears.distant_players.DistantPlayersMod;
import com.onewhohears.distant_players.common.core.DPServerManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DistantPlayersMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DPCommonForgeEvents {
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void startTrackingEvent(PlayerEvent.StartTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getTarget() instanceof ServerPlayer target)) return;

        DPServerManager.get().addEntityToPlayerView(player, target);
        DPServerManager.get().removeEntityFromVisibility(player, target);
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void stopTrackingEvent(PlayerEvent.StopTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getTarget() instanceof ServerPlayer target)) return;
        DPServerManager.get().removeEntityFromPlayerView(player, target);
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void playerLogIn(PlayerEvent.PlayerLoggedInEvent event) {
        DPServerManager.get().onPlayerLogIn(event.getEntity());
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void playerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        DPServerManager.get().onPlayerLogOut(player);
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void serverStarted(ServerStartedEvent event) {
        DPServerManager.init();
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void serverTick(TickEvent.ServerTickEvent event) {
        DPServerManager.get().tick(event.getServer());
    }
}

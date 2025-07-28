package com.onewhohears.distant_players.common.event.handler;

import com.onewhohears.distant_players.DistantPlayersMod;
import com.onewhohears.distant_players.common.core.DPServerManager;
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
        if (event.getEntity().getLevel().isClientSide()) return;
        DPServerManager.get().onPlayerStartTrack(event.getEntity(), event.getTarget());
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void stopTrackingEvent(PlayerEvent.StopTracking event) {
        if (event.getEntity().getLevel().isClientSide()) return;
        DPServerManager.get().onPlayerStopTrack(event.getEntity(), event.getTarget());
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void playerLogIn(PlayerEvent.PlayerLoggedInEvent event) {
        DPServerManager.get().onPlayerLogIn(event.getEntity());
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void playerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        DPServerManager.get().onPlayerLogOut(event.getEntity());
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

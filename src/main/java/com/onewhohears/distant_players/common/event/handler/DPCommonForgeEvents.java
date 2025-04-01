package com.onewhohears.distant_players.common.event.handler;

import com.onewhohears.distant_players.DistantPlayersMod;
import com.onewhohears.onewholibs.util.UtilEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DistantPlayersMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DPCommonForgeEvents {

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void startTrackingEvent(PlayerEvent.StartTracking event) {
        if (!UtilEntity.isPlayer(event.getTarget())) return;
        System.out.println("player "+event.getEntity()+" start tracking "+event.getTarget());
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void stopTrackingEvent(PlayerEvent.StopTracking event) {
        if (!UtilEntity.isPlayer(event.getTarget())) return;
        System.out.println("player "+event.getEntity()+" stop tracking "+event.getTarget());
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void playerLogIn(PlayerEvent.PlayerLoggedInEvent event) {

    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void playerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {

    }

}

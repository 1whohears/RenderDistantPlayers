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
    // TODO - Some system to prevent overzealous sending of packets (for the sake of performance)
    //  Perhaps distances exceeding some amount? Maybe based on client requests for info? Since armour and items do not
    //  render properly, this data must be sent to clients... I don't want this to be done if not necessary - for the
    //  sake of performance. If it's more expensive to implement a comprehensive system, it's either we choose a
    //  crappier way or take the performance hit. This is why I got rid of the previous system, its goal was to hide
    //  players at the expense of performance.
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void startTrackingEvent(PlayerEvent.StartTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getTarget() instanceof ServerPlayer target)) return;

        DPServerManager.get().removeEntityFromPlayerView(player, target);
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void stopTrackingEvent(PlayerEvent.StopTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getTarget() instanceof ServerPlayer target)) return;

        DPServerManager.get().addEntityToPlayerView(player, target);
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void playerLogIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        int serverViewDistance = player.server.getPlayerList().getViewDistance();

        player.getLevel().players().forEach(
                otherPlayer -> {
                    if (player.equals(otherPlayer)) return;
                    if (player.distanceToSqr(otherPlayer) <= (serverViewDistance ^ 2)) return;

                    DPServerManager.get().addEntityToPlayerView(otherPlayer, player);
                    DPServerManager.get().addEntityToPlayerView(player, otherPlayer);
                }
        );
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void playerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        DPServerManager.get().removeAllEntitiesFromView(player);

        player.getLevel().players().forEach(
                otherPlayer -> DPServerManager.get().removeEntityFromPlayerView(otherPlayer, player)
        );
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

package com.onewhohears.distant_players.common.event.handler;

import com.mojang.logging.LogUtils;
import com.onewhohears.distant_players.DistantPlayersMod;
import com.onewhohears.distant_players.common.core.DPServerManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod.EventBusSubscriber(modid = DistantPlayersMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DPCommonForgeEvents {
    public static Logger LOGGER = LogUtils.getLogger();

    // TODO - Some system to prevent overzealous sending of packets (for the sake of performance)
    //  Perhaps distances exceeding some amount? Maybe based on client requests for info? Since armour and items do not
    //  render properly, this data must be sent to clients... I don't want this to be done if not necessary - for the
    //  sake of performance. If it's more expensive to implement a comprehensive system, it's either we choose a
    //  crappier way or take the performance hit. This is why I got rid of the previous system, its goal was to hide
    //  players at the expense of performance.
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void startTrackingEvent(PlayerEvent.StartTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        if (event.getTarget() instanceof ServerPlayer || DPServerManager.get().isPlayerlessEntity(event.getTarget()))
            DPServerManager.get().removeEntityFromPlayerView(player, event.getTarget());
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void stopTrackingEvent(PlayerEvent.StopTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;
        if (event.getTarget() instanceof ServerPlayer || DPServerManager.get().isPlayerlessEntity(event.getTarget()))
            DPServerManager.get().addEntityToPlayerView(player, event.getTarget());
    }

    // FIXME - This doesn't feel very robust.
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void entityRemovedEvent(EntityLeaveLevelEvent event) {
        if (!(event.getEntity().level instanceof ServerLevel level) || event.getEntity() instanceof Player) return;

        DPServerManager.get().removePlayerlessEntity(event.getEntity());
        level.players().forEach(
                player -> DPServerManager.get().removeEntityFromPlayerView(player, event.getEntity())
        );
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

        for (int id : DPServerManager.get().getPlayerlessEntitiesCopy(player.getLevel())) {
            Entity toAdd = player.getLevel().getEntity(id);
            if (toAdd == null) {
                LOGGER.error("Skipping null Entity with ID {}", id);
                continue;
            }

            DPServerManager.get().addEntityToPlayerView(player, toAdd);
        }
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

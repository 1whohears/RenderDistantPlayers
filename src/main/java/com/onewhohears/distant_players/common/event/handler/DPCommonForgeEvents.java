package com.onewhohears.distant_players.common.event.handler;

import com.onewhohears.distant_players.DistantPlayersMod;
import com.onewhohears.distant_players.common.core.DPServerManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DistantPlayersMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DPCommonForgeEvents {
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void entityRemovedEvent(EntityLeaveLevelEvent event) {
        if (event.getEntity().level.isClientSide) return;
        if (Entity.RemovalReason.UNLOADED_TO_CHUNK.equals(event.getEntity().getRemovalReason())) return;
        DPServerManager.get().removeEntityFromDistantView(event.getEntity());
    }

    @SubscribeEvent
    public static void playerClonedEvent(PlayerEvent.Clone event) {
        if (!(event.getOriginal() instanceof ServerPlayer original)) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        DPServerManager.get().removeEntityFromDistantView(original);
        DPServerManager.get().addEntityToPlayerView(player);
    }

    // this is used over PlayerLoggedInEvent to account for players switching dimensions
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void entityAddedEvent(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        DPServerManager.get().addEntityToPlayerView(player);
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void serverStarted(ServerStartedEvent event) {
        DPServerManager.init();
    }
}

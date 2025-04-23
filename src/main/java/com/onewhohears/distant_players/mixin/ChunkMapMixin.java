package com.onewhohears.distant_players.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.onewhohears.distant_players.common.command.DPGameRules;
import com.onewhohears.distant_players.common.core.DPServerManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {
    @WrapOperation(
            method = "addEntity",
            at = @At(value = "NEW", target = "(Lnet/minecraft/server/level/ChunkMap;Lnet/minecraft/world/entity/Entity;IIZ)Lnet/minecraft/server/level/ChunkMap$TrackedEntity;")
    )
    private ChunkMap.TrackedEntity changeEffectiveRange(
            ChunkMap this$0,
            Entity pEntity, int pRange, int pUpdateInterval, boolean pTrackDelta,
            Operation<ChunkMap.TrackedEntity> original
    ) {
        // TODO - maybe a data-driven array is loaded here and checks against entity types? (to allow making any entity
        //  type render at a distance?) Is DPServerManager even necessary at this point?
        if (!(pEntity instanceof ServerPlayer) && !DPServerManager.get().isInDistantView(pEntity))
            return original.call(this$0, pEntity, pRange, pUpdateInterval, pTrackDelta);

        MinecraftServer server = pEntity.getServer();
        // this should never be null; this class is only instantiated with serversided entities
        assert server != null;

        return original.call(
                this$0, pEntity, DPGameRules.getMaxDistance(server) * 16, pUpdateInterval, pTrackDelta
        );
    }
}

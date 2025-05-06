package com.onewhohears.distant_players.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.onewhohears.distant_players.common.command.DPGameRules;
import com.onewhohears.distant_players.common.core.DPServerManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;

@Mixin(targets = "net.minecraft.server.level.ChunkMap$TrackedEntity")
public abstract class TrackedEntityMixin {
    @Final
    @Shadow
    Entity entity;

    /*
        This still might affect anything else mixing into here, but this is still far less intrusive than
        trying to operate on the local double d0.
     */
    @WrapOperation(
            method = "updatePlayer",
            at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I")
    )
    private int updatePlayerIncreasedDistance(int a, int b, Operation<Integer> original) {
        // preserve side effects of other mixins
        int ogReturn = original.call(a, b);

        if (
                !(this.entity instanceof ServerPlayer)
                        && !DPServerManager.get().isInDistantView(this.entity)
                        && this.entity.hasPassenger(
                                passenger ->
                                        passenger != null
                                        && (passenger instanceof ServerPlayer
                                        || DPServerManager.get().isInDistantView(passenger))
                )
        ) {
            return ogReturn;
        }

        return Math.max(a, b);
    }

    @WrapOperation(
            method = "getEffectiveRange",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/EntityType;clientTrackingRange()I"
            )
    )
    private int increaseEffectiveRange(
            EntityType<?> instance, Operation<Integer> original,
            @Local LocalRef<Entity> entity
    ) {
        // preserve side effects of other mixins
        int og = original.call(instance);
        if (!(entity.get() instanceof ServerPlayer) && !DPServerManager.get().isInDistantView(entity.get())) return og;
        return DPGameRules.getMaxDistance(Objects.requireNonNull(entity.get().getServer()));
    }
}

package com.onewhohears.distant_players.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {
    @WrapOperation(
            method = "addEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;clientTrackingRange()I")
    )
    private int changeEffectiveRange(EntityType<?> instance, Operation<Integer> original) {
        // preserve side effects
        int ogResult = original.call(instance);
        // FIXME - updates do not always occur when they should
        // TODO - maybe a data-driven array is loaded here and checks against entity types? (to allow making any entity
        //  type render at a distance?) Is DPServerManager even necessary at this point?
        return ogResult;
    }
}

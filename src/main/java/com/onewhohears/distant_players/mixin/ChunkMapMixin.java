package com.onewhohears.distant_players.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.onewhohears.distant_players.common.command.DPGameRules;
import com.onewhohears.distant_players.common.core.DPServerManager;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;

@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {
    @WrapOperation(
            method = "addEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;clientTrackingRange()I")
    )
    private int changeEffectiveRange(
            EntityType<?> instance, Operation<Integer> original,
            @Local(argsOnly = true) LocalRef<Entity> entityRef
    ) {
        // preserve side effects from other mixins
        int og = original.call(instance);

        // TODO - maybe a data-driven array is loaded here and checks against entity types? (to allow making any entity
        //  type render at a distance?) Is using events even necessary at that point?
        if (!(entityRef.get() instanceof ServerPlayer) && !DPServerManager.get().isInDistantView(entityRef.get()))
            return og;

        return DPGameRules.getMaxDistance(Objects.requireNonNull(entityRef.get().getServer()));
    }
}

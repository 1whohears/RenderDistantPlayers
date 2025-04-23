package com.onewhohears.distant_players.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.onewhohears.distant_players.common.command.DPGameRules;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.server.level.ChunkMap$TrackedEntity")
public abstract class TrackedEntityMixin {
    @Final
    @Shadow
    Entity entity;

    @Shadow
    protected abstract int scaledRange(int i);

    /*
        This still might affect anything else mixing into here, but this is still far less brittle & intrusive than
        trying to operate on the local double d0.
     */
    @WrapOperation(
            method = "updatePlayer",
            at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I")
    )
    private int changeEffectiveRange(int a, int b, Operation<Integer> original) {
        // preserve side effects of other mixins
        int ogReturn = original.call(a, b);
        // this should never be null; this class is only instantiated with serversided entities
        assert this.entity.getServer() != null;
        int maxRange = DPGameRules.getMaxDistance(this.entity.getServer()) * 16;

        if (a < this.scaledRange(maxRange) && b < maxRange) return ogReturn;

        return maxRange;
    }
}

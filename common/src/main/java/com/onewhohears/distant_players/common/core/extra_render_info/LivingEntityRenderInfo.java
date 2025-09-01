package com.onewhohears.distant_players.common.core.extra_render_info;

import com.mojang.blaze3d.vertex.PoseStack;
import com.onewhohears.distant_players.mixin.EntityAccess;
import com.onewhohears.distant_players.mixin.LivingEntityAccess;
import com.onewhohears.onewholibs.util.math.QuaternionF;
import com.onewhohears.onewholibs.util.math.UtilAngles;
import com.onewhohears.onewholibs.util.math.Vec3f;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class LivingEntityRenderInfo implements ExtraRenderTargetInfo {

    @NotNull private ItemStack mainHand = ItemStack.EMPTY;
    @NotNull private ItemStack offHand = ItemStack.EMPTY;
    @NotNull private Pose pose = Pose.STANDING;
    private boolean fallFlying;

    @Override
    public void tickFakeEntity(@NotNull Entity entity) {
        if (!(entity instanceof LivingEntity living)) return;
        living.calculateEntityAnimation(fallFlying);
        if (fallFlying) ((LivingEntityAccess)living).setFallFlyTicks(living.getFallFlyingTicks() + 1);
        else ((LivingEntityAccess)living).setFallFlyTicks(0);
    }

    @Override
    public void updateFakeEntity(@NotNull Entity entity) {
        if (!(entity instanceof LivingEntity living)) return;
        living.setItemInHand(InteractionHand.MAIN_HAND, mainHand);
        living.setItemInHand(InteractionHand.OFF_HAND, offHand);
        ((EntityAccess)entity).invokeSetSharedFlag(7, fallFlying);
        living.setPose(pose);
    }

    @Override
    public void setupEntityOnCreate(@NotNull Entity entity) {

    }

    @Override
    public void getInfoServerSide(@NotNull Entity entity) {
        if (!(entity instanceof LivingEntity living)) return;
        mainHand = living.getMainHandItem();
        offHand = living.getOffhandItem();
        fallFlying = living.isFallFlying();
        pose = living.getPose();
    }

    @Override
    public void getInfoClientSide(FriendlyByteBuf buffer) {
        mainHand = buffer.readItem();
        offHand = buffer.readItem();
        fallFlying = buffer.readBoolean();
        pose = buffer.readEnum(Pose.class);
    }

    @Override
    public void encodeInfoServerSide(FriendlyByteBuf buffer) {
        buffer.writeItem(mainHand);
        buffer.writeItem(offHand);
        buffer.writeBoolean(fallFlying);
        buffer.writeEnum(pose);
    }

    @Override
    public Vec3 onRender(@NotNull Entity entity, PoseStack poseStack, Camera camera, float yaw,
                         Vec3 renderDisplacement, float partialTick, MultiBufferSource buffer, int packedLight) {
        renderDisplacement = ExtraRenderTargetInfo.super.onRender(entity, poseStack, camera, yaw,
                renderDisplacement, partialTick, buffer, packedLight);
        if (fallFlying) {
            QuaternionF rollQ = Vec3f.ZP.rotationDegrees(yaw);
            poseStack.mulPose(rollQ.convert());
            rollQ.conj();
            renderDisplacement = UtilAngles.rotateVector(renderDisplacement, rollQ);
        }
        return renderDisplacement;
    }
}

package com.onewhohears.distant_players.client.event.handler;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.onewhohears.distant_players.DistantPlayersMod;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DistantPlayersMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class DPClientForgeEvents {

    private static Matrix4f viewMat = new Matrix4f();
    private static Matrix4f projMat = new Matrix4f();

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void getViewMatrices(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) return;
        Minecraft m = Minecraft.getInstance();
        Vec3 view = m.gameRenderer.getMainCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-view.x, -view.y, -view.z);
        viewMat = poseStack.last().pose();
        projMat = event.getProjectionMatrix();
        poseStack.popPose();
    }

    public static Matrix4f getViewMatrix() {
        return viewMat;
    }

    public static Matrix4f getProjMatrix() {
        return projMat;
    }

}

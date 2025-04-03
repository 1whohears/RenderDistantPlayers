package com.onewhohears.distant_players.client.core;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.onewhohears.onewholibs.util.math.UtilAngles;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class DPClientManager {

    public static final long MAX_TARGET_AGE = 1100;
    public static final double R_RENDER = 32;

    private final IntObjectMap<RenderTargetInfo> targets = new IntObjectHashMap<>();

    public void handleRenderPlayerPacket(RenderTargetInfo info) {
        if (!targets.containsKey(info.getId())) targets.put(info.getId(), info);
        else targets.get(info.getId()).update(info);
    }

    public void onRender(PoseStack poseStack, Camera camera, float partialTick) {
        Minecraft m = Minecraft.getInstance();
        MultiBufferSource.BufferSource buffer = m.renderBuffers().bufferSource();
        targets.forEach((id, info) -> {
            poseStack.pushPose();
            Entity fake = info.getFakeEntity();
            if (fake == null) return;
            int packedLight = m.getEntityRenderDispatcher().getPackedLightCoords(fake, partialTick);
            double dx = Mth.lerp(partialTick, fake.xOld, fake.getX());
            double dy = Mth.lerp(partialTick, fake.yOld, fake.getY());
            double dz = Mth.lerp(partialTick, fake.zOld, fake.getZ());
            float f = Mth.lerp(partialTick, fake.yRotO, fake.getYRot());
            Vec3 dist = new Vec3(dx, dy, dz).subtract(camera.getPosition());
            float scale = (float) (R_RENDER / dist.length());
            poseStack.scale(scale, scale, scale);
            Vec3 d = dist.normalize().scale(R_RENDER/scale);

            Quaternion yawQ = Vector3f.YN.rotationDegrees(f);
            poseStack.mulPose(yawQ);
            yawQ.conj();
            d = UtilAngles.rotateVector(d, yawQ);

            m.getEntityRenderDispatcher().render(fake, d.x, d.y, d.z, f, partialTick, poseStack, buffer, packedLight);
            poseStack.popPose();
        });
    }

    public void onTick() {
        long currentTime = System.currentTimeMillis();
        targets.entrySet().removeIf(entry->currentTime-entry.getValue().getLastUpdateTime() > MAX_TARGET_AGE);
        targets.forEach((id, info) -> info.tick());
    }

    private static DPClientManager INSTANCE;

    public static void init() {
        INSTANCE = new DPClientManager();
    }

    public static DPClientManager get() {
        return INSTANCE;
    }

    private DPClientManager() {}
}

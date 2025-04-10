package com.onewhohears.distant_players.client.core;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.onewhohears.onewholibs.util.UtilEntity;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;

public final class DPClientManager {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final long MAX_TARGET_AGE = 500;

    private final IntObjectMap<RenderTargetInfo> targets = new IntObjectHashMap<>();
    private final Set<String> bannedEntityTypes = new HashSet<>();

    public void handleRenderPlayerPacket(RenderTargetInfo info) {
        if (!targets.containsKey(info.getId())) targets.put(info.getId(), info);
        else targets.get(info.getId()).update(info);
    }

    public void onRender(PoseStack poseStack, Camera camera, float partialTick) {
        Minecraft m = Minecraft.getInstance();
        MultiBufferSource.BufferSource buffer = m.renderBuffers().bufferSource();
        double renderRadius = getRenderRadius(m);
        targets.forEach((id, info) -> {
            poseStack.pushPose();
            Entity fake = info.getFakeEntity();
            if (fake == null) return;

            int packedLight = m.getEntityRenderDispatcher().getPackedLightCoords(fake, partialTick);
            double dx = Mth.lerp(partialTick, fake.xOld, fake.getX());
            double dy = Mth.lerp(partialTick, fake.yOld, fake.getY());
            double dz = Mth.lerp(partialTick, fake.zOld, fake.getZ());
            float f = Mth.lerp(partialTick, fake.yRotO, fake.getYRot());
            Vec3 camPos = camera.getPosition();
            Vec3 dist = new Vec3(dx, dy, dz).subtract(camPos);
            float scale = (float) (renderRadius / dist.length());
            poseStack.scale(scale, scale, scale);
            Vec3 d = dist.normalize().scale(renderRadius/scale);

            if (info.getExtraInfo() != null) d = info.getExtraInfo().onRender(fake, poseStack, camera,
                    f, d, partialTick, buffer, packedLight);

            try {
                m.getEntityRenderDispatcher().render(fake, d.x, d.y, d.z, f, partialTick, poseStack, buffer, packedLight);
            } catch (ReportedException e) {
                banEntityType(fake);
                info.setInvalidEntityType();
                LOGGER.error("Attempted to render a fake entity and an error was thrown. " +
                        "Will not try to render this entity type until the game is reloaded. " +
                        "The error that would have crashed the game is the following.");
                LOGGER.error(e.getReport().getFriendlyReport());
            }
            poseStack.popPose();
        });
    }

    private double getRenderRadius(Minecraft m) {
        int renderDist = m.options.getEffectiveRenderDistance();
        return Math.max(8, renderDist*8-8);
    }

    public void onTick() {
        long currentTime = System.currentTimeMillis();
        targets.entrySet().removeIf(entry->currentTime-entry.getValue().getLastUpdateTime() > MAX_TARGET_AGE);
        targets.forEach((id, info) -> info.tick());
    }

    public boolean isEntityTypeBanned(String type) {
        return bannedEntityTypes.contains(type);
    }

    public void banEntityType(Entity entity) {
        bannedEntityTypes.add(UtilEntity.getEntityTypeId(entity));
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

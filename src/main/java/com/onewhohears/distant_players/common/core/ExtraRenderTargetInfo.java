package com.onewhohears.distant_players.common.core;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.onewhohears.onewholibs.util.math.UtilAngles;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * This interface provides an abstraction layer for attaching relevant data to entities which may have special
 * rendering requirements. Pass your instance of <code>this</code> to
 * {@link ExtraInfoManager#register(EntityType, Supplier)} prior to the
 * {@link net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent} to attach that data to an entity.
 */
public interface ExtraRenderTargetInfo {
    /**
     * will run every tick. use this to update the entity's state in between
     * {@link #updateFakeEntity(Entity)} calls.
     */
    void tickFakeEntity(@NotNull Entity entity);

    /**
     * if the server determines the client can still see this entity,
     * this function is used to sync the server side state with the fake client side state.
     * the server owner may choose to not send these update packets every tick based on
     * the distant_players:posUpdateRate gamerule.
     */
    void updateFakeEntity(@NotNull Entity entity);

    /**
     * run any other initial setup functions for the entity on the client side.
     * gets called right after the entity is created from the EntityType#create method.
     */
    void setupEntityOnCreate(@NotNull Entity entity);

    /**
     * use this function to save current state values from the server side entity to fields in the
     * implemented class. These fields will then be encoded in {@link #encodeInfoServerSide(FriendlyByteBuf)}
     */
    void getInfoServerSide(@NotNull Entity entity);

    /**
     * this function saves current state values to fields in the implemented class.
     * This function should mirror {@link #encodeInfoServerSide(FriendlyByteBuf)} so that
     * all values that are sent from the server are read here.
     */
    void getInfoClientSide(FriendlyByteBuf buffer);

    /**
     * encode the fields saved in {@link #getInfoServerSide(Entity)} so they can be sent to the
     * client side and read in {@link #getInfoClientSide(FriendlyByteBuf)}
     */
    void encodeInfoServerSide(FriendlyByteBuf buffer);

    /**
     * Used to change the rendered direction of the entity, and any other custom rendering.
     * The default implementation just changes the yaw of the entity.
     * So an example custom implementation could be the same as the default but yaw += 180.
     * Some entity models like the boat don't look in the same direction as the player model.
     *
     * @param entity             the fake entity
     * @param poseStack          pop and push are called around this function. should not be needed in your implementation
     * @param camera             the client's camera
     * @param yaw                the current interpolated yaw of the entity based on partialTicks
     * @param renderDisplacement where the entity is rendered in relation to the camera
     * @param partialTick        between 0 and 1, how far in between ticks
     * @param buffer             a buffer source for additional custom rendering
     * @param packedLight        light information
     * @return most of the time just return renderDisplacement. However, if the poseStack is modified
     * then renderDisplacement will need to be modified by the inverse of that matrix change.
     */
    default Vec3 onRender(@NotNull Entity entity, PoseStack poseStack, Camera camera, float yaw, Vec3 renderDisplacement,
                          float partialTick, MultiBufferSource buffer, int packedLight) {
        Quaternion yawQ = Vector3f.YN.rotationDegrees(yaw);
        poseStack.mulPose(yawQ);
        yawQ.conj();
        return UtilAngles.rotateVector(renderDisplacement, yawQ);
    }

    class DefaultRenderInfo implements ExtraRenderTargetInfo {
        @Override
        public void tickFakeEntity(@NotNull Entity entity) {}
        @Override
        public void updateFakeEntity(@NotNull Entity entity) {}
        @Override
        public void setupEntityOnCreate(@NotNull Entity entity) {}
        @Override
        public void getInfoServerSide(@NotNull Entity entity) {}
        @Override
        public void getInfoClientSide(FriendlyByteBuf buffer) {}
        @Override
        public void encodeInfoServerSide(FriendlyByteBuf buffer) {}
    }
}

package com.onewhohears.distant_players.common.core;

import com.onewhohears.distant_players.client.core.DPClientManager;
import com.onewhohears.onewholibs.util.UtilEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Data-carrying class intended to ferry information about an entity from serverside to clientside for the
 * purposes of rendering. Used to update entities on clients.
 */
public class RenderTargetInfo {
    private final int id;
    private final UUID uuid;
    private final String entityTypeId;
    @Nullable private String vehicleTypeId;
    private String name;
    private Vec3 pos;
    private Vec3 move;
    private float xRot, yRot;
    private long lastUpdateTime = System.currentTimeMillis();
    @Nullable private Entity entity;
    private boolean invalidEntityType = false;
    @Nullable private ExtraRenderTargetInfo extraInfo;
    private String prevExtraInfoId = "";

    public void tickFakeEntity(@NotNull Entity entity) {
        entity.setOldPosAndRot();
        entity.setPos(entity.position().add(getMove()));
        if (extraInfo != null) extraInfo.tickFakeEntity(entity);
    }

    public void updateFakeEntity(@NotNull Entity entity) {
        entity.setOldPosAndRot();
        entity.setPos(getPos());
        entity.setXRot(getXRot());
        entity.setYRot(getYRot());
        entity.setDeltaMovement(getMove());
        if (extraInfo != null) extraInfo.updateFakeEntity(entity);
    }

    @Nullable
    public Entity getFakeEntity(DPClientManager clientManager) {
        if (invalidEntityType) return null;
        if (didEntityChange()) {
            entity = clientManager.createFakeEntity(this);
            return entity;
        }
        return entity;
    }

    private boolean didEntityChange() {
        if (entity == null) return true;
        String type = UtilEntity.getEntityTypeId(entity);
        if (vehicleTypeId != null && !type.equals(vehicleTypeId)) return true;
        return vehicleTypeId == null && !type.equals(entityTypeId);
    }

    public void update(RenderTargetInfo newest, DPClientManager clientManager) {
        lastUpdateTime = System.currentTimeMillis();
        name = newest.name;
        pos = newest.pos;
        move = newest.move;
        xRot = newest.xRot;
        yRot = newest.yRot;
        vehicleTypeId = newest.vehicleTypeId;
        extraInfo = newest.extraInfo;
        Entity fake = getFakeEntity(clientManager);
        if (fake != null) updateFakeEntity(fake);
    }

    private void updateExtraInfo() {
        String id = getVehicleOrEntityTypeId();
        if (extraInfo == null || !id.equals(prevExtraInfoId)) {
            extraInfo = ExtraInfoManager.get(id);
            prevExtraInfoId = id;
        }
    }

    public String getVehicleOrEntityTypeId() {
        return vehicleTypeId != null ? vehicleTypeId : entityTypeId;
    }

    public RenderTargetInfo(Entity target) {
        id = target.getId();
        uuid = target.getUUID();
        name = target.getScoreboardName();
        entityTypeId = UtilEntity.getEntityTypeId(target);
        Entity entity;
        if (target.isPassenger()) {
            entity = target.getRootVehicle();
            vehicleTypeId = UtilEntity.getEntityTypeId(entity);
        } else {
            entity = target;
            vehicleTypeId = null;
        }
        pos = entity.position();
        move = entity.getDeltaMovement();
        if (target.isOnGround() && move.y < 0) move = move.multiply(1, 0, 1);
        xRot = entity.getXRot();
        yRot = entity.getYRot();
        updateExtraInfo();
        if (extraInfo != null) extraInfo.getInfoServerSide(entity);
    }

    public RenderTargetInfo(FriendlyByteBuf buffer) {
        id = buffer.readInt();
        uuid = buffer.readUUID();
        name = buffer.readUtf();
        double px = buffer.readFloat();
        double py = buffer.readFloat();
        double pz = buffer.readFloat();
        pos = new Vec3(px, py, pz);
        double mx = buffer.readFloat();
        double my = buffer.readFloat();
        double mz = buffer.readFloat();
        move = new Vec3(mx, my, mz);
        xRot = buffer.readFloat();
        yRot = buffer.readFloat();
        entityTypeId = buffer.readUtf();
        boolean isPassenger = buffer.readBoolean();
        if (isPassenger) vehicleTypeId = buffer.readUtf();
        updateExtraInfo();
        if (extraInfo != null) extraInfo.getInfoClientSide(buffer);
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(id);
        buffer.writeUUID(uuid);
        buffer.writeUtf(name);
        buffer.writeFloat((float)pos.x);
        buffer.writeFloat((float)pos.y);
        buffer.writeFloat((float)pos.z);
        buffer.writeFloat((float)move.x);
        buffer.writeFloat((float)move.y);
        buffer.writeFloat((float)move.z);
        buffer.writeFloat(xRot);
        buffer.writeFloat(yRot);
        buffer.writeUtf(entityTypeId);
        buffer.writeBoolean(vehicleTypeId != null);
        if (vehicleTypeId != null) buffer.writeUtf(vehicleTypeId);
        if (extraInfo != null) extraInfo.encodeInfoServerSide(buffer);
    }

    public long getLastUpdateTime(){
        return lastUpdateTime;
    }

    public Vec3 getMove() {
        return move;
    }

    public Vec3 getPos() {
        return pos;
    }

    public float getXRot() {
        return xRot;
    }

    public float getYRot() {
        return yRot;
    }

    public int getId() {
        return id;
    }

    public String getEntityTypeId() {
        return entityTypeId;
    }

    @Nullable
    public String getVehicleTypeId() {
        return vehicleTypeId;
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public ExtraRenderTargetInfo getExtraInfo() {
        return extraInfo;
    }

    public void setInvalidEntityType() {
        invalidEntityType = true;
    }
}

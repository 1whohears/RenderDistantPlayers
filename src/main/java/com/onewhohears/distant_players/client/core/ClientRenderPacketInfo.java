package com.onewhohears.distant_players.client.core;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class ClientRenderPacketInfo {

    public final int id;
    public final Vec3 pos;
    public final Vec3 move;
    public final float xRot, yRot;
    public final String entityTypeId;

    public ClientRenderPacketInfo(Player player) {
        id = player.getId();
        pos = player.position();
        move = player.getDeltaMovement();
        xRot = player.getXRot();
        yRot = player.getYRot();
        entityTypeId = player.getType().toString();
    }

    public ClientRenderPacketInfo(FriendlyByteBuf buffer) {
        id = buffer.readInt();
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
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(id);
        buffer.writeFloat((float)pos.x);
        buffer.writeFloat((float)pos.y);
        buffer.writeFloat((float)pos.z);
        buffer.writeFloat((float)move.x);
        buffer.writeFloat((float)move.y);
        buffer.writeFloat((float)move.z);
        buffer.writeFloat(xRot);
        buffer.writeFloat(yRot);
        buffer.writeUtf(entityTypeId);
    }
}

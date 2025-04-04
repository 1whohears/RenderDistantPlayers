package com.onewhohears.distant_players.client.core;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public interface ExtraRenderTargetInfo {
    void tickFakeEntity(@NotNull Entity entity);
    void updateFakeEntity(@NotNull Entity entity);
    void getInfoServerSide(@NotNull Entity entity);
    void getInfoClientSide(FriendlyByteBuf buffer);
    void encodeInfoServerSide(FriendlyByteBuf buffer);
}

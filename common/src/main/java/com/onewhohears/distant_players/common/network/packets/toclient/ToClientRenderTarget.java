package com.onewhohears.distant_players.common.network.packets.toclient;

import com.onewhohears.distant_players.client.core.DPClientManager;
import com.onewhohears.distant_players.common.core.RenderTargetInfo;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * netty packet responsible for sending and handling {@link RenderTargetInfo} from serverside to clientside.
 */
public class ToClientRenderTarget {
    private final RenderTargetInfo info;

    public ToClientRenderTarget(@NotNull Entity target) {
        info = new RenderTargetInfo(target);
    }

    public ToClientRenderTarget(FriendlyByteBuf buffer) {
        info = new RenderTargetInfo(buffer);
    }

    public void encode(FriendlyByteBuf buffer) {
        info.encode(buffer);
    }

    public void handle(Supplier<NetworkManager.PacketContext> ctx) {
        DPClientManager.get().handleRenderPlayerPacket(info);
    }
}

package com.onewhohears.distant_players.common.network.packets.toclient;

import com.onewhohears.distant_players.client.core.DPClientManager;
import com.onewhohears.distant_players.common.core.RenderTargetInfo;
import com.onewhohears.distant_players.common.network.DPPacketHandler;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * netty packet responsible for sending and handling {@link RenderTargetInfo} from serverside to clientside.
 */
public class ToClientRenderTarget extends BaseS2CMessage {
    private final RenderTargetInfo info;

    public ToClientRenderTarget(@NotNull Entity target) {
        info = new RenderTargetInfo(target);
    }

    public ToClientRenderTarget(FriendlyByteBuf buffer) {
        info = new RenderTargetInfo(buffer);
    }

    @Override
    public MessageType getType() {
        return DPPacketHandler.S2C_RENDER_TARGET;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        info.encode(buffer);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        DPClientManager.get().handleRenderPlayerPacket(info);
    }
}

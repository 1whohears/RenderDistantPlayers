package com.onewhohears.distant_players.common.network.packets.toclient;

import com.onewhohears.distant_players.client.core.DPClientManager;
import com.onewhohears.distant_players.common.core.RenderTargetInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * netty packet responsible for sending and handling {@link RenderTargetInfo} from serverside to clientside.
 */
public class ToClientRenderPlayer {
    private final RenderTargetInfo info;

    public ToClientRenderPlayer(Entity target) {
        info = new RenderTargetInfo(target);
    }

    public ToClientRenderPlayer(FriendlyByteBuf buffer) {
        info = new RenderTargetInfo(buffer);
    }

    public void encode(FriendlyByteBuf buffer) {
        info.encode(buffer);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        DPClientManager.get().handleRenderPlayerPacket(info);
    }
}

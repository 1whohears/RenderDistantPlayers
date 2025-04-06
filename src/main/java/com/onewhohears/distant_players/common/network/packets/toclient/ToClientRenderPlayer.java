package com.onewhohears.distant_players.common.network.packets.toclient;

import com.onewhohears.distant_players.common.core.RenderTargetInfo;
import com.onewhohears.distant_players.client.core.DPClientManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ToClientRenderPlayer {
    private final RenderTargetInfo info;

    public ToClientRenderPlayer(Player target) {
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

package com.onewhohears.distant_players.common.network.packets.toclient;

import com.onewhohears.distant_players.client.core.DPClientManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ToClientRenderPlayer {

    public ToClientRenderPlayer(Player target) {

    }

    public ToClientRenderPlayer(FriendlyByteBuf buffer) {

    }

    public void encode(FriendlyByteBuf buffer) {

    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        DPClientManager.get().handleRenderPlayerPacket();
    }
}

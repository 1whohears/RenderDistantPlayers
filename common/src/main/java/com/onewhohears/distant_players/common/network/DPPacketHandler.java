package com.onewhohears.distant_players.common.network;

import com.onewhohears.distant_players.DistantPlayersMod;

import com.onewhohears.distant_players.common.network.packets.toclient.ToClientRenderTarget;
import dev.architectury.networking.NetworkChannel;
import net.minecraft.resources.ResourceLocation;

public final class DPPacketHandler {

    private DPPacketHandler() {}

    public static final NetworkChannel INSTANCE = NetworkChannel.create(new ResourceLocation(
            DistantPlayersMod.MOD_ID, "networking_channel"));

    public static void register() {
        INSTANCE.register(ToClientRenderTarget.class,
                ToClientRenderTarget::encode,
                ToClientRenderTarget::new,
                ToClientRenderTarget::handle);
    }

}


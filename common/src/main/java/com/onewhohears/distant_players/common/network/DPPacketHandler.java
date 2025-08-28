package com.onewhohears.distant_players.common.network;

import com.onewhohears.distant_players.DistantPlayersMod;

import com.onewhohears.distant_players.common.network.packets.toclient.ToClientRenderTarget;
import dev.architectury.networking.simple.MessageType;
import dev.architectury.networking.simple.SimpleNetworkManager;

public final class DPPacketHandler {

    private DPPacketHandler() {}

    public static final SimpleNetworkManager INSTANCE = SimpleNetworkManager.create(DistantPlayersMod.MOD_ID);

    public static final MessageType S2C_RENDER_TARGET = INSTANCE.registerS2C(
            "render_target", ToClientRenderTarget::new);

    public static void init() {}

}


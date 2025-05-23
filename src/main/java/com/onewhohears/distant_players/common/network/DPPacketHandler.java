package com.onewhohears.distant_players.common.network;

import com.onewhohears.distant_players.DistantPlayersMod;

import com.onewhohears.distant_players.common.network.packets.toclient.ToClientRenderPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class DPPacketHandler {

    private DPPacketHandler() {}

    private static final String PROTOCOL_VERSION = "1.0";

    public static SimpleChannel INSTANCE;

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(DistantPlayersMod.MOD_ID, "messages"))
                .networkProtocolVersion(() -> PROTOCOL_VERSION)
                .clientAcceptedVersions(s -> s.equals(PROTOCOL_VERSION))
                .serverAcceptedVersions(s -> s.equals(PROTOCOL_VERSION))
                .simpleChannel();
        INSTANCE = net;
        int index = 0;
        net.messageBuilder(ToClientRenderPlayer.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ToClientRenderPlayer::encode)
                .decoder(ToClientRenderPlayer::new)
                .consumerMainThread(ToClientRenderPlayer::handle)
                .add();

    }

}


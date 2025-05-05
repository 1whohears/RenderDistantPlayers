package com.onewhohears.distant_players.common.network;

import com.onewhohears.distant_players.DistantPlayersMod;

import com.onewhohears.distant_players.common.network.packets.S2CViewInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
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
        net.messageBuilder(S2CViewInfo.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CViewInfo::encode)
                .decoder(S2CViewInfo::new)
                .consumerMainThread(S2CViewInfo::handle)
                .add();

    }

    public static <MSG> void sendToClients(MSG message) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }
}


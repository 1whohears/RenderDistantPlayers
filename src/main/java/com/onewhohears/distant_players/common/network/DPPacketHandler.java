package com.onewhohears.distant_players.common.network;

import com.onewhohears.distant_players.DistantPlayersMod;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class DPPacketHandler {

    private DPPacketHandler() {}

    private static final String PROTOCOL_VERSION = "1.0";

    public static SimpleChannel INSTANCE;

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(DistantPlayersMod.MODID, "messages"))
                .networkProtocolVersion(() -> PROTOCOL_VERSION)
                .clientAcceptedVersions(s -> s.equals(PROTOCOL_VERSION))
                .serverAcceptedVersions(s -> s.equals(PROTOCOL_VERSION))
                .simpleChannel();
        INSTANCE = net;
        int index = 0;
        /*net.messageBuilder(ToServerVehicleControl.class, index++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ToServerVehicleControl::encode)
                .decoder(ToServerVehicleControl::new)
                .consumerMainThread(ToServerVehicleControl::handle)
                .add();*/

    }

}


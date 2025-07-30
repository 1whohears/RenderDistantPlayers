package com.onewhohears.distant_players;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {

    public static class Client {
        public final ForgeConfigSpec.DoubleValue maxRenderRadius;
        public Client(ForgeConfigSpec.Builder builder) {
            maxRenderRadius = builder
                    .defineInRange("maxRenderRadius", 24d, 8d, 160d);
        }
    }

    static final ForgeConfigSpec clientSpec;
    public static final Config.Client CLIENT;

    static {
        final Pair<Client, ForgeConfigSpec> clientSpecPair = new ForgeConfigSpec.Builder()
                .configure(Config.Client::new);
        clientSpec = clientSpecPair.getRight();
        CLIENT = clientSpecPair.getLeft();
    }

}


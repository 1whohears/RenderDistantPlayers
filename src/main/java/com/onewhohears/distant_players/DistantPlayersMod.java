package com.onewhohears.distant_players;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(DistantPlayersMod.MOD_ID)
public class DistantPlayersMod {
    public static final String MOD_ID = "distant_players";

    public DistantPlayersMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.clientSpec);
    }
}

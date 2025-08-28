package com.onewhohears.distant_players.forge;

import com.onewhohears.distant_players.DistantPlayersMod;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(DistantPlayersMod.MOD_ID)
public final class DistantPlayersModForge {

    // Leave this here for now otherwise we will be incompatible w/ older versions of Forge
    public DistantPlayersModForge() {
        @SuppressWarnings("removal")
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        EventBuses.registerModEventBus(DistantPlayersMod.MOD_ID, modEventBus);

        DistantPlayersMod.init();
    }

    // Compatible with newer versions of Forge
    public DistantPlayersModForge(FMLJavaModLoadingContext loadingContext) {
        IEventBus modEventBus = loadingContext.getModEventBus();
        EventBuses.registerModEventBus(DistantPlayersMod.MOD_ID, modEventBus);

        DistantPlayersMod.init();
    }

}

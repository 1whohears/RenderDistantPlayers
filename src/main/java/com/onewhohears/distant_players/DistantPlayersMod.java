package com.onewhohears.distant_players;

import com.mojang.logging.LogUtils;
import com.onewhohears.distant_players.common.command.DPGameRules;
import com.onewhohears.distant_players.common.network.DPPacketHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(DistantPlayersMod.MODID)
public class DistantPlayersMod {
    public static final String MODID = "distant_players";
    private static final Logger LOGGER = LogUtils.getLogger();

    public DistantPlayersMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        DPPacketHandler.register();
        DPGameRules.register();
    }

}

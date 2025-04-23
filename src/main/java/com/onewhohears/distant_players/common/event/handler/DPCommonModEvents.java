package com.onewhohears.distant_players.common.event.handler;

import com.onewhohears.distant_players.DistantPlayersMod;
import com.onewhohears.distant_players.common.command.DPGameRules;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = DistantPlayersMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DPCommonModEvents {
    @SubscribeEvent
    public static void commonSetup(final FMLCommonSetupEvent event) {
        DPGameRules.register();
    }
}

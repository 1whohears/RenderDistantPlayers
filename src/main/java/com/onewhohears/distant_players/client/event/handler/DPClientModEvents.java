package com.onewhohears.distant_players.client.event.handler;

import com.onewhohears.distant_players.client.core.DPClientManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static com.onewhohears.distant_players.DistantPlayersMod.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class DPClientModEvents {
    /**
     * Instantiates singleton of <code>DPClientManager</code>
     */
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        DPClientManager.init();
    }
}

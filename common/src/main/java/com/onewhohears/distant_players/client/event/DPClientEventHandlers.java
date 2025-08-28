package com.onewhohears.distant_players.client.event;

import com.onewhohears.distant_players.client.core.DPClientManager;
import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.Minecraft;

public class DPClientEventHandlers {

    public static void init() {
        ClientTickEvent.CLIENT_POST.register(DPClientEventHandlers::onClientTick);
    }

    /**
     * Executes mod logic at the end of every (client) tick.
     */
    public static void onClientTick(Minecraft minecraft) {
        DPClientManager.get().tick();
    }
}

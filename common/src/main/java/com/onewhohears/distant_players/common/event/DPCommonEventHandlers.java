package com.onewhohears.distant_players.common.event;

import com.onewhohears.distant_players.common.core.DPServerManager;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class DPCommonEventHandlers {

    public static void init() {
        LifecycleEvent.SERVER_STARTED.register(DPCommonEventHandlers::serverStarted);
        TickEvent.SERVER_POST.register(DPCommonEventHandlers::serverTick);
        PlayerEvent.PLAYER_JOIN.register(DPCommonEventHandlers::playerLogIn);
        PlayerEvent.PLAYER_QUIT.register(DPCommonEventHandlers::playerLogOut);
    }

    public static void playerLogIn(ServerPlayer player) {
        DPServerManager.get().onPlayerLogIn(player);
    }

    public static void playerLogOut(ServerPlayer player) {
        DPServerManager.get().onPlayerLogOut(player);
    }

    public static void serverStarted(MinecraftServer server) {
        DPServerManager.init();
    }

    public static void serverTick(MinecraftServer server) {
        DPServerManager.get().tick(server);
        DPServerManager.get().testExtraTrackableEntity(server);
    }

}

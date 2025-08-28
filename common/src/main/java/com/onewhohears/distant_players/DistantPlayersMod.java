package com.onewhohears.distant_players;

import com.onewhohears.distant_players.client.core.DPClientManager;
import com.onewhohears.distant_players.client.event.DPClientEventHandlers;
import com.onewhohears.distant_players.common.command.DPGameRules;
import com.onewhohears.distant_players.common.core.ExtraInfoManager;
import com.onewhohears.distant_players.common.event.DPCommonEventHandlers;
import com.onewhohears.distant_players.common.network.DPPacketHandler;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;

public class DistantPlayersMod {
    public static final String MOD_ID = "distant_players";

    public static void init() {
        DPPacketHandler.init();
        DPCommonEventHandlers.init();
        DPGameRules.register();
        ExtraInfoManager.registerBuiltIn();
        if (Platform.getEnvironment() == Env.CLIENT) {
            DPClientEventHandlers.init();
            DPClientManager.init();
        }
    }
}

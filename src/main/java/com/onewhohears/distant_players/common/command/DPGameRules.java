package com.onewhohears.distant_players.common.command;

import com.onewhohears.onewholibs.common.command.CustomGameRules;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameRules;

public class DPGameRules {
    public static GameRules.Key<GameRules.IntegerValue> POS_UPDATE_RATE;

    public static void register() {
        POS_UPDATE_RATE = CustomGameRules.registerInteger(
                "distant_players:posUpdateRate", 5, GameRules.Category.PLAYER
        );
    }

    public static int getPosUpdateRate(MinecraftServer server) {
        return server.getGameRules().getInt(POS_UPDATE_RATE);
    }
}

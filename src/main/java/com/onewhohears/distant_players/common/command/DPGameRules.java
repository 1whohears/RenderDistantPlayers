package com.onewhohears.distant_players.common.command;

import com.onewhohears.onewholibs.common.command.CustomGameRules;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameRules;

public class DPGameRules {
    public static GameRules.Key<GameRules.IntegerValue> MAX_DISTANCE;

    public static void register() {
        MAX_DISTANCE = CustomGameRules.registerInteger(
                "distant_players:maxDistance", 32, GameRules.Category.PLAYER
        );
    }

    public static int getMaxDistance(MinecraftServer server) {
        return server.getGameRules().getInt(MAX_DISTANCE);
    }
}

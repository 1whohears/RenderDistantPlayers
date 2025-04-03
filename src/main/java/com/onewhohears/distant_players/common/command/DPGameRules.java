package com.onewhohears.distant_players.common.command;

import com.onewhohears.onewholibs.common.command.CustomGameRules;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameRules;

public class DPGameRules {

    public static GameRules.Key<GameRules.IntegerValue> POS_UPDATE_RATE;
    public static GameRules.Key<GameRules.IntegerValue> CHECK_VISIBLE_RATE;
    public static GameRules.Key<GameRules.IntegerValue> RAY_CAST_DEPTH;
    public static GameRules.Key<GameRules.IntegerValue> MAX_VIEW_DISTANCE;

    public static void register() {
        POS_UPDATE_RATE = CustomGameRules.registerInteger("distant_players-posUpdateRate", 5, GameRules.Category.PLAYER);
        CHECK_VISIBLE_RATE = CustomGameRules.registerInteger("distant_players-checkVisibleRate", 20, GameRules.Category.PLAYER);
        RAY_CAST_DEPTH = CustomGameRules.registerInteger("distant_players-rayCastDepth", 320, GameRules.Category.PLAYER);
        MAX_VIEW_DISTANCE = CustomGameRules.registerInteger("distant_players-maxViewDistance", 1000, GameRules.Category.PLAYER);
    }

    public static int getPosUpdateRate(MinecraftServer server) {
        return server.getGameRules().getInt(POS_UPDATE_RATE);
    }

    public static int getCheckVisibleRate(MinecraftServer server) {
        return server.getGameRules().getInt(CHECK_VISIBLE_RATE);
    }

    public static int getRayCastDepth(MinecraftServer server) {
        return server.getGameRules().getInt(RAY_CAST_DEPTH);
    }

    public static int getViewDistance(MinecraftServer server) {
        return server.getGameRules().getInt(MAX_VIEW_DISTANCE);
    }

}

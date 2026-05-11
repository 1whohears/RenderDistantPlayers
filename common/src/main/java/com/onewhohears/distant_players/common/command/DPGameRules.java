package com.onewhohears.distant_players.common.command;

import com.onewhohears.onewholibs.common.command.CustomGameRules;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameRules;

public class DPGameRules {
    public static GameRules.Key<GameRules.IntegerValue> POS_UPDATE_RATE;
    public static GameRules.Key<GameRules.IntegerValue> MAX_VIEW_DISTANCE;
    public static GameRules.Key<GameRules.IntegerValue> MAX_EXTRA_ENTITIES;
    public static GameRules.Key<GameRules.BooleanValue> TEST_MODE;

    public static void register() {
        POS_UPDATE_RATE = CustomGameRules.registerInteger("distant_players:posUpdateRate", 4, GameRules.Category.PLAYER);
        MAX_VIEW_DISTANCE = CustomGameRules.registerInteger("distant_players:maxViewDistance", 1000, GameRules.Category.PLAYER);
        MAX_EXTRA_ENTITIES = CustomGameRules.registerInteger("distant_players:maxExtraEntities", 50, GameRules.Category.PLAYER);
        TEST_MODE = CustomGameRules.registerBoolean("distant_players:testMode", false, GameRules.Category.PLAYER);
    }

    public static int getPosUpdateRate(MinecraftServer server) {
        return server.getGameRules().getInt(POS_UPDATE_RATE);
    }

    public static int getViewDistance(MinecraftServer server) {
        return server.getGameRules().getInt(MAX_VIEW_DISTANCE);
    }

    public static int getMaxExtraEntities(MinecraftServer server) {
        return server.getGameRules().getInt(MAX_EXTRA_ENTITIES);
    }

    public static boolean isTestMode(MinecraftServer server) {
        return server.getGameRules().getBoolean(TEST_MODE);
    }
}

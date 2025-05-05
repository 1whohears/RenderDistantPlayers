package com.onewhohears.distant_players.common.core;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class DPClientManager {
    private static DPClientManager INSTANCE;

    public static void init() {
        INSTANCE = new DPClientManager();
    }

    public static DPClientManager get() {
        return INSTANCE;
    }

    private Map<ResourceKey<Level>, IntSet> distantEntities = new HashMap<>();

    public void update(Map<ResourceKey<Level>, IntList> entities) {
        ImmutableMap.Builder<ResourceKey<Level>, IntSet> builder = ImmutableMap.builder();

        for (Map.Entry<ResourceKey<Level>, IntList> entry : entities.entrySet()) {
            builder.put(entry.getKey(), new IntArraySet(entry.getValue()));
        }

        this.distantEntities = builder.build();
    }

    public boolean isInDistantView(Entity target) {
        if (!(target.getLevel() instanceof ClientLevel level))
            throw new IllegalArgumentException(target.getScoreboardName() + "\" is a server-sided entity!");

        return this.getDistantEntitiesForLevel(level).contains(target.getId());
    }

    private IntSet getDistantEntitiesForLevel(ClientLevel level) {
        return this.distantEntities.getOrDefault(
                level.dimension(),
                new IntArraySet()
        );
    }

    private DPClientManager() {}
}

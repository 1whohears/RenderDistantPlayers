package com.onewhohears.distant_players.common.core;

import com.onewhohears.distant_players.common.network.DPPacketHandler;
import com.onewhohears.distant_players.common.network.packets.S2CViewInfo;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public final class DPServerManager {
    private static DPServerManager INSTANCE;

    public static void init() {
        INSTANCE = new DPServerManager();
    }

    public static DPServerManager get() {
        return INSTANCE;
    }

    private final Map<ResourceKey<Level>, IntSet> distantEntities = new HashMap<>();

    public boolean isInDistantView(Entity target) {
        if (!(target.getLevel() instanceof ServerLevel level))
            throw new IllegalArgumentException(target.getScoreboardName() + "\" is a client-sided entity!");
        
        return this.getDistantEntitiesForLevel(level).contains(target.getId());
    }

    /**
     * Public hook to add an entity to the views of players. This method is intended to be called whenever an entity
     * that should render at a distance first joins a level. Note that this is automatically done for
     * {@code ServerPlayer}s.
     */
    public void addEntityToPlayerView(Entity target) {
        if (!(target.getLevel() instanceof ServerLevel level))
            throw new IllegalArgumentException(target.getScoreboardName() + "\" is a client-sided entity!");

        if (this.getDistantEntitiesForLevel(level).add(target.getId()))
            DPPacketHandler.sendToClients(new S2CViewInfo(this.distantEntities));
    }

    public void removeEntityFromDistantView(Entity target) {
        if (!(target.getLevel() instanceof ServerLevel level))
            throw new IllegalArgumentException(target.getScoreboardName() + "\" is a client-sided entity!");

        boolean changed = this.getDistantEntitiesForLevel(level).remove(target.getId());

        for (Entity entity : target.getIndirectPassengers()) {
            if (this.getDistantEntitiesForLevel(level).remove(entity.getId()))
                changed = true;
        }

        if (changed) DPPacketHandler.sendToClients(new S2CViewInfo(this.distantEntities));
    }

    private IntSet getDistantEntitiesForLevel(ServerLevel level) {
        return this.distantEntities.computeIfAbsent(
                level.dimension(),
                dimension -> new IntArraySet()
        );
    }

    private DPServerManager() {}
}

package com.onewhohears.distant_players.common.core;

import com.onewhohears.distant_players.common.command.DPGameRules;
import com.onewhohears.distant_players.common.network.DPPacketHandler;
import com.onewhohears.distant_players.common.network.packets.toclient.ToClientRenderPlayer;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;

// TODO - Maybe this mod can be rewritten entirely by redoing server-sided behaviour regarding tracking...
// TODO - documentation lol
/**
 * Brain of the mod. Responsible for coordinating tracked entity information and updating the information in
 * the {@link com.onewhohears.distant_players.client.core.DPClientManager}.
 */
public final class DPServerManager {
    private static DPServerManager INSTANCE;

    public static void init() {
        INSTANCE = new DPServerManager();
    }

    public static DPServerManager get() {
        return INSTANCE;
    }

    private final Map<ResourceKey<Level>, IntObjectMap<IntSet>> distantEntities = new HashMap<>();
    // TODO - Vehicles dismounted by players should continue to render for a bit independently
    private final Map<ResourceKey<Level>, IntSet> playerlessEntities = new HashMap<>();

    /**
     * TODO - addEntityToAllPlayerViews(Entity)
     * Public hook to add an entity to the views of a player. This method is intended to be called whenever an entity
     * that should render at a distance first joins a level; taking the player(s) which should see that entity & the
     * entity itself as parameters. Note that this is automatically done for {@code ServerPlayer}s and the vehicle(s)
     * they are riding. DSCombat compatibility will iterate over all {@code ServerPlayer}s in a level when calling this.
     */
    public void addEntityToPlayerView(ServerPlayer player, Entity target) {
        if (!player.getLevel().equals(target.getLevel()))
            throw new IllegalArgumentException("Player \"" + player.getGameProfile().getName() + "\" and passed Entity \"" + target.getScoreboardName() + "\" do not share a Level!");

        if (!this.isPlayerlessEntity(target) && !(target instanceof ServerPlayer)) this.addPlayerlessEntity(target);

        this.getDistantEntitiesForPlayer(player)
                .add(target.getId());
    }

    public void removeEntityFromPlayerView(ServerPlayer player, Entity target) {
        if (!player.getLevel().equals(target.getLevel()))
            throw new IllegalArgumentException("Player \"" + player.getGameProfile().getName() + "\" and passed Entity \"" + target.getScoreboardName() + "\" do not share a Level!");

        this.getDistantEntitiesForPlayer(player)
                .remove(target.getId());
    }

    public void removeAllEntitiesFromView(ServerPlayer player) {
        this.getDistantEntitiesForPlayer(player)
                .remove(player.getId());
    }

    public void addPlayerlessEntity(Entity target) {
        if (!(target.level instanceof ServerLevel level) || target instanceof ServerPlayer)
            throw new IllegalArgumentException("Passed Entity \"" + target.getScoreboardName() + "\" is not on a ServerLevel!");

        this.getPlayerlessEntities(level).add(target.getId());
    }

    public void removePlayerlessEntity(Entity target) {
        if (!(target.level instanceof ServerLevel level))
            throw new IllegalArgumentException("Passed Entity \"" + target.getScoreboardName() + "\" is not on a ServerLevel!");

        this.getPlayerlessEntities(level).remove(target.getId());
    }

    public boolean isPlayerlessEntity(Entity target) {
        if (!(target.level instanceof ServerLevel level))
            throw new IllegalArgumentException("Passed Entity \"" + target.getScoreboardName() + "\" is not on a ServerLevel!");

        return this.getPlayerlessEntities(level).contains(target.getId());
    }

    /**
     * @return shallow copy as {@code int[]} of {@link this#getPlayerlessEntities(ServerLevel)}
     */
    public int[] getPlayerlessEntitiesCopy(ServerLevel level) {
        return this.getPlayerlessEntities(level).toIntArray();
    }

    private IntSet getDistantEntitiesForPlayer(ServerPlayer player) {
        IntObjectMap<IntSet> forLevel = this.distantEntities.computeIfAbsent(
                player.getLevel().dimension(),
                dimension -> new IntObjectHashMap<>()
        );

        return forLevel.computeIfAbsent(
                player.getId(),
                id -> new IntArraySet()
        );
    }

    private IntSet getPlayerlessEntities(ServerLevel level) {
        return this.playerlessEntities.computeIfAbsent(
                level.dimension(),
                key -> new IntArraySet()
        );
    }

    public void sendPayload(ServerPlayer player, Entity target) {
        DPPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ToClientRenderPlayer(target));
    }

    public void sendPayloads(MinecraftServer server) {
        for (Map.Entry<ResourceKey<Level>, IntObjectMap<IntSet>> entry : this.distantEntities.entrySet()) {
            Level level = server.getLevel(entry.getKey());
            if (level == null)
                throw new AssertionError("Requested Level \"" + entry.getKey() + "\" was unable to be obtained!");

            for (Player player : level.players()) {
                if (!(player instanceof ServerPlayer serverPlayer)) continue;
                IntSet entities = this.getDistantEntitiesForPlayer(serverPlayer);

                entities.forEach(
                        id -> {
                            Entity forRender = level.getEntity(id);
                            if (forRender == null) return;
                            this.sendPayload(serverPlayer, forRender);
                        });
            }
        }
    }

    public void tick(MinecraftServer server) {
        int posUpdateRate = DPGameRules.getPosUpdateRate(server);
        if (server.getTickCount() % posUpdateRate == 0) this.sendPayloads(server);
    }

    private DPServerManager() {}
}

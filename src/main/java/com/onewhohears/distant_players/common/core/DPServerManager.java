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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;

// TODO - Maybe this mod can be rewritten entirely by redoing server-sided behaviour regarding tracking...
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
    // TODO
    private final IntSet playerlessEntities = new IntArraySet();

    public void addEntityToPlayerView(ServerPlayer player, Entity target) {
        if (!player.getLevel().equals(target.getLevel()))
            throw new IllegalArgumentException("Player \"" + player.getGameProfile().getName() + "\" and passed Entity \"" + target.getScoreboardName() + "\" do not share a Level!");

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

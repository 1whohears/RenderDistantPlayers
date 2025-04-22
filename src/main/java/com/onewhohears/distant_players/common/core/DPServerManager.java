package com.onewhohears.distant_players.common.core;

import com.onewhohears.distant_players.common.command.DPGameRules;
import com.onewhohears.distant_players.common.network.DPPacketHandler;
import com.onewhohears.distant_players.common.network.packets.toclient.ToClientRenderPlayer;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

import static net.minecraft.world.level.Level.OVERWORLD;

/* FIXME - There should probably be an instance for every Level on the MinecraftServer, or some other impl that
    that considers players in different dimensions. It seems the 1wholibs canEntitySeeEntity util method does actually
    account for this (or maybe tracking?), but this seems like a solution liable to failure.
 */
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

    private final IntObjectMap<IntSet> distantEntities = new IntObjectHashMap<>();
    // TODO
    private final IntSet playerlessEntities = new IntArraySet();

    public void addEntityToPlayerView(ServerPlayer player, Entity target) {
        this.getDistantEntitiesForPlayer(player)
                .add(target.getId());
    }

    public void removeEntityFromPlayerView(ServerPlayer player, Entity target) {
        this.getDistantEntitiesForPlayer(player)
                .remove(target.getId());
    }

    public void removeAllEntitiesFromView(ServerPlayer player) {
        this.distantEntities.remove(player.getId());
    }

    private IntSet getDistantEntitiesForPlayer(ServerPlayer player) {
        return this.distantEntities.computeIfAbsent(
                player.getId(),
                (id) -> new IntArraySet()
        );
    }

    public void sendPayload(ServerPlayer player, Entity target) {
        DPPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ToClientRenderPlayer(target));
    }

    // FIXME - This only works in the overworld. Fix this when per-level impl is made.
    public void sendPayloads(MinecraftServer server) {
        Level overworld = server.getLevel(OVERWORLD);
        // if the overworld isn't loaded, something is SERIOUSLY wrong
        assert overworld != null;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            IntSet entities = this.getDistantEntitiesForPlayer(player);

            entities.forEach(
                    id -> {
                        Entity forRender = overworld.getEntity(id);
                        if (forRender == null) return;
                        this.sendPayload(player, forRender);
                    });
        }
    }

    public void tick(MinecraftServer server) {
        int posUpdateRate = DPGameRules.getPosUpdateRate(server);
        if (server.getTickCount() % posUpdateRate == 0) this.sendPayloads(server);
    }

    // TODO - ?
    public void onPlayerLogIn(Player player) {

    }

    private DPServerManager() {}
}

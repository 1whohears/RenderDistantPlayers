package com.onewhohears.distant_players.common.core;

import com.onewhohears.distant_players.common.command.DPGameRules;
import com.onewhohears.distant_players.common.network.DPPacketHandler;
import com.onewhohears.distant_players.common.network.packets.toclient.ToClientRenderPlayer;
import com.onewhohears.onewholibs.util.UtilEntity;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

/*
   TODO - To be honest, I think it'll be fine to leave culling on the clientside. It's already possible to see entities
    through walls with mods or (if one has them installed) cheats. I don't see much of a point attempting to stop this
    on serverside when the only advantage conferred to someone cheating that way is the same with or without having
    this mod installed; that is, being able to see players from anywhere. This is, in my eyes, a problem that is
    "further up the chain", so to speak.
 */

// FIXME - There should probably be an instance for every Level on the MinecraftServer, or some other impl that
//  that considers players in different dimensions
/**
 * Brain of the mod. Responsible for coordinating tracked entity information and updating the information in
 * the {@link com.onewhohears.distant_players.client.core.DPClientManager}. Sends entity information to other
 * clients, and doesn't when it's deemed that they shouldn't be able to see each other.
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
    private final IntObjectMap<IntSet> visible = new IntObjectHashMap<>();

    public void addEntityToPlayerView(ServerPlayer player, Entity target) {
        this.getDistantEntitiesForPlayer(player)
                .add(target.getId());
    }

    public void removeEntityFromPlayerView(ServerPlayer player, Entity target) {
        this.getDistantEntitiesForPlayer(player)
                .remove(target.getId());
    }

    public boolean isEntityInPlayerView(ServerPlayer player, Entity target) {
        return !this.getDistantEntitiesForPlayer(player).contains(target.getId());
    }

    private IntSet getDistantEntitiesForPlayer(ServerPlayer player) {
        return this.distantEntities.computeIfAbsent(
                player.getId(),
                (id) -> new IntArraySet()
        );
    }

    public void addEntityToVisibility(ServerPlayer player, Entity target) {
        this.getPlayerVisibility(player)
                .add(target.getId());
    }

    public void removeEntityFromVisibility(ServerPlayer player, Entity target) {
        this.getPlayerVisibility(player)
                .remove(target.getId());
    }

    private IntSet getPlayerVisibility(ServerPlayer player) {
        return this.visible.computeIfAbsent(
                player.getId(),
                (id) -> new IntArraySet()
        );
    }

    public void checkVisibility(MinecraftServer server) {
        int maxDist = DPGameRules.getViewDistance(server);
        int maxDistSqr = maxDist * maxDist;
        int rayCastDepth = DPGameRules.getRayCastDepth(server);

        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        for (int i = 0; i < players.size(); i++) {
            ServerPlayer player1 = players.get(i);

            for (int j = i + 1; j < players.size(); j++) {
                ServerPlayer player2 = players.get(j);
                boolean canSee = false, canSeeChecked = false;

                if (this.isEntityInPlayerView(player1, player2)) {
                    canSee = this.checkCanSee(player1, player2, false, maxDistSqr, rayCastDepth);
                    canSeeChecked = true;
                }

                if (canSeeChecked) {
                    if (canSee) {
                        this.addEntityToVisibility(player1, player2);
                    } else {
                        this.removeEntityFromVisibility(player1, player2);
                        this.removeEntityFromVisibility(player2, player1);
                        continue;
                    }
                } else {
                    this.removeEntityFromVisibility(player1, player2);
                }

                if (
                        this.isEntityInPlayerView(player2, player1)
                        && this.checkCanSee(player2, player1, canSee, maxDistSqr, rayCastDepth)
                ) {
                    this.addEntityToVisibility(player2,  player1);
                } else {
                    this.removeEntityFromVisibility(player2, player1);
                }
            }
        }
    }

    public void sendPayload(ServerPlayer player, ServerPlayer target) {
        DPPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ToClientRenderPlayer(target));
    }

    public void sendPayloads(MinecraftServer server) {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        for (int i = 0; i < players.size(); i++) {
            ServerPlayer player1 = players.get(i);
            for (int j = i + 1; j < players.size(); j++) {
                ServerPlayer player2 = players.get(j);
                if (this.getPlayerVisibility(player1).contains(player2.getId()))
                    this.sendPayload(player1, player2);
                if (this.getPlayerVisibility(player2).contains(player1.getId()))
                    this.sendPayload(player2, player1);
            }
        }
    }

    private boolean checkCanSee(ServerPlayer player, ServerPlayer target, boolean skipBlockCheck,
                                int maxDistSqr, int rayCastDepth) {
        if (!skipBlockCheck) {
            if (player.distanceToSqr(target) > maxDistSqr) return false;
            return UtilEntity.canEntitySeeEntity(player, target, rayCastDepth);
        }
        return true;
    }

    public void tick(MinecraftServer server) {
        int checkVisibleRate = DPGameRules.getCheckVisibleRate(server);
        int posUpdateRate = DPGameRules.getPosUpdateRate(server);
        if (server.getTickCount() % checkVisibleRate == 0) this.checkVisibility(server);
        if (server.getTickCount() % posUpdateRate == 0) this.sendPayloads(server);
    }

    // TODO - ?
    public void onPlayerLogIn(Player player) {

    }

    public void onPlayerLogOut(ServerPlayer player) {
        this.distantEntities.remove(player.getId());
        this.visible.remove(player.getId());
    }

    private DPServerManager() {}
}

package com.onewhohears.distant_players.common.core;

import com.onewhohears.distant_players.common.network.DPPacketHandler;
import com.onewhohears.distant_players.common.network.packets.toclient.ToClientRenderPlayer;
import com.onewhohears.onewholibs.util.UtilEntity;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

public final class DPServerManager {

    public static final int PAYLOAD_SEND_RATE = 5;
    public static final int CHECK_VISIBLE_RATE = 20;
    public static final double MAX_DISTANCE = 1000;
    public static final double MAX_DISTANCE_SQR = MAX_DISTANCE * MAX_DISTANCE;
    public static final int BLOCK_CHECK_DEPTH = 320;

    private final IntObjectMap<IntSet> tracks = new IntObjectHashMap<>();
    private final IntObjectMap<IntSet> visible = new IntObjectHashMap<>();

    public void checkVisible(MinecraftServer server) {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        for (int i = 0; i < players.size(); i++) {
            ServerPlayer player1 = players.get(i);
            for (int j = i + 1; j < players.size(); j++) {
                ServerPlayer player2 = players.get(j);
                boolean canSee = false, canSeeChecked = false;
                if (isPlayerNotTracking(player1, player2)) {
                    canSee = checkCanSee(player1, player2, false);
                    canSeeChecked = true;
                }
                if (canSeeChecked) {
                    if (canSee) {
                        getPlayerVisible(player1).add(player2.getId());
                    } else {
                        getPlayerVisible(player1).remove(player2.getId());
                        getPlayerVisible(player2).remove(player1.getId());
                        continue;
                    }
                } else {
                    getPlayerVisible(player1).remove(player2.getId());
                }
                if (isPlayerNotTracking(player2, player1) && checkCanSee(player2, player1, canSee)) {
                    getPlayerVisible(player2).add(player1.getId());
                } else {
                    getPlayerVisible(player2).remove(player1.getId());
                }
            }
        }
    }

    public void sendPayload(ServerPlayer player, ServerPlayer target) {
        DPPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(()->player), new ToClientRenderPlayer(target));
    }

    public void sendPayloads(MinecraftServer server) {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        for (int i = 0; i < players.size(); i++) {
            ServerPlayer player1 = players.get(i);
            for (int j = i + 1; j < players.size(); j++) {
                ServerPlayer player2 = players.get(j);
                if (getPlayerVisible(player1).contains(player2.getId()))
                    sendPayload(player1, player2);
                if (getPlayerVisible(player2).contains(player1.getId()))
                    sendPayload(player2, player1);
            }
        }
    }

    private boolean checkCanSee(ServerPlayer player, ServerPlayer target, boolean skipBlockCheck) {
        if (!skipBlockCheck) {
            if (player.distanceToSqr(target) > MAX_DISTANCE_SQR) return false;
            return UtilEntity.canEntitySeeEntity(player, target, BLOCK_CHECK_DEPTH);
        }
        return true;
    }

    public boolean isPlayerNotTracking(ServerPlayer player, ServerPlayer target) {
        return !getPlayerTracks(player).contains(target.getId());
    }

    public void tick(MinecraftServer server) {
        if (server.getTickCount() % CHECK_VISIBLE_RATE == 0) checkVisible(server);
        if (server.getTickCount() % PAYLOAD_SEND_RATE == 0) sendPayloads(server);
    }

    public void onPlayerStartTrack(Player player, Player target) {
        getPlayerTracks(player).add(target.getId());
        getPlayerVisible(player).remove(target.getId());
    }

    public void onPlayerStopTrack(Player player, Player target) {
        getPlayerTracks(player).remove(target.getId());
    }

    public void onPlayerLogIn(Player player) {

    }

    public void onPlayerLogOut(Player player) {
        tracks.remove(player.getId());
        visible.remove(player.getId());
    }

    private IntSet getPlayerTracks(Player player) {
        if (!tracks.containsKey(player.getId())) {
            IntSet set = new IntArraySet();
            tracks.put(player.getId(), set);
            return set;
        }
        return tracks.get(player.getId());
    }

    private IntSet getPlayerVisible(Player player) {
        if (!visible.containsKey(player.getId())) {
            IntSet set = new IntArraySet();
            visible.put(player.getId(), set);
            return set;
        }
        return visible.get(player.getId());
    }

    private static DPServerManager INSTANCE;

    public static void init() {
        INSTANCE = new DPServerManager();
    }

    public static DPServerManager get() {
        return INSTANCE;
    }

    private DPServerManager() {}
}

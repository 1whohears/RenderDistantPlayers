package com.onewhohears.distant_players.common.core;

import com.onewhohears.distant_players.common.command.DPGameRules;
import com.onewhohears.distant_players.common.network.DPPacketHandler;
import com.onewhohears.distant_players.common.network.packets.toclient.ToClientRenderTarget;
import com.onewhohears.onewholibs.util.UtilEntity;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/*
   TODO - To be honest, I think it'll be fine to leave culling on the clientside. It's already possible to see entities
    through walls with mods or (if one has them installed) cheats. I don't see much of a point attempting to stop this
    on serverside when the only advantage conferred to someone cheating that way is the same with or without having
    this mod installed; that is, being able to see players from anywhere. This is, in my eyes, a problem that is
    "further up the chain", so to speak.
 */

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

    private final IntObjectMap<IntSet> tracks = new IntObjectHashMap<>();
    private final IntObjectMap<IntSet> visible = new IntObjectHashMap<>();
    private final IntObjectMap<ExtraEntity> extraEntities = new IntObjectHashMap<>();

    /**
     * Allow players to see non player entities from a distance.
     * This must be called at least once per second to keep the entity visible.
     * You may also specify that only specific players can see this entity from a distance.
     */
    public void addExtraTrackableEntity(@NotNull MinecraftServer server, @NotNull Entity entity, @NotNull ServerPlayer... visibleTo) {
        int max = DPGameRules.getMaxExtraEntities(server);
        if (extraEntities.size() >= max) return;
        int[] visibleToIDs = new int[visibleTo.length];
        for (int i = 0; i < visibleTo.length; ++i) visibleToIDs[i] = visibleTo[i].getId();
        ExtraEntity extra = new ExtraEntity(entity, server.getTickCount(), visibleToIDs);
        extraEntities.put(entity.getId(), extra);
    }

    public void testExtraTrackableEntity(@NotNull MinecraftServer server) {
        if (!DPGameRules.isTestMode(server)) return;
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        for (ServerPlayer player : players) {
            AABB golemBox = player.getBoundingBox().inflate(1000);
            List<IronGolem> golems = player.getLevel().getEntitiesOfClass(IronGolem.class, golemBox);
            for (IronGolem golem : golems) addExtraTrackableEntity(server, golem);
        }
    }

    record ExtraEntity(Entity entity, int addTime, int[] visibleToIDs) {
        boolean onVisibleList(int id) {
            if (visibleToIDs.length == 0) return true;
            for (int visibleToID : visibleToIDs) if (id == visibleToID) return true;
            return false;
        }
    }

    public void checkVisible(MinecraftServer server) {
        int maxDist = DPGameRules.getViewDistance(server);
        int maxDistSqr = maxDist * maxDist;
        int rayCastDepth = DPGameRules.getRayCastDepth(server);
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        for (int i = 0; i < players.size(); i++) {
            ServerPlayer player1 = players.get(i);
            for (int j = i + 1; j < players.size(); j++) {
                ServerPlayer player2 = players.get(j);
                boolean canSee = false, canSeeChecked = false;
                if (isPlayerNotTracking(player1, player2)) {
                    canSee = checkCanSee(player1, player2, false, maxDistSqr, rayCastDepth);
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
                if (isPlayerNotTracking(player2, player1)
                        && checkCanSee(player2, player1, canSee, maxDistSqr, rayCastDepth)) {
                    getPlayerVisible(player2).add(player1.getId());
                } else {
                    getPlayerVisible(player2).remove(player1.getId());
                }
            }
        }
        removeOldExtras(server);
        extraEntities.forEach((id, extra) -> {
            for (ServerPlayer player : players) {
                if (!extra.onVisibleList(player.getId())) continue;
                if (isPlayerNotTracking(player, extra.entity())
                        && checkCanSee(player, extra.entity(), false, maxDistSqr, rayCastDepth)) {
                    getPlayerVisible(player).add(extra.entity().getId());
                } else {
                    getPlayerVisible(player).remove(extra.entity().getId());
                }
            }
        });
    }

    private void removeOldExtras(MinecraftServer server) {
        extraEntities.entrySet().removeIf(entry ->
                server.getTickCount() - entry.getValue().addTime() > 21);
    }

    public void sendPayload(@NotNull ServerPlayer player, @NotNull Entity target) {
        DPPacketHandler.INSTANCE.sendToPlayer(player, new ToClientRenderTarget(target));
    }

    public void sendPayloads(MinecraftServer server) {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        for (ServerPlayer player : players) {
            IntSet visibles = getPlayerVisible(player);
            for (int id : visibles) {
                Entity target;
                if (extraEntities.containsKey(id)) target = extraEntities.get(id).entity();
                else target = player.getLevel().getEntity(id);
                if (target == null) continue;
                sendPayload(player, target);
            }
        }
    }

    private boolean checkCanSee(ServerPlayer player, Entity target, boolean skipBlockCheck,
                                int maxDistSqr, int rayCastDepth) {
        if (!isSameDimension(player, target)) return false;
        if (!skipBlockCheck) {
            if (player.distanceToSqr(target) > maxDistSqr) return false;
            return UtilEntity.canEntitySeeEntity(player, target, rayCastDepth);
        }
        return true;
    }

    public static boolean isSameDimension(@NotNull Entity e1, @NotNull Entity e2) {
        return e1.getLevel().dimension().equals(e2.getLevel().dimension());
    }

    public boolean isPlayerNotTracking(ServerPlayer player, Entity target) {
        return !getPlayerTracks(player).contains(target.getId());
    }

    public void tick(MinecraftServer server) {
        int checkVisibleRate = DPGameRules.getCheckVisibleRate(server);
        int posUpdateRate = DPGameRules.getPosUpdateRate(server);
        if (server.getTickCount() % checkVisibleRate == 0) checkVisible(server);
        if (server.getTickCount() % posUpdateRate == 0) sendPayloads(server);
    }

    public void onPlayerStartTrack(Player player, Entity target) {
        getPlayerTracks(player).add(target.getId());
        getPlayerVisible(player).remove(target.getId());
    }

    public void onPlayerStopTrack(Player player, Entity target) {
        getPlayerTracks(player).remove(target.getId());
        MinecraftServer server = player.getServer();
        if (server != null && (extraEntities.containsKey(target.getId()) || UtilEntity.isPlayer(target))) {
            int maxDist = DPGameRules.getViewDistance(server);
            int maxDistSqr = maxDist * maxDist;
            int rayCastDepth = DPGameRules.getRayCastDepth(server);
            ServerPlayer sp = (ServerPlayer) player;
            if (checkCanSee(sp, target, false, maxDistSqr, rayCastDepth)) {
                getPlayerVisible(player).add(target.getId());
                sendPayload(sp, target);
            }
        }
    }

    public void onPlayerLogIn(Player player) {

    }

    public void onPlayerLogOut(Player player) {
        tracks.remove(player.getId());
        visible.remove(player.getId());
    }

    private IntSet getPlayerTracks(Player player) {
        return this.tracks.computeIfAbsent(
                player.getId(),
                (id) -> {
                    IntSet set = new IntArraySet();
                    this.tracks.put(player.getId(), set);
                    return set;
                }
        );
    }

    private IntSet getPlayerVisible(Player player) {
        return this.visible.computeIfAbsent(
                player.getId(),
                (id) -> {
                    IntSet set = new IntArraySet();
                    this.visible.put(player.getId(), set);
                    return set;
                }
        );
    }

    private DPServerManager() {}
}

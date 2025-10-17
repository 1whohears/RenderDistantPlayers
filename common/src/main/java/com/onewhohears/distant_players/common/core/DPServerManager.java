package com.onewhohears.distant_players.common.core;

import com.onewhohears.distant_players.common.command.DPGameRules;
import com.onewhohears.distant_players.common.network.packets.toclient.ToClientRenderTarget;
import com.onewhohears.onewholibs.common.core.DistantRayCastManager;
import com.onewhohears.onewholibs.util.UtilEntity;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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

    public static final long RAY_CAST_TIMEOUT = 550;

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
            List<IronGolem> golems = UtilEntity.getLevel(player).getEntitiesOfClass(IronGolem.class, golemBox);
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
        long rayCastLifeTime = getRayCastLifeTime(server);
        int maxDist = DPGameRules.getViewDistance(server);
        int maxDistSqr = maxDist * maxDist;
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        for (int i = 0; i < players.size(); i++) {
            ServerPlayer player1 = players.get(i);
            for (int j = i + 1; j < players.size(); j++) {
                ServerPlayer player2 = players.get(j);
                boolean shouldCheck = false;
                if (isPlayerNotTracking(player1, player2)) {
                    shouldCheck = true;
                } else {
                    getPlayerVisible(player1).remove(player2.getId());
                }
                if (isPlayerNotTracking(player2, player1)) {
                    shouldCheck = true;
                } else {
                    getPlayerVisible(player2).remove(player1.getId());
                }
                if (shouldCheck && basicCheck(player1, player2, maxDistSqr)) {
                    DistantRayCastManager.distantRayCast(getLevel(player1), player1, player2,
                            (level, eyeEntity, targetEntity, pass) -> {
                                if (pass) {
                                    getPlayerVisible((ServerPlayer)eyeEntity).add(targetEntity.getId());
                                    getPlayerVisible((ServerPlayer)targetEntity).add(eyeEntity.getId());
                                } else {
                                    getPlayerVisible((ServerPlayer)eyeEntity).remove(targetEntity.getId());
                                    getPlayerVisible((ServerPlayer)targetEntity).remove(eyeEntity.getId());
                                }
                            },
                            RAY_CAST_TIMEOUT, rayCastLifeTime, 0, 0);
                } else {
                    getPlayerVisible(player1).remove(player2.getId());
                    getPlayerVisible(player2).remove(player1.getId());
                }
            }
        }
        removeOldExtras(server);
        extraEntities.forEach((id, extra) -> {
            for (ServerPlayer player : players) {
                if (!extra.onVisibleList(player.getId())) continue;
                if (isPlayerNotTracking(player, extra.entity())) {
                    DistantRayCastManager.distantRayCast(getLevel(player), player, extra.entity(),
                            (level, eyeEntity, targetEntity, pass) -> {
                                if (pass) {
                                    getPlayerVisible((ServerPlayer)eyeEntity).add(targetEntity.getId());
                                } else {
                                    getPlayerVisible((ServerPlayer)eyeEntity).remove(targetEntity.getId());
                                }
                            },
                            RAY_CAST_TIMEOUT, rayCastLifeTime, 0, 0);
                } else {
                    getPlayerVisible(player).remove(extra.entity().getId());
                }
            }
        });
    }

    private boolean basicCheck(Entity entity1, Entity entity2, double maxDistSqr) {
        return isSameDimension(entity1, entity2) && entity1.distanceToSqr(entity2) <= maxDistSqr;
    }

    private ServerLevel getLevel(ServerPlayer player) {
        return player.serverLevel();
    }

    private void removeOldExtras(MinecraftServer server) {
        extraEntities.entrySet().removeIf(entry ->
                server.getTickCount() - entry.getValue().addTime() > 21);
    }

    public void sendPayload(@NotNull ServerPlayer player, @NotNull Entity target) {
        new ToClientRenderTarget(target).sendTo(player);
    }

    public void sendPayloads(MinecraftServer server) {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        for (ServerPlayer player : players) {
            IntSet visibles = getPlayerVisible(player);
            for (int id : visibles) {
                Entity target;
                if (extraEntities.containsKey(id)) target = extraEntities.get(id).entity();
                else target = UtilEntity.getLevel(player).getEntity(id);
                if (target == null) continue;
                sendPayload(player, target);
            }
        }
    }

    public static boolean isSameDimension(@NotNull Entity e1, @NotNull Entity e2) {
        return UtilEntity.getLevel(e1).dimension().equals(UtilEntity.getLevel(e2).dimension());
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
            if (!basicCheck(player, target, maxDistSqr)) return;
            ServerPlayer sp = (ServerPlayer) player;
            long rayCastLifeTime = getRayCastLifeTime(server);
            DistantRayCastManager.distantRayCast(getLevel(sp), sp, target,
                    (level, eyeEntity, targetEntity, pass) -> {
                        if (pass) {
                            getPlayerVisible((ServerPlayer)eyeEntity).add(targetEntity.getId());
                            sendPayload((ServerPlayer)eyeEntity, targetEntity);
                        }
                    },
                    RAY_CAST_TIMEOUT, rayCastLifeTime, 0, 0);
        }
    }

    public long getRayCastLifeTime(MinecraftServer server) {
        return DPGameRules.getCheckVisibleRate(server) * 50L + 50L;
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

package com.onewhohears.distant_players.common.core;

import com.mojang.logging.LogUtils;
import com.onewhohears.distant_players.common.command.DPGameRules;
import com.onewhohears.distant_players.common.network.packets.toclient.ToClientRenderTarget;
import com.onewhohears.onewholibs.common.core.DistantVisibleManager;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * Brain of the mod. Responsible for coordinating tracked entity information and updating the information in
 * the {@link com.onewhohears.distant_players.client.core.DPClientManager}. Sends entity information to other
 * clients, and doesn't when it's deemed that they shouldn't be able to see each other.
 */
public final class DPServerManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static DPServerManager INSTANCE;

    public static void init() {
        INSTANCE = new DPServerManager();
    }

    public static DPServerManager get() {
        return INSTANCE;
    }

    public static final long RAY_CAST_TIMEOUT = 550;
    public static final int VISIBLE_UPDATE_RATE = 10;

    private final IntObjectMap<IntSet> tracks = new IntObjectHashMap<>();
    private final IntObjectMap<IntSet> visible = new IntObjectHashMap<>();
    private final IntObjectMap<ExtraEntity> extraEntities = new IntObjectHashMap<>();
    private final Set<Integer> removeExtras = new HashSet<>();

    /**
     * Allow players to see non player entities from a distance.
     * This must be called at least once per second to keep the entity visible.
     * You may also specify that only specific players can see this entity from a distance.
     */
    public void addExtraTrackableEntity(@NotNull MinecraftServer server, @NotNull Entity entity,
                                        @Nullable BiFunction<Level, Integer, Entity> customEntityGetter,
                                        @NotNull ServerPlayer... visibleTo) {
        int max = DPGameRules.getMaxExtraEntities(server);
        if (extraEntities.size() >= max) return;
        int[] visibleToIDs = new int[visibleTo.length];
        for (int i = 0; i < visibleTo.length; ++i) visibleToIDs[i] = visibleTo[i].getId();
        ExtraEntity extra = new ExtraEntity(entity.getId(), server.getTickCount(), visibleToIDs, customEntityGetter);
        extraEntities.put(entity.getId(), extra);
    }

    /**
     * Allow players to see non player entities from a distance.
     * This must be called at least once per second to keep the entity visible.
     * You may also specify that only specific players can see this entity from a distance.
     */
    public void addExtraTrackableEntity(@NotNull MinecraftServer server, @NotNull Entity entity,
                                        @NotNull ServerPlayer... visibleTo) {
        addExtraTrackableEntity(server, entity, null, visibleTo);
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

    record ExtraEntity(int entityId, int addTime, int[] visibleToIDs,
                       @Nullable BiFunction<Level, Integer, Entity> customEntityGetter) {
        boolean onVisibleList(int id) {
            if (visibleToIDs.length == 0) return true;
            for (int visibleToID : visibleToIDs) if (id == visibleToID) return true;
            return false;
        }
        @Nullable
        Entity getEntity(@NotNull Level level) {
            if (customEntityGetter != null) return customEntityGetter.apply(level, entityId);
            return level.getEntity(entityId);
        }
    }

    public static final DistantVisibleManager.VisibleRequestData PLAYER_VISIBLE_DATA = new DistantVisibleManager.VisibleRequestData(
            0x4501, 20, VISIBLE_UPDATE_RATE, event -> {
                if (event.result().passed) {
                    get().getPlayerVisible(event.data().entityId1).add(event.data().entityId2);
                    get().getPlayerVisible(event.data().entityId2).add(event.data().entityId1);
                } else {
                    get().getPlayerVisible(event.data().entityId1).remove(event.data().entityId2);
                    get().getPlayerVisible(event.data().entityId2).remove(event.data().entityId1);
                }
    });

    public static final DistantVisibleManager.VisibleRequestData EXTRA_ENTITY_VISIBLE_DATA = new DistantVisibleManager.VisibleRequestData(
            0x4502, 20, VISIBLE_UPDATE_RATE, event -> {
        if (event.result().passed) {
            get().getPlayerVisible(event.data().entityId1).add(event.data().entityId2);
        } else {
            get().getPlayerVisible(event.data().entityId1).remove(event.data().entityId2);
        }
    });

    public static final DistantVisibleManager.VisibleRequestData STOP_TRACK_VISIBLE_DATA = new DistantVisibleManager.VisibleRequestData(
            0x4503, 20, 15, event -> {
        if (event.result().passed) {
            get().getPlayerVisible(event.data().entityId1).add(event.data().entityId2);
            get().sendPayload((ServerPlayer)event.entity1(), event.entity2());
        } else {
            get().getPlayerVisible(event.data().entityId1).remove(event.data().entityId2);
        }
    });

    public void checkVisible(MinecraftServer server) {
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
                    DistantVisibleManager.queryVisible(server, player1, player2, PLAYER_VISIBLE_DATA);
                } else {
                    getPlayerVisible(player1).remove(player2.getId());
                    getPlayerVisible(player2).remove(player1.getId());
                }
            }
        }
        removeOldExtras(server);
        extraEntities.forEach((id, extra) -> {
            for (ServerPlayer player : players) {
                Entity entity = extra.getEntity(UtilEntity.getLevel(player));
                if (entity != null && extra.onVisibleList(player.getId())
                        && !isInvisible(entity)
                        && isPlayerNotTracking(player, entity)) {
                    DistantVisibleManager.queryVisible(server, player, entity, EXTRA_ENTITY_VISIBLE_DATA);
                } else {
                    getPlayerVisible(player).remove(extra.entityId());
                }
            }
        });
    }

    private boolean basicCheck(Entity entity1, Entity entity2, double maxDistSqr) {
        return isSameDimension(entity1, entity2) && entity1.distanceToSqr(entity2) <= maxDistSqr
                && !(isInvisible(entity1) && isInvisible(entity2));
    }

    public static boolean isInvisible(Entity entity) {
        return entity.isInvisible() || entity.isSpectator();
    }

    private ServerLevel getLevel(ServerPlayer player) {
        return player.serverLevel();
    }

    private void removeOldExtras(MinecraftServer server) {
        int tickCount = server.getTickCount();
        extraEntities.forEach((id, extra) -> {
            if (tickCount - extra.addTime() > 21) removeExtras.add(id);
        });
        removeExtras.forEach(extraEntities::remove);
        visible.forEach((id, ids) -> ids.removeAll(removeExtras));
        removeExtras.clear();
    }

    public void sendPayload(@NotNull ServerPlayer player, @NotNull Entity target) {
        if (isInvisible(target)) return;
        new ToClientRenderTarget(target).sendTo(player);
    }

    public void sendPayloads(MinecraftServer server) {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        for (ServerPlayer player : players) {
            IntSet visibles = getPlayerVisible(player);
            for (int id : visibles) {
                Entity target;
                if (extraEntities.containsKey(id)) {
                    target = extraEntities.get(id).getEntity(UtilEntity.getLevel(player));
                } else target = UtilEntity.getLevel(player).getEntity(id);
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
        int posUpdateRate = DPGameRules.getPosUpdateRate(server);
        if (server.getTickCount() % VISIBLE_UPDATE_RATE == 0) checkVisible(server);
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
            if (isInvisible(target)) return;
            DistantVisibleManager.queryVisible(server, player, target, STOP_TRACK_VISIBLE_DATA);
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

    private IntSet getPlayerVisible(int playerId) {
        return this.visible.computeIfAbsent(
                playerId,
                (id) -> {
                    IntSet set = new IntArraySet();
                    this.visible.put(playerId, set);
                    return set;
                }
        );
    }

    private IntSet getPlayerVisible(Player player) {
        return getPlayerVisible(player.getId());
    }

    private DPServerManager() {}
}

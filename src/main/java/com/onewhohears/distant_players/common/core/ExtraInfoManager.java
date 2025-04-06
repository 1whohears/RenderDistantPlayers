package com.onewhohears.distant_players.common.core;

import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Associates an <code>ExtraRenderTargetInfo</code> with a <code>String</code> as an entity type's ID
 */
public class ExtraInfoManager {
    private static final Map<String, Supplier<ExtraRenderTargetInfo>> EXTRAS = new HashMap<>();

    /**
     * this is called during {@link net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent}.
     * use this to add additional data to vanilla entities and your modded entities.
     */
    public static void register(EntityType<?> type, Supplier<ExtraRenderTargetInfo> extra) {
        EXTRAS.put(EntityType.getKey(type).toString(), extra);
    }

    /**
     * returns a registered {@link ExtraRenderTargetInfo} or the default
     * {@link ExtraRenderTargetInfo.DefaultRenderInfo} which only handles yaw rotation rendering.
     */
    @NotNull
    public static ExtraRenderTargetInfo get(String entityTypeId) {
        return EXTRAS.getOrDefault(entityTypeId, ExtraRenderTargetInfo.DefaultRenderInfo::new).get();
    }
}

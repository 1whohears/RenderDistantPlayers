package com.onewhohears.distant_players.client.core;

import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ExtraInfoManager {

    private static final Map<String, Supplier<ExtraRenderTargetInfo>> extras = new HashMap<>();

    /**
     * this is called during {@link net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent}.
     * use this to add additional data to vanilla entities and your modded entities.
     */
    public static void register(EntityType<?> type, Supplier<ExtraRenderTargetInfo> extra) {
        extras.put(EntityType.getKey(type).toString(), extra);
    }

    /**
     * returns a registered {@link ExtraRenderTargetInfo} or the default
     * {@link ExtraRenderTargetInfo.DefaultRenderInfo} which only handles yaw rotation rendering.
     */
    @NotNull
    public static ExtraRenderTargetInfo get(String entityTypeId) {
        return extras.getOrDefault(entityTypeId, ExtraRenderTargetInfo.DefaultRenderInfo::new).get();
    }

}

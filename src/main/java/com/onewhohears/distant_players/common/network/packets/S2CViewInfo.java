package com.onewhohears.distant_players.common.network.packets;

import com.google.common.collect.ImmutableMap;
import com.onewhohears.distant_players.common.core.DPClientManager;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;

public class S2CViewInfo {
    private final Map<ResourceKey<Level>, IntList> distantEntities;

    public S2CViewInfo(Map<ResourceKey<Level>, IntSet> distantEntities) {
        ImmutableMap.Builder<ResourceKey<Level>, IntList> builder = ImmutableMap.builder();

        for (Map.Entry<ResourceKey<Level>, IntSet> entry : distantEntities.entrySet()) {
            builder.put(entry.getKey(), new IntArrayList(entry.getValue()));
        }

        this.distantEntities = builder.build();
    }

    public S2CViewInfo(FriendlyByteBuf buf) {
        this.distantEntities = buf.readMap(
                reader -> reader.readResourceKey(Registry.DIMENSION_REGISTRY),
                FriendlyByteBuf::readIntIdList
        );
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeMap(
                this.distantEntities,
                FriendlyByteBuf::writeResourceKey,
                FriendlyByteBuf::writeIntIdList
        );
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                // Make sure it's only executed on the physical client
                DistExecutor.unsafeRunWhenOn(
                        Dist.CLIENT,
                        () -> () -> DPClientManager.get().update(this.distantEntities)
                )
        );
        ctx.get().setPacketHandled(true);
    }
}

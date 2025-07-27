package com.onewhohears.distant_players.common.core;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlayerRenderInfo implements ExtraRenderTargetInfo {

    @NotNull private ItemStack mainHand = ItemStack.EMPTY;
    @NotNull private ItemStack offHand = ItemStack.EMPTY;
    @NotNull private List<ItemStack> armor = NonNullList.withSize(4, ItemStack.EMPTY);
    private boolean fallFlying;

    @Override
    public void tickFakeEntity(@NotNull Entity entity) {

    }

    @Override
    public void updateFakeEntity(@NotNull Entity entity) {
        if (!(entity instanceof Player player)) return;
        player.setItemInHand(InteractionHand.MAIN_HAND, mainHand);
        player.setItemInHand(InteractionHand.OFF_HAND, offHand);
        for (int i = 0; i < armor.size(); ++i)
            player.getInventory().armor.set(i, armor.get(i));
        // TODO use accessor mixin Entity#setSharedFlag(7, fallFlying) to set elytra flying to true
    }

    @Override
    public void setupEntityOnCreate(@NotNull Entity entity) {

    }

    @Override
    public void getInfoServerSide(@NotNull Entity entity) {
        if (!(entity instanceof Player player)) return;
        mainHand = player.getMainHandItem();
        offHand = player.getOffhandItem();
        armor = player.getInventory().armor;
        fallFlying = player.isFallFlying();
    }

    @Override
    public void getInfoClientSide(FriendlyByteBuf buffer) {
        mainHand = buffer.readItem();
        offHand = buffer.readItem();
        int armorCount = buffer.readInt();
        for (int i = 0; i < armorCount; i++) {
            ItemStack stack = buffer.readItem();
            armor.set(i, stack);
        }
        fallFlying = buffer.readBoolean();
    }

    @Override
    public void encodeInfoServerSide(FriendlyByteBuf buffer) {
        buffer.writeItem(mainHand);
        buffer.writeItem(offHand);
        buffer.writeInt(armor.size());
        for (ItemStack stack : armor) buffer.writeItem(stack);
        buffer.writeBoolean(fallFlying);
    }
}

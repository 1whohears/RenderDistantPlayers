package com.onewhohears.distant_players.common.core.extra_render_info;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlayerRenderInfo extends LivingEntityRenderInfo {

    @NotNull private List<ItemStack> armor = NonNullList.withSize(4, ItemStack.EMPTY);

    @Override
    public void updateFakeEntity(@NotNull Entity entity) {
        super.updateFakeEntity(entity);
        if (!(entity instanceof Player player)) return;
        for (int i = 0; i < armor.size(); ++i)
            player.getInventory().armor.set(i, armor.get(i));
    }

    @Override
    public void getInfoServerSide(@NotNull Entity entity) {
        super.getInfoServerSide(entity);
        if (!(entity instanceof Player player)) return;
        armor = player.getInventory().armor;
    }

    @Override
    public void getInfoClientSide(FriendlyByteBuf buffer) {
        super.getInfoClientSide(buffer);
        int armorCount = buffer.readInt();
        for (int i = 0; i < armorCount; i++) {
            ItemStack stack = buffer.readItem();
            armor.set(i, stack);
        }
    }

    @Override
    public void encodeInfoServerSide(FriendlyByteBuf buffer) {
        super.encodeInfoServerSide(buffer);
        buffer.writeInt(armor.size());
        for (ItemStack stack : armor) buffer.writeItem(stack);
    }
}

package mekanism.common.item;

import javax.annotation.Nonnull;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;

public class ItemProxy extends Item {

    public ItemProxy(Properties properties) {
        super(properties.maxDamage(1));
    }

    @Nonnull
    @Override
    public ItemStack getContainerItem(@Nonnull ItemStack stack) {
        return getSavedItem(stack);
    }

    @Override
    public boolean hasContainerItem(ItemStack itemStack) {
        return !getSavedItem(itemStack).isEmpty();
    }

    public void setSavedItem(ItemStack stack, ItemStack save) {
        if (save == null || save.isEmpty()) {
            ItemDataUtils.setBoolean(stack, "hasStack", false);
            ItemDataUtils.removeData(stack, "savedItem");
        } else {
            ItemDataUtils.setBoolean(stack, "hasStack", true);
            ItemDataUtils.setCompound(stack, "savedItem", save.write(new CompoundNBT()));
        }
    }

    public ItemStack getSavedItem(ItemStack stack) {
        if (ItemDataUtils.getBoolean(stack, "hasStack")) {
            return ItemStack.read(ItemDataUtils.getCompound(stack, "savedItem"));
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
                ItemStack itemStack = player.inventory.mainInventory.get(i);
                if (!itemStack.isEmpty() && itemStack.getItem() == this) {
                    player.inventory.mainInventory.remove(i);
                }
            }
        }
    }
}
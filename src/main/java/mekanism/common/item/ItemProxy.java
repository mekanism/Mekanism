package mekanism.common.item;

import javax.annotation.Nonnull;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ItemProxy extends Item {

    public ItemProxy() {
        super();
        setMaxDamage(1);
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
            ItemDataUtils.setCompound(stack, "savedItem", save.writeToNBT(new NBTTagCompound()));
        }
    }

    public ItemStack getSavedItem(ItemStack stack) {
        if (ItemDataUtils.getBoolean(stack, "hasStack")) {
            return new ItemStack(ItemDataUtils.getCompound(stack, "savedItem"));
        }

        return ItemStack.EMPTY;
    }

    @Override
    public void onUpdate(ItemStack stacks, World world, Entity entity, int j, boolean flag) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;

            for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
                if (!player.inventory.mainInventory.get(i).isEmpty()
                      && player.inventory.mainInventory.get(i).getItem() == this) {
                    player.inventory.mainInventory.remove(i);
                }
            }
        }
    }
}

package mekanism.tools.common.recipe;

import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.tools.common.item.ItemMekanismShield;
import mekanism.tools.common.registries.ToolsRecipeSerializers;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.BannerItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class MekBannerShieldRecipe extends SpecialRecipe {

    public MekBannerShieldRecipe(ResourceLocation id) {
        super(id);
    }

    @Override
    public boolean matches(CraftingInventory inv, @Nonnull World world) {
        ItemStack shieldStack = ItemStack.EMPTY;
        ItemStack bannerStack = ItemStack.EMPTY;
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stackInSlot = inv.getItem(i);
            if (!stackInSlot.isEmpty()) {
                if (stackInSlot.getItem() instanceof BannerItem) {
                    if (!bannerStack.isEmpty()) {
                        return false;
                    }
                    bannerStack = stackInSlot;
                } else {
                    if (!(stackInSlot.getItem() instanceof ItemMekanismShield) || !shieldStack.isEmpty() || stackInSlot.getTagElement(NBTConstants.BLOCK_ENTITY_TAG) != null) {
                        return false;
                    }
                    shieldStack = stackInSlot;
                }
            }
        }
        return !shieldStack.isEmpty() && !bannerStack.isEmpty();
    }

    @Nonnull
    @Override
    public ItemStack assemble(CraftingInventory inv) {
        ItemStack bannerStack = ItemStack.EMPTY;
        ItemStack shieldStack = ItemStack.EMPTY;
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stackInSlot = inv.getItem(i);
            if (!stackInSlot.isEmpty()) {
                if (stackInSlot.getItem() instanceof BannerItem) {
                    bannerStack = stackInSlot;
                } else if (stackInSlot.getItem() instanceof ItemMekanismShield) {
                    shieldStack = stackInSlot.copy();
                }
            }
        }
        if (shieldStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        CompoundNBT blockEntityTag = bannerStack.getTagElement(NBTConstants.BLOCK_ENTITY_TAG);
        CompoundNBT tag = blockEntityTag == null ? new CompoundNBT() : blockEntityTag.copy();
        tag.putInt(NBTConstants.BASE, ((BannerItem) bannerStack.getItem()).getColor().getId());
        shieldStack.addTagElement(NBTConstants.BLOCK_ENTITY_TAG, tag);
        return shieldStack;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ToolsRecipeSerializers.BANNER_SHIELD.get();
    }
}
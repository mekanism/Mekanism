package mekanism.common.recipe.bin;

import javax.annotation.Nonnull;
import mekanism.common.inventory.BinMekanismInventory;
import mekanism.common.inventory.slot.BinInventorySlot;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

//Note: We don't bother checking anywhere to ensure the bin's item stack size is one, as we only allow bins
// to be in stacks of one anyways. If this changes at some point, then we will need to adjust this recipe
public abstract class BinRecipe extends SpecialRecipe {

    protected BinRecipe(ResourceLocation id) {
        super(id);
    }

    protected static BinInventorySlot convertToSlot(@Nonnull ItemStack binStack) {
        return BinMekanismInventory.create(binStack).getBinSlot();
    }

    @Override
    public abstract boolean matches(@Nonnull CraftingInventory inv, @Nonnull World world);

    @Nonnull
    @Override
    public abstract ItemStack getCraftingResult(@Nonnull CraftingInventory inv);

    @Nonnull
    @Override
    public abstract NonNullList<ItemStack> getRemainingItems(CraftingInventory inv);

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 1;
    }
}
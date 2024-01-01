package mekanism.common.recipe.bin;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.inventory.BinMekanismInventory;
import mekanism.common.inventory.slot.BinInventorySlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;

//Note: We don't bother checking anywhere to ensure the bin's item stack size is one, as we only allow bins
// to be in stacks of one anyway. If this changes at some point, then we will need to adjust this recipe
@NothingNullByDefault
public abstract class BinRecipe extends CustomRecipe {

    protected BinRecipe(CraftingBookCategory category) {
        super(category);
    }

    protected static BinInventorySlot convertToSlot(ItemStack binStack) {
        return BinMekanismInventory.create(binStack).getBinSlot();
    }

    @Override
    public boolean isIncomplete() {
        return false;
    }
}
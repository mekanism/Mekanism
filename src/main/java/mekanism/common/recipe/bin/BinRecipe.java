package mekanism.common.recipe.bin;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.common.inventory.BinMekanismInventory;
import mekanism.common.inventory.slot.BinInventorySlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.ResourceLocation;

//Note: We don't bother checking anywhere to ensure the bin's item stack size is one, as we only allow bins
// to be in stacks of one anyways. If this changes at some point, then we will need to adjust this recipe
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class BinRecipe extends SpecialRecipe {

    protected BinRecipe(ResourceLocation id) {
        super(id);
    }

    protected static BinInventorySlot convertToSlot(ItemStack binStack) {
        return BinMekanismInventory.create(binStack).getBinSlot();
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 1;
    }
}
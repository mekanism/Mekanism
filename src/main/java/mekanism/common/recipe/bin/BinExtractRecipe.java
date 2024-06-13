package mekanism.common.recipe.bin;

import mekanism.api.Action;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.attachments.containers.item.ComponentBackedBinInventorySlot;
import mekanism.common.item.block.ItemBlockBin;
import mekanism.common.registries.MekanismRecipeSerializersInternal;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

@NothingNullByDefault
public class BinExtractRecipe extends BinRecipe {

    public BinExtractRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput inv, Level world) {
        ItemStack binStack = findBinStack(inv);
        if (binStack.isEmpty()) {
            //If we didn't find a singular bin our recipe can't possibly match
            return false;
        }
        //Only match the recipe if we have items in the bin that we can extract from
        return !convertToSlot(binStack).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider provider) {
        ItemStack binStack = findBinStack(inv);
        if (binStack.isEmpty()) {
            //If we didn't find a singular bin our recipe can't possibly match
            return ItemStack.EMPTY;
        }
        //Display that our output will be the bottom stack
        return convertToSlot(binStack).getBottomStack();
    }

    private ItemStack findBinStack(CraftingInput inv) {
        ItemStack binStack = ItemStack.EMPTY;
        //Note: We don't use inv#getItems as that may do unnecessary copies depending on impl
        for (int i = 0, slots = inv.size(); i < slots; ++i) {
            ItemStack stackInSlot = inv.getItem(i);
            if (!stackInSlot.isEmpty()) {
                if (stackInSlot.getItem() instanceof ItemBlockBin) {
                    if (!binStack.isEmpty() || stackInSlot.getCount() > 1) {
                        //If we already have a bin, or our first bin has a stack size greater than one then this is not a bin recipe
                        return ItemStack.EMPTY;
                    }
                    binStack = stackInSlot;
                } else {
                    //This recipe only allows extracting from bins, so it has to be only a bin
                    return ItemStack.EMPTY;
                }
            }
        }
        return binStack;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
        int slots = inv.size();
        NonNullList<ItemStack> remaining = NonNullList.withSize(slots, ItemStack.EMPTY);
        for (int i = 0; i < slots; ++i) {
            ItemStack stackInSlot = inv.getItem(i);
            if (stackInSlot.getItem() instanceof ItemBlockBin) {
                ItemStack binStack = stackInSlot.copy();
                ComponentBackedBinInventorySlot slot = convertToSlot(binStack);
                ItemStack bottomStack = slot.getBottomStack();
                if (!bottomStack.isEmpty()) {
                    //Only attempt to do anything if there are items to try and remove
                    MekanismUtils.logMismatchedStackSize(slot.shrinkStack(bottomStack.getCount(), Action.EXECUTE), bottomStack.getCount());
                    remaining.set(i, binStack);
                }
                break;
            }
        }
        return remaining;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return MekanismRecipeSerializersInternal.BIN_EXTRACT.get();
    }
}
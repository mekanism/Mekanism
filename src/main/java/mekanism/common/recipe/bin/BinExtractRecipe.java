package mekanism.common.recipe.bin;

import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.Action;
import mekanism.common.inventory.slot.BinInventorySlot;
import mekanism.common.item.block.ItemBlockBin;
import mekanism.common.registries.MekanismRecipeSerializers;
import mekanism.common.util.MekanismUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BinExtractRecipe extends BinRecipe {

    public BinExtractRecipe(ResourceLocation id) {
        super(id);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level world) {
        ItemStack binStack = ItemStack.EMPTY;
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stackInSlot = inv.getItem(i);
            if (!stackInSlot.isEmpty()) {
                if (stackInSlot.getItem() instanceof ItemBlockBin) {
                    if (!binStack.isEmpty()) {
                        //If we already have a bin then this is not a bin recipe
                        return false;
                    }
                    binStack = stackInSlot;
                } else {
                    //This recipe only allows extracting from bins, so it has to be only a bin
                    return false;
                }
            }
        }
        if (binStack.isEmpty()) {
            //If we didn't find a bin our recipe can't possibly match
            return false;
        }
        //Only match the recipe if we have items in the bin that we can extract
        return !convertToSlot(binStack).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingContainer inv) {
        ItemStack binStack = ItemStack.EMPTY;
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stackInSlot = inv.getItem(i);
            if (!stackInSlot.isEmpty()) {
                if (stackInSlot.getItem() instanceof ItemBlockBin) {
                    if (!binStack.isEmpty()) {
                        //If we already have a bin then this is not a bin recipe
                        return ItemStack.EMPTY;
                    }
                    binStack = stackInSlot;
                } else {
                    //This recipe only allows extracting from bins, so it has to be only a bin
                    return ItemStack.EMPTY;
                }
            }
        }
        if (binStack.isEmpty()) {
            //If we didn't find a bin our recipe can't possibly match
            return ItemStack.EMPTY;
        }
        //Display that our output will be the bottom stack
        return convertToSlot(binStack).getBottomStack();
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < remaining.size(); ++i) {
            ItemStack stackInSlot = inv.getItem(i);
            if (stackInSlot.getItem() instanceof ItemBlockBin) {
                ItemStack binStack = stackInSlot.copy();
                BinInventorySlot slot = convertToSlot(binStack);
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
    public RecipeSerializer<?> getSerializer() {
        return MekanismRecipeSerializers.BIN_EXTRACT.get();
    }
}
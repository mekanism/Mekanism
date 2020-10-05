package mekanism.common.recipe.bin;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.common.inventory.slot.BinInventorySlot;
import mekanism.common.item.block.ItemBlockBin;
import mekanism.common.registries.MekanismRecipeSerializers;
import mekanism.common.util.MekanismUtils;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BinExtractRecipe extends BinRecipe {

    public BinExtractRecipe(ResourceLocation id) {
        super(id);
    }

    @Override
    public boolean matches(CraftingInventory inv, World world) {
        ItemStack binStack = ItemStack.EMPTY;
        for (int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack stackInSlot = inv.getStackInSlot(i);
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
    public ItemStack getCraftingResult(CraftingInventory inv) {
        ItemStack binStack = ItemStack.EMPTY;
        for (int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack stackInSlot = inv.getStackInSlot(i);
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
    public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
        for (int i = 0; i < remaining.size(); ++i) {
            ItemStack stackInSlot = inv.getStackInSlot(i);
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
    public IRecipeSerializer<?> getSerializer() {
        return MekanismRecipeSerializers.BIN_EXTRACT.getRecipeSerializer();
    }
}
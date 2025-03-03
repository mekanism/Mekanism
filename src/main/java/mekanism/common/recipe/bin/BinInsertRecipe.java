package mekanism.common.recipe.bin;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.attachments.containers.item.ComponentBackedBinInventorySlot;
import mekanism.common.item.block.ItemBlockBin;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.registries.MekanismRecipeSerializersInternal;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.ItemCraftedEvent;

//TODO: Test this recipe in various modded crafting tables/auto crafters
@NothingNullByDefault
public class BinInsertRecipe extends BinRecipe {

    public BinInsertRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput inv, Level world) {
        ItemStack binStack = ItemStack.EMPTY;
        ItemStack foundType = ItemStack.EMPTY;
        //Note: We don't use inv#getItems as that may do unnecessary copies depending on impl
        for (int i = 0, slots = inv.size(); i < slots; ++i) {
            ItemStack stackInSlot = inv.getItem(i);
            if (!stackInSlot.isEmpty()) {
                if (stackInSlot.getItem() instanceof ItemBlockBin) {
                    if (!binStack.isEmpty() || stackInSlot.getCount() > 1) {
                        //If we already have a bin, or our first bin has a stack size greater than one then this is not a bin recipe
                        return false;
                    }
                    binStack = stackInSlot;
                } else if (foundType.isEmpty()) {
                    foundType = stackInSlot;
                } else if (!ItemStack.isSameItemSameComponents(foundType, stackInSlot)) {
                    //If we have types that don't stack in the grid at once,
                    // then we cannot combine them both into the bin
                    return false;
                }
            }
        }
        if (binStack.isEmpty() || foundType.isEmpty()) {
            //If we didn't find a bin or an item to add it, we don't match the bin insertion recipe
            return false;
        }
        ComponentBackedBinInventorySlot slot = convertToSlot(binStack);
        ItemStack remaining = slot.insertItem(foundType, Action.SIMULATE, AutomationType.MANUAL);
        //Return that it doesn't match if our simulation claims we would not be able to accept any items into the bin
        return !ItemStack.matches(remaining, foundType);
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider provider) {
        ItemStack binStack = ItemStack.EMPTY;
        ItemStack foundType = ItemStack.EMPTY;
        List<ItemStack> foundItems = new ArrayList<>();
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
                    continue;
                } else if (foundType.isEmpty()) {
                    foundType = stackInSlot;
                } else if (!ItemStack.isSameItemSameComponents(foundType, stackInSlot)) {
                    //If we have types that don't stack in the grid at once,
                    // then we cannot combine them both into the bin
                    return ItemStack.EMPTY;
                }
                foundItems.add(stackInSlot);
            }
        }
        if (binStack.isEmpty() || foundType.isEmpty()) {
            //If we didn't find a bin or an item to add it, we don't match the bin insertion recipe
            return ItemStack.EMPTY;
        }
        //Copy the stack
        binStack = binStack.copy();
        ComponentBackedBinInventorySlot slot = convertToSlot(binStack);
        boolean hasInserted = false;
        for (ItemStack stack : foundItems) {
            //Try inserting a single item (as crafting grids only go one item at a time)
            //TODO: This is part of what causes it to show a lower number than what potentially gets handled by the container
            // and is the part we need to address and change for when handling it as a SpecialQIORecipe
            ItemStack toInsert = stack.copyWithCount(1);
            ItemStack remainder = slot.insertItem(toInsert, Action.EXECUTE, AutomationType.MANUAL);
            if (remainder.isEmpty()) {
                //We could insert it
                hasInserted = true;
            } else if (hasInserted) {
                //We managed to insert some of it into the bin, just return our bin stack without marking it as being from a recipe
                // as there is no benefit to checking if we can insert extra stuff if we know we can't fit more
                return binStack;
            } else {
                //Return that it doesn't match if we aren't actually able to insert any items into the bin
                return ItemStack.EMPTY;
            }
        }
        //TODO: I think we can just skip this when handling it as a SpecialQIORecipe
        binStack.set(MekanismDataComponents.FROM_RECIPE, true);
        return binStack;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
        int slots = inv.size();
        NonNullList<ItemStack> remainingItems = NonNullList.withSize(slots, ItemStack.EMPTY);
        ItemStack binStack = ItemStack.EMPTY;
        ItemStack foundType = ItemStack.EMPTY;
        Int2ObjectMap<ItemStack> foundSlots = new Int2ObjectArrayMap<>(slots);
        for (int i = 0; i < slots; ++i) {
            ItemStack stackInSlot = inv.getItem(i);
            if (!stackInSlot.isEmpty()) {
                if (stackInSlot.getItem() instanceof ItemBlockBin) {
                    if (!binStack.isEmpty()) {
                        //If we already have a bin then this is not a bin recipe
                        return remainingItems;
                    }
                    binStack = stackInSlot;
                    continue;
                } else if (foundType.isEmpty()) {
                    foundType = stackInSlot;
                } else if (!ItemStack.isSameItemSameComponents(foundType, stackInSlot)) {
                    //If we have types that don't stack in the grid at once,
                    // then we cannot combine them both into the bin
                    return remainingItems;
                }
                foundSlots.put(i, stackInSlot);
            }
        }
        if (binStack.isEmpty() || foundType.isEmpty()) {
            //If we didn't find a bin or an item to add it, we don't match the bin insertion recipe
            return remainingItems;
        }
        //Copy the stack
        binStack = binStack.copy();
        ComponentBackedBinInventorySlot slot = convertToSlot(binStack);
        for (ObjectIterator<Int2ObjectMap.Entry<ItemStack>> iterator = Int2ObjectMaps.fastIterator(foundSlots); iterator.hasNext(); ) {
            Int2ObjectMap.Entry<ItemStack> entry = iterator.next();
            ItemStack slotItem = entry.getValue();
            //Only try inserting a single item into the bin. We execute on a copy of the bin stack so that we can mutate it and chain insertions
            // to validate if we can insert across multiple slots
            //TODO: Do we want to allow inserting more when we are acting as a SpecialQIORecipe? (Is that even the case for this as it is the remainder)
            ItemStack remaining = slot.insertItem(slotItem.copyWithCount(1), Action.EXECUTE, AutomationType.MANUAL);
            if (!remaining.isEmpty()) {
                //Can't insert the stack so just mark that we still have a left-over item in that slot
                remainingItems.set(entry.getIntKey(), remaining);
            }
        }
        return remainingItems;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        //Require at least two slots as we have to represent at least the bin and the stack we are adding to it
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return MekanismRecipeSerializersInternal.BIN_INSERT.get();
    }

    public static void onCrafting(ItemCraftedEvent event) {
        ItemStack result = event.getCrafting();
        if (!result.isEmpty() && result.getItem() instanceof ItemBlockBin) {
            //Remove the marker that the bin was crafted from a bin recipe
            Boolean fromRecipe = result.remove(MekanismDataComponents.FROM_RECIPE);
            if (fromRecipe != null && fromRecipe) {
                //And if it was try to move extra items from the container into it
                ComponentBackedBinInventorySlot slot = convertToSlot(result);
                ItemStack storedStack = slot.getStack();
                if (!storedStack.isEmpty()) {
                    Container craftingMatrix = event.getInventory();
                    for (int i = 0, slots = craftingMatrix.getContainerSize(); i < slots; ++i) {
                        ItemStack stack = craftingMatrix.getItem(i);
                        //Check remaining items
                        if (stack.getCount() > 1 && ItemStack.isSameItemSameComponents(storedStack, stack)) {
                            //Try to insert any excess items in the slot (we lower it by one as the input slots have not been lowered yet)
                            ItemStack toInsert = stack.copyWithCount(stack.getCount() - 1);
                            ItemStack remaining = slot.insertItem(toInsert, Action.EXECUTE, AutomationType.MANUAL);
                            if (remaining.isEmpty()) {
                                //Set it to the single item we skipped
                                craftingMatrix.setItem(i, stack.copyWithCount(1));
                            } else if (remaining.getCount() < toInsert.getCount()) {
                                //Set the stack to whatever amount we were unable to insert
                                craftingMatrix.setItem(i, stack.copyWithCount(remaining.getCount() + 1));
                            }
                        }
                    }
                }
            }
        }
    }
}
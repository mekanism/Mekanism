package mekanism.common.recipe.bin;

import mekanism.api.AutomationType;
import mekanism.common.component.containers.item.ComponentBackedBinInventorySlot;
import mekanism.common.item.block.ItemBlockBin;
import mekanism.common.lib.transaction.TransactionHelper;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.registries.MekanismRecipeSerializersInternal;
import mekanism.common.util.ItemAccessUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.ItemCraftedEvent;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

//TODO: Test this recipe in various modded crafting tables/auto crafters
public class BinInsertRecipe extends BinRecipe {

    public static final BinInsertRecipe INSTANCE = new BinInsertRecipe();

    @Override
    public boolean matches(CraftingInput inv, Level world) {
        ItemResource binType = ItemResource.EMPTY;
        ItemResource foundType = ItemResource.EMPTY;
        //Note: We don't use inv#getItems as that may do unnecessary copies depending on impl
        for (int i = 0, slots = inv.size(); i < slots; ++i) {
            ItemStack stackInSlot = inv.getItem(i);
            if (!stackInSlot.isEmpty()) {
                if (stackInSlot.getItem() instanceof ItemBlockBin) {
                    if (!binType.isEmpty() || stackInSlot.count() > 1) {
                        //If we already have a bin, or our first bin has a stack size greater than one then this is not a bin recipe
                        return false;
                    }
                    binType = ItemResource.of(stackInSlot);
                } else if (foundType.isEmpty()) {
                    foundType = ItemResource.of(stackInSlot);
                } else if (!foundType.matches(stackInSlot)) {
                    //If we have types that don't stack in the grid at once,
                    // then we cannot combine them both into the bin
                    return false;
                }
            }
        }
        if (binType.isEmpty() || foundType.isEmpty()) {
            //If we didn't find a bin or an item to add it, we don't match the bin insertion recipe
            return false;
        }
        ComponentBackedBinInventorySlot slot = convertToSlot(ItemAccessUtils.sideEffectFreeAccess(binType));
        //Protect against any mods that might be doing transactional logic, such as if an auto crafter validates it has enough energy before calling this method
        try (Transaction simulation = TransactionHelper.openTransactionSafe()) {
            //Return that it doesn't match if our simulation claims we would not be able to accept any items into the bin
            return slot.insert(foundType, 1, simulation, AutomationType.MANUAL) > 0;
        }
    }

    @Override
    public ItemStack assemble(CraftingInput inv) {
        ItemResource binType = ItemResource.EMPTY;
        ItemResource foundType = ItemResource.EMPTY;
        int toInsert = 0;
        //Note: We don't use inv#getItems as that may do unnecessary copies depending on impl
        for (int i = 0, slots = inv.size(); i < slots; ++i) {
            ItemStack stackInSlot = inv.getItem(i);
            if (!stackInSlot.isEmpty()) {
                if (stackInSlot.getItem() instanceof ItemBlockBin) {
                    if (!binType.isEmpty() || stackInSlot.count() > 1) {
                        //If we already have a bin, or our first bin has a stack size greater than one then this is not a bin recipe
                        return ItemStack.EMPTY;
                    }
                    binType = ItemResource.of(stackInSlot);
                    continue;
                } else if (foundType.isEmpty()) {
                    foundType = ItemResource.of(stackInSlot);
                } else if (!foundType.matches(stackInSlot)) {
                    //If we have types that don't stack in the grid at once,
                    // then we cannot combine them both into the bin
                    return ItemStack.EMPTY;
                }
                //Try inserting a single item (as crafting grids only go one item at a time)
                //TODO: This is part of what causes it to show a lower number than what potentially gets handled by the container
                // and is the part we need to address and change for when handling it as a SpecialQIORecipe
                toInsert += 1;//stackInSlot.count();
            }
        }
        if (binType.isEmpty() || foundType.isEmpty()) {
            //If we didn't find a bin or an item to add it, we don't match the bin insertion recipe
            return ItemStack.EMPTY;
        }
        ItemAccess binAccess = ItemAccessUtils.sideEffectFreeAccess(binType);
        ComponentBackedBinInventorySlot slot = convertToSlot(binAccess);
        //Protect against any mods that might be doing transactional logic, such as if an auto crafter validates it has enough energy before calling this method
        try (Transaction transaction = TransactionHelper.openTransactionSafe()) {
            int inserted = slot.insert(foundType, toInsert, transaction, AutomationType.MANUAL);
            if (inserted == 0) {
                //Return that it doesn't match if we aren't actually able to insert any items into the bin
                return ItemStack.EMPTY;
            } else if (inserted == toInsert) {
                //We could insert it all
                //TODO: I think we can just skip this when handling it as a SpecialQIORecipe
                ItemAccessUtils.exchange(binAccess, binAccess.getResource().with(MekanismDataComponents.FROM_RECIPE, true), transaction);
            }
            //Note: If we only managed to insert some of it into the bin, we skip marking our bin stack as being from a recipe
            // as there is no benefit to checking if we can insert extra stuff if we know we can't fit anymore
            transaction.commit();
            return ItemAccessUtils.asStack(binAccess);
        }
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
        int slots = inv.size();
        NonNullList<ItemStack> remainingItems = NonNullList.withSize(slots, ItemStack.EMPTY);
        ItemResource binType = ItemResource.EMPTY;
        ItemResource foundType = ItemResource.EMPTY;
        boolean[] foundSlots = new boolean[slots];
        for (int i = 0; i < slots; ++i) {
            ItemStack stackInSlot = inv.getItem(i);
            if (!stackInSlot.isEmpty()) {
                if (stackInSlot.getItem() instanceof ItemBlockBin) {
                    if (!binType.isEmpty() || stackInSlot.count() > 1) {
                        //If we already have a bin, or our first bin has a stack size greater than one then this is not a bin recipe
                        return remainingItems;
                    }
                    binType = ItemResource.of(stackInSlot);
                    continue;
                } else if (foundType.isEmpty()) {
                    foundType = ItemResource.of(stackInSlot);
                } else if (!foundType.matches(stackInSlot)) {
                    //If we have types that don't stack in the grid at once,
                    // then we cannot combine them both into the bin
                    return remainingItems;
                }
                foundSlots[i] = true;
            }
        }
        if (binType.isEmpty() || foundType.isEmpty()) {
            //If we didn't find a bin or an item to add it, we don't match the bin insertion recipe
            return remainingItems;
        }
        ItemAccess binAccess = ItemAccessUtils.sideEffectFreeAccess(binType);
        ComponentBackedBinInventorySlot slot = convertToSlot(binAccess);
        //Protect against any mods that might be doing transactional logic, such as if an auto crafter validates it has enough energy before calling this method
        try (Transaction transaction = TransactionHelper.openTransactionSafe()) {
            for (int i = 0; i < foundSlots.length; i++) {
                if (foundSlots[i]) {
                    //Only try inserting a single item into the bin. We execute on a copy of the bin stack so that we can mutate it and chain insertions
                    // to validate if we can insert across multiple slots
                    //TODO: Do we want to allow inserting more when we are acting as a SpecialQIORecipe? (Is that even the case for this as it is the remainder)
                    int inserted = slot.insert(foundType, 1, transaction, AutomationType.MANUAL);
                    if (inserted == 0) {
                        //Can't insert the stack so just mark that we still have a left-over item in that slot
                        remainingItems.set(i, foundType.toStack());
                    }
                }
            }
            transaction.commit();
        }
        return remainingItems;
    }

    @Override
    public RecipeSerializer<BinInsertRecipe> getSerializer() {
        return MekanismRecipeSerializersInternal.BIN_INSERT.get();
    }

    public static void onCrafting(ItemCraftedEvent event) {
        ItemStack result = event.getCrafting();
        if (!result.isEmpty() && result.getItem() instanceof ItemBlockBin && result.count() == 1) {
            //Remove the marker that the bin was crafted from a bin recipe
            Boolean fromRecipe = result.remove(MekanismDataComponents.FROM_RECIPE);
            if (fromRecipe != null && fromRecipe) {
                //And if it was, try to move extra items from the container into it
                ComponentBackedBinInventorySlot slot = convertToSlot(ItemAccess.forStack(result));
                ItemResource storedResource = slot.resource();
                if (!storedResource.isEmpty()) {
                    //Protect against any mods that might be doing transactional logic, such as if an auto crafter validates it has enough energy before this event is fired
                    try (Transaction transaction = TransactionHelper.openTransactionSafe()) {
                        Container craftingMatrix = event.getInventory();
                        for (int i = 0, slots = craftingMatrix.getContainerSize(); i < slots; ++i) {
                            ItemStack stack = craftingMatrix.getItem(i);
                            //Check remaining items
                            if (stack.count() > 1 && storedResource.matches(stack)) {
                                //Try to insert any excess items in the slot (we lower it by one as the input slots have not been lowered yet)
                                int toInsert = stack.count() - 1;
                                int inserted = slot.insert(storedResource, toInsert, transaction, AutomationType.MANUAL);
                                if (inserted == toInsert) {
                                    //Set it to the single item we skipped
                                    craftingMatrix.setItem(i, storedResource.toStack());
                                } else if (inserted < toInsert) {
                                    //Set the stack to whatever amount we were unable to insert
                                    craftingMatrix.setItem(i, storedResource.toStack(toInsert + 1 - inserted));
                                }
                            }
                        }
                        transaction.commit();
                    }
                }
            }
        }
    }
}
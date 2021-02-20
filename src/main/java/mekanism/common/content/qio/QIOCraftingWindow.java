package mekanism.common.content.qio;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.IContentsListener;
import mekanism.api.inventory.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.HotBarSlot;
import mekanism.common.inventory.container.slot.MainInventorySlot;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.slot.CraftingWindowInventorySlot;
import mekanism.common.inventory.slot.CraftingWindowOutputInventorySlot;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.util.NonNullList;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Contract;

public class QIOCraftingWindow implements IContentsListener {

    private final CraftingWindowInventorySlot[] inputSlots = new CraftingWindowInventorySlot[9];
    private final CraftingWindowOutputInventorySlot outputSlot;
    private final QIOCraftingInventory craftingInventory;
    private final IQIOCraftingWindowHolder holder;
    private final byte windowIndex;
    @Nullable
    private ICraftingRecipe lastRecipe;
    private boolean isCrafting;
    private boolean changedWhileCrafting;

    public QIOCraftingWindow(IQIOCraftingWindowHolder holder, byte windowIndex) {
        this.windowIndex = windowIndex;
        this.holder = holder;
        for (int slotIndex = 0; slotIndex < 9; slotIndex++) {
            inputSlots[slotIndex] = CraftingWindowInventorySlot.input(this);
        }
        outputSlot = CraftingWindowOutputInventorySlot.create(this);
        craftingInventory = new QIOCraftingInventory();
    }

    public byte getWindowIndex() {
        return windowIndex;
    }

    public CraftingWindowInventorySlot getInputSlot(int slot) {
        if (slot < 0 || slot >= 9) {
            throw new IllegalArgumentException("Input slot out of range");
        }
        return inputSlots[slot];
    }

    public CraftingWindowOutputInventorySlot getOutputSlot() {
        return outputSlot;
    }

    /**
     * Checks if the stack is equivalent to the current output.
     */
    public boolean isOutput(@Nonnull ItemStack stack) {
        return ItemHandlerHelper.canItemStacksStack(outputSlot.getStack(), stack);
    }

    @Override
    public void onContentsChanged() {
        //Mark the contents as having changed in the holder so as to make sure it properly persists
        holder.onContentsChanged();
        //Update the output slot
        if (isCrafting) {
            //If we are currently crafting mark that we changed while crafting
            changedWhileCrafting = true;
        } else {
            //If we are not currently crafting, recalculate the contents for the output slot
            World world = holder.getHolderWorld();
            if (world != null && !world.isRemote) {
                updateOutputSlot(world);
            }
        }
    }

    public void invalidateRecipe() {
        //Clear the cached recipe and output slot
        lastRecipe = null;
        if (!outputSlot.isEmpty()) {
            outputSlot.setEmpty();
        }
        World world = holder.getHolderWorld();
        if (world != null && !world.isRemote) {
            //And recheck the recipe
            updateOutputSlot(world);
        }
    }

    /**
     * @apiNote Only call on server
     */
    private void updateOutputSlot(@Nonnull World world) {
        if (world.getServer() != null) {
            if (craftingInventory.isEmpty()) {
                //If there is no input, then set the output to empty as there can't be a matching recipe
                if (!outputSlot.isEmpty()) {
                    outputSlot.setEmpty();
                }
            } else if (lastRecipe != null && lastRecipe.matches(craftingInventory, world)) {
                //If the recipe matches, and the output slot is empty
                if (outputSlot.isEmpty()) {
                    //Set the slot to the recipe result, this fixes it not properly updating when
                    // we remove a single item recipe such as for buttons, and put it back in
                    outputSlot.setStack(lastRecipe.getCraftingResult(craftingInventory));
                }
            } else {
                //If we don't have a cached recipe, or our cached recipe doesn't match our inventory contents, lookup the recipe
                ICraftingRecipe recipe = world.getServer().getRecipeManager().getRecipe(IRecipeType.CRAFTING, craftingInventory, world).orElse(null);
                if (recipe != lastRecipe) {
                    if (recipe == null) {
                        //If there is no found recipe, clear the output, but don't update our last recipe
                        // as we can start by checking if they are doing the same recipe as we last found
                        if (!outputSlot.isEmpty()) {
                            outputSlot.setEmpty();
                        }
                    } else {
                        //If the recipe is different, update the output
                        lastRecipe = recipe;
                        outputSlot.setStack(lastRecipe.getCraftingResult(craftingInventory));
                    }
                }
            }
        }
    }

    @Contract("null, _ -> false")
    private boolean validateAndUnlockRecipe(@Nullable World world, @Nonnull PlayerEntity player) {
        if (world == null || lastRecipe == null || !lastRecipe.matches(craftingInventory, world)) {
            //If the recipe isn't valid for the inputs, fail
            //Note: lastRecipe shouldn't be null here but we validate it just in case
            return false;
        }
        if (lastRecipe != null && !lastRecipe.isDynamic()) {
            if (player instanceof ServerPlayerEntity && world.getGameRules().getBoolean(GameRules.DO_LIMITED_CRAFTING) &&
                !((ServerPlayerEntity) player).getRecipeBook().isUnlocked(lastRecipe)) {
                //If the player cannot use the recipe, don't allow crafting
                return false;
            }
            //Unload the recipe for the player
            player.unlockRecipes(Collections.singleton(lastRecipe));
        }
        return true;
    }

    private void craftingStarted(@Nonnull PlayerEntity player) {
        isCrafting = true;
        ForgeHooks.setCraftingPlayer(player);
    }

    private void craftingFinished(@Nonnull World world) {
        ForgeHooks.setCraftingPlayer(null);
        isCrafting = false;
        if (changedWhileCrafting) {
            //If our inputs changed while crafting then update the output slot
            changedWhileCrafting = false;
            updateOutputSlot(world);
        }
    }

    /**
     * Calculates absolute maximum of an output to attempt to craft, this may be higher than how much we have materials for
     */
    private int calculateMaxCraftAmount(@Nonnull ItemStack stack, @Nullable QIOFrequency frequency) {
        int outputSize = stack.getCount();
        int inputSize = 64;
        for (CraftingWindowInventorySlot inputSlot : inputSlots) {
            int count = inputSlot.getCount();
            if (count > 0 && count < inputSize) {
                inputSize = count;
                if (inputSize == 1) {
                    //Exit early if we find a stack that only has a single item in it anyways
                    break;
                }
            }
        }
        if (inputSize > 1) {
            //If we have multiple inputs, attempt to craft the amount of output that would be crafted if the recipe
            // was performed in a normal crafting bench. For example four stacks of stone would make four stacks of
            // stone bricks instead of just a single stack of stone bricks
            return inputSize * outputSize;
        }
        //Otherwise if we can't perform multiple crafts based on what our inputs are and we will need to interact with
        // the QIO Frequency to craft more items
        if (frequency == null) {
            //If we don't have a frequency just return however much we are going to end up crafting from the single craft
            return outputSize;
        }
        int maxToCraft = stack.getMaxStackSize();
        //If we do, and the recipe isn't some weird edge case that produces more output that the item stacks to
        if (outputSize < maxToCraft) {
            //Round down our "stack" that we are producing to be as close but under a stack as we can get
            maxToCraft -= maxToCraft % outputSize;
        }
        return maxToCraft;
    }

    private void useInput(IInventorySlot inputSlot) {
        MekanismUtils.logMismatchedStackSize(1, inputSlot.shrinkStack(1, Action.EXECUTE));
    }

    /**
     * @apiNote For use with shift clicking
     */
    public void performCraft(@Nonnull PlayerEntity player, List<HotBarSlot> hotBarSlots, List<MainInventorySlot> mainInventorySlots) {
        if (lastRecipe == null || outputSlot.isEmpty()) {
            //No recipe, return no result
            // Note: lastRecipe will always null on the client, so we can assume we are server side below
            return;
        }
        World world = holder.getHolderWorld();
        if (!validateAndUnlockRecipe(world, player)) {
            //If the recipe isn't valid, fail
            return;
        }
        QIOFrequency frequency = holder.getFrequency();
        //Mark that we are crafting so changes to the slots below don't force a bunch of recalculations to take place
        craftingStarted(player);
        //Figure out the base of the result stack after crafting (onCreated can adjust it slightly)
        ItemStack result = outputSlot.getStack().copy();
        Item resultItem = result.getItem();
        resultItem.onCreated(result, world, player);
        Stat<Item> itemCraftedStat = Stats.ITEM_CRAFTED.get(resultItem);
        int maxToCraft = calculateMaxCraftAmount(result, frequency);
        int amountPerCraft = result.getCount();
        //Note: We initialized crafted here instead of in the for loop so that we can query how much was actually crafted
        int crafted = 0;
        boolean updatedInputs = false;
        boolean recheckOutput = false;
        NonNullList<ItemStack> remaining = lastRecipe.getRemainingItems(craftingInventory);
        for (; crafted < maxToCraft; crafted += amountPerCraft) {
            if (recheckOutput && changedWhileCrafting) {
                //If our inputs changed while crafting and we are supposed to recheck the output,
                // update the contents of the output slot and the recipe that we are performing as
                // if there is an NBT sensitive recipe, the output may have changed
                recheckOutput = false;
                changedWhileCrafting = false;
                ICraftingRecipe oldRecipe = lastRecipe;
                updateOutputSlot(world);
                if (oldRecipe != lastRecipe) {
                    //If the recipe changed, exit regardless of if the new recipe will produce the same output as the old one
                    // as there is a good chance something odd is going on or potentially even the doLimitedCrafting GameRule
                    // is enabled and they only have access to one of the crafting recipes
                    break;
                }
                ItemStack updatedOutput = outputSlot.getStack();
                if (updatedOutput.isEmpty() || updatedOutput.getItem() != resultItem) {
                    //If we can't craft anymore or the resulting item changed entirely, stop crafting
                    break;
                }
                //If they may still be compatible, copy the stack, and apply the onCreated to it so that
                // we can adjust the NBT if it needs adjusting
                ItemStack potentialUpdatedOutput = updatedOutput.copy();
                resultItem.onCreated(potentialUpdatedOutput, world, player);
                if (!ItemStack.areItemStacksEqual(result, potentialUpdatedOutput)) {
                    //If some of the data is different about the output, stop crafting
                    // Note: we check if the stacks are equal instead of just if they can stack as if they are different sizes
                    // for some reason we want to stop to allow the player to decide if they want to keep going, or stop crafting
                    // This also has a side effect of not requiring us to then recalculate the value of maxToCraft
                    break;
                }
                //We also need to make sure to update the remaining items as even though the recipe still outputs the same result
                // the remaining items may have changed such as durability of a container item, and we want to make sure to use
                // the proper remaining stacks
                remaining = lastRecipe.getRemainingItems(craftingInventory);
            }
            //Simulate insertion into hotbar and then main inventory, allowing for inserting into empty slots,
            // as we just want to do a quick general check to see if there is room for the result, we will do
            // the secondary checks afterwards while working on actually inserting into it
            // The reason this is needed is because if we only have space for two more items, but our crafting recipe will
            // produce three more, then we won't have room for that singular extra item and need to exit
            ItemStack simulatedRemainder = MekanismContainer.insertItem(hotBarSlots, result, false, Action.SIMULATE);
            simulatedRemainder = MekanismContainer.insertItem(mainInventorySlots, simulatedRemainder, false, Action.SIMULATE);
            if (!simulatedRemainder.isEmpty()) {
                //Note: If we aren't able to fit all of the items we are crafting into the player's inventory we exit
                // instead of attempting to insert the overflow into the QIO as it is easy enough if the player is trying
                // to fill the QIO with something to then just transfer the contents into the QIO, and otherwise they are
                // likely just trying to top their inventory off on a specific item and may be confused or not notice if
                // some of the contents ended up in their storage system instead
                break;
            }
            //TODO: Look into seeing if we can improve the performance of this further by doing something along the lines of
            // keeping track of the last slot/index of what we successfully inserted into and starting with trying to insert
            // it into that slot. In theory should work, but may get a little messy around the order of us looking at empty
            // slots and us ignoring empty slots
            //Actually transfer the output to the player's inventory now that we know it will fit
            //Insert into stacks that already contain an item in the order hot bar -> main inventory
            ItemStack toInsert = result;
            toInsert = MekanismContainer.insertItem(hotBarSlots, toInsert, true);
            toInsert = MekanismContainer.insertItem(mainInventorySlots, toInsert, true);
            //If we still have any left then input into the empty stacks in the order of main inventory -> hot bar
            // Note: Even though we are doing the main inventory, we still need to do both, ignoring empty then not instead of
            // just directly inserting into the main inventory, in case there are empty slots before the one we can stack with
            toInsert = MekanismContainer.insertItem(hotBarSlots, toInsert, false);
            toInsert = MekanismContainer.insertItem(mainInventorySlots, toInsert, false);
            if (!toInsert.isEmpty()) {
                //If something went horribly wrong adding it to the player's inventory given we calculated there was room
                // and suddenly a few lines down there is no longer room, then just drop the items as the player
                player.dropItem(toInsert, false);
            }
            //Update slots with remaining contents
            for (int index = 0; index < remaining.size(); index++) {
                ItemStack remainder = remaining.get(index);
                CraftingWindowInventorySlot inputSlot = inputSlots[index];
                if (inputSlot.getCount() > 1) {
                    //If the input slot contains an item that is stacked, reduce the size of it by one
                    //Note: We "ignore" the fact that the container item may still be valid for the recipe, if the input is stacked
                    useInput(inputSlot);
                } else if (inputSlot.getCount() == 1) {
                    updatedInputs = updateRemainderHelperInputs(updatedInputs, remainder);
                    //Else if the input slot only has a single item in it, try removing from the frequency
                    if (RemainderHelper.INSTANCE.isStackStillValid(lastRecipe, world, remainder, index) || frequency == null) {
                        //If the remaining item is still valid for the recipe in that slot, or we don't have a frequency and it is the
                        // last stack in the slot, remove the stack from the slot
                        useInput(inputSlot);
                        // and mark that we should recheck our output as the recipe output may have changed or we may
                        // no longer have enough inputs to craft an output
                        recheckOutput = true;
                    } else {
                        //Otherwise try and remove the stack from the QIO frequency
                        ItemStack removed = frequency.removeItem(inputSlot.getStack(), 1);
                        if (removed.isEmpty()) {
                            //If we were not able to remove any from the frequency, remove it from the crafting grid
                            useInput(inputSlot);
                            // and mark that the we should recheck our output as we likely no longer have enough items to
                            // craft another iteration of it
                            recheckOutput = true;
                        } else {
                            //TODO: Otherwise if we used up all of the input, see if we have another valid input
                            // stored in the frequency and replace it with it, AND stop crafting so that the player
                            // has a chance to notice that the item it is going to use changed
                            //TODO: Debate potentially briefly highlighting the slot to make it more evident to the player
                            // that something about the slot changed
                        }
                    }
                } else if (!remainder.isEmpty()) {
                    //Otherwise if the slot is empty, but we don't have an empty remaining stack because of a mod doing odd things
                    // or having some edge case behavior that creates items in a slot, mark that we need to recheck our output.
                    // Technically we maybe would fail to add the item to the slot, but given that is highly unlikely we just
                    // recheck anyways
                    recheckOutput = true;
                }
                //TODO: If we end up making it go into crafting inventory before dropping, then we need to recheck the output in that situation
                addRemainingItem(player, frequency, inputSlot, remainder, true);
            }
        }
        if (crafted > 0) {
            //Add to the stat how much of the item the player crafted that the player crafted the item
            player.addStat(itemCraftedStat, crafted);
            //Note: We don't fire a crafting event as we don't want to allow for people to modify the output
            // stack or more importantly the input inventory during crafting
            //TODO: If this ends up causing major issues with some weird way another mod ends up doing crafting
            // we can evaluate how we want to handle it then/try to integrate support for firing it.
            //BasicEventHooks.firePlayerCraftingEvent(player, result, craftingInventory);
        }
        //Mark that we are done crafting
        craftingFinished(world);
    }

    @Nonnull
    public ItemStack performCraft(@Nonnull PlayerEntity player, @Nonnull ItemStack result, int amountCrafted) {
        //TODO: Given we don't fire a crafting event and even if we did things would likely not work properly,
        // we may want to special case our bin filling and emptying recipes so that they can take directly from the frequency
        // and be a quick way to fill/empty an entire bin at once (also implement the same special handling for shift clicking)
        if (amountCrafted == 0 || lastRecipe == null || result.isEmpty()) {
            //Nothing actually crafted or no recipe, return no result
            // Note: lastRecipe will always null on the client, so we can assume we are server side below
            return ItemStack.EMPTY;
        }
        World world = holder.getHolderWorld();
        if (!validateAndUnlockRecipe(world, player)) {
            //If the recipe isn't valid, fail
            return ItemStack.EMPTY;
        }
        QIOFrequency frequency = holder.getFrequency();
        //Mark that we are crafting so changes to the slots below don't force a bunch of recalculations to take place
        craftingStarted(player);
        //Craft the result
        result.onCrafting(world, player, amountCrafted);
        //Note: We don't fire a crafting event as we don't want to allow for people to modify the output
        // stack or more importantly the input inventory during crafting
        //TODO: If this ends up causing major issues with some weird way another mod ends up doing crafting
        // we can evaluate how we want to handle it then/try to integrate support for firing it.
        //BasicEventHooks.firePlayerCraftingEvent(player, result, craftingInventory);
        NonNullList<ItemStack> remaining = lastRecipe.getRemainingItems(craftingInventory);
        boolean updatedInputs = false;
        //Update slots with remaining contents
        for (int index = 0; index < remaining.size(); index++) {
            ItemStack remainder = remaining.get(index);
            CraftingWindowInventorySlot inputSlot = inputSlots[index];
            if (inputSlot.getCount() > 1) {
                //If the input slot contains an item that is stacked, reduce the size of it by one
                //Note: We "ignore" the fact that the container item may still be valid for the recipe, if the input is stacked
                useInput(inputSlot);
            } else if (inputSlot.getCount() == 1) {
                //Else if the input slot only has a single item in it, try removing from the frequency
                if (frequency == null) {
                    //If we have no frequency remove from the crafting window
                    useInput(inputSlot);
                } else {
                    updatedInputs = updateRemainderHelperInputs(updatedInputs, remainder);
                    if (RemainderHelper.INSTANCE.isStackStillValid(lastRecipe, world, remainder, index)) {
                        //If the remaining item is still valid for the recipe in that slot, remove the stack from the slot
                        useInput(inputSlot);
                    } else {
                        //Otherwise try and remove the stack from the QIO frequency
                        ItemStack removed = frequency.removeItem(inputSlot.getStack(), 1);
                        if (removed.isEmpty()) {
                            //If we were not able to remove any from the frequency, remove it from the crafting grid
                            useInput(inputSlot);
                        } else {
                            //TODO: Otherwise if we used up all of the input, see if we have another valid input
                            // stored in the frequency and replace it with it.
                            //TODO: Debate potentially briefly highlighting the slot to make it more evident to the player
                            // that something about the slot changed.
                        }
                    }
                }
            }
            //Note: No special handling needed here for if the remainder is empty
            addRemainingItem(player, frequency, inputSlot, remainder, false);
        }
        //Mark that we are done crafting
        craftingFinished(world);
        return result;
    }

    private void addRemainingItem(PlayerEntity player, @Nullable QIOFrequency frequency, IInventorySlot slot, @Nonnull ItemStack remainder, boolean copyIfNeeded) {
        //Rough explanation of our handling for remainder items:
        // if container item is still valid in that slot for the recipe (and it isn't currently a stacked input)
        //    or we don't have enough contents to do the recipe again,
        //      put it there
        // else try putting it into the player's inventory
        //    if there is no room for it then try putting it in the backing storage
        //      if there is no room there for it either (due to item type restrictions),
        //        put it in the crafting slots
        // if everything fails do what vanilla does as fallback and just drops it on the ground as the player
        //Add the remaining stack for the slot back into the slot
        //Note: We don't bother checking if it is empty as insertItem will just short circuit if it is
        int toInsert = remainder.getCount();
        remainder = slot.insertItem(remainder, Action.EXECUTE, AutomationType.INTERNAL);
        if (!remainder.isEmpty()) {
            if (copyIfNeeded && toInsert == remainder.getCount()) {
                //If we plan on reusing the same stack of the remainder, and we didn't insert part of it into the slot
                // so we don't already have a copy of it, make a copy of the remainder to allow vanilla to safely modify
                // it in addItemStackToInventory if it needs/wants to
                remainder = remainder.copy();
            }
            //If some or all of the stack could not be returned to the input slot add it to the player's inventory
            if (!player.inventory.addItemStackToInventory(remainder)) {
                //failing that try adding it to the qio frequency if there is one
                if (frequency != null) {
                    remainder = frequency.addItem(remainder);
                    if (remainder.isEmpty()) {
                        //If we added it all to the QIO, don't bother trying to drop it
                        return;
                    }
                }
                //TODO: Before dropping it we may want to try putting it into the crafting inventory,
                // and then have the current stack go into the inventory/QIO?? In theory it sounds like
                // a good idea but may be way too convoluted to implement cleanly
                //If there is no frequency or we couldn't add it all to the QIO, drop the remaining item as the player
                player.dropItem(remainder, false);
            }
        }
    }

    private boolean updateRemainderHelperInputs(boolean updated, @Nonnull ItemStack remainder) {
        if (updated) {
            //If it has already been updated, no reason to update it again
            return true;
        } else if (remainder.isEmpty()) {
            //If the remainder is empty we don't actually need to update what our inputs are
            return false;
        }
        //Update inputs and return that we have updated them
        RemainderHelper.INSTANCE.updateInputs(inputSlots);
        return true;
    }

    private class QIOCraftingInventory extends CraftingInventory {

        //TODO: Suppress warning and also add a note that we override all the places where it being
        // null would cause issues, and we handle slots individually as well
        public QIOCraftingInventory() {
            super(null, 0, 0);
        }

        @Override
        public int getSizeInventory() {
            return 9;
        }

        @Override
        public boolean isEmpty() {
            return Arrays.stream(inputSlots).allMatch(BasicInventorySlot::isEmpty);
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int index) {
            if (index >= 0 && index < getSizeInventory()) {
                IInventorySlot inputSlot = getInputSlot(index);
                if (!inputSlot.isEmpty()) {
                    //Note: We copy this as we don't want to allow someone trying to interact with the stack directly
                    // to change the size of it
                    return inputSlot.getStack().copy();
                }
            }
            return ItemStack.EMPTY;
        }

        @Nonnull
        @Override
        public ItemStack removeStackFromSlot(int index) {
            if (index >= 0 && index < getSizeInventory()) {
                IInventorySlot inputSlot = getInputSlot(index);
                ItemStack stored = inputSlot.getStack();
                inputSlot.setEmpty();
                //Note: We don't bother copying the stack as we are setting it to empty so
                // it doesn't really matter if we modify the previously internal stack or not
                return stored;
            }
            return ItemStack.EMPTY;
        }

        @Nonnull
        @Override
        public ItemStack decrStackSize(int index, int count) {
            if (index >= 0 && index < getSizeInventory()) {
                return getInputSlot(index).extractItem(count, Action.EXECUTE, AutomationType.INTERNAL);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public void setInventorySlotContents(int index, @Nonnull ItemStack stack) {
            if (index >= 0 && index < getSizeInventory()) {
                getInputSlot(index).setStack(stack);
            }
        }

        @Override
        public void clear() {
            //TODO: Re-evaluate, should this instead just force a refresh or something?
            for (CraftingWindowInventorySlot inputSlot : inputSlots) {
                inputSlot.setEmpty();
            }
        }

        @Override
        public int getHeight() {
            return 3;
        }

        @Override
        public int getWidth() {
            return 3;
        }

        @Override
        public void fillStackedContents(@Nonnull RecipeItemHelper helper) {
            //Note: We don't copy it as it seems to be read only
            // Don't trust custom implementations though to be read only
            boolean copyNeeded = helper.getClass() != RecipeItemHelper.class;
            for (CraftingWindowInventorySlot inputSlot : inputSlots) {
                ItemStack stack = inputSlot.getStack();
                helper.accountPlainStack(copyNeeded ? stack.copy() : stack);
            }
        }
    }

    private static class RemainderHelper {

        public static final RemainderHelper INSTANCE = new RemainderHelper();

        private final CraftingInventory dummy = MekanismUtils.getDummyCraftingInv();

        public void updateInputs(IInventorySlot[] inputSlots) {
            for (int index = 0; index < inputSlots.length; index++) {
                //TODO: Does this and isStackStillValid need to be copying the stack
                dummy.setInventorySlotContents(index, inputSlots[index].getStack());
            }
        }

        public boolean isStackStillValid(ICraftingRecipe recipe, World world, ItemStack stack, int index) {
            ItemStack old = dummy.getStackInSlot(index);
            dummy.setInventorySlotContents(index, stack);
            if (recipe.matches(dummy, world)) {
                //If the remaining item is still valid in the recipe in that position
                // return that it is still valid
                return true;
            }
            //Otherwise, revert the contents of the slot to what used to be in that slot
            // and return that the remaining item is not still valid in the slot
            dummy.setInventorySlotContents(index, old);
            return false;
        }

    }
}
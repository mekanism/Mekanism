package mekanism.common.content.qio;

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.IntFunction;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.container.SelectedWindowData.WindowType;
import mekanism.common.inventory.container.slot.HotBarSlot;
import mekanism.common.inventory.container.slot.IInsertableSlot;
import mekanism.common.inventory.container.slot.MainInventorySlot;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.slot.CraftingWindowInventorySlot;
import mekanism.common.inventory.slot.CraftingWindowOutputInventorySlot;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.common.util.RecipeMatcher;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Contract;

public class QIOCraftingWindow implements IContentsListener {

    private static final SelectedWindowData[] WINDOWS = new SelectedWindowData[IQIOCraftingWindowHolder.MAX_CRAFTING_WINDOWS];

    static {
        for (byte tableIndex = 0; tableIndex < WINDOWS.length; tableIndex++) {
            WINDOWS[tableIndex] = new SelectedWindowData(WindowType.CRAFTING, tableIndex);
        }
    }

    private final CraftingWindowInventorySlot[] inputSlots = new CraftingWindowInventorySlot[9];
    private final ReplacementHelper replacementHelper = new ReplacementHelper();
    private final RemainderHelper remainderHelper = new RemainderHelper();
    private final CraftingWindowOutputInventorySlot outputSlot;
    private final QIOCraftingInventory craftingInventory;
    private final IQIOCraftingWindowHolder holder;
    private final SelectedWindowData windowData;
    private final byte windowIndex;
    @Nullable
    private CraftingRecipe lastRecipe;
    private boolean isCrafting;
    private boolean changedWhileCrafting;

    public QIOCraftingWindow(IQIOCraftingWindowHolder holder, byte windowIndex) {
        this.windowIndex = windowIndex;
        this.holder = holder;
        this.windowData = WINDOWS[windowIndex];
        for (int slotIndex = 0; slotIndex < 9; slotIndex++) {
            inputSlots[slotIndex] = CraftingWindowInventorySlot.input(this, this.holder);
        }
        outputSlot = CraftingWindowOutputInventorySlot.create(this);
        craftingInventory = new QIOCraftingInventory();
    }

    public SelectedWindowData getWindowData() {
        return windowData;
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
        //Note: We don't need to mark the holder as the contents have changed as that is done via the save listener
        if (isCrafting) {
            //If we are currently crafting mark that we changed while crafting
            changedWhileCrafting = true;
        } else {
            //If we are not currently crafting, recalculate the contents for the output slot
            Level world = holder.getHolderWorld();
            if (world != null && !world.isClientSide) {
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
        Level world = holder.getHolderWorld();
        if (world != null && !world.isClientSide) {
            //And recheck the recipe
            updateOutputSlot(world);
        }
    }

    /**
     * @apiNote Only call on server
     */
    private void updateOutputSlot(@Nonnull Level world) {
        if (world.getServer() != null) {
            if (craftingInventory.isEmpty()) {
                //If there is no input, then set the output to empty as there can't be a matching recipe
                if (!outputSlot.isEmpty()) {
                    outputSlot.setEmpty();
                }
            } else if (lastRecipe != null && lastRecipe.matches(craftingInventory, world)) {
                //If the recipe matches make sure we update the output anyway, as the output may have changed based on NBT
                // If the output slot was empty, then setting the slot to the recipe result fixes it not properly updating
                // when we remove a single item recipe such as for buttons, and put it back in;
                // and otherwise we update so that cases like bin upgrade recipes that the inputs match the recipe but the
                // output is dependent on the specific inputs gets updated properly
                //Note: We make sure to only call updateOutputSlot if we believe our inputs have changed type
                outputSlot.setStack(lastRecipe.assemble(craftingInventory));
            } else {
                //If we don't have a cached recipe, or our cached recipe doesn't match our inventory contents, lookup the recipe
                CraftingRecipe recipe = MekanismRecipeType.getRecipeFor(RecipeType.CRAFTING, craftingInventory, world).orElse(null);
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
                        outputSlot.setStack(lastRecipe.assemble(craftingInventory));
                    }
                }
            }
        }
    }

    public boolean canViewRecipe(@Nonnull ServerPlayer player) {
        if (lastRecipe == null) {
            //If there is no last recipe, they can't craft it
            //Note: We don't check if it matches as if we don't have a match there won't
            // be anything in our output slot, so it doesn't matter
            return false;
        }
        //If the recipe is dynamic, doLimitedCrafting is disabled, or the recipe is unlocked
        // allow viewing the recipe
        return lastRecipe.isSpecial() || !player.level.getGameRules().getBoolean(GameRules.RULE_LIMITED_CRAFTING) || player.getRecipeBook().contains(lastRecipe);
    }

    @Contract("null, _ -> false")
    private boolean validateAndUnlockRecipe(@Nullable Level world, @Nonnull Player player) {
        if (world == null || lastRecipe == null || !lastRecipe.matches(craftingInventory, world)) {
            //If the recipe isn't valid for the inputs, fail
            //Note: lastRecipe shouldn't be null here, but we validate it just in case
            return false;
        }
        if (lastRecipe != null && !lastRecipe.isSpecial()) {
            if (player instanceof ServerPlayer serverPlayer && world.getGameRules().getBoolean(GameRules.RULE_LIMITED_CRAFTING) &&
                !serverPlayer.getRecipeBook().contains(lastRecipe)) {
                //If the player cannot use the recipe, don't allow crafting
                return false;
            }
            //Unload the recipe for the player
            player.awardRecipes(Collections.singleton(lastRecipe));
        }
        return true;
    }

    private void craftingStarted(@Nonnull Player player) {
        isCrafting = true;
        ForgeHooks.setCraftingPlayer(player);
    }

    private void craftingFinished(@Nonnull Level world) {
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
                    //Exit early if we find a stack that only has a single item in it anyway
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
        //Otherwise, if we can't perform multiple crafts based on what our inputs are, and we will need to interact with
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
        MekanismUtils.logMismatchedStackSize(inputSlot.shrinkStack(1, Action.EXECUTE), 1);
    }

    /**
     * @apiNote For use with shift clicking
     */
    public void performCraft(@Nonnull Player player, List<HotBarSlot> hotBarSlots, List<MainInventorySlot> mainInventorySlots) {
        if (lastRecipe == null || outputSlot.isEmpty()) {
            //No recipe, return no result
            // Note: lastRecipe will always null on the client, so we can assume we are server side below
            return;
        }
        Level world = holder.getHolderWorld();
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
        resultItem.onCraftedBy(result, world, player);
        Stat<Item> itemCraftedStat = Stats.ITEM_CRAFTED.get(resultItem);
        int maxToCraft = calculateMaxCraftAmount(result, frequency);
        int amountPerCraft = result.getCount();
        //Note: We initialized crafted here instead of in the for loop so that we can query how much was actually crafted
        int crafted = 0;
        remainderHelper.reset();
        replacementHelper.reset();
        boolean recheckOutput = false;
        LastInsertTarget lastInsertTarget = new LastInsertTarget();
        NonNullList<ItemStack> remaining = lastRecipe.getRemainingItems(craftingInventory);
        for (; crafted < maxToCraft; crafted += amountPerCraft) {
            if (recheckOutput && changedWhileCrafting) {
                //If our inputs changed while crafting, and we are supposed to recheck the output,
                // update the contents of the output slot and the recipe that we are performing as
                // if there is an NBT sensitive recipe, the output may have changed
                recheckOutput = false;
                changedWhileCrafting = false;
                CraftingRecipe oldRecipe = lastRecipe;
                updateOutputSlot(world);
                if (oldRecipe != lastRecipe) {
                    //If the recipe changed, exit regardless of if the new recipe will produce the same output as the old one
                    // as there is a good chance something odd is going on or potentially even the doLimitedCrafting GameRule
                    // is enabled, and they only have access to one of the crafting recipes
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
                resultItem.onCraftedBy(potentialUpdatedOutput, world, player);
                if (!ItemStack.matches(result, potentialUpdatedOutput)) {
                    //If some data is different about the output, stop crafting
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
            // The reason this is needed is that if we only have space for two more items, but our crafting recipe will
            // produce three more, then we won't have room for that singular extra item and need to exit
            ItemStack simulatedRemainder = MekanismContainer.insertItemCheckAll(hotBarSlots, result, windowData, Action.SIMULATE);
            simulatedRemainder = MekanismContainer.insertItemCheckAll(mainInventorySlots, simulatedRemainder, windowData, Action.SIMULATE);
            if (!simulatedRemainder.isEmpty()) {
                //Note: If we aren't able to fit all the items we are crafting into the player's inventory we exit
                // instead of attempting to insert the overflow into the QIO as it is easy enough if the player is trying
                // to fill the QIO with something to then just transfer the contents into the QIO, and otherwise they are
                // likely just trying to top their inventory off on a specific item and may be confused or not notice if
                // some contents ended up in their storage system instead
                break;
            }
            //Actually transfer the output to the player's inventory now that we know it will fit
            ItemStack toInsert = lastInsertTarget.tryInserting(hotBarSlots, mainInventorySlots, windowData, result);
            if (!toInsert.isEmpty()) {
                //If something went horribly wrong adding it to the player's inventory given we calculated there was room
                // and suddenly a few lines down there is no longer room, then just drop the items as the player
                player.drop(toInsert, false);
            }
            boolean stopCrafting = false;
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
                    if (frequency == null || remainderHelper.isStackStillValid(world, remainder, index)) {
                        //If the remaining item is still valid for the recipe in that slot, or we don't have a frequency, and it is the
                        // last stack in the slot, remove the stack from the slot
                        useInput(inputSlot);
                        // and mark that we should recheck our output as the recipe output may have changed, or we may
                        // no longer have enough inputs to craft an output
                        recheckOutput = true;
                    } else {
                        //Otherwise, try and remove the stack from the QIO frequency
                        ItemStack current = inputSlot.getStack();
                        ItemStack removed = frequency.removeItem(current, 1);
                        if (removed.isEmpty()) {
                            //If we were not able to remove any from the frequency, remove it from the crafting grid
                            useInput(inputSlot);
                            // see if we have another valid input stored in the frequency and replace it with it if we do
                            replacementHelper.findEquivalentItem(world, frequency, inputSlot, index, current);
                            // and stop crafting even if we have another valid item for that spot, as we want to give the player a chance
                            // to notice the item it will be using changed in case it got replaced with some very expensive alternative
                            stopCrafting = true;
                        }
                    }
                } else if (!remainder.isEmpty()) {
                    //Otherwise, if the slot is empty, but we don't have an empty remaining stack because of a mod doing odd things
                    // or having some edge case behavior that creates items in a slot, mark that we need to recheck our output.
                    // Technically we maybe would fail to add the item to the slot, but given that is highly unlikely we just
                    // recheck anyway
                    recheckOutput = true;
                }
                addRemainingItem(player, frequency, inputSlot, remainder, true);
            }
            if (stopCrafting) {
                //Note: We need to increment the amount crafted here, as breaking will skip the increment
                // that happens at the end of the loop
                crafted += amountPerCraft;
                break;
            }
        }
        if (crafted > 0) {
            //Add to the stat how much of the item the player crafted that the player crafted the item
            player.awardStat(itemCraftedStat, crafted);
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
    public ItemStack performCraft(@Nonnull Player player, @Nonnull ItemStack result, int amountCrafted) {
        //TODO - 1.18: Given we don't fire a crafting event and even if we did things would likely not work properly,
        // we may want to special case our bin filling and emptying recipes so that they can take directly from the frequency
        // and be a quick way to fill/empty an entire bin at once (also implement the same special handling for shift clicking)
        // Maybe for now an IQIOIntegratedCraftingRecipe or something like that that we can call?
        if (amountCrafted == 0 || lastRecipe == null || result.isEmpty()) {
            //Nothing actually crafted or no recipe, return no result
            // Note: lastRecipe will always null on the client, so we can assume we are server side below
            return ItemStack.EMPTY;
        }
        Level world = holder.getHolderWorld();
        if (!validateAndUnlockRecipe(world, player)) {
            //If the recipe isn't valid, fail
            return ItemStack.EMPTY;
        }
        QIOFrequency frequency = holder.getFrequency();
        //Mark that we are crafting so changes to the slots below don't force a bunch of recalculations to take place
        craftingStarted(player);
        //Craft the result
        result.onCraftedBy(world, player, amountCrafted);
        //Note: We don't fire a crafting event as we don't want to allow for people to modify the output
        // stack or more importantly the input inventory during crafting
        //TODO: If this ends up causing major issues with some weird way another mod ends up doing crafting
        // we can evaluate how we want to handle it then/try to integrate support for firing it.
        //BasicEventHooks.firePlayerCraftingEvent(player, result, craftingInventory);
        NonNullList<ItemStack> remaining = lastRecipe.getRemainingItems(craftingInventory);
        remainderHelper.reset();
        replacementHelper.reset();
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
                if (frequency == null || remainderHelper.isStackStillValid(world, remainder, index)) {
                    //If we have no frequency or the remaining item is still valid for the recipe in that slot,
                    // remove from the crafting window
                    useInput(inputSlot);
                } else {
                    //Otherwise, try and remove the stack from the QIO frequency
                    ItemStack current = inputSlot.getStack();
                    ItemStack removed = frequency.removeItem(current, 1);
                    if (removed.isEmpty()) {
                        //If we were not able to remove any from the frequency, remove it from the crafting grid
                        useInput(inputSlot);
                        // see if we have another valid input stored in the frequency and replace it with it if we do
                        replacementHelper.findEquivalentItem(world, frequency, inputSlot, index, current);
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

    private void addRemainingItem(Player player, @Nullable QIOFrequency frequency, IInventorySlot slot, @Nonnull ItemStack remainder, boolean copyIfNeeded) {
        //Rough explanation of our handling for remainder items:
        // if container item is still valid in that slot for the recipe (and it isn't currently a stacked input)
        //    or we don't have enough contents to do the recipe again,
        //      put it there
        // else try putting it into the player's inventory
        //    if there is no room for it then try putting it in the backing storage
        //      if there is no room there for it either (due to item type restrictions),
        //        put it in the crafting slots (skipped for now, see below todo)
        // if everything fails do what vanilla does as fallback and just drops it on the ground as the player
        //Add the remaining stack for the slot back into the slot
        //Note: We don't bother checking if it is empty as insertItem will just short circuit if it is
        int toInsert = remainder.getCount();
        //Try inserting the item back into the slot it came from, this should only be able to actually insert it if it
        // is still valid for the recipe and the rest of the stack has been used completely
        remainder = slot.insertItem(remainder, Action.EXECUTE, AutomationType.INTERNAL);
        if (!remainder.isEmpty()) {
            if (copyIfNeeded && toInsert == remainder.getCount()) {
                //If we plan on reusing the same stack of the remainder, and we didn't insert part of it into the slot,
                // so we don't already have a copy of it, make a copy of the remainder to allow vanilla to safely modify
                // it in addItemStackToInventory if it needs/wants to
                remainder = remainder.copy();
            }
            //If some or all of the stack could not be returned to the input slot add it to the player's inventory
            if (!player.getInventory().add(remainder)) {
                //failing that try adding it to the qio frequency if there is one
                if (frequency != null) {
                    remainder = frequency.addItem(remainder);
                    if (remainder.isEmpty()) {
                        //If we added it all to the QIO, don't bother trying to drop it
                        return;
                    }
                }
                //TODO: Before dropping it we may want to try putting it into the crafting inventory in any slot that will take it,
                // and then have the current stack go into the inventory/QIO?? In theory this sounds like a good idea but it is very
                // convoluted so has been skipped for now and probably ever. If it does get implemented then we need to make sure to
                // mark the output as needing an update in the "shift crafting" version that calls this method
                //If there is no frequency or we couldn't add it all to the QIO, drop the remaining item as the player
                player.drop(remainder, false);
            }
        }
    }

    /**
     * Used for helping keep track of were we were for inserting
     */
    private static class LastInsertTarget {

        private boolean wasHotBar = true;
        private int lastIndex;

        public ItemStack tryInserting(List<HotBarSlot> hotBarSlots, List<MainInventorySlot> mainInventorySlots, SelectedWindowData windowData, ItemStack toInsert) {
            //Insert into stacks that already contain an item in the order hot bar -> main inventory
            // Note: The target helps us skip checking some slot types that we know may not be valid
            toInsert = insertItem(hotBarSlots, toInsert, true, true, windowData);
            toInsert = insertItem(mainInventorySlots, toInsert, true, false, windowData);
            //If we still have any left then input into the empty stacks in the order of main inventory -> hot bar
            // Note: Even though we are doing the main inventory, we still need to do both, ignoring empty then not instead of
            // just directly inserting into the main inventory, in case there are empty slots before the one we can stack with
            toInsert = insertItem(hotBarSlots, toInsert, false, true, windowData);
            toInsert = insertItem(mainInventorySlots, toInsert, false, false, windowData);
            return toInsert;
        }

        /**
         * Based on MekanismContainer#insertItem except with extra handling to keep track of where we last were.
         */
        @Nonnull
        private <SLOT extends Slot & IInsertableSlot> ItemStack insertItem(List<SLOT> slots, @Nonnull ItemStack stack, boolean ignoreEmpty, boolean isHotBar,
              @Nullable SelectedWindowData selectedWindow) {
            if (stack.isEmpty()) {
                //Skip doing anything if the stack is already empty.
                // Makes it easier to chain calls, rather than having to check if the stack is empty after our previous call
                return stack;
            }
            //Note: We don't check if we just ignored empty or not, as we should be able to insert into
            // and filled slot so once we do that slot stops being "empty" and we want to start at it
            for (int i = ignoreEmpty && wasHotBar == isHotBar ? lastIndex : 0, slotCount = slots.size(); i < slotCount; i++) {
                SLOT slot = slots.get(i);
                if (ignoreEmpty != slot.hasItem()) {
                    //Skip checking empty stacks if we want to ignore them, and skipp non-empty stacks if we don't want ot ignore them
                    continue;
                } else if (!slot.exists(selectedWindow)) {
                    // or if the slot doesn't "exist" for the current window configuration
                    continue;
                }
                stack = slot.insertItem(stack, Action.EXECUTE);
                if (stack.isEmpty()) {
                    //We finished inserting, update where we last targeted
                    wasHotBar = isHotBar;
                    lastIndex = i;
                    break;
                }
            }
            return stack;
        }
    }

    private class QIOCraftingInventory extends CraftingContainer {

        //Note: We suppress the warning about this passing null as the container as we override all methods that
        // that make use of the container to use our handler instead
        @SuppressWarnings("ConstantConditions")
        public QIOCraftingInventory() {
            super(null, 0, 0);
        }

        @Override
        public int getContainerSize() {
            return inputSlots.length;
        }

        @Override
        public boolean isEmpty() {
            return Arrays.stream(inputSlots).allMatch(BasicInventorySlot::isEmpty);
        }

        @Nonnull
        @Override
        public ItemStack getItem(int index) {
            if (index >= 0 && index < getContainerSize()) {
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
        public ItemStack removeItemNoUpdate(int index) {
            if (index >= 0 && index < getContainerSize()) {
                IInventorySlot inputSlot = getInputSlot(index);
                ItemStack stored = inputSlot.getStack();
                inputSlot.setEmpty();
                //Note: We don't bother copying the stack as we are setting it to empty, so
                // it doesn't really matter if we modify the previously internal stack or not
                return stored;
            }
            return ItemStack.EMPTY;
        }

        @Nonnull
        @Override
        public ItemStack removeItem(int index, int count) {
            if (index >= 0 && index < getContainerSize()) {
                return getInputSlot(index).extractItem(count, Action.EXECUTE, AutomationType.INTERNAL);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public void setItem(int index, @Nonnull ItemStack stack) {
            if (index >= 0 && index < getContainerSize()) {
                getInputSlot(index).setStack(stack);
            }
        }

        @Override
        public void clearContent() {
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
        public void fillStackedContents(@Nonnull StackedContents helper) {
            //Note: We don't copy it as it seems to be read only
            // Don't trust custom implementations though to be read only
            boolean copyNeeded = helper.getClass() != StackedContents.class;
            for (CraftingWindowInventorySlot inputSlot : inputSlots) {
                ItemStack stack = inputSlot.getStack();
                helper.accountSimpleStack(copyNeeded ? stack.copy() : stack);
            }
        }
    }

    private class RemainderHelper {

        private final CraftingContainer dummy = MekanismUtils.getDummyCraftingInv();

        private boolean updated;

        public void reset() {
            if (updated) {
                updated = false;
                //Only clear the contents if we need to
                dummy.clearContent();
            }
        }

        private void updateInputs(@Nonnull ItemStack remainder) {
            //If it has already been updated, no reason to update it again
            //If the remainder is empty we don't actually need to update what our inputs are
            if (!updated && !remainder.isEmpty()) {
                //Update inputs and mark that we have updated them
                for (int index = 0; index < inputSlots.length; index++) {
                    dummy.setItem(index, StackUtils.size(inputSlots[index].getStack(), 1));
                }
                updated = true;
            }
        }

        public void updateInputsWithReplacement(int index, ItemStack old) {
            //If it has already been updated, no reason to update it again
            if (!updated) {
                //Update inputs and mark that we have updated them
                for (int i = 0; i < inputSlots.length; i++) {
                    //If our index matches the one we are replacing the value of instead of getting from the slot
                    // use the stack we are replacing it with instead
                    ItemStack stack = i == index ? old : inputSlots[i].getStack();
                    dummy.setItem(i, StackUtils.size(stack, 1));
                }
                updated = true;
            }
        }

        public boolean isStackStillValid(Level world, ItemStack stack, int index) {
            updateInputs(stack);
            ItemStack old = dummy.getItem(index);
            dummy.setItem(index, StackUtils.size(stack, 1));
            if (lastRecipe != null && lastRecipe.matches(dummy, world)) {
                //If the remaining item is still valid in the recipe in that position return that it is still valid.
                // Note: The recipe should never actually be null here
                return true;
            }
            //Otherwise, revert the contents of the slot to what used to be in that slot
            // and return that the remaining item is not still valid in the slot
            dummy.setItem(index, old);
            return false;
        }
    }

    private class ReplacementHelper {

        private final Int2ObjectMap<Ingredient> slotIngredients = new Int2ObjectArrayMap<>(inputSlots.length);
        private boolean mapped;
        private boolean invalid;

        public void reset() {
            if (mapped) {
                //Only bother clearing maps and stuff if we don't know they are already empty
                mapped = false;
                invalid = false;
                slotIngredients.clear();
            }
        }

        public void findEquivalentItem(Level world, @Nonnull QIOFrequency frequency, CraftingWindowInventorySlot slot, int index, ItemStack used) {
            mapRecipe(index, used);
            if (invalid) {
                //If something about mapping the recipe went wrong, we can't find any equivalents
                return;
            }
            Ingredient usedIngredient = slotIngredients.getOrDefault(index, Ingredient.EMPTY);
            //Validate the ingredient was valid for its spot, because if it isn't something went wrong and there is no point
            // in attempting to find a replacement
            if (usedIngredient.test(used)) {
                for (ItemStack item : usedIngredient.getItems()) {
                    //If the ingredient is not vanilla, we start by checking against the exact stack it has stored as an item
                    // Note: We can use a raw hashed item here as we don't store it anywhere, and just use it as a lookup
                    if (!usedIngredient.isVanilla() && testEquivalentItem(world, frequency, slot, index, usedIngredient, HashedItem.raw(item))) {
                        //Match found, we can exit
                        return;
                    }
                    // if that didn't find a match, we go through all the items of the same basic type as the target item. For
                    // vanilla ingredients this is expected to end up finding a match for the first item that we have types for,
                    // but we check them all just in case the recipe is doing other validation in the matches check that doesn't
                    // get reflected in the ingredient matching, for example how MekanismShapedRecipe works. For more complex
                    // ingredients we do this because maybe we have some sort of "partial nbt" match or something and by checking
                    // the larger grouping of potential matches we may find one we would otherwise have missed
                    for (HashedItem type : frequency.getTypesForItem(item.getItem())) {
                        if (testEquivalentItem(world, frequency, slot, index, usedIngredient, type)) {
                            //Match found, we can exit
                            return;
                        }
                    }
                }
            }
        }

        private boolean testEquivalentItem(Level world, @Nonnull QIOFrequency frequency, CraftingWindowInventorySlot slot, int index, Ingredient usedIngredient,
              HashedItem replacementType) {
            if (frequency.getStored(replacementType) == 0 || !usedIngredient.test(replacementType.getStack())) {
                //Our frequency doesn't actually have the item stored we are trying to use or the type we are trying
                // doesn't actually match the ingredient we have for that slot
                return false;
            }
            ItemStack replacement = replacementType.createStack(1);
            //Make use of the fact that the remainder helper is called before checking for equivalent items so that
            // the base items in the recipe are filled in properly for it, we also make sure to properly initialize
            // the remainder helper's "inventory" while mapping the recipe if it hasn't already been initialized so
            // that we are able to just grab the "old" stack from the inventory like this
            ItemStack old = remainderHelper.dummy.getItem(index);
            if (remainderHelper.isStackStillValid(world, replacement, index)) {
                // Then we test if our replacement will work properly in our recipe, and if it does, and we are able to
                // insert it into the slot (which we should be able to), then we try removing the found item from the
                // frequency and adding it to the slot
                if (slot.insertItem(replacement, Action.SIMULATE, AutomationType.INTERNAL).isEmpty()) {
                    ItemStack removed = frequency.removeByType(replacementType, 1);
                    if (!removed.isEmpty()) {
                        ItemStack stack = slot.insertItem(removed, Action.EXECUTE, AutomationType.INTERNAL);
                        if (!stack.isEmpty()) {
                            //Note: This should never happen as we pre-validate attempting to insert, but in case it does, log it
                            Mekanism.logger.error("Failed to insert item ({} with NBT: {}) into crafting window: {}.", removed.getItem(), removed.getTag(),
                                  windowIndex);
                        }
                        //TODO - 1.18: Debate potentially briefly highlighting the slot to make it more evident to the player
                        // that something about the slot changed.
                        return true;
                    }
                }
                //If we couldn't insert it into the slot for some reason, or we somehow failed to remove it from the frequency
                // then we need to revert the stack from the remainder helper
                remainderHelper.dummy.setItem(index, old);
            }
            return false;
        }

        private void mapRecipe(int index, ItemStack used) {
            //If it has already been updated, no reason to update it again
            //If the remainder is empty we don't actually need to update what our inputs are
            if (!mapped) {
                mapped = true;
                if (lastRecipe == null || lastRecipe.isSpecial()) {
                    //The recipe should never be null, but we check it anyway. We also check if the recipe is
                    // special, because if it is then there are no "known" ingredients that we can use to try
                    // and figure out replacements
                    invalid = true;
                    return;
                }
                NonNullList<Ingredient> ingredients = lastRecipe.getIngredients();
                if (ingredients.isEmpty()) {
                    //Something went wrong
                    invalid = true;
                    return;
                }
                //Ensure our remainder helper has been initialized as we will make use of it in validation
                remainderHelper.updateInputsWithReplacement(index, used);
                IntFunction<ItemStack> itemGetter = i -> {
                    if (i == index) {
                        return used;
                    } else if (i >= 0 && i < inputSlots.length) {
                        return inputSlots[i].getStack();
                    }
                    return ItemStack.EMPTY;
                };
                if (lastRecipe instanceof IShapedRecipe<?> shapedRecipe) {
                    //It is a shaped recipe, make use of this information to attempt to find the proper match
                    mapShapedRecipe(shapedRecipe, ingredients, itemGetter);
                } else {
                    mapShapelessRecipe(ingredients, itemGetter);
                }
            }
        }

        private void mapShapedRecipe(IShapedRecipe<?> shapedRecipe, NonNullList<Ingredient> ingredients, IntFunction<ItemStack> itemGetter) {
            int recipeWidth = shapedRecipe.getRecipeWidth();
            int recipeHeight = shapedRecipe.getRecipeHeight();
            for (int columnStart = 0; columnStart <= 3 - recipeWidth; columnStart++) {
                for (int rowStart = 0; rowStart <= 3 - recipeHeight; rowStart++) {
                    //Note: Even though some shaped recipe implementations might not support a mirrored recipe as a match
                    // it really doesn't matter as we already know the recipe matched initially, and are mainly trying to
                    // find the offset for it. So if it doesn't support mirroring then it likely won't end up having it
                    // be so that it matches when mirrored, and if it does, the ingredients still should be close enough
                    // for the various spots given this is more of a heuristic than actually having to match no matter what,
                    // because we will end up testing the recipe with the item we try to use anyway at the end before moving it.
                    if (mapShapedRecipe(ingredients, columnStart, rowStart, recipeWidth, recipeHeight, true, itemGetter) ||
                        mapShapedRecipe(ingredients, columnStart, rowStart, recipeWidth, recipeHeight, false, itemGetter)) {
                        return;
                    }
                }
            }
            //Failed to find a matching way of handling it
            invalid = true;
        }

        private boolean mapShapedRecipe(NonNullList<Ingredient> ingredients, int columnStart, int rowStart, int recipeWidth, int recipeHeight, boolean mirrored,
              IntFunction<ItemStack> itemGetter) {
            for (int actualColumn = 0; actualColumn < 3; actualColumn++) {
                for (int actualRow = 0; actualRow < 3; actualRow++) {
                    int column = actualColumn - columnStart;
                    int row = actualRow - rowStart;
                    Ingredient ingredient = Ingredient.EMPTY;
                    if (column >= 0 && row >= 0 && column < recipeWidth && row < recipeHeight) {
                        if (mirrored) {
                            ingredient = ingredients.get(recipeWidth - column - 1 + row * recipeWidth);
                        } else {
                            ingredient = ingredients.get(column + row * recipeWidth);
                        }
                    }
                    int index = actualColumn + actualRow * 3;
                    if (ingredient.test(itemGetter.apply(index))) {
                        //If the ingredient matches, add it to our map
                        slotIngredients.put(index, ingredient);
                    } else {
                        //Otherwise, if the ingredient doesn't match, clear our stored ingredients
                        // as they were empty to start and return there is no match
                        slotIngredients.clear();
                        return false;
                    }
                }
            }
            return true;
        }

        private void mapShapelessRecipe(NonNullList<Ingredient> ingredients, IntFunction<ItemStack> itemGetter) {
            //Note: We don't make use of the "simple" way of looking the ingredients up that Vanilla's Shapeless recipe uses,
            // when all the ingredients are simple, as we care about which slot the various things happens in, which is much
            // easier to grab from forge's RecipeMatcher which works for simple ingredients as well, and is just not used
            // normally as it has slightly more overhead
            Int2IntMap actualLookup = new Int2IntArrayMap(inputSlots.length);
            List<ItemStack> inputs = new ArrayList<>(inputSlots.length);
            for (int index = 0; index < inputSlots.length; index++) {
                ItemStack stack = itemGetter.apply(index);
                if (!stack.isEmpty()) {
                    actualLookup.put(inputs.size(), index);
                    inputs.add(stack);
                }
            }
            int[] matches = RecipeMatcher.findMatches(inputs, ingredients);
            if (matches != null) {
                for (int ingredientIndex = 0; ingredientIndex < matches.length; ingredientIndex++) {
                    int actualSlot = actualLookup.getOrDefault(matches[ingredientIndex], -1);
                    if (actualSlot == -1) {
                        invalid = true;
                        return;
                    }
                    slotIngredients.put(actualSlot, ingredients.get(ingredientIndex));
                }
            } else {
                //No match
                invalid = true;
            }
        }
    }
}
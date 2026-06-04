package mekanism.common.content.qio;

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.attachments.containers.type.ContainerType;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.container.SelectedWindowData.WindowType;
import mekanism.common.inventory.container.slot.HotBarSlot;
import mekanism.common.inventory.container.slot.ITransactionalSlot;
import mekanism.common.inventory.container.slot.MainInventorySlot;
import mekanism.common.inventory.container.slot.TransactionalSlot;
import mekanism.common.inventory.slot.CraftingWindowInventorySlot;
import mekanism.common.inventory.slot.CraftingWindowOutputInventorySlot;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.util.RecipeMatcher;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QIOCraftingWindow implements IContentsListener {

    public static final int SLOTS_PER_WINDOW = 9;
    private static final SelectedWindowData[] WINDOWS = new SelectedWindowData[IQIOCraftingWindowHolder.MAX_CRAFTING_WINDOWS];

    static {
        for (byte tableIndex = 0; tableIndex < WINDOWS.length; tableIndex++) {
            WINDOWS[tableIndex] = new SelectedWindowData(WindowType.CRAFTING, tableIndex);
        }
    }

    private final CraftingWindowInventorySlot[] inputSlots = new CraftingWindowInventorySlot[SLOTS_PER_WINDOW];
    private final LastRecipeJournal lastRecipeJournal = new LastRecipeJournal();
    private final ReplacementHelper replacementHelper = new ReplacementHelper();
    private final RemainderHelper remainderHelper = new RemainderHelper();
    private final CraftingWindowInventorySlot outputSlot;
    private final IQIOCraftingWindowHolder holder;
    private final SelectedWindowData windowData;
    private final byte windowIndex;

    public QIOCraftingWindow(IQIOCraftingWindowHolder holder, byte windowIndex) {
        this.windowIndex = windowIndex;
        this.holder = holder;
        this.windowData = WINDOWS[windowIndex];
        for (int slotIndex = 0; slotIndex < SLOTS_PER_WINDOW; slotIndex++) {
            inputSlots[slotIndex] = CraftingWindowInventorySlot.input(this, this.holder);
        }
        outputSlot = CraftingWindowOutputInventorySlot.create(this);
    }

    public QIOCraftingWindow(IQIOCraftingWindowHolder holder, byte windowIndex, IntFunction<IContentsListener> inputSaveListener) {
        this.windowIndex = windowIndex;
        this.holder = holder;
        this.windowData = WINDOWS[windowIndex];
        for (int slotIndex = 0; slotIndex < SLOTS_PER_WINDOW; slotIndex++) {
            inputSlots[slotIndex] = CraftingWindowInventorySlot.input(this, inputSaveListener.apply(slotIndex));
        }
        outputSlot = CraftingWindowOutputInventorySlot.create(this);
    }

    public SelectedWindowData getWindowData() {
        return windowData;
    }

    public byte getWindowIndex() {
        return windowIndex;
    }

    public CraftingWindowInventorySlot getInputSlot(int slot) {
        if (slot < 0 || slot >= SLOTS_PER_WINDOW) {
            throw new IllegalArgumentException("Input slot out of range");
        }
        return inputSlots[slot];
    }

    public CraftingWindowInventorySlot getOutputSlot() {
        return outputSlot;
    }

    /**
     * Checks if the stack is equivalent to the current output.
     */
    public boolean isOutput(@NotNull ItemResource resource) {
        return outputSlot.resource().equals(resource);
    }

    @Override
    public void onContentsChanged() {
        //TODO: Is there some way to batch this so that when it happens post crafting then the updateOutputSlot only runs once
        // instead of once per input slot that has changed type
        //Note: We don't need to mark the holder as the contents have changed as that is done via the save listener
        //If we are not currently crafting, recalculate the contents for the output slot
        Level world = holder.getLevel();
        if (world != null && !world.isClientSide()) {
            updateOutputSlot(world, null);
        }
    }

    public void invalidateRecipe() {
        //Clear the cached recipe and output slot
        lastRecipeJournal.updateRecipe(null, null);
        ContainerType.ITEM.clearContents(outputSlot, null);
        Level world = holder.getLevel();
        if (world != null && !world.isClientSide()) {
            //And recheck the recipe
            updateOutputSlot(world, null);
        }
    }

    /**
     * @apiNote Only call on server
     */
    private void updateOutputSlot(@NotNull Level world, @Nullable TransactionContext transaction) {
        if (world.getServer() != null) {
            CraftingInput craftingInput = asCraftingInput().input();
            if (craftingInput.isEmpty()) {
                //If there is no input, then set the output to empty as there can't be a matching recipe
                ContainerType.ITEM.clearContents(outputSlot, transaction);
            } else if (lastRecipeJournal.recipe != null && lastRecipeJournal.recipe.value().matches(craftingInput, world)) {
                //If the recipe matches make sure we update the output anyway, as the output may have changed based on NBT
                // If the output slot was empty, then setting the slot to the recipe result fixes it not properly updating
                // when we remove a single item recipe such as for buttons, and put it back in;
                // and otherwise we update so that cases like bin upgrade recipes that the inputs match the recipe but the
                // output is dependent on the specific inputs gets updated properly
                //Note: We make sure to only call updateOutputSlot if we believe our inputs have changed type
                ItemStack result = assembleRecipe(craftingInput, lastRecipeJournal.recipe.value());
                outputSlot.setContents(ItemResource.of(result), result.count(), transaction);
            } else {
                //If we don't have a cached recipe, or our cached recipe doesn't match our inventory contents, lookup the recipe
                RecipeHolder<CraftingRecipe> recipe = MekanismRecipeType.getRecipeFor(RecipeType.CRAFTING, craftingInput, world).orElse(null);
                if (!Objects.equals(recipe, lastRecipeJournal.recipe)) {
                    if (recipe == null) {
                        //If there is no found recipe, clear the output, but don't update our last recipe
                        // as we can start by checking if they are doing the same recipe as we last found
                        ContainerType.ITEM.clearContents(outputSlot, transaction);
                    } else {
                        //If the recipe is different, update the output
                        lastRecipeJournal.updateRecipe(recipe, transaction);
                        ItemStack result = assembleRecipe(craftingInput, recipe.value());
                        outputSlot.setContents(ItemResource.of(result), result.count(), transaction);
                    }
                }
            }
        }
    }

    private ItemStack assembleRecipe(CraftingInput craftingInput, CraftingRecipe recipe) {
        //TODO - RecipeStages: Reinstate this when RecipeStages updates
        /*if (Mekanism.hooks.recipeStages.isLoaded()) {
            if (recipe instanceof IStagedRecipe stagedRecipe) {
                //Force assemble it as we handle validating if specific players can see/grab the output ourselves
                return stagedRecipe.forceAssemble(craftingInput);
            }
        }*/
        return recipe.assemble(craftingInput);
    }

    public boolean canViewRecipe(@NotNull ServerPlayer player) {
        if (lastRecipeJournal.recipe == null) {
            //If there is no last recipe, they can't craft it
            //Note: We don't check if it matches as if we don't have a match there won't
            // be anything in our output slot, so it doesn't matter
            return false;
        }
        //TODO - RecipeStages: Reinstate this when RecipeStages updates
        /*if (Mekanism.hooks.recipeStages.isLoaded()) {
            //If recipe stages is loaded check if the player has access to the recipe
            if (!RecipeStagesUtil.hasStageForRecipe(lastRecipeJournal.recipe.value(), player)) {
                return false;
            }
        }*/
        //If the recipe is dynamic, doLimitedCrafting is disabled, or the recipe is unlocked
        // allow viewing the recipe
        return lastRecipeJournal.recipe.value().isSpecial() || !player.level().getGameRules().get(GameRules.LIMITED_CRAFTING) || player.getRecipeBook().contains(lastRecipeJournal.recipe.id());
    }

    @Contract("null, _, _ -> false")
    private boolean validateAndUnlockRecipe(@Nullable Level world, @NotNull Player player, CraftingInput craftingInput) {
        if (world == null || lastRecipeJournal.recipe == null || !lastRecipeJournal.recipe.value().matches(craftingInput, world)) {
            //If the recipe isn't valid for the inputs, fail
            //Note: lastRecipe shouldn't be null here, but we validate it just in case
            return false;
        }
        if (lastRecipeJournal.recipe != null) {
            player.triggerRecipeCrafted(lastRecipeJournal.recipe, craftingInput.items());
            if (!lastRecipeJournal.recipe.value().isSpecial()) {
                if (player instanceof ServerPlayer serverPlayer && world instanceof ServerLevel level && level.getGameRules().get(GameRules.LIMITED_CRAFTING) &&
                    !serverPlayer.getRecipeBook().contains(lastRecipeJournal.recipe.id())) {
                    //If the player cannot use the recipe, don't allow crafting
                    return false;
                }
                //Unlock the recipe for the player
                player.awardRecipes(Collections.singleton(lastRecipeJournal.recipe));
            }
        }
        return true;
    }

    /**
     * Calculates absolute maximum of an output to attempt to craft, this may be higher than how much we have materials for
     */
    private int calculateMaxCraftAmount(@NotNull ItemResource itemType, int outputSize, @Nullable QIOFrequency frequency) {
        //Note: We start at the absolute max stack size, rather than at integer max value just to be a little more accurate
        int inputSize = Item.ABSOLUTE_MAX_STACK_SIZE;
        for (IInventorySlot inputSlot : inputSlots) {
            int count = inputSlot.amountAsInt();
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
        int maxToCraft = itemType.getMaxStackSize();
        //If we do, and the recipe isn't some weird edge case that produces more output that the item stacks to
        if (outputSize < maxToCraft) {
            //Round down our "stack" that we are producing to be as close but under a stack as we can get
            maxToCraft -= maxToCraft % outputSize;
        }
        return maxToCraft;
    }

    /**
     * @apiNote Only call from the server
     */
    public void emptyTo(boolean toPlayerInv, Iterable<TransactionalSlot> playerInventory, @Nullable TransactionContext transaction) {
        QIOFrequency frequency = holder.getFrequency();
        for (IInventorySlot inputSlot : inputSlots) {
            ItemResource slotResource = inputSlot.resource();
            if (!slotResource.isEmpty()) {
                int extracted;
                try (Transaction simulation = Transaction.open(transaction)) {
                    extracted = inputSlot.extract(slotResource, inputSlot.amountAsInt(), simulation, AutomationType.INTERNAL);
                    if (extracted == 0) {
                        continue;
                    }
                }
                try (Transaction subTransaction = Transaction.open(transaction)) {
                    int inserted;
                    if (toPlayerInv || frequency == null) {
                        inserted = MekanismContainer.insertItem(playerInventory, slotResource, extracted, subTransaction, windowData);
                    } else {
                        inserted = frequency.addItem(slotResource, extracted, subTransaction);
                    }
                    if (inserted > 0 && inputSlot.extract(slotResource, inserted, subTransaction, AutomationType.INTERNAL) == inserted) {
                        //Assuming nothing went wrong, commit all the changes
                        subTransaction.commit();
                    }
                }
            }
        }
    }

    /**
     * @apiNote For use with shift clicking
     */
    public void performCraft(@NotNull Player player, List<HotBarSlot> hotBarSlots, List<MainInventorySlot> mainInventorySlots) {
        try (Transaction transaction = Transaction.openRoot()) {
            performCraft(player, hotBarSlots, mainInventorySlots, transaction);
            transaction.commit();
        }
    }

    private void performCraft(@NotNull Player player, List<HotBarSlot> hotBarSlots, List<MainInventorySlot> mainInventorySlots, TransactionContext transaction) {
        if (lastRecipeJournal.recipe == null || outputSlot.isEmpty()) {
            //No recipe, return no result
            // Note: lastRecipeJournal.recipe will always null on the client, so we can assume we are server side below
            return;
        }
        Level world = holder.getLevel();
        CraftingInput.Positioned craftingInput = asCraftingInput();
        if (!validateAndUnlockRecipe(world, player, craftingInput.input())) {
            //If the recipe isn't valid, fail
            return;
        }
        QIOFrequency frequency = holder.getFrequency();
        //Mark that we are crafting so changes to the slots below don't force a bunch of recalculations to take place
        CommonHooks.setCraftingPlayer(player);
        //Figure out the base of the result stack after crafting (onCreated can adjust it slightly)
        final int amountPerCraft = outputSlot.amountAsInt();
        final ItemStack result = outputSlot.resource().toStack(amountPerCraft);
        Item resultItem = result.getItem();
        resultItem.onCraftedBy(result, player);
        final ItemResource resultType = ItemResource.of(result);
        int maxToCraft = calculateMaxCraftAmount(resultType, amountPerCraft, frequency);
        //Note: We initialized crafted here instead of in the for loop so that we can query how much was actually crafted
        int crafted = 0;
        remainderHelper.reset();
        replacementHelper.reset();
        boolean recheckOutput = false;
        LastInsertTarget lastInsertTarget = new LastInsertTarget();
        NonNullList<ItemStack> remaining = lastRecipeJournal.recipe.value().getRemainingItems(craftingInput.input());
        for (boolean stopCrafting = false; !stopCrafting && crafted < maxToCraft; crafted += amountPerCraft) {
            if (recheckOutput) {
                //If our inputs changed while crafting, and we are supposed to recheck the output,
                // update the contents of the output slot and the recipe that we are performing as
                // if there is an NBT sensitive recipe, the output may have changed
                recheckOutput = false;
                RecipeHolder<CraftingRecipe> oldRecipe = lastRecipeJournal.recipe;
                updateOutputSlot(world, transaction);
                if (!Objects.equals(oldRecipe, lastRecipeJournal.recipe)) {
                    //If the recipe changed, exit regardless of if the new recipe will produce the same output as the old one
                    // as there is a good chance something odd is going on or potentially even the doLimitedCrafting GameRule
                    // is enabled, and they only have access to one of the crafting recipes
                    break;
                }
                ItemResource updatedOutput = outputSlot.resource();
                if (updatedOutput.isEmpty() || !updatedOutput.is(resultItem)) {
                    //If we can't craft anymore or the resulting item changed entirely, stop crafting
                    break;
                }
                //If they may still be compatible, copy the stack, and apply the onCreated to it so that
                // we can adjust the NBT if it needs adjusting
                ItemStack potentialUpdatedOutput = updatedOutput.toStack(outputSlot.amountAsInt());
                resultItem.onCraftedBy(potentialUpdatedOutput, player);
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
                craftingInput = asCraftingInput();
                remaining = lastRecipeJournal.recipe.value().getRemainingItems(craftingInput.input());
            }
            try (Transaction subTransaction = Transaction.open(transaction)) {
                boolean craftFailed = false;
                //Try to insert into the hotbar and then the main inventory
                int inserted = lastInsertTarget.tryInserting(hotBarSlots, mainInventorySlots, windowData, resultType, amountPerCraft, subTransaction);
                if (inserted < amountPerCraft) {
                    //Note: If we aren't able to fit all the items we are crafting into the player's inventory, roll back the transaction
                    // and exit instead of attempting to insert the overflow into the QIO as it is easy enough if the player is trying
                    // to fill the QIO with something to then just transfer the contents into the QIO, and otherwise they are likely just trying
                    // to top their inventory off on a specific item and may be confused or not notice if some contents ended up in their storage system instead
                    break;
                }
                //Update slots with remaining contents
                for (int subIndex = 0, size = remaining.size(); subIndex < size; subIndex++) {
                    ItemStack remainder = remaining.get(subIndex);
                    int index = getIndexFromRemaining(craftingInput, subIndex);
                    IInventorySlot inputSlot = inputSlots[index];
                    if (inputSlot.amountAsLong() > 1) {
                        //If the input slot contains an item that is stacked, reduce the size of it by one
                        //Note: We "ignore" the fact that the container item may still be valid for the recipe, if the input is stacked
                        if (inputSlot.extract(inputSlot.resource(), 1, subTransaction, AutomationType.MANUAL) == 0) {
                            //If we couldn't actually extract the contents mark that it failed
                            craftFailed = true;
                            break;
                        }
                    } else if (inputSlot.amountAsLong() == 1) {
                        //Else if the input slot only has a single item in it, try removing from the frequency
                        if (frequency == null || remainderHelper.isStackStillValid(world, remainder, index)) {
                            //If the remaining item is still valid for the recipe in that slot, or we don't have a frequency, and it is the
                            // last stack in the slot, remove the stack from the slot
                            if (inputSlot.extract(inputSlot.resource(), 1, subTransaction, AutomationType.MANUAL) == 0) {
                                //If we couldn't actually extract the contents mark that it failed
                                craftFailed = true;
                                break;
                            }
                            // and mark that we should recheck our output as the recipe output may have changed, or we may
                            // no longer have enough inputs to craft an output
                            recheckOutput = true;
                        } else {
                            //Otherwise, try and remove the stack from the QIO frequency
                            ItemResource current = inputSlot.resource();
                            if (frequency.massExtract(current, 1, subTransaction) == 0) {
                                //If we were not able to remove any from the frequency, remove it from the crafting grid
                                if (inputSlot.extract(inputSlot.resource(), 1, subTransaction, AutomationType.MANUAL) == 0) {
                                    // if we couldn't actually extract the contents mark that it failed
                                    craftFailed = true;
                                    break;
                                }
                                // see if we have another valid input stored in the frequency and replace it with it if we do
                                replacementHelper.findEquivalentItem(world, frequency, inputSlot, index, current, subTransaction);
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
                    addRemainingItem(player, frequency, inputSlot, remainder, subTransaction);
                }
                if (craftFailed) {
                    //Roll back the last craft, and exit marking whatever we have successfully crafted as crafted
                    break;
                }
                //Commit this iteration of the craft
                subTransaction.commit();
            }
        }
        if (crafted > 0) {
            //Add to the stat how much of the item the player crafted that the player crafted the item
            player.awardStat(Stats.ITEM_CRAFTED.get(resultItem), crafted);
            //Note: We don't fire a crafting event as we don't want to allow for people to modify the output
            // stack or more importantly the input inventory during crafting
            //TODO: If this ends up causing major issues with some weird way another mod ends up doing crafting
            // we can evaluate how we want to handle it then/try to integrate support for firing it.
            //BasicEventHooks.firePlayerCraftingEvent(player, result, craftingInventory);
        }
        //Mark that we are done crafting
        CommonHooks.setCraftingPlayer(null);
    }

    private static int getIndexFromRemaining(CraftingInput.Positioned craftingInput, int subIndex) {
        int width = craftingInput.input().width();
        int height = craftingInput.input().height();
        int row = craftingInput.top() + (subIndex / width) % height;
        int column = craftingInput.left() + subIndex % width;
        return 3 * row + column;
    }

    @NotNull
    public ItemStack performCraft(@NotNull Player player, @NotNull ItemStack result, int amountCrafted) {
        //TODO - 1.18: Given we don't fire a crafting event and even if we did things would likely not work properly,
        // we may want to special case our bin filling and emptying recipes so that they can take directly from the frequency
        // and be a quick way to fill/empty an entire bin at once (also implement the same special handling for shift clicking)
        // Maybe for now an IQIOIntegratedCraftingRecipe or something like that that we can call?
        if (amountCrafted == 0 || lastRecipeJournal.recipe == null || result.isEmpty()) {
            //Nothing actually crafted or no recipe, return no result
            // Note: lastRecipe will always null on the client, so we can assume we are server side below
            return ItemStack.EMPTY;
        }
        Level world = holder.getLevel();
        CraftingInput.Positioned craftingInput = asCraftingInput();
        if (!validateAndUnlockRecipe(world, player, craftingInput.input())) {
            //If the recipe isn't valid, fail
            return ItemStack.EMPTY;
        }
        QIOFrequency frequency = holder.getFrequency();
        //Mark that we are crafting so changes to the slots below don't force a bunch of recalculations to take place
        CommonHooks.setCraftingPlayer(player);
        //Craft the result, note the result stack should always be a new instance by the time this method is called
        result.onCraftedBy(player, amountCrafted);
        //Note: We don't fire a crafting event as we don't want to allow for people to modify the output
        // stack or more importantly the input inventory during crafting
        //TODO: If this ends up causing major issues with some weird way another mod ends up doing crafting
        // we can evaluate how we want to handle it then/try to integrate support for firing it.
        //BasicEventHooks.firePlayerCraftingEvent(player, result, craftingInventory);
        NonNullList<ItemStack> remaining = lastRecipeJournal.recipe.value().getRemainingItems(craftingInput.input());
        try (Transaction transaction = Transaction.openRoot()) {
            remainderHelper.reset();
            replacementHelper.reset();
            //Update slots with remaining contents
            for (int subIndex = 0, size = remaining.size(); subIndex < size; subIndex++) {
                ItemStack remainder = remaining.get(subIndex);
                int index = getIndexFromRemaining(craftingInput, subIndex);
                IInventorySlot inputSlot = inputSlots[index];
                if (inputSlot.amountAsLong() > 1) {
                    //If the input slot contains an item that is stacked, reduce the size of it by one
                    //Note: We "ignore" the fact that the container item may still be valid for the recipe, if the input is stacked
                    if (inputSlot.extract(inputSlot.resource(), 1, transaction, AutomationType.MANUAL) == 0) {
                        //Failed to extract the item from the slot, bail and return nothing was crafted
                        return ItemStack.EMPTY;
                    }
                } else if (inputSlot.amountAsLong() == 1) {
                    //Else if the input slot only has a single item in it, try removing from the frequency
                    if (frequency == null || remainderHelper.isStackStillValid(world, remainder, index)) {
                        //If we have no frequency or the remaining item is still valid for the recipe in that slot,
                        // remove from the crafting window
                        if (inputSlot.extract(inputSlot.resource(), 1, transaction, AutomationType.MANUAL) == 0) {
                            //Failed to extract the item from the slot, bail and return nothing was crafted
                            return ItemStack.EMPTY;
                        }
                    } else {
                        //Otherwise, try and remove the stack from the QIO frequency
                        ItemResource current = inputSlot.resource();
                        if (frequency.massExtract(current, 1, transaction) == 0) {
                            //If we were not able to remove any from the frequency, remove it from the crafting grid
                            if (inputSlot.extract(inputSlot.resource(), 1, transaction, AutomationType.MANUAL) == 0) {
                                //Failed to extract the item from the slot, bail and return nothing was crafted
                                return ItemStack.EMPTY;
                            }
                            // see if we have another valid input stored in the frequency and replace it with it if we do
                            replacementHelper.findEquivalentItem(world, frequency, inputSlot, index, current, transaction);
                        }
                    }
                }
                //Note: No special handling needed here for if the remainder is empty
                addRemainingItem(player, frequency, inputSlot, remainder, transaction);
            }
            //Mark that we are done crafting
            CommonHooks.setCraftingPlayer(null);
            transaction.commit();
            return result;
        }
    }

    private void addRemainingItem(Player player, @Nullable QIOFrequency frequency, IInventorySlot slot, @NotNull ItemStack remainder, TransactionContext transaction) {
        if (remainder.isEmpty()) {
            //If there is no remainder, just exit
            return;
        }
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
        //Note: This is similar to ResultSlot#onTake's handling of the replacement
        int toInsert = remainder.count();
        ItemResource itemType = ItemResource.of(remainder);
        //Try inserting the item back into the slot it came from, this should only be able to actually insert it if it
        // is still valid for the recipe and the rest of the stack has been used completely
        toInsert -= slot.insert(itemType, toInsert, transaction, AutomationType.INTERNAL);
        if (toInsert > 0) {
            //If some or all of the stack could not be returned to the input slot add it to the player's inventory
            PlayerInventoryWrapper playerInv = PlayerInventoryWrapper.of(player);
            toInsert -= playerInv.insert(itemType, toInsert, transaction);
            if (toInsert > 0) {
                //failing that try adding it to the qio frequency if there is one
                if (frequency != null) {
                    toInsert -= frequency.addItem(itemType, toInsert, transaction);
                    if (toInsert == 0) {
                        //If we added it all to the QIO, don't bother trying to drop it
                        return;
                    }
                }
                //TODO: Before dropping it we may want to try putting it into the crafting inventory in any slot that will take it,
                // and then have the current stack go into the inventory/QIO?? In theory this sounds like a good idea but it is very
                // convoluted so has been skipped for now and probably ever. If it does get implemented then we need to make sure to
                // mark the output as needing an update in the "shift crafting" version that calls this method
                //If there is no frequency or we couldn't add it all to the QIO, drop the remaining item as the player
                playerInv.drop(itemType, toInsert, false, false, transaction);
            }
        }
    }

    /**
     * Used for helping keep track of were we were for inserting
     */
    private static class LastInsertTarget {

        private boolean wasHotBar = true;
        private int lastIndex;

        /// @return amount inserted
        public int tryInserting(List<HotBarSlot> hotBarSlots, List<MainInventorySlot> mainInventorySlots, SelectedWindowData windowData, ItemResource typeToInsert,
              int amountToInsert, @Nullable TransactionContext transaction) {
            try (Transaction subTransaction = Transaction.open(transaction)) {
                //Insert into stacks that already contain an item in the order hot bar -> main inventory
                // Note: The target helps us skip checking some slot types that we know may not be valid
                int inserted = insertItem(hotBarSlots, typeToInsert, amountToInsert, true, true, windowData, subTransaction);
                inserted += insertItem(mainInventorySlots, typeToInsert, amountToInsert - inserted, true, false, windowData, subTransaction);
                //If we still have any left then input into the empty stacks in the order of main inventory -> hot bar
                // Note: Even though we are doing the main inventory, we still need to do both, ignoring empty then not instead of
                // just directly inserting into the main inventory, in case there are empty slots before the one we can stack with
                inserted += insertItem(hotBarSlots, typeToInsert, amountToInsert - inserted, false, true, windowData, subTransaction);
                inserted += insertItem(mainInventorySlots, typeToInsert, amountToInsert - inserted, false, false, windowData, subTransaction);
                subTransaction.commit();
                return inserted;
            }
        }

        /**
         * Based on {@link MekanismContainer#insertItem(Iterable, ItemResource, int, TransactionContext, boolean, SelectedWindowData)} except with extra handling to keep
         * track of where we last were.
         *
         * @return Amount inserted
         */
        private <SLOT extends Slot & ITransactionalSlot> int insertItem(List<SLOT> slots, ItemResource itemType, final int amount, boolean ignoreEmpty, boolean isHotBar,
              @Nullable SelectedWindowData selectedWindow, TransactionContext transaction) {
            if (itemType.isEmpty() || amount == 0) {
                //Skip doing anything if the stack is already empty.
                // Makes it easier to chain calls, rather than having to check if the stack is empty after our previous call
                return 0;
            }
            int toInsert = amount;
            //Note: We don't check if we just ignored empty or not, as we should be able to insert into
            // and filled slot so once we do that slot stops being "empty" and we want to start at it
            for (int i = ignoreEmpty && wasHotBar == isHotBar ? lastIndex : 0, slotCount = slots.size(); i < slotCount; i++) {
                SLOT slot = slots.get(i);
                if (ignoreEmpty != slot.hasItem()) {
                    //Skip checking empty stacks if we want to ignore them, and skip non-empty stacks if we don't want to ignore them
                    continue;
                } else if (!slot.exists(selectedWindow)) {
                    // or if the slot doesn't "exist" for the current window configuration
                    continue;
                }
                //Decrease amount to insert by how much we were able to insert
                toInsert -= slot.insert(itemType, toInsert, transaction);
                if (toInsert == 0) {
                    //We finished inserting, update where we last targeted
                    wasHotBar = isHotBar;
                    lastIndex = i;
                    break;
                }
            }
            return amount - toInsert;
        }
    }

    public CraftingInput.Positioned asCraftingInput() {
        List<ItemStack> items = new ArrayList<>(SLOTS_PER_WINDOW);
        for (IInventorySlot inputSlot : inputSlots) {
            //Note: We copy this as we don't want to allow someone trying to interact with the stack directly
            // to change the size of it. We also add it regardless of it is empty as that is what the method expects
            // We also copy it to a count of one, to validate that no mods are trying to do stupid stacked recipe input based hacks
            items.add(inputSlot.resource().toStack());
        }
        return CraftingInput.ofPositioned(3, 3, items);
    }

    private static class LastRecipeJournal extends SnapshotJournal<@Nullable RecipeHolder<CraftingRecipe>> {

        @Nullable
        private RecipeHolder<CraftingRecipe> recipe;

        public void updateRecipe(@Nullable RecipeHolder<CraftingRecipe> recipe, @Nullable TransactionContext transaction) {
            if (transaction != null) {
                updateSnapshots(transaction);
            }
            this.recipe = recipe;
        }

        @Override
        protected @Nullable RecipeHolder<CraftingRecipe> createSnapshot() {
            return recipe;
        }

        @Override
        protected void revertToSnapshot(@Nullable RecipeHolder<CraftingRecipe> snapshot) {
            this.recipe = snapshot;
        }
    }

    private class RemainderHelper {

        private final NonNullList<ItemStack> dummy = NonNullList.withSize(SLOTS_PER_WINDOW, ItemStack.EMPTY);

        private boolean updated;

        public void reset() {
            if (updated) {
                updated = false;
                //Only clear the contents if we need to
                dummy.clear();
            }
        }

        private void updateInputs(@NotNull ItemResource remainder) {
            //If it has already been updated, no reason to update it again
            //If the remainder is empty we don't actually need to update what our inputs are
            if (!updated && !remainder.isEmpty()) {
                //Update inputs and mark that we have updated them
                for (int index = 0; index < inputSlots.length; index++) {
                    dummy.set(index, inputSlots[index].resource().toStack());
                }
                updated = true;
            }
        }

        public void updateInputsWithReplacement(int index, ItemResource oldType) {
            //If it has already been updated, no reason to update it again
            if (!updated) {
                //Update inputs and mark that we have updated them
                for (int i = 0; i < inputSlots.length; i++) {
                    //If our index matches the one we are replacing the value of instead of getting from the slot
                    // use the stack we are replacing it with instead
                    ItemResource type = i == index ? oldType : inputSlots[i].resource();
                    dummy.set(i, type.toStack());
                }
                updated = true;
            }
        }

        //TODO - 26.1: Re-evaluate callers
        public boolean isStackStillValid(Level world, ItemStack stack, int index) {
            return isStackStillValid(world, ItemResource.of(stack), index);
        }

        public boolean isStackStillValid(Level world, ItemResource itemType, int index) {
            updateInputs(itemType);
            ItemStack old = dummy.get(index);
            dummy.set(index, itemType.toStack(1));
            if (lastRecipeJournal.recipe != null && lastRecipeJournal.recipe.value().matches(CraftingInput.of(3, 3, dummy), world)) {
                //If the remaining item is still valid in the recipe in that position return that it is still valid.
                // Note: The recipe should never actually be null here
                return true;
            }
            //Otherwise, revert the contents of the slot to what used to be in that slot
            // and return that the remaining item is not still valid in the slot
            dummy.set(index, old);
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

        private static Iterable<ItemStack> getItems(Ingredient ingredient) {
            //todo - 26.1: unpack ingredients, check RecipeIndex(Cache)
            return Collections.emptyList();
        }

        public void findEquivalentItem(Level world, @NotNull QIOFrequency frequency, IInventorySlot slot, int index, ItemResource used, TransactionContext transaction) {
            mapRecipe(index, used);
            if (invalid) {
                //If something about mapping the recipe went wrong, we can't find any equivalents
                return;
            }
            Ingredient usedIngredient = slotIngredients.get(index);
            //Validate the ingredient was valid for its spot, because if it isn't something went wrong and there is no point
            // in attempting to find a replacement
            if (usedIngredient != null && usedIngredient.test(used.toStack())) {
                for (ItemStack item : getItems(usedIngredient)) {
                    if (item.isEmpty()) {
                        //If for some reason the ingredient returns empty stacks, just skip those
                        continue;
                    }
                    //Start by checking against the exact stack it has stored as an item
                    // Note: We can use a raw hashed item here as we don't store it anywhere, and just use it as a lookup
                    if (testEquivalentItem(world, frequency, slot, index, usedIngredient, ItemResource.of(item), transaction)) {
                        //Match found, we can exit
                        return;
                    }
                    // if that didn't find a match, we go through all the items of the same basic type as the target item. For
                    // vanilla ingredients this is expected to end up finding a match for the first item that we have types for,
                    // but we check them all just in case the recipe is doing other validation in the matches check that doesn't
                    // get reflected in the ingredient matching, for example how MekanismShapedRecipe works. For more complex
                    // ingredients we do this because maybe we have some sort of "partial nbt" match or something and by checking
                    // the larger grouping of potential matches we may find one we would otherwise have missed
                    for (ItemResource type : frequency.getTypesForItem(item.getItem())) {
                        if (testEquivalentItem(world, frequency, slot, index, usedIngredient, type, transaction)) {
                            //Match found, we can exit
                            return;
                        }
                    }
                }
            }
        }

        private boolean testEquivalentItem(Level world, @NotNull QIOFrequency frequency, IInventorySlot slot, int index, Ingredient usedIngredient,
              ItemResource replacementType, TransactionContext transaction) {
            if (!frequency.isStoring(replacementType) || !usedIngredient.test(replacementType.toStack())) {
                //Our frequency doesn't actually have the item stored we are trying to use or the type we are trying
                // doesn't actually match the ingredient we have for that slot
                return false;
            }
            //Make use of the fact that the remainder helper is called before checking for equivalent items so that
            // the base items in the recipe are filled in properly for it, we also make sure to properly initialize
            // the remainder helper's "inventory" while mapping the recipe if it hasn't already been initialized so
            // that we are able to just grab the "old" stack from the inventory like this
            ItemStack old = remainderHelper.dummy.get(index);
            if (remainderHelper.isStackStillValid(world, replacementType, index)) {
                // Then we test if our replacement will work properly in our recipe, and if it does, and we are able to
                // insert it into the slot (which we should be able to), then we try removing the found item from the
                // frequency and adding it to the slot
                try (Transaction subTransaction = Transaction.open(transaction)) {
                    int inserted = slot.insert(replacementType, 1, subTransaction, AutomationType.INTERNAL);
                    if (inserted > 0 && frequency.massExtract(replacementType, inserted, subTransaction) == inserted) {
                        //We were able to remove the one item we tried to, so commit our insertion to the slot
                        subTransaction.commit();
                        //TODO - 1.18: Debate potentially briefly highlighting the slot to make it more evident to the player
                        // that something about the slot changed.
                        return true;
                    }
                }
                //If we couldn't insert it into the slot for some reason, or we somehow failed to remove it from the frequency
                // then we need to revert the stack from the remainder helper
                remainderHelper.dummy.set(index, old);
            }
            return false;
        }

        private void mapRecipe(int index, ItemResource used) {
            //If it has already been updated, no reason to update it again
            //If the remainder is empty we don't actually need to update what our inputs are
            if (!mapped) {
                mapped = true;
                if (lastRecipeJournal.recipe == null || lastRecipeJournal.recipe.value().isSpecial()) {
                    //The recipe should never be null, but we check it anyway. We also check if the recipe is
                    // special, because if it is then there are no "known" ingredients that we can use to try
                    // and figure out replacements
                    invalid = true;
                    return;
                }
                List<Ingredient> ingredients = getIngredients(lastRecipeJournal.recipe.value());
                if (ingredients.isEmpty()) {
                    //Something went wrong
                    invalid = true;
                    return;
                }
                //Ensure our remainder helper has been initialized as we will make use of it in validation
                remainderHelper.updateInputsWithReplacement(index, used);
                if (lastRecipeJournal.recipe.value() instanceof ShapedRecipe shapedRecipe) {
                    //It is a shaped recipe, make use of this information to attempt to find the proper match
                    mapShapedRecipe(shapedRecipe, ingredients, index, used);
                } else {
                    mapShapelessRecipe(ingredients, index, used);
                }
            }
        }

        //TODO - 26.1: Recipes. probably use the display() and make it a util func
        private static List<Ingredient> getIngredients(CraftingRecipe value) {
            return Collections.emptyList();//value.getIngredients();
        }

        private ItemStack getItem(int i, int index, ItemResource used) {
            if (i == index) {
                return used.toStack();
            } else if (i >= 0 && i < inputSlots.length) {
                return inputSlots[i].resource().toStack();
            }
            return ItemStack.EMPTY;
        }

        private void mapShapedRecipe(ShapedRecipe shapedRecipe, List<Ingredient> ingredients, int index, ItemResource used) {
            int recipeWidth = shapedRecipe.getWidth();
            int recipeHeight = shapedRecipe.getHeight();
            for (int columnStart = 0; columnStart <= 3 - recipeWidth; columnStart++) {
                for (int rowStart = 0; rowStart <= 3 - recipeHeight; rowStart++) {
                    //Note: Even though some shaped recipe implementations might not support a mirrored recipe as a match
                    // it really doesn't matter as we already know the recipe matched initially, and are mainly trying to
                    // find the offset for it. So if it doesn't support mirroring then it likely won't end up having it
                    // be so that it matches when mirrored, and if it does, the ingredients still should be close enough
                    // for the various spots given this is more of a heuristic than actually having to match no matter what,
                    // because we will end up testing the recipe with the item we try to use anyway at the end before moving it.
                    if (mapShapedRecipe(ingredients, columnStart, rowStart, recipeWidth, recipeHeight, true, index, used) ||
                        mapShapedRecipe(ingredients, columnStart, rowStart, recipeWidth, recipeHeight, false, index, used)) {
                        return;
                    }
                }
            }
            //Failed to find a matching way of handling it
            invalid = true;
        }

        private boolean mapShapedRecipe(List<Ingredient> ingredients, int columnStart, int rowStart, int recipeWidth, int recipeHeight, boolean mirrored,
              int index, ItemResource used) {
            for (int actualColumn = 0; actualColumn < 3; actualColumn++) {
                for (int actualRow = 0; actualRow < 3; actualRow++) {
                    int column = actualColumn - columnStart;
                    int row = actualRow - rowStart;
                    Ingredient ingredient = null;
                    if (column >= 0 && row >= 0 && column < recipeWidth && row < recipeHeight) {
                        if (mirrored) {
                            ingredient = ingredients.get(recipeWidth - column - 1 + row * recipeWidth);
                        } else {
                            ingredient = ingredients.get(column + row * recipeWidth);
                        }
                    }
                    int i = actualColumn + actualRow * 3;
                    if (ingredient != null && ingredient.test(getItem(i, index, used))) {
                        //If the ingredient matches, add it to our map
                        slotIngredients.put(i, ingredient);
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

        private void mapShapelessRecipe(List<Ingredient> ingredients, int index, ItemResource used) {
            //Note: We don't make use of the "simple" way of looking the ingredients up that Vanilla's Shapeless recipe uses,
            // when all the ingredients are simple, as we care about which slot the various things happens in, which is much
            // easier to grab from forge's RecipeMatcher which works for simple ingredients as well, and is just not used
            // normally as it has slightly more overhead
            Int2IntMap actualLookup = new Int2IntArrayMap(inputSlots.length);
            List<ItemStack> inputs = new ArrayList<>(inputSlots.length);
            for (int i = 0; i < inputSlots.length; i++) {
                ItemStack stack = getItem(i, index, used);
                if (!stack.isEmpty()) {
                    actualLookup.put(inputs.size(), i);
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
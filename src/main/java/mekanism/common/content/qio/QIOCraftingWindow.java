package mekanism.common.content.qio;

import java.util.Arrays;
import java.util.Collections;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.IContentsListener;
import mekanism.api.inventory.AutomationType;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.slot.CraftingWindowInventorySlot;
import mekanism.common.inventory.slot.CraftingWindowOutputInventorySlot;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.util.NonNullList;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.hooks.BasicEventHooks;

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
            updateOutputSlot();
        }
    }

    //TODO: Re-evaluate our implementation of this
    private void updateOutputSlot() {
        World world = holder.getHolderWorld();
        if (world != null && !world.isRemote && world.getServer() != null) {
            if (craftingInventory.isEmpty()) {
                //If there is no input, then set the output to empty as there can't be a matching recipe
                if (!outputSlot.isEmpty()) {
                    outputSlot.setEmpty();
                }
            }//If we don't have a cached recipe, or our cached recipe doesn't match our inventory contents
            else if (lastRecipe == null || !lastRecipe.matches(craftingInventory, world)) {
                //Lookup the recipe
                ICraftingRecipe recipe = world.getServer().getRecipeManager().getRecipe(IRecipeType.CRAFTING, craftingInventory, world).orElse(null);
                if (recipe != lastRecipe) {
                    //If the recipe is different, update the output
                    lastRecipe = recipe;
                    if (lastRecipe == null) {
                        outputSlot.setEmpty();
                    } else {
                        outputSlot.setStack(recipe.getCraftingResult(craftingInventory));
                    }
                }
            }
        }
    }

    @Nonnull
    public ItemStack performCraft(@Nonnull PlayerEntity player, @Nonnull ItemStack result, int amountCrafted, boolean shiftClicked) {
        //TODO: Implement shiftClicking properly
        //TODO: Test how well our crafting windows handle how our bins allow inputting and outputting their inputs via crafting recipes
        if (amountCrafted == 0 || lastRecipe == null || result.isEmpty()) {
            //Nothing actually crafted or no recipe, return no result
            return ItemStack.EMPTY;
        }
        World world = holder.getHolderWorld();
        if (world == null || !lastRecipe.matches(craftingInventory, world)) {
            //If the recipe isn't valid for the inputs, fail
            return ItemStack.EMPTY;
        }
        if (lastRecipe != null && !lastRecipe.isDynamic()) {
            if (player instanceof ServerPlayerEntity && world.getGameRules().getBoolean(GameRules.DO_LIMITED_CRAFTING) &&
                !((ServerPlayerEntity) player).getRecipeBook().isUnlocked(lastRecipe)) {
                //If the player cannot use the recipe, don't allow crafting
                return ItemStack.EMPTY;
            }
            //Unload the recipe for the player
            player.unlockRecipes(Collections.singleton(lastRecipe));
        }
        //Mark that we are crafting so changes to the slots below don't force a bunch of recalculations to take place
        isCrafting = true;
        //Craft the result
        //TODO: We need to make sure we take the amountCrafted into account properly when shift clicking
        // as we will need to increment the item crafted stat further. It may be easier for us to just
        // manually increment the stat rather than holding off on calling result.onCrafting until we
        // know how much is actually being crafted as we pass it to the firePlayerCraftingEvent.
        // Is this even valid though given if we want to represent how much actually is being crafted
        // we may still need to just fire the event multiple times, especially in "weird" cases where
        // the container item is still valid for the recipe, but then the output becomes something different
        // because of NBT
        result.onCrafting(world, player, amountCrafted);
        BasicEventHooks.firePlayerCraftingEvent(player, result, craftingInventory);
        ForgeHooks.setCraftingPlayer(player);
        NonNullList<ItemStack> remaining = lastRecipe.getRemainingItems(craftingInventory);
        //Update slots with remaining contents
        //TODO: Make caller loop when shift clicking, which should be able to improve the performance a fair bit,
        // we can also then take into account the handling of removal of items and adding of container items so
        // that if we aren't updating the inputs because the container item is still valid (for example a damageable
        // input, in which case we would need to recheck), we won't have to relookup the remaining items as nothing
        // will have changed so they will still be valid
        //TODO: Behavior for shift clicking, use "all" the inputs, so if we say have four stacks of stone then
        // we craft four stacks of stone bricks and leave four pieces of stone in the crafting window. Then if
        // we shift click again we only craft up to one stack of the output. Note: We probably shouldn't have
        // it craft more items than fit in the inventory even if there is room in the QIO for them. The player
        // can easily enough just move items from the inventory into the QIO, if they are trying to fill the
        // QIO with something
        //TODO: We will do a similar thing for normal clicking in regards to the backing QIO inventory, in that
        // if the inputs are stacked we just use from the crafting window, otherwise we try taking from the QIO's
        // inventory, and only take from the crafting window if there is nothing stored in the QIO
        //TODO: Handling for remainder items:
        // if container item is still valid in that slot for the recipe
        //    or we don't have enough contents to do the recipe again,
        //      put it there
        // else try putting it into the player's inventory
        //    if there is no room for it then try putting it in the backing storage
        //      if there is no room there for it either (due to item type restrictions),
        //        put it in the crafting slots
        // if everything fails do what vanilla does as fallback and just drops it on the ground as the player
        //TODO: Figure out how we want to handle refilling if we run out of an item but have another item stored
        // that is valid for the recipe in that spot. Potentially we want to stop crafting, and refill it, but then
        // highlight the slot or something so that the player has an easier time seeing it changed
        for (int i = 0; i < remaining.size(); i++) {
            CraftingWindowInventorySlot inputSlot = inputSlots[i];
            //If the input slot contains an item, reduce the size of it by one
            if (!inputSlot.isEmpty()) {
                MekanismUtils.logMismatchedStackSize(1, inputSlot.shrinkStack(1, Action.EXECUTE));
            }
            //Add the remaining stack for the slot back into the slot
            //Note: We don't bother checking if it is empty as insertItem will just short circuit if it is
            ItemStack remainder = inputSlot.insertItem(remaining.get(i), Action.EXECUTE, AutomationType.INTERNAL);
            if (!remainder.isEmpty()) {
                //If some or all of the stack could not be returned to the input slot
                // add it to the player's inventory
                if (!player.inventory.addItemStackToInventory(remainder)) {
                    // and failing that drop it as the player
                    player.dropItem(remainder, false);
                    //TODO: Decide if before dropping it (or maybe even before adding to the player's inventory)
                    // we should have it first try to insert it into the QIO. Probably, yes before dropping,
                    // no before player inventory
                }
            }
        }
        ForgeHooks.setCraftingPlayer(null);
        //Mark that we are done crafting
        isCrafting = false;
        if (changedWhileCrafting) {
            //If our inputs changed while crafting then update the output slot
            changedWhileCrafting = false;
            updateOutputSlot();
        }
        return result;
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
            //TODO: Evaluate this, do we really want to/need to be copying?
            if (index >= 0 && index < getSizeInventory()) {
                return getInputSlot(index).getStack().copy();
            }
            return ItemStack.EMPTY;
        }

        @Nonnull
        @Override
        public ItemStack removeStackFromSlot(int index) {
            if (index >= 0 && index < getSizeInventory()) {
                CraftingWindowInventorySlot inputSlot = getInputSlot(index);
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
            //TODO: Re-evalauate, should this instead just force a refresh or something?
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
}
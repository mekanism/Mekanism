package mekanism.common.content.qio;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Supplier;
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
    private final byte windowIndex;
    @Nullable
    private ICraftingRecipe lastRecipe;
    private boolean isCrafting;
    private boolean changedWhileCrafting;
    private Supplier<World> worldSupplier;

    public QIOCraftingWindow(byte windowIndex, Supplier<World> worldSupplier) {
        this.windowIndex = windowIndex;
        this.worldSupplier = worldSupplier;
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
        //TODO: Call the tile/item to update persistence of contents as well
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
        World world = worldSupplier.get();
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
    public ItemStack performCraft(@Nonnull PlayerEntity player, @Nonnull ItemStack result, int amountCrafted) {
        if (amountCrafted == 0 || lastRecipe == null || result.isEmpty()) {
            //Nothing actually crafted or no recipe, return no result
            return ItemStack.EMPTY;
        }
        World world = worldSupplier.get();
        if (!lastRecipe.matches(craftingInventory, world)) {
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
        result.onCrafting(world, player, amountCrafted);
        BasicEventHooks.firePlayerCraftingEvent(player, result, craftingInventory);
        ForgeHooks.setCraftingPlayer(player);
        NonNullList<ItemStack> remaining = lastRecipe.getRemainingItems(craftingInventory);
        ForgeHooks.setCraftingPlayer(null);
        //Update slots with remaining contents
        //TODO: Figure out how we want to handle this in relationship to the stored stuff in the QIO
        // Aka we don't necessarily want to actually remove everything from the inputs if we have more
        // that is stored in the QIO that can be used
        //TODO: Make caller loop when shift clicking, if we also do this when handling removal and make it
        // so that container/other remaining items don't stay in the input slots by default unless there are
        // no more items, then we can probably even bypass/skip rechecking if the recipe matches as we know
        // for certain that it will match, we will need a way to know if it is shift crafting but that should
        // hopefully be relatively simple to achieve
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
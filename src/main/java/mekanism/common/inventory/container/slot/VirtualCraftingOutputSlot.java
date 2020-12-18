package mekanism.common.inventory.container.slot;

import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.inventory.AutomationType;
import mekanism.common.content.qio.QIOCraftingWindow;
import mekanism.common.inventory.slot.BasicInventorySlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class VirtualCraftingOutputSlot extends VirtualInventoryContainerSlot {

    @Nonnull
    private final QIOCraftingWindow craftingWindow;
    //TODO: Implement this on a player by player frame of reference so that we don't render the stack
    // if the player can't interact with it, maybe we could just use a custom boolean sync data, and
    // then make it so that the sync data when checking if dirty is player aware? Probably would be
    // messier than it is worth it, so might be better to just have a custom packet
    private boolean canCraft = true;
    private int amountCrafted;

    public VirtualCraftingOutputSlot(BasicInventorySlot slot, @Nullable SlotOverlay slotOverlay, Consumer<ItemStack> uncheckedSetter,
          @Nonnull QIOCraftingWindow craftingWindow) {
        super(slot, slotOverlay, uncheckedSetter);
        this.craftingWindow = craftingWindow;
    }

    @Override
    public boolean canMergeWith(@Nonnull ItemStack stack) {
        //Don't allow double clicking to pickup stacks from the output slot
        return false;
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        //Short circuit to avoid looking through the various predicates
        return false;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(@Nonnull ItemStack stack, Action action) {
        //Short circuit don't allow inserting into the output slot
        return stack;
    }

    @Nonnull
    @Override
    public ItemStack decrStackSize(int amount) {
        //Simulate extraction so as to not actually modify the slot
        //TODO: Does simulating here cause issues in relation to the canTakeStack
        // because of us not actually decreasing the size and then maybe something assuming
        // that it does get reduced in size
        //TODO: Also evaluate putStack as we don't want other people to be able to do it, we just want to do it ourselves
        ItemStack extracted = getInventorySlot().extractItem(amount, Action.SIMULATE, AutomationType.MANUAL);
        //Adjust amount crafted by the amount that would have actually been extracted
        amountCrafted += extracted.getCount();
        return extracted;
    }

    @Override
    protected void onCrafting(@Nonnull ItemStack stack, int amount) {
        amountCrafted += amount;
        onCrafting(stack);
    }

    @Override
    protected void onSwapCraft(int numItemsCrafted) {
        amountCrafted += numItemsCrafted;
    }

    @Nonnull
    @Override
    public ItemStack onTake(@Nonnull PlayerEntity player, @Nonnull ItemStack stack) {
        ItemStack result = craftingWindow.performCraft(player, stack, amountCrafted);
        amountCrafted = 0;
        return result;
    }

    @Override
    public boolean canTakeStack(@Nonnull PlayerEntity player) {
        return canCraft && super.canTakeStack(player);
    }

    @Nonnull
    @Override
    public ItemStack getStackToRender() {
        return canCraft ? super.getStackToRender() : ItemStack.EMPTY;
    }
}
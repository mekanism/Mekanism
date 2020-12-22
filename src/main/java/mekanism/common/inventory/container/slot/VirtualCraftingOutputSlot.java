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
        //TODO - cont: At the very least I think not decreasing the stack size, and then setting it again POST crafting,
        // will cause issues because of how shift clicking in vanilla with the transferStackInSlot call loops until the
        // output slot is empty. In theory if we have to we potentially could get around this by keeping track of which
        // game tick the output slot was last modified and just not allowing shift clicking to happen "twice" in a
        // single game tick
        //TODO: Also evaluate putStack as we don't want other people to be able to do it, we just want to be able to do it ourselves
        // for purposes of syncing from server to client. Best solution is probably add another method that we have our sync specifically
        // call for purposes of unchecked setting. Using unchecked setting may be valid though at least for purposes of realistically
        // people should be checking isValid before calling it, and if they don't it is a bug on their end?
        ItemStack extracted = getInventorySlot().extractItem(amount, Action.SIMULATE, AutomationType.MANUAL);
        //Adjust amount crafted by the amount that would have actually been extracted
        amountCrafted += extracted.getCount();
        return extracted;
    }

    /**
     * @implNote We override this similar to how {@link net.minecraft.inventory.container.CraftingResultSlot} does, but this never actually ends up getting called for our
     * slots.
     */
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
        //TODO: Pass shiftClicked as needed, maybe we can add a method to let slots handle shift clicking,
        // and default it to MekanismContainer#transferSuccess.
        ItemStack result = craftingWindow.performCraft(player, stack, amountCrafted, false);
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
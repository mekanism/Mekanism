package mekanism.api.inventory.slot;

import javax.annotation.Nonnull;
import mekanism.api.Action;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

//TODO: Handle persistence somewhere, so that we can load/save the slots in an IMekanismInventory
public interface IInventorySlot {

    /**
     * Returns the {@link ItemStack} in this {@link IInventorySlot}.
     *
     * The result's stack size may be greater than the itemstack's max size.
     *
     * If the result is empty, then the slot is empty.
     *
     * <p>
     * <strong>IMPORTANT:</strong> This {@link ItemStack} <em>MUST NOT</em> be modified. This method is not for
     * altering an inventory's contents. Any implementers who are able to detect modification through this method should throw an exception.
     * </p>
     * <p>
     * <strong><em>SERIOUSLY: DO NOT MODIFY THE RETURNED ITEMSTACK</em></strong>
     * </p>
     *
     * @return {@link ItemStack} in this {@link IInventorySlot}. Empty {@link ItemStack} if this {@link IInventorySlot} is empty.
     *
     * @apiNote <strong>IMPORTANT:</strong> Do not modify this {@link ItemStack}.
     */
    @Nonnull
    ItemStack getStack();

    /**
     * Overrides the stack in this {@link IInventorySlot}.
     *
     * @param stack {@link ItemStack} to set this slot to (may be empty).
     *
     * @throws RuntimeException if this slot is called in a way that it was not expecting.
     */
    void setStack(@Nonnull ItemStack stack);

    /**
     * <p>
     * Inserts an {@link ItemStack} into this {@link IInventorySlot} and return the remainder. The {@link ItemStack} <em>should not</em> be modified in this function!
     * </p>
     * Note: This behaviour is subtly different from {@link IFluidHandler#fill(FluidStack, FluidAction)}
     *
     * @param stack  {@link ItemStack} to insert. This must not be modified by the slot.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     *
     * @return The remaining {@link ItemStack} that was not inserted (if the entire stack is accepted, then return an empty {@link ItemStack}). May be the same as the
     * input {@link ItemStack} if unchanged, otherwise a new {@link ItemStack}. The returned ItemStack can be safely modified after
     *
     * @implNote The {@link ItemStack} <em>should not</em> be modified in this function!
     */
    @Nonnull
    ItemStack insertItem(@Nonnull ItemStack stack, Action action);

    /**
     * Extracts an {@link ItemStack} from this {@link IInventorySlot}.
     * <p>
     * The returned value must be empty if nothing is extracted, otherwise its stack size must be less than or equal to {@code amount} and {@link
     * ItemStack#getMaxStackSize()}.
     * </p>
     *
     * @param amount Amount to extract (may be greater than the current stack's max limit)
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     *
     * @return {@link ItemStack} extracted from the slot, must be empty if nothing can be extracted. The returned {@link ItemStack} can be safely modified after, so the
     * slot should return a new or copied stack.
     *
     * @implNote The returned {@link ItemStack} can be safely modified after, so a new or copied stack should be returned.
     */
    @Nonnull
    ItemStack extractItem(int amount, Action action);

    /**
     * Retrieves the maximum stack size allowed to exist in this {@link IInventorySlot}.
     *
     * @return The maximum stack size allowed in this {@link IInventorySlot}.
     */
    int getLimit();

    /**
     * <p>
     * This function re-implements the vanilla function {@link IInventory#isItemValidForSlot(int, ItemStack)}. It should be used instead of simulated insertions in cases
     * where the contents and state of the inventory are irrelevant, mainly for the purpose of automation and logic (for instance, testing if a minecart can wait to
     * deposit its items into a full inventory, or if the items in the minecart can never be placed into the inventory and should move on).
     * </p>
     * <ul>
     * <li>isItemValid is false when insertion of the item is never valid.</li>
     * <li>When isItemValid is true, no assumptions can be made and insertion must be simulated case-by-case.</li>
     * <li>The actual items in the inventory, its fullness, or any other state are <strong>not</strong> considered by isItemValid.</li>
     * </ul>
     *
     * @param stack Stack to test with for validity
     *
     * @return true if this {@link IInventorySlot} can accept the {@link ItemStack}, not considering the current state of the inventory. false if this {@link
     * IInventorySlot} can never insert the {@link ItemStack} in any situation.
     */
    boolean isItemValid(@Nonnull ItemStack stack);
}
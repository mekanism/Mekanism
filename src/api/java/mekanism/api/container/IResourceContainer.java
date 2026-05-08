package mekanism.api.container;

import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

//TODO - 26.1: Docs and decide if we want the bound for RESOURCE to be RegisteredResource or just Resource
public interface IResourceContainer<RESOURCE extends Resource> extends ValueIOSerializable, IContentsListener {

    RESOURCE getResource();

    //TODO - 26.1: Do we want to have two forms of get amount for our slot type similar to how the handler supports reporting a long variant?
    // It might be worth it, so that then fluids and chemicals can have storage of longs
    int amount();

    void setContents(RESOURCE type, int storedAmount);//TODO - 26.1: Do we want a transactional form of this? Probably would be semi useful

    int insert(RESOURCE resource, int amount, TransactionContext transaction, AutomationType automationType);

    //TODO - 26.1: Check callers and make sure none are relying on the fact that in the past for items extraction would be clamped at the max stack size
    int extract(RESOURCE resource, int amount, TransactionContext transaction, AutomationType automationType);

    /**
     * Retrieves the maximum stack size allowed to exist in this {@link IResourceContainer}. Unlike {@link IItemHandler#getSlotLimit(int)} this takes a stack that it can use
     * for checking max stack size, if this {@link IResourceContainer} wants to respect the maximum stack size.
     *
     * @param stack The stack we want to know the limit for in case this {@link IResourceContainer} wants to obey the stack limit. If the empty stack is passed, then it
     *              returns the max amount of any item this slot can store.
     *
     * @return The maximum stack size allowed in this {@link IResourceContainer}.
     *
     * @implNote The implementation of this CAN take into account the max size of this stack but is not required to.
     */
    int getLimit(RESOURCE resource);//TODO - 26.1: Update docs

    //TODO - 26.1: Re-evaluate name and add docs
    default int getCurrentLimit() {
        return getLimit(getResource());
    }

    /**
     * Gets the amount of fluid needed by this {@link IResourceContainer} to reach a filled state.
     *
     * @return Amount of fluid needed
     */
    default int getNeeded() {
        //TODO - 26.1: Do we want to allow passing a resource for calculating a more accurate limit when empty
        return Math.max(0, getCurrentLimit() - amount());
    }

    /**
     * <p>
     * This function re-implements the vanilla function {@link net.minecraft.world.Container#canPlaceItem(int, ItemStack)}. It should be used instead of simulated
     * insertions in cases where the contents and state of the inventory are irrelevant, mainly for the purpose of automation and logic (for instance, testing if a
     * minecart can wait to deposit its items into a full inventory, or if the items in the minecart can never be placed into the inventory and should move on).
     * </p>
     * <ul>
     * <li>isItemValid is false when insertion of the item is never valid.</li>
     * <li>When isItemValid is true, no assumptions can be made and insertion must be simulated case-by-case.</li>
     * <li>The actual items in the inventory, its fullness, or any other state are <strong>not</strong> considered by isItemValid.</li>
     * </ul>
     *
     * @param stack Stack to test with for validity
     *
     * @return true if this {@link IResourceContainer} can accept the {@link ItemStack}, not considering the current state of the inventory. false if this
     * {@link IResourceContainer} can never insert the {@link ItemStack} in any situation.
     */
    boolean isValid(RESOURCE type);
    //TODO - 26.1: Update docs and figure out handling of empty resource
    // Also Neo changed it to be if it is ever valid instead of valid for insertion, I believe we already behaved as such
    // but we should validate that we obey that properly

    /**
     * Ignores current contents
     */
    boolean isCurrentValidForExtraction(AutomationType automationType);//TODO - 26.1: Update docs

    /**
     * Ignores current contents
     */
    boolean isValidForInsertion(RESOURCE type, AutomationType automationType);//TODO - 26.1: Update docs

    /**
     * Convenience method for checking if this slot is empty.
     *
     * @return True if the slot is empty, false otherwise.
     */
    default boolean isEmpty() {//TODO - 26.1: Should we also validate that the amount isn't somehow zero?
        return getResource().isEmpty();
    }

    /**
     * Convenience method for emptying this {@link IResourceContainer}.
     */
    void setEmpty();//TODO - 26.1: Re-evaluate usages and the existence of this method
}
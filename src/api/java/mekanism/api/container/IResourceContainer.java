package mekanism.api.container;

import com.google.common.primitives.Ints;
import com.mojang.serialization.Codec;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.Range;

//TODO - 26.1: Docs and decide if we want the bound for RESOURCE to be RegisteredResource or just Resource
//TODO - 26.1: Should we rename this package to resource?
@NothingNullByDefault
public interface IResourceContainer<RESOURCE extends Resource> extends ValueIOSerializable, IContentsListener {

    RESOURCE getResource();

    default LargeResourceStack<RESOURCE> asStack() {
        //TODO - 26.1: Re-evaluate this method
        return new LargeResourceStack<>(getResource(), amountAsLong());
    }

    //TODO - 26.1: Do we want to have two forms of get amount for our slot type similar to how the handler supports reporting a long variant?
    // It might be worth it, so that then fluids and chemicals can have storage of longs
    @NonExtendable
    @Range(from = 0, to = Integer.MAX_VALUE)
    default int amount() {//TODO - 26.1: Review uses and see what should be moved to amountAsLong
        return Ints.saturatedCast(amountAsLong());
    }

    @Range(from = 0, to = Long.MAX_VALUE)
    long amountAsLong();

    //TODO - 26.1: Re-evaluate this method
    default void setContents(LargeResourceStack<RESOURCE> stack) {
        setContents(stack.resource(), stack.amount());
    }

    void setContents(RESOURCE type, @Range(from = 0, to = Long.MAX_VALUE) long storedAmount);//TODO - 26.1: Do we want a transactional form of this? Probably would be semi useful

    //TODO - 26.1: Re-evaluate this method and its callers
    void setContentsUnchecked(RESOURCE type, @Range(from = 0, to = Long.MAX_VALUE) long storedAmount);

    @Range(from = 0, to = Integer.MAX_VALUE)
    int insert(RESOURCE resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType);

    //TODO - 26.1: Check callers and make sure none are relying on the fact that in the past for items extraction would be clamped at the max stack size
    @Range(from = 0, to = Integer.MAX_VALUE)
    int extract(RESOURCE resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType);

    @NonExtendable
    @Range(from = 0, to = Integer.MAX_VALUE)
    default int getLimit(RESOURCE resource) {//TODO - 26.1: Review uses and see what should be moved to getLimitAsLong
        //TODO - 26.1: Update docs
        //TODO - 26.1: Do we want limit and amount to both have asInt for the base method name?
        return Ints.saturatedCast(getLimitAsLong(resource));
    }

    @Range(from = 0, to = Long.MAX_VALUE)
    long getLimitAsLong(RESOURCE resource);

    //TODO - 26.1: Re-evaluate name and add docs
    @NonExtendable
    @Range(from = 0, to = Integer.MAX_VALUE)
    default int getCurrentLimit() {
        return Ints.saturatedCast(getCurrentLimitAsLong());
    }

    @Range(from = 0, to = Long.MAX_VALUE)
    default long getCurrentLimitAsLong() {
        return getLimitAsLong(getResource());
    }

    /**
     * Gets the amount of fluid needed by this {@link IResourceContainer} to reach a filled state.
     *
     * @return Amount of fluid needed
     */
    @NonExtendable
    @Range(from = 0, to = Integer.MAX_VALUE)
    default int getNeeded() {
        //TODO - 26.1: Do we want to allow passing a resource for calculating a more accurate limit when empty
        //TODO - 26.1: Should this be a saturated cast of getNeededAsLong
        //return Math.max(0, getCurrentLimit() - amount());
        return Ints.saturatedCast(getNeededAsLong());
    }

    //TODO - 26.1: Re-evaluate callers of this method that used to use IChemicalTank#getNeeded. Do they need to know it as a long? Most probably don't
    @Range(from = 0, to = Long.MAX_VALUE)
    default long getNeededAsLong() {
        //TODO - 26.1: Do we want to allow passing a resource for calculating a more accurate limit when empty
        return Math.max(0, getLimitAsLong(getResource()) - amountAsLong());
    }

    boolean isValid(RESOURCE type);
    //TODO - 26.1: Update docs and figure out handling of empty resource
    // Also Neo changed it to be if it is ever valid instead of valid for insertion, I believe we already behaved as such
    // but we should validate that we obey that properly

    /**
     * Ignores current contents
     */
    default boolean isCurrentValidForExtraction(AutomationType automationType) {//TODO - 26.1: Update docs
        return true;
    }

    /**
     * Ignores current contents
     */
    default boolean isValidForInsertion(RESOURCE type, AutomationType automationType) {//TODO - 26.1: Update docs
        return true;
    }

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
    @NonExtendable
    default void setEmpty() {//TODO - 26.1: Re-evaluate usages and the existence of this method
        setContents(emptyStack());
    }

    @Override
    default void serialize(ValueOutput output) {
        //TODO - 1.21: This is a copy of BasicInventorySlot#serializeNBT. We might need to also grab the specific overrides of
        // that method as special component backed inventory slots, that then access and put that other data as a different component?
        // Also make sure to override things like TileEntityMekanism#applyInventorySlots and TileEntityMekanism#collectInventorySlots
        LargeResourceStack<RESOURCE> stored = asStack();
        if (!stored.isEmpty()) {
            //TODO - 26.1: Does using stored work fine for if something has multiple types of containers on a single stack?
            // Items used to store to the key "item", but fluids and chemicals used "stored"
            output.store(SerializationConstants.STORED, resourceStackCodec(), stored);
            //TODO - 26.1: Should we remove the key if stored is empty like we do for transmitters?
        }
    }

    @Override
    default void deserialize(ValueInput input) {
        LargeResourceStack<RESOURCE> stack = input.read(SerializationConstants.STORED, resourceStackCodec()).orElse(emptyStack());
        //Set the stack in an unchecked way so that if it is no longer valid, we don't end up
        // crashing due to the stack not being valid
        setContentsUnchecked(stack.resource(), stack.amount());
    }

    Codec<LargeResourceStack<RESOURCE>> resourceStackCodec();

    //TODO - 26.1: Re-evaluate this method vs having inheritors implement deserialize and setEmpty
    LargeResourceStack<RESOURCE> emptyStack();
}
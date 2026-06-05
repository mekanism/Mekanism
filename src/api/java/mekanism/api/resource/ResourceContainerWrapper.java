package mekanism.api.resource;

import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.Nullable;

/// Helper class to simplify wrapping a resource container while allowing providing overrides for specific methods.
///
/// @since 10.8.0
@NothingNullByDefault
public abstract class ResourceContainerWrapper<RESOURCE extends Resource, CONTAINER extends IResourceContainer<RESOURCE>> implements IResourceContainer<RESOURCE> {

    protected final CONTAINER internal;

    /// @param internal The container to wrap.
    protected ResourceContainerWrapper(CONTAINER internal) {
        this.internal = internal;
    }

    /// This method gets the innermost resource container for use in [copyContents][IResourceContainer#copyContents(IResourceContainer, TransactionContext)] when instance
    /// checks are required.
    public IResourceContainer<RESOURCE> getInternal() {
        IResourceContainer<RESOURCE> internal = this.internal;
        if (internal instanceof ResourceContainerWrapper<RESOURCE, ?> wrapper) {
            //For cases like valve fluid wrappers that are wrapping a merged tank
            // We want to return the actual source container
            return wrapper.getInternal();
        }
        return internal;
    }

    @Override
    public void setContents(LargeResourceStack<RESOURCE> contents, @Nullable TransactionContext transaction) {
        internal.setContents(contents, transaction);
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int insert(RESOURCE resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        return internal.insert(resource, amount, transaction, automationType);
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int extract(RESOURCE resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        return internal.extract(resource, amount, transaction, automationType);
    }

    @Override
    public void copyContents(IResourceContainer<RESOURCE> other, @Nullable TransactionContext transaction) {
        internal.copyContents(other, transaction);
    }

    @Override
    public void serialize(ValueOutput output) {
        internal.serialize(output);
    }

    @Override
    public void deserialize(ValueInput input) {
        internal.deserialize(input);
    }

    @Override
    public RESOURCE resource() {
        return internal.resource();
    }

    @Override
    public LargeResourceStack<RESOURCE> asStack() {
        return internal.asStack();
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long amountAsLong() {
        return internal.amountAsLong();
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long capacityAsLong(RESOURCE resource) {
        return internal.capacityAsLong(resource);
    }

    @Override
    public boolean isValid(RESOURCE resource) {
        return internal.isValid(resource);
    }

    @Override
    public boolean isCurrentValidForExtraction(AutomationType automationType) {
        return internal.isCurrentValidForExtraction(automationType);
    }

    @Override
    public boolean isValidForInsertion(RESOURCE resource, AutomationType automationType) {
        return internal.isValidForInsertion(resource, automationType);
    }
}
package mekanism.common.capabilities;

import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.resource.IResourceContainer;
import mekanism.api.resource.LargeResourceStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.Nullable;

@NothingNullByDefault//TODO - 26.1: Do we want to expose this to the API?
public abstract class ResourceContainerWrapper<RESOURCE extends Resource, CONTAINER extends IResourceContainer<RESOURCE>> implements IResourceContainer<RESOURCE> {

    protected final CONTAINER internal;

    public ResourceContainerWrapper(CONTAINER internal) {
        this.internal = internal;
    }

    @Override
    public void setContents(LargeResourceStack<RESOURCE> contents, @Nullable TransactionContext transaction) {
        internal.setContents(contents, transaction);
    }

    @Override
    public void setContents(RESOURCE type, @Range(from = 0, to = Long.MAX_VALUE) long storedAmount, @Nullable TransactionContext transaction) {
        internal.setContents(type, storedAmount, transaction);
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
    public void onContentsChanged() {
        internal.onContentsChanged();
    }

    @Override
    public boolean isEmpty() {
        return internal.isEmpty();
    }

    @Override
    public void copyContents(IResourceContainer<RESOURCE> other) {
        //TODO - 26.1: Evaluate how this method interacts with things doing instance checks
        internal.copyContents(other);
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
    @Range(from = 0, to = Long.MAX_VALUE)
    public long getNeededAsLong(RESOURCE resource) {
        return internal.getNeededAsLong(resource);
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
package mekanism.common.inventory.container.sync.chemical;

import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IEmptyStackProvider;
import mekanism.common.inventory.container.sync.ISyncableData;

/**
 * Version of {@link net.minecraft.util.IntReferenceHolder} for handling chemical stacks
 */
public abstract class SyncableChemicalStack<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> implements ISyncableData,
      IEmptyStackProvider<CHEMICAL, STACK> {

    @Nonnull
    private ChemicalStack<CHEMICAL> lastKnownValue;
    private final Supplier<@NonNull STACK> getter;
    private final Consumer<@NonNull STACK> setter;

    protected SyncableChemicalStack(Supplier<@NonNull STACK> getter, Consumer<@NonNull STACK> setter) {
        this.getter = getter;
        this.setter = setter;
        lastKnownValue = getEmptyStack();
    }

    @Nonnull
    protected abstract STACK createStack(STACK stored, long size);

    @Nonnull
    public STACK get() {
        return getter.get();
    }

    public void set(@Nonnull STACK value) {
        setter.accept(value);
    }

    public void set(long amount) {
        STACK stack = get();
        if (!stack.isEmpty()) {
            //Double check it is not empty
            set(createStack(stack, amount));
        }
    }

    @Override
    public DirtyType isDirty() {
        STACK value = get();
        boolean sameType = value.isTypeEqual(this.lastKnownValue);
        if (!sameType || value.getAmount() != this.lastKnownValue.getAmount()) {
            //Make sure to copy it in case our infusion stack object is the same object so would be getting modified
            // only do so though if it is dirty, as we don't need to spam object creation
            this.lastKnownValue = value.copy();
            return sameType ? DirtyType.SIZE : DirtyType.DIRTY;
        }
        return DirtyType.CLEAN;
    }
}
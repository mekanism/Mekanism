package mekanism.common.inventory.container.sync;

import javax.annotation.Nonnull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalTank;

/**
 * Version of {@link net.minecraft.util.IntReferenceHolder} for handling chemical stacks
 */
public abstract class SyncableChemicalStack<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> implements ISyncableData {

    @Nonnull
    private ChemicalStack<CHEMICAL> lastKnownValue;
    private final ChemicalTank<CHEMICAL, STACK> handler;

    protected SyncableChemicalStack(ChemicalTank<CHEMICAL, STACK> handler) {
        this.handler = handler;
        lastKnownValue = getEmptyStack();
        //TODO: Set to empty
    }

    //TODO: Is there a better way to make this super class know about the empty stack?
    @Nonnull
    protected abstract STACK getEmptyStack();

    @Nonnull
    protected abstract STACK createStack(STACK stored, int size);

    @Nonnull
    public STACK get() {
        return handler.getStack();
    }

    public void set(@Nonnull STACK value) {
        handler.setStack(value);
    }

    public void set(int amount) {
        if (!handler.isEmpty()) {
            //Double check it is not empty
            handler.setStack(createStack(handler.getStack(), amount));
        }
    }

    @Override
    public DirtyType isDirty() {
        STACK value = this.get();
        boolean sameType = value.isTypeEqual(this.lastKnownValue);
        if (!sameType || value.getAmount() != this.lastKnownValue.getAmount()) {
            //Make sure to copy it in case our infusion stack object is the same object so would be getting modified
            // only do so though if it is dirty, as we don't need to spam object creation
            this.lastKnownValue = value.copy();
            return !sameType ? DirtyType.DIRTY : DirtyType.SIZE;
        }
        return DirtyType.CLEAN;
    }
}
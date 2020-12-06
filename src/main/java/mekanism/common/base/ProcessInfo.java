package mekanism.common.base;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.inventory.slot.FactoryInputInventorySlot;

public class ProcessInfo {

    @Nonnull
    private final FactoryInputInventorySlot<?> inputSlot;
    @Nonnull
    private final IInventorySlot outputSlot;
    @Nullable
    private final IInventorySlot secondaryOutputSlot;
    private final int process;

    public ProcessInfo(int process, @Nonnull FactoryInputInventorySlot<?> inputSlot, @Nonnull IInventorySlot outputSlot, @Nullable IInventorySlot secondaryOutputSlot) {
        this.inputSlot = inputSlot;
        this.outputSlot = outputSlot;
        this.secondaryOutputSlot = secondaryOutputSlot;
        this.process = process;
    }

    public int getProcess() {
        return process;
    }

    @Nonnull
    public FactoryInputInventorySlot<?> getInputSlot() {
        return inputSlot;
    }

    @Nonnull
    public IInventorySlot getOutputSlot() {
        return outputSlot;
    }

    @Nullable
    public IInventorySlot getSecondaryOutputSlot() {
        return secondaryOutputSlot;
    }
}
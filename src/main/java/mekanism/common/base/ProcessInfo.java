package mekanism.common.base;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.inventory.IInventorySlot;

public class ProcessInfo {

    @Nonnull
    private final IInventorySlot inputSlot;
    @Nonnull
    private final IInventorySlot outputSlot;
    @Nullable
    private final IInventorySlot secondaryOutputSlot;
    private final int process;

    public ProcessInfo(int process, @Nonnull IInventorySlot inputSlot, @Nonnull IInventorySlot outputSlot, @Nullable IInventorySlot secondaryOutputSlot) {
        this.inputSlot = inputSlot;
        this.outputSlot = outputSlot;
        this.secondaryOutputSlot = secondaryOutputSlot;
        this.process = process;
    }

    public int getProcess() {
        return process;
    }

    @Nonnull
    public IInventorySlot getInputSlot() {
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
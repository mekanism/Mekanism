package mekanism.common.inventory.container.sync;

import javax.annotation.Nonnull;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.gas.BasicGasTank;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.common.network.container.property.GasStackPropertyData;
import mekanism.common.network.container.property.IntPropertyData;
import mekanism.common.network.container.property.PropertyData;

/**
 * Version of {@link net.minecraft.util.IntReferenceHolder} for handling gas stacks
 */
public class SyncableGasStack extends SyncableChemicalStack<Gas, GasStack> {

    public static SyncableGasStack create(BasicGasTank handler) {
        return new SyncableGasStack(handler);
    }

    private SyncableGasStack(IChemicalTank<Gas, GasStack> handler) {
        super(handler);
    }

    @Nonnull
    @Override
    protected GasStack getEmptyStack() {
        return GasStack.EMPTY;
    }

    @Nonnull
    @Override
    protected GasStack createStack(GasStack stored, int size) {
        return new GasStack(stored, size);
    }

    @Override
    public PropertyData getPropertyData(short property, DirtyType dirtyType) {
        if (dirtyType == DirtyType.SIZE) {
            //If only the size changed, don't bother re-syncing the type
            return new IntPropertyData(property, get().getAmount());
        }
        return new GasStackPropertyData(property, get());
    }
}
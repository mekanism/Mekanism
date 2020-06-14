package mekanism.common.inventory.container.sync.chemical;

import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IEmptyGasProvider;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.common.network.container.property.LongPropertyData;
import mekanism.common.network.container.property.PropertyData;
import mekanism.common.network.container.property.chemical.GasStackPropertyData;

/**
 * Version of {@link net.minecraft.util.IntReferenceHolder} for handling gas stacks
 */
public class SyncableGasStack extends SyncableChemicalStack<Gas, GasStack> implements IEmptyGasProvider {

    public static SyncableGasStack create(IGasTank handler) {
        return new SyncableGasStack(handler::getStack, handler::setStack);
    }

    public static SyncableGasStack create(Supplier<@NonNull GasStack> getter, Consumer<@NonNull GasStack> setter) {
        return new SyncableGasStack(getter, setter);
    }

    private SyncableGasStack(Supplier<@NonNull GasStack> getter, Consumer<@NonNull GasStack> setter) {
        super(getter, setter);
    }

    @Nonnull
    @Override
    protected GasStack createStack(GasStack stored, long size) {
        return new GasStack(stored, size);
    }

    @Override
    public PropertyData getPropertyData(short property, DirtyType dirtyType) {
        if (dirtyType == DirtyType.SIZE) {
            //If only the size changed, don't bother re-syncing the type
            return new LongPropertyData(property, get().getAmount());
        }
        return new GasStackPropertyData(property, get());
    }
}
package mekanism.common.inventory.container.sync.chemical;

import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IEmptyGasProvider;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.common.network.to_client.container.property.LongPropertyData;
import mekanism.common.network.to_client.container.property.PropertyData;
import mekanism.common.network.to_client.container.property.chemical.GasStackPropertyData;

/**
 * Version of {@link net.minecraft.util.IntReferenceHolder} for handling gas stacks
 */
public class SyncableGasStack extends SyncableChemicalStack<Gas, GasStack> implements IEmptyGasProvider {

    public static SyncableGasStack create(IGasTank handler) {
        return create(handler, false);
    }

    public static SyncableGasStack create(IGasTank handler, boolean isClient) {
        //Note: While strictly speaking the server should never end up having the setter called, because we have side
        // information readily available here we use the checked setter on the server side just to be safe. The reason
        // that we need to use unchecked setters on the client is that if a recipe got removed so there is a substance
        // in a tank that was valid but no longer is valid, we want to ensure that the client is able to properly render
        // it instead of printing an error due to the client thinking that it is invalid
        return create(handler::getStack, isClient ? handler::setStackUnchecked : handler::setStack);
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
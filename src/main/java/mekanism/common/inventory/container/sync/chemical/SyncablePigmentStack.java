package mekanism.common.inventory.container.sync.chemical;

import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.api.chemical.pigment.IEmptyPigmentProvider;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.common.network.to_client.container.property.LongPropertyData;
import mekanism.common.network.to_client.container.property.PropertyData;
import mekanism.common.network.to_client.container.property.chemical.PigmentStackPropertyData;
import org.jetbrains.annotations.NotNull;

/**
 * Version of {@link net.minecraft.world.inventory.DataSlot} for handling pigment stacks
 */
public class SyncablePigmentStack extends SyncableChemicalStack<Pigment, PigmentStack> implements IEmptyPigmentProvider {

    public static SyncablePigmentStack create(IPigmentTank handler) {
        return create(handler, false);
    }

    public static SyncablePigmentStack create(IPigmentTank handler, boolean isClient) {
        //Note: While strictly speaking the server should never end up having the setter called, because we have side
        // information readily available here we use the checked setter on the server side just to be safe. The reason
        // that we need to use unchecked setters on the client is that if a recipe got removed so there is a substance
        // in a tank that was valid but no longer is valid, we want to ensure that the client is able to properly render
        // it instead of printing an error due to the client thinking that it is invalid
        return create(handler::getStack, isClient ? handler::setStackUnchecked : handler::setStack);
    }

    public static SyncablePigmentStack create(Supplier<@NotNull PigmentStack> getter, Consumer<@NotNull PigmentStack> setter) {
        return new SyncablePigmentStack(getter, setter);
    }

    private SyncablePigmentStack(Supplier<@NotNull PigmentStack> getter, Consumer<@NotNull PigmentStack> setter) {
        super(getter, setter);
    }

    @NotNull
    @Override
    protected PigmentStack createStack(PigmentStack stored, long size) {
        return new PigmentStack(stored, size);
    }

    @Override
    public PropertyData getPropertyData(short property, DirtyType dirtyType) {
        if (dirtyType == DirtyType.SIZE) {
            //If only the size changed, don't bother re-syncing the type
            return new LongPropertyData(property, get().getAmount());
        }
        return new PigmentStackPropertyData(property, get());
    }
}
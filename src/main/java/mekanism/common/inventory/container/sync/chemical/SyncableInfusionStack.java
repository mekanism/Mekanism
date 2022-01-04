package mekanism.common.inventory.container.sync.chemical;

import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.infuse.IEmptyInfusionProvider;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.common.network.to_client.container.property.LongPropertyData;
import mekanism.common.network.to_client.container.property.PropertyData;
import mekanism.common.network.to_client.container.property.chemical.InfusionStackPropertyData;

/**
 * Version of {@link net.minecraft.world.inventory.DataSlot} for handling infusion stacks
 */
public class SyncableInfusionStack extends SyncableChemicalStack<InfuseType, InfusionStack> implements IEmptyInfusionProvider {

    public static SyncableInfusionStack create(IInfusionTank handler) {
        return create(handler, false);
    }

    public static SyncableInfusionStack create(IInfusionTank handler, boolean isClient) {
        //Note: While strictly speaking the server should never end up having the setter called, because we have side
        // information readily available here we use the checked setter on the server side just to be safe. The reason
        // that we need to use unchecked setters on the client is that if a recipe got removed so there is a substance
        // in a tank that was valid but no longer is valid, we want to ensure that the client is able to properly render
        // it instead of printing an error due to the client thinking that it is invalid
        return create(handler::getStack, isClient ? handler::setStackUnchecked : handler::setStack);
    }

    public static SyncableInfusionStack create(Supplier<@NonNull InfusionStack> getter, Consumer<@NonNull InfusionStack> setter) {
        return new SyncableInfusionStack(getter, setter);
    }

    private SyncableInfusionStack(Supplier<@NonNull InfusionStack> getter, Consumer<@NonNull InfusionStack> setter) {
        super(getter, setter);
    }

    @Nonnull
    @Override
    protected InfusionStack createStack(InfusionStack stored, long size) {
        return new InfusionStack(stored, size);
    }

    @Override
    public PropertyData getPropertyData(short property, DirtyType dirtyType) {
        if (dirtyType == DirtyType.SIZE) {
            //If only the size changed, don't bother re-syncing the type
            return new LongPropertyData(property, get().getAmount());
        }
        return new InfusionStackPropertyData(property, get());
    }
}
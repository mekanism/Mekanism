package mekanism.common.inventory.container.sync.chemical;

import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.api.chemical.slurry.IEmptySlurryProvider;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.network.to_client.container.property.LongPropertyData;
import mekanism.common.network.to_client.container.property.PropertyData;
import mekanism.common.network.to_client.container.property.chemical.SlurryStackPropertyData;
import org.jetbrains.annotations.NotNull;

/**
 * Version of {@link net.minecraft.world.inventory.DataSlot} for handling slurry stacks
 */
public class SyncableSlurryStack extends SyncableChemicalStack<Slurry, SlurryStack> implements IEmptySlurryProvider {

    public static SyncableSlurryStack create(ISlurryTank handler) {
        return create(handler, false);
    }

    public static SyncableSlurryStack create(ISlurryTank handler, boolean isClient) {
        //Note: While strictly speaking the server should never end up having the setter called, because we have side
        // information readily available here we use the checked setter on the server side just to be safe. The reason
        // that we need to use unchecked setters on the client is that if a recipe got removed so there is a substance
        // in a tank that was valid but no longer is valid, we want to ensure that the client is able to properly render
        // it instead of printing an error due to the client thinking that it is invalid
        return create(handler::getStack, isClient ? handler::setStackUnchecked : handler::setStack);
    }

    public static SyncableSlurryStack create(Supplier<@NotNull SlurryStack> getter, Consumer<@NotNull SlurryStack> setter) {
        return new SyncableSlurryStack(getter, setter);
    }

    private SyncableSlurryStack(Supplier<@NotNull SlurryStack> getter, Consumer<@NotNull SlurryStack> setter) {
        super(getter, setter);
    }

    @NotNull
    @Override
    protected SlurryStack createStack(SlurryStack stored, long size) {
        return new SlurryStack(stored, size);
    }

    @Override
    public PropertyData getPropertyData(short property, DirtyType dirtyType) {
        if (dirtyType == DirtyType.SIZE) {
            //If only the size changed, don't bother re-syncing the type
            return new LongPropertyData(property, get().getAmount());
        }
        return new SlurryStackPropertyData(property, get());
    }
}
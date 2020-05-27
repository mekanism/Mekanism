package mekanism.common.inventory.container.sync.chemical;

import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.slurry.IEmptySlurryProvider;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.network.container.property.LongPropertyData;
import mekanism.common.network.container.property.PropertyData;
import mekanism.common.network.container.property.chemical.SlurryStackPropertyData;

/**
 * Version of {@link net.minecraft.util.IntReferenceHolder} for handling slurry stacks
 */
public class SyncableSlurryStack extends SyncableChemicalStack<Slurry, SlurryStack> implements IEmptySlurryProvider {

    public static SyncableSlurryStack create(ISlurryTank handler) {
        return new SyncableSlurryStack(handler::getStack, handler::setStack);
    }

    public static SyncableSlurryStack create(Supplier<@NonNull SlurryStack> getter, Consumer<@NonNull SlurryStack> setter) {
        return new SyncableSlurryStack(getter, setter);
    }

    private SyncableSlurryStack(Supplier<@NonNull SlurryStack> getter, Consumer<@NonNull SlurryStack> setter) {
        super(getter, setter);
    }

    @Nonnull
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
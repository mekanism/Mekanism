package mekanism.common.inventory.container.sync.chemical;

import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.pigment.IEmptyPigmentProvider;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.common.network.container.property.LongPropertyData;
import mekanism.common.network.container.property.PropertyData;
import mekanism.common.network.container.property.chemical.PigmentStackPropertyData;

/**
 * Version of {@link net.minecraft.util.IntReferenceHolder} for handling pigment stacks
 */
public class SyncablePigmentStack extends SyncableChemicalStack<Pigment, PigmentStack> implements IEmptyPigmentProvider {

    public static SyncablePigmentStack create(IPigmentTank handler) {
        return new SyncablePigmentStack(handler::getStack, handler::setStack);
    }

    public static SyncablePigmentStack create(Supplier<@NonNull PigmentStack> getter, Consumer<@NonNull PigmentStack> setter) {
        return new SyncablePigmentStack(getter, setter);
    }

    private SyncablePigmentStack(Supplier<@NonNull PigmentStack> getter, Consumer<@NonNull PigmentStack> setter) {
        super(getter, setter);
    }

    @Nonnull
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
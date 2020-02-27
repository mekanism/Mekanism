package mekanism.common.inventory.container.sync;

import javax.annotation.Nonnull;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.infuse.InfuseType;
import mekanism.api.infuse.InfusionStack;
import mekanism.api.infuse.BasicInfusionTank;
import mekanism.common.network.container.property.InfusionStackPropertyData;
import mekanism.common.network.container.property.IntPropertyData;
import mekanism.common.network.container.property.PropertyData;

/**
 * Version of {@link net.minecraft.util.IntReferenceHolder} for handling infusion stacks
 */
public class SyncableInfusionStack extends SyncableChemicalStack<InfuseType, InfusionStack> {

    public static SyncableInfusionStack create(BasicInfusionTank handler) {
        return new SyncableInfusionStack(handler);
    }

    private SyncableInfusionStack(IChemicalTank<InfuseType, InfusionStack> handler) {
        super(handler);
    }

    @Nonnull
    @Override
    protected InfusionStack getEmptyStack() {
        return InfusionStack.EMPTY;
    }

    @Nonnull
    @Override
    protected InfusionStack createStack(InfusionStack stored, int size) {
        return new InfusionStack(stored, size);
    }

    @Override
    public PropertyData getPropertyData(short property, DirtyType dirtyType) {
        if (dirtyType == DirtyType.SIZE) {
            //If only the size changed, don't bother re-syncing the type
            return new IntPropertyData(property, get().getAmount());
        }
        return new InfusionStackPropertyData(property, get());
    }
}
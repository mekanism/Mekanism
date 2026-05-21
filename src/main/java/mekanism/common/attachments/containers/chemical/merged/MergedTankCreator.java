package mekanism.common.attachments.containers.chemical.merged;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.attachments.containers.chemical.ComponentBackedChemicalTank;
import mekanism.common.attachments.containers.creator.IBasicContainerCreator;
import mekanism.common.attachments.containers.fluid.ComponentBackedFluidTank;
import mekanism.common.capabilities.merged.MergedTank;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.transfer.access.ItemAccess;

//TODO: Re-evaluate/rethink this as using rawtypes to get around things like this is very cursed
@NothingNullByDefault
@SuppressWarnings({"rawtypes", "unchecked"})
public class MergedTankCreator implements IBasicContainerCreator {

    private final IBasicContainerCreator<? extends ComponentBackedChemicalTank> chemicalCreator;
    private final IBasicContainerCreator<? extends ComponentBackedFluidTank> fluidCreator;

    public MergedTankCreator(IBasicContainerCreator<? extends ComponentBackedChemicalTank> chemicalCreator,
          IBasicContainerCreator<? extends ComponentBackedFluidTank> fluidCreator) {
        this.chemicalCreator = chemicalCreator;
        this.fluidCreator = fluidCreator;
    }

    private MergedTank createMergedTank(ContainerType containerType, ItemAccess attachedAccess, int containerIndex) {
        return MergedTank.create(
              fluidCreator.create(containerType, attachedAccess, containerIndex),
              chemicalCreator.create(containerType, attachedAccess, containerIndex)
        );
    }

    @Override
    public ValueIOSerializable create(ContainerType containerType, ItemAccess attachedAccess, int containerIndex) {
        if (containerType == ContainerType.FLUID) {
            return createMergedTank(containerType, attachedAccess, containerIndex).getFluidTank();
        } else if (containerType == ContainerType.CHEMICAL) {
            return createMergedTank(containerType, attachedAccess, containerIndex).getChemicalTank();
        }
        throw new IllegalStateException("Unexpected container type " + containerType.getComponentName() + " for merged tank creation");
    }
}
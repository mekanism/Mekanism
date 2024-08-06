package mekanism.common.attachments.containers.chemical.merged;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.attachments.containers.chemical.ComponentBackedChemicalTank;
import mekanism.common.attachments.containers.creator.IBasicContainerCreator;
import mekanism.common.attachments.containers.fluid.ComponentBackedFluidTank;
import mekanism.common.capabilities.merged.MergedTank;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;

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

    private MergedTank createMergedTank(ContainerType containerType, ItemStack attachedTo, int containerIndex) {
        return MergedTank.create(
              fluidCreator.create(containerType, attachedTo, containerIndex),
              chemicalCreator.create(containerType, attachedTo, containerIndex)
        );
    }

    @Override
    public INBTSerializable<CompoundTag> create(ContainerType containerType, ItemStack attachedTo, int containerIndex) {
        if (containerType == ContainerType.FLUID) {
            return createMergedTank(containerType, attachedTo, containerIndex).getFluidTank();
        } else if (containerType == ContainerType.CHEMICAL) {
            return createMergedTank(containerType, attachedTo, containerIndex).getChemicalTank();
        }
        throw new IllegalStateException("Unexpected container type " + containerType.getComponentName() + " for merged tank creation");
    }
}
package mekanism.common.attachments.containers.chemical.merged;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.attachments.containers.chemical.gas.ComponentBackedGasTank;
import mekanism.common.attachments.containers.chemical.infuse.ComponentBackedInfusionTank;
import mekanism.common.attachments.containers.chemical.pigment.ComponentBackedPigmentTank;
import mekanism.common.attachments.containers.chemical.slurry.ComponentBackedSlurryTank;
import mekanism.common.attachments.containers.creator.IBasicContainerCreator;
import mekanism.common.attachments.containers.fluid.ComponentBackedFluidTank;
import mekanism.common.capabilities.merged.MergedTank;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

//TODO: Re-evaluate/rethink this as using rawtypes to get around things like this is very cursed
@NothingNullByDefault
@SuppressWarnings({"rawtypes", "unchecked"})
public class MergedTankCreator implements IBasicContainerCreator {

    private final IBasicContainerCreator<? extends ComponentBackedGasTank> gasCreator;
    private final IBasicContainerCreator<? extends ComponentBackedInfusionTank> infusionCreator;
    private final IBasicContainerCreator<? extends ComponentBackedPigmentTank> pigmentCreator;
    private final IBasicContainerCreator<? extends ComponentBackedSlurryTank> slurryCreator;
    @Nullable
    private final IBasicContainerCreator<? extends ComponentBackedFluidTank> fluidCreator;

    public MergedTankCreator(IBasicContainerCreator<? extends ComponentBackedGasTank> gasCreator,
          IBasicContainerCreator<? extends ComponentBackedInfusionTank> infusionCreator,
          IBasicContainerCreator<? extends ComponentBackedPigmentTank> pigmentCreator,
          IBasicContainerCreator<? extends ComponentBackedSlurryTank> slurryCreator) {
        this(gasCreator, infusionCreator, pigmentCreator, slurryCreator, null);
    }

    public MergedTankCreator(IBasicContainerCreator<? extends ComponentBackedGasTank> gasCreator,
          IBasicContainerCreator<? extends ComponentBackedInfusionTank> infusionCreator,
          IBasicContainerCreator<? extends ComponentBackedPigmentTank> pigmentCreator,
          IBasicContainerCreator<? extends ComponentBackedSlurryTank> slurryCreator,
          @Nullable IBasicContainerCreator<? extends ComponentBackedFluidTank> fluidCreator) {
        this.gasCreator = gasCreator;
        this.infusionCreator = infusionCreator;
        this.pigmentCreator = pigmentCreator;
        this.slurryCreator = slurryCreator;
        this.fluidCreator = fluidCreator;
    }

    private MergedChemicalTank createMergedTank(ContainerType containerType, ItemStack attachedTo, int containerIndex) {
        if (fluidCreator == null) {
            return MergedChemicalTank.create(
                  gasCreator.create(containerType, attachedTo, containerIndex),
                  infusionCreator.create(containerType, attachedTo, containerIndex),
                  pigmentCreator.create(containerType, attachedTo, containerIndex),
                  slurryCreator.create(containerType, attachedTo, containerIndex)
            );
        }
        return MergedTank.create(
              fluidCreator.create(containerType, attachedTo, containerIndex),
              gasCreator.create(containerType, attachedTo, containerIndex),
              infusionCreator.create(containerType, attachedTo, containerIndex),
              pigmentCreator.create(containerType, attachedTo, containerIndex),
              slurryCreator.create(containerType, attachedTo, containerIndex)
        );
    }

    @Override
    public INBTSerializable<CompoundTag> create(ContainerType containerType, ItemStack attachedTo, int containerIndex) {
        if (containerType == ContainerType.FLUID) {
            if (fluidCreator != null) {
                return ((MergedTank) createMergedTank(containerType, attachedTo, containerIndex)).getFluidTank();
            }
        } else if (containerType == ContainerType.GAS) {
            return createMergedTank(containerType, attachedTo, containerIndex).getGasTank();
        } else if (containerType == ContainerType.INFUSION) {
            return createMergedTank(containerType, attachedTo, containerIndex).getInfusionTank();
        } else if (containerType == ContainerType.PIGMENT) {
            return createMergedTank(containerType, attachedTo, containerIndex).getPigmentTank();
        } else if (containerType == ContainerType.SLURRY) {
            return createMergedTank(containerType, attachedTo, containerIndex).getSlurryTank();
        }
        throw new IllegalStateException("Unexpected container type " + containerType.getComponentName() + " for merged tank creation");
    }
}
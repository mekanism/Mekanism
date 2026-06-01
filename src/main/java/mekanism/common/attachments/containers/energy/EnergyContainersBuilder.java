package mekanism.common.attachments.containers.energy;

import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.attachments.containers.creator.IContainerCreator;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.shared.ModuleEnergyUnit;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import org.jetbrains.annotations.NotNull;

public class EnergyContainersBuilder {

    public static final IContainerCreator<IEnergyContainer, Long> MEKASUIT = creator(attachedAccess -> new ComponentBackedEnergyContainer(
          attachedAccess, BasicEnergyContainer.manualOnly, ConstantPredicates.alwaysTrue(),
          () -> ModuleEnergyUnit.getChargeRate(attachedAccess, MekanismConfig.gear.mekaSuitBaseChargeRate),
          () -> ModuleEnergyUnit.getEnergyCapacity(attachedAccess, MekanismConfig.gear.mekaSuitBaseEnergyCapacity)
    ));
    public static final IContainerCreator<IEnergyContainer, Long> ENERGY_CUBE = creator(ComponentBackedEnergyCubeContainer::create);
    public static final IContainerCreator<IEnergyContainer, Long> RESISTIVE_HEATER = creator(ComponentBackedResistiveEnergyContainer::create);

    private EnergyContainersBuilder() {
    }

    public static IContainerCreator<IEnergyContainer, Long> basicCreator(IntSupplier rate, LongSupplier maxEnergy) {
        return creator(attachedAccess -> new ComponentBackedEnergyContainer(attachedAccess, BasicEnergyContainer.manualOnly, ConstantPredicates.alwaysTrue(), rate, maxEnergy));
    }

    public static IContainerCreator<IEnergyContainer, Long> basicCreator(Predicate<@NotNull AutomationType> canExtract, Predicate<@NotNull AutomationType> canInsert, IntSupplier rate,
          LongSupplier maxEnergy) {
        return creator(attachedAccess -> new ComponentBackedEnergyContainer(attachedAccess, canExtract, canInsert, rate, maxEnergy));
    }

    public static IContainerCreator<IEnergyContainer, Long> creator(Function<ItemAccess, IEnergyContainer> creator) {
        return new BaseEnergyContainerCreator(creator);
    }

    private record BaseEnergyContainerCreator(Function<ItemAccess, IEnergyContainer> creator) implements IContainerCreator<IEnergyContainer, Long> {

        @Override
        public int totalContainers() {
            return 1;
        }

        @Override
        public Long initStorage() {
            return 0L;
        }

        @Override
        public IEnergyContainer create(ItemAccess attachedAccess, int containerIndex) {
            return creator.apply(attachedAccess);
        }
    }
}
package mekanism.common.component.containers.energy;

import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.component.containers.creator.IContainerCreator;
import mekanism.common.content.gear.shared.ModuleEnergyUnit;
import mekanism.common.util.EnergyUtils;
import net.neoforged.neoforge.transfer.access.ItemAccess;

public class EnergyContainerBuilder {

    public static final IContainerCreator<IEnergyContainer, Long> ENERGY_CUBE = creator(ComponentBackedEnergyCubeContainer::create);
    public static final IContainerCreator<IEnergyContainer, Long> RESISTIVE_HEATER = creator(ComponentBackedResistiveEnergyContainer::create);

    private EnergyContainerBuilder() {
    }

    public static IContainerCreator<IEnergyContainer, Long> mekaGear(DoubleSupplier percentageRate, LongSupplier maxEnergy) {
        return creator(attachedAccess -> {
            LongSupplier scaledCapacity = () -> ModuleEnergyUnit.getEnergyCapacity(attachedAccess, maxEnergy);
            return new ComponentBackedEnergyContainer(
                  attachedAccess, BasicEnergyContainer.manualOnly, ConstantPredicates.alwaysTrue(),
                  scaledCapacity, EnergyUtils.calculateRate(percentageRate, scaledCapacity)
            );
        });
    }

    public static IContainerCreator<IEnergyContainer, Long> basicCreator(DoubleSupplier percentageRate, LongSupplier maxEnergy) {
        return basicCreator(EnergyUtils.calculateRate(percentageRate, maxEnergy), maxEnergy);
    }

    public static IContainerCreator<IEnergyContainer, Long> basicCreator(IntSupplier rate, LongSupplier maxEnergy) {
        return creator(attachedAccess -> new ComponentBackedEnergyContainer(attachedAccess, BasicEnergyContainer.manualOnly, ConstantPredicates.alwaysTrue(), maxEnergy, rate));
    }

    public static IContainerCreator<IEnergyContainer, Long> basicCreator(Predicate<AutomationType> canExtract, Predicate<AutomationType> canInsert, DoubleSupplier percentageRate,
          LongSupplier maxEnergy) {
        return basicCreator(canExtract, canInsert, EnergyUtils.calculateRate(percentageRate, maxEnergy), maxEnergy);
    }

    public static IContainerCreator<IEnergyContainer, Long> basicCreator(Predicate<AutomationType> canExtract, Predicate<AutomationType> canInsert, IntSupplier rate,
          LongSupplier maxEnergy) {
        return creator(attachedAccess -> new ComponentBackedEnergyContainer(attachedAccess, canExtract, canInsert, maxEnergy, rate));
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
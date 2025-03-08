package mekanism.common.attachments.containers.energy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.attachments.containers.creator.BaseContainerCreator;
import mekanism.common.attachments.containers.creator.IBasicContainerCreator;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.shared.ModuleEnergyUnit;
import org.jetbrains.annotations.NotNull;

public class EnergyContainersBuilder {

    private static final IBasicContainerCreator<? extends ComponentBackedEnergyContainer> MEKASUIT = (type, attachedTo, containerIndex) -> new ComponentBackedNoClampEnergyContainer(
          attachedTo, containerIndex, BasicEnergyContainer.manualOnly, ConstantPredicates.alwaysTrue(),
          () -> ModuleEnergyUnit.getChargeRate(attachedTo, MekanismConfig.gear.mekaSuitBaseChargeRate),
          () -> ModuleEnergyUnit.getEnergyCapacity(attachedTo, MekanismConfig.gear.mekaSuitBaseEnergyCapacity)
    );

    public static EnergyContainersBuilder builder() {
        return new EnergyContainersBuilder();
    }

    private final List<IBasicContainerCreator<? extends ComponentBackedEnergyContainer>> containerCreators = new ArrayList<>();

    private EnergyContainersBuilder() {
    }

    public BaseContainerCreator<AttachedEnergy, ComponentBackedEnergyContainer> build() {
        return new BaseEnergyContainerCreator(containerCreators);
    }

    public EnergyContainersBuilder addMekaSuit() {
        return addContainer(MEKASUIT);
    }

    public EnergyContainersBuilder addBasic(LongSupplier rate, LongSupplier maxEnergy) {
        return addContainer((type, attachedTo, containerIndex) -> new ComponentBackedEnergyContainer(attachedTo, containerIndex, BasicEnergyContainer.manualOnly,
              ConstantPredicates.alwaysTrue(), rate, maxEnergy));
    }

    public EnergyContainersBuilder addBasic(Predicate<@NotNull AutomationType> canExtract, Predicate<@NotNull AutomationType> canInsert, LongSupplier rate,
          LongSupplier maxEnergy) {
        return addContainer((type, attachedTo, containerIndex) -> new ComponentBackedEnergyContainer(attachedTo, containerIndex, canExtract, canInsert, rate, maxEnergy));
    }

    public EnergyContainersBuilder addContainer(IBasicContainerCreator<? extends ComponentBackedEnergyContainer> capacitor) {
        containerCreators.add(capacitor);
        return this;
    }

    private static class BaseEnergyContainerCreator extends BaseContainerCreator<AttachedEnergy, ComponentBackedEnergyContainer> {

        public BaseEnergyContainerCreator(List<IBasicContainerCreator<? extends ComponentBackedEnergyContainer>> creators) {
            super(creators);
        }

        @Override
        public AttachedEnergy initStorage(int containers) {
            return AttachedEnergy.create(containers);
        }
    }
}
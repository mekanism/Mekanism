package mekanism.common.attachments.containers.energy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.attachments.containers.creator.BaseContainerCreator;
import mekanism.common.attachments.containers.creator.IBasicContainerCreator;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.shared.ModuleEnergyUnit;
import org.jetbrains.annotations.NotNull;

public class EnergyContainersBuilder {

    private static final IBasicContainerCreator<IEnergyContainer> MEKASUIT = (attachedAccess, containerIndex) ->
          new ComponentBackedNoClampEnergyContainer(attachedAccess, containerIndex, BasicEnergyContainer.manualOnly, ConstantPredicates.alwaysTrue(),
                () -> ModuleEnergyUnit.getChargeRate(attachedAccess, MekanismConfig.gear.mekaSuitBaseChargeRate),
                () -> ModuleEnergyUnit.getEnergyCapacity(attachedAccess, MekanismConfig.gear.mekaSuitBaseEnergyCapacity)
          );

    public static EnergyContainersBuilder builder() {
        return new EnergyContainersBuilder();
    }

    private final List<IBasicContainerCreator<IEnergyContainer>> containerCreators = new ArrayList<>();

    private EnergyContainersBuilder() {
    }

    public BaseContainerCreator<AttachedEnergy, IEnergyContainer> build() {
        return new BaseEnergyContainerCreator(containerCreators);
    }

    public EnergyContainersBuilder addMekaSuit() {
        return addContainer(MEKASUIT);
    }

    public EnergyContainersBuilder addBasic(LongSupplier rate, LongSupplier maxEnergy) {
        return addContainer((attachedAccess, containerIndex) -> new ComponentBackedEnergyContainer(attachedAccess, containerIndex,
              BasicEnergyContainer.manualOnly, ConstantPredicates.alwaysTrue(), rate, maxEnergy));
    }

    public EnergyContainersBuilder addBasic(Predicate<@NotNull AutomationType> canExtract, Predicate<@NotNull AutomationType> canInsert, LongSupplier rate,
          LongSupplier maxEnergy) {
        return addContainer((attachedAccess, containerIndex) -> new ComponentBackedEnergyContainer(attachedAccess, containerIndex, canExtract, canInsert, rate, maxEnergy));
    }

    public EnergyContainersBuilder addContainer(IBasicContainerCreator<IEnergyContainer> capacitor) {
        containerCreators.add(capacitor);
        return this;
    }

    private static class BaseEnergyContainerCreator extends BaseContainerCreator<AttachedEnergy, IEnergyContainer> {

        public BaseEnergyContainerCreator(List<IBasicContainerCreator<IEnergyContainer>> creators) {
            super(creators);
        }

        @Override
        public AttachedEnergy initStorage() {
            int containers = totalContainers();
            if (containers == 0) {
                return AttachedEnergy.EMPTY;
            }
            return AttachedEnergy.create(containers);
        }
    }
}
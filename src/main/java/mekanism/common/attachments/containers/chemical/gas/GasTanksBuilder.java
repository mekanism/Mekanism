package mekanism.common.attachments.containers.chemical.gas;

import java.util.List;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.common.attachments.containers.chemical.ChemicalTanksBuilder;
import mekanism.common.attachments.containers.creator.BaseContainerCreator;
import mekanism.common.attachments.containers.creator.IBasicContainerCreator;
import mekanism.common.config.MekanismConfig;
import org.jetbrains.annotations.NotNull;

public class GasTanksBuilder extends ChemicalTanksBuilder<Gas, GasStack, ComponentBackedGasTank, GasTanksBuilder> {

    public static GasTanksBuilder builder() {
        return new GasTanksBuilder();
    }

    private GasTanksBuilder() {
    }

    @Override
    public BaseContainerCreator<AttachedGases, ComponentBackedGasTank> build() {
        return new BaseGasTankBuilder(tankCreators);
    }

    @Override
    public GasTanksBuilder addBasic(LongSupplier capacity, Predicate<@NotNull Gas> isValid) {
        return addTank((type, attachedTo, containerIndex) -> new ComponentBackedGasTank(attachedTo, containerIndex, ChemicalTankBuilder.GAS.manualOnly,
              ChemicalTankBuilder.GAS.alwaysTrueBi, isValid, MekanismConfig.general.chemicalItemFillRate, capacity, null));
    }

    @Override
    public GasTanksBuilder addBasic(LongSupplier capacity) {
        return addTank((type, attachedTo, containerIndex) -> new ComponentBackedGasTank(attachedTo, containerIndex, ChemicalTankBuilder.GAS.manualOnly,
              ChemicalTankBuilder.GAS.alwaysTrueBi, ChemicalTankBuilder.GAS.alwaysTrue, MekanismConfig.general.chemicalItemFillRate, capacity, null));
    }

    @Override
    public GasTanksBuilder addInternalStorage(LongSupplier rate, LongSupplier capacity, Predicate<@NotNull Gas> isValid) {
        return addTank((type, attachedTo, containerIndex) -> new ComponentBackedGasTank(attachedTo, containerIndex, ChemicalTankBuilder.GAS.notExternal,
              ChemicalTankBuilder.GAS.alwaysTrueBi, isValid, rate, capacity, null));
    }

    private static class BaseGasTankBuilder extends BaseContainerCreator<AttachedGases, ComponentBackedGasTank> {

        public BaseGasTankBuilder(List<IBasicContainerCreator<? extends ComponentBackedGasTank>> creators) {
            super(creators);
        }

        @Override
        public AttachedGases initStorage(int containers) {
            return new AttachedGases(containers);
        }
    }
}
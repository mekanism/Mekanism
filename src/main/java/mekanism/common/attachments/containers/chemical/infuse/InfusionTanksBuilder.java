package mekanism.common.attachments.containers.chemical.infuse;

import java.util.List;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.common.attachments.containers.chemical.ChemicalTanksBuilder;
import mekanism.common.attachments.containers.creator.BaseContainerCreator;
import mekanism.common.attachments.containers.creator.IBasicContainerCreator;
import mekanism.common.config.MekanismConfig;
import org.jetbrains.annotations.NotNull;

public class InfusionTanksBuilder extends ChemicalTanksBuilder<InfuseType, InfusionStack, ComponentBackedInfusionTank, InfusionTanksBuilder> {

    public static InfusionTanksBuilder builder() {
        return new InfusionTanksBuilder();
    }

    private InfusionTanksBuilder() {
    }

    @Override
    public BaseContainerCreator<AttachedInfuseTypes, ComponentBackedInfusionTank> build() {
        return new BaseInfusionTankBuilder(tankCreators);
    }

    @Override
    public InfusionTanksBuilder addBasic(LongSupplier capacity, Predicate<@NotNull InfuseType> isValid) {
        return addTank((type, attachedTo, containerIndex) -> new ComponentBackedInfusionTank(attachedTo, containerIndex, ChemicalTankBuilder.INFUSION.manualOnly,
              ChemicalTankBuilder.INFUSION.alwaysTrueBi, isValid, MekanismConfig.general.chemicalItemFillRate, capacity, null));
    }

    @Override
    public InfusionTanksBuilder addBasic(LongSupplier capacity) {
        return addTank((type, attachedTo, containerIndex) -> new ComponentBackedInfusionTank(attachedTo, containerIndex, ChemicalTankBuilder.INFUSION.manualOnly,
              ChemicalTankBuilder.INFUSION.alwaysTrueBi, ChemicalTankBuilder.INFUSION.alwaysTrue, MekanismConfig.general.chemicalItemFillRate, capacity, null));
    }

    @Override
    public InfusionTanksBuilder addInternalStorage(LongSupplier rate, LongSupplier capacity, Predicate<@NotNull InfuseType> isValid) {
        return addTank((type, attachedTo, containerIndex) -> new ComponentBackedInfusionTank(attachedTo, containerIndex, ChemicalTankBuilder.INFUSION.notExternal,
              ChemicalTankBuilder.INFUSION.alwaysTrueBi, isValid, rate, capacity, null));
    }

    private static class BaseInfusionTankBuilder extends BaseContainerCreator<AttachedInfuseTypes, ComponentBackedInfusionTank> {

        public BaseInfusionTankBuilder(List<IBasicContainerCreator<? extends ComponentBackedInfusionTank>> creators) {
            super(creators);
        }

        @Override
        public AttachedInfuseTypes initStorage(int containers) {
            return new AttachedInfuseTypes(containers);
        }
    }
}
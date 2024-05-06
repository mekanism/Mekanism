package mekanism.common.attachments.containers.chemical.pigment;

import java.util.List;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.common.attachments.containers.chemical.ChemicalTanksBuilder;
import mekanism.common.attachments.containers.creator.BaseContainerCreator;
import mekanism.common.attachments.containers.creator.IBasicContainerCreator;
import mekanism.common.config.MekanismConfig;
import org.jetbrains.annotations.NotNull;

public class PigmentTanksBuilder extends ChemicalTanksBuilder<Pigment, PigmentStack, ComponentBackedPigmentTank, PigmentTanksBuilder> {

    public static PigmentTanksBuilder builder() {
        return new PigmentTanksBuilder();
    }

    private PigmentTanksBuilder() {
    }

    @Override
    public BaseContainerCreator<AttachedPigments, ComponentBackedPigmentTank> build() {
        return new BasePigmentTankBuilder(tankCreators);
    }

    @Override
    public PigmentTanksBuilder addBasic(LongSupplier capacity, Predicate<@NotNull Pigment> isValid) {
        return addTank((type, attachedTo, containerIndex) -> new ComponentBackedPigmentTank(attachedTo, containerIndex, ChemicalTankBuilder.PIGMENT.manualOnly,
              ChemicalTankBuilder.PIGMENT.alwaysTrueBi, isValid, MekanismConfig.general.chemicalItemFillRate, capacity, null));
    }

    @Override
    public PigmentTanksBuilder addBasic(LongSupplier capacity) {
        return addTank((type, attachedTo, containerIndex) -> new ComponentBackedPigmentTank(attachedTo, containerIndex, ChemicalTankBuilder.PIGMENT.manualOnly,
              ChemicalTankBuilder.PIGMENT.alwaysTrueBi, ChemicalTankBuilder.PIGMENT.alwaysTrue, MekanismConfig.general.chemicalItemFillRate, capacity, null));
    }

    @Override
    public PigmentTanksBuilder addInternalStorage(LongSupplier rate, LongSupplier capacity, Predicate<@NotNull Pigment> isValid) {
        return addTank((type, attachedTo, containerIndex) -> new ComponentBackedPigmentTank(attachedTo, containerIndex, ChemicalTankBuilder.PIGMENT.notExternal,
              ChemicalTankBuilder.PIGMENT.alwaysTrueBi, isValid, rate, capacity, null));
    }

    private static class BasePigmentTankBuilder extends BaseContainerCreator<AttachedPigments, ComponentBackedPigmentTank> {

        public BasePigmentTankBuilder(List<IBasicContainerCreator<? extends ComponentBackedPigmentTank>> creators) {
            super(creators);
        }

        @Override
        public AttachedPigments initStorage(int containers) {
            return AttachedPigments.create(containers);
        }
    }
}
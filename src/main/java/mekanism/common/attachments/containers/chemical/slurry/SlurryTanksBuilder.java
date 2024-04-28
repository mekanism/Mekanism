package mekanism.common.attachments.containers.chemical.slurry;

import java.util.List;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.attachments.containers.chemical.ChemicalTanksBuilder;
import mekanism.common.attachments.containers.creator.BaseContainerCreator;
import mekanism.common.attachments.containers.creator.IBasicContainerCreator;
import mekanism.common.config.MekanismConfig;
import org.jetbrains.annotations.NotNull;

public class SlurryTanksBuilder extends ChemicalTanksBuilder<Slurry, SlurryStack, ComponentBackedSlurryTank, SlurryTanksBuilder> {

    public static SlurryTanksBuilder builder() {
        return new SlurryTanksBuilder();
    }

    private SlurryTanksBuilder() {
    }

    @Override
    public BaseContainerCreator<AttachedSlurries, ComponentBackedSlurryTank> build() {
        return new BaseSlurryTankBuilder(tankCreators);
    }

    @Override
    public SlurryTanksBuilder addBasic(LongSupplier capacity, Predicate<@NotNull Slurry> isValid) {
        return addTank((type, attachedTo, containerIndex) -> new ComponentBackedSlurryTank(attachedTo, containerIndex, ChemicalTankBuilder.SLURRY.manualOnly,
              ChemicalTankBuilder.SLURRY.alwaysTrueBi, isValid, MekanismConfig.general.chemicalItemFillRate, capacity, null));
    }

    @Override
    public SlurryTanksBuilder addBasic(LongSupplier capacity) {
        return addTank((type, attachedTo, containerIndex) -> new ComponentBackedSlurryTank(attachedTo, containerIndex, ChemicalTankBuilder.SLURRY.manualOnly,
              ChemicalTankBuilder.SLURRY.alwaysTrueBi, ChemicalTankBuilder.SLURRY.alwaysTrue, MekanismConfig.general.chemicalItemFillRate, capacity, null));
    }

    @Override
    public SlurryTanksBuilder addInternalStorage(LongSupplier rate, LongSupplier capacity, Predicate<@NotNull Slurry> isValid) {
        return addTank((type, attachedTo, containerIndex) -> new ComponentBackedSlurryTank(attachedTo, containerIndex, ChemicalTankBuilder.SLURRY.notExternal,
              ChemicalTankBuilder.SLURRY.alwaysTrueBi, isValid, rate, capacity, null));
    }

    private static class BaseSlurryTankBuilder extends BaseContainerCreator<AttachedSlurries, ComponentBackedSlurryTank> {

        public BaseSlurryTankBuilder(List<IBasicContainerCreator<? extends ComponentBackedSlurryTank>> creators) {
            super(creators);
        }

        @Override
        public AttachedSlurries initStorage(int containers) {
            return new AttachedSlurries(containers);
        }
    }
}
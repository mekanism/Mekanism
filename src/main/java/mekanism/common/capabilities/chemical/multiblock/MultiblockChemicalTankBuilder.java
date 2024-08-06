package mekanism.common.capabilities.chemical.multiblock;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.common.capabilities.chemical.variable.VariableCapacityChemicalTank;
import mekanism.common.lib.multiblock.MultiblockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//todo why is this different from VariableCapacityTankBuilder??
@NothingNullByDefault
public class MultiblockChemicalTankBuilder {

    public static final MultiblockChemicalTankBuilder CHEMICAL = new MultiblockChemicalTankBuilder(ChemicalTankBuilder.CHEMICAL, VariableCapacityChemicalTank::new);

    private final MultiblockTankCreator tankCreator;
    private final ChemicalTankBuilder tankBuilder;

    private MultiblockChemicalTankBuilder(ChemicalTankBuilder tankBuilder, MultiblockTankCreator tankCreator) {
        this.tankBuilder = tankBuilder;
        this.tankCreator = tankCreator;
    }

    public IChemicalTank create(MultiblockData multiblock, LongSupplier capacity, Predicate<Chemical> validator, @Nullable IContentsListener listener) {
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(validator, "Chemical validity check cannot be null");
        return createUnchecked(multiblock, capacity, validator, listener);
    }

    private IChemicalTank createUnchecked(MultiblockData multiblock, LongSupplier capacity, Predicate<Chemical> validator, @Nullable IContentsListener listener) {
        return tankCreator.create(capacity, multiblock.formedBiPred(), multiblock.formedBiPred(), validator, null, listener);
    }

    public IChemicalTank create(LongSupplier capacity, BiPredicate<Chemical, @NotNull AutomationType> canExtract,
          BiPredicate<Chemical, @NotNull AutomationType> canInsert, Predicate<Chemical> validator, @Nullable IContentsListener listener) {
        return create(capacity, canExtract, canInsert, validator, null, listener);
    }

    public IChemicalTank input(MultiblockData multiblock, LongSupplier capacity, Predicate<Chemical> validator, @Nullable IContentsListener listener) {
        return input(multiblock, capacity, validator, null, listener);
    }

    public IChemicalTank input(MultiblockData multiblock, LongSupplier capacity, Predicate<Chemical> validator, @Nullable ChemicalAttributeValidator attributeValidator,
          @Nullable IContentsListener listener) {
        return create(capacity, multiblock.notExternalFormedBiPred(), multiblock.formedBiPred(), validator, attributeValidator, listener);
    }

    public IChemicalTank output(MultiblockData multiblock, LongSupplier capacity, Predicate<Chemical> validator, @Nullable IContentsListener listener) {
        return output(multiblock, capacity, validator, null, listener);
    }

    public IChemicalTank output(MultiblockData multiblock, LongSupplier capacity, Predicate<Chemical> validator, @Nullable ChemicalAttributeValidator attributeValidator,
          @Nullable IContentsListener listener) {
        return create(capacity, multiblock.formedBiPred(), multiblock.notExternalFormedBiPred(), validator, attributeValidator, listener);
    }

    public IChemicalTank create(LongSupplier capacity, BiPredicate<Chemical, @NotNull AutomationType> canExtract,
          BiPredicate<Chemical, @NotNull AutomationType> canInsert, Predicate<Chemical> validator,
          @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(validator, "Chemical validity check cannot be null");
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        return tankCreator.create(capacity, canExtract, canInsert, validator, attributeValidator, listener);
    }

    @FunctionalInterface
    private interface MultiblockTankCreator {

        IChemicalTank create(LongSupplier capacity, BiPredicate<Chemical, @NotNull AutomationType> canExtract,
              BiPredicate<Chemical, @NotNull AutomationType> canInsert, Predicate<Chemical> validator,
              @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener);
    }

}
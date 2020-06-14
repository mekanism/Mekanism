package mekanism.common.capabilities.chemical.multiblock;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.inventory.AutomationType;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.tile.prefab.TileEntityMultiblock;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SuppressWarnings("Convert2Diamond")
//The types cannot properly be inferred
public class MultiblockChemicalTankBuilder<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>> {

    public static final MultiblockChemicalTankBuilder<Gas, GasStack, IGasTank> GAS = new MultiblockChemicalTankBuilder<Gas, GasStack, IGasTank>(ChemicalTankBuilder.GAS, MultiblockGasTank::new);
    public static final MultiblockChemicalTankBuilder<InfuseType, InfusionStack, IInfusionTank> INFUSION = new MultiblockChemicalTankBuilder<InfuseType, InfusionStack, IInfusionTank>(ChemicalTankBuilder.INFUSION, MultiblockInfusionTank::new);
    public static final MultiblockChemicalTankBuilder<Pigment, PigmentStack, IPigmentTank> PIGMENT = new MultiblockChemicalTankBuilder<Pigment, PigmentStack, IPigmentTank>(ChemicalTankBuilder.PIGMENT, MultiblockPigmentTank::new);
    public static final MultiblockChemicalTankBuilder<Slurry, SlurryStack, ISlurryTank> SLURRY = new MultiblockChemicalTankBuilder<Slurry, SlurryStack, ISlurryTank>(ChemicalTankBuilder.SLURRY, MultiblockSlurryTank::new);

    private final MultiblockTankCreator<CHEMICAL, STACK, TANK> tankCreator;
    private final ChemicalTankBuilder<CHEMICAL, STACK, TANK> tankBuilder;

    private MultiblockChemicalTankBuilder(ChemicalTankBuilder<CHEMICAL, STACK, TANK> tankBuilder, MultiblockTankCreator<CHEMICAL, STACK, TANK> tankCreator) {
        this.tankBuilder = tankBuilder;
        this.tankCreator = tankCreator;
    }

    public <MULTIBLOCK extends MultiblockData> TANK create(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile,
          LongSupplier capacity, Predicate<@NonNull CHEMICAL> validator) {
        Objects.requireNonNull(tile, "Tile cannot be null");
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(validator, "Chemical validity check cannot be null");
        return createUnchecked(multiblock, tile, capacity, validator);
    }

    private <MULTIBLOCK extends MultiblockData> TANK createUnchecked(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile, LongSupplier capacity,
          Predicate<@NonNull CHEMICAL> validator) {
        return tankCreator.create(multiblock, tile, capacity, (stack, automationType) -> automationType != AutomationType.EXTERNAL || multiblock.isFormed(),
              (stack, automationType) -> automationType != AutomationType.EXTERNAL || multiblock.isFormed(), validator, null, null);
    }

    public <MULTIBLOCK extends MultiblockData> TANK create(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile,
          LongSupplier capacity, BiPredicate<@NonNull CHEMICAL, @NonNull AutomationType> canExtract, BiPredicate<@NonNull CHEMICAL, @NonNull AutomationType> canInsert,
          Predicate<@NonNull CHEMICAL> validator) {
        return create(multiblock, tile, capacity, canExtract, canInsert, validator, null, null);
    }

    public <MULTIBLOCK extends MultiblockData> TANK input(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile, LongSupplier capacity,
          Predicate<@NonNull CHEMICAL> validator) {
        return create(multiblock, tile, capacity, (stack, automationType) -> automationType != AutomationType.EXTERNAL && multiblock.isFormed(),
              (stack, automationType) -> multiblock.isFormed(), validator, null, null);
    }

    public <MULTIBLOCK extends MultiblockData> TANK output(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile, LongSupplier capacity,
          Predicate<@NonNull CHEMICAL> validator) {
        return create(multiblock, tile, capacity, (stack, automationType) -> multiblock.isFormed(),
              (stack, automationType) -> automationType != AutomationType.EXTERNAL && multiblock.isFormed(), validator, null, null);
    }

    public <MULTIBLOCK extends MultiblockData> TANK create(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile, LongSupplier capacity,
          BiPredicate<@NonNull CHEMICAL, @NonNull AutomationType> canExtract, BiPredicate<@NonNull CHEMICAL, @NonNull AutomationType> canInsert,
          Predicate<@NonNull CHEMICAL> validator, @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        Objects.requireNonNull(tile, "Tile cannot be null");
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(validator, "Chemical validity check cannot be null");
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        return tankCreator.create(multiblock, tile, capacity, canExtract, canInsert, validator, attributeValidator, listener);
    }

    @FunctionalInterface
    private interface MultiblockTankCreator<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>> {

        <MULTIBLOCK extends MultiblockData> TANK create(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile, LongSupplier capacity,
              BiPredicate<@NonNull CHEMICAL, @NonNull AutomationType> canExtract, BiPredicate<@NonNull CHEMICAL, @NonNull AutomationType> canInsert,
              Predicate<@NonNull CHEMICAL> validator, @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener);
    }

    public static class MultiblockGasTank<MULTIBLOCK extends MultiblockData> extends MultiblockChemicalTank<Gas, GasStack, MULTIBLOCK> implements IGasHandler, IGasTank {

        protected MultiblockGasTank(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile, LongSupplier capacity,
              BiPredicate<@NonNull Gas, @NonNull AutomationType> canExtract, BiPredicate<@NonNull Gas, @NonNull AutomationType> canInsert,
              Predicate<@NonNull Gas> validator, @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
            super(multiblock, tile, capacity, canExtract, canInsert, validator, attributeValidator, listener);
        }
    }

    public static class MultiblockInfusionTank<MULTIBLOCK extends MultiblockData> extends MultiblockChemicalTank<InfuseType, InfusionStack, MULTIBLOCK>
          implements IInfusionHandler, IInfusionTank {

        protected MultiblockInfusionTank(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile, LongSupplier capacity,
              BiPredicate<@NonNull InfuseType, @NonNull AutomationType> canExtract, BiPredicate<@NonNull InfuseType, @NonNull AutomationType> canInsert,
              Predicate<@NonNull InfuseType> validator, @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
            super(multiblock, tile, capacity, canExtract, canInsert, validator, attributeValidator, listener);
        }
    }

    public static class MultiblockPigmentTank<MULTIBLOCK extends MultiblockData> extends MultiblockChemicalTank<Pigment, PigmentStack, MULTIBLOCK>
          implements IPigmentHandler, IPigmentTank {

        protected MultiblockPigmentTank(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile, LongSupplier capacity,
              BiPredicate<@NonNull Pigment, @NonNull AutomationType> canExtract, BiPredicate<@NonNull Pigment, @NonNull AutomationType> canInsert,
              Predicate<@NonNull Pigment> validator, @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
            super(multiblock, tile, capacity, canExtract, canInsert, validator, attributeValidator, listener);
        }
    }

    public static class MultiblockSlurryTank<MULTIBLOCK extends MultiblockData> extends MultiblockChemicalTank<Slurry, SlurryStack, MULTIBLOCK>
          implements ISlurryHandler, ISlurryTank {

        protected MultiblockSlurryTank(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile, LongSupplier capacity,
              BiPredicate<@NonNull Slurry, @NonNull AutomationType> canExtract, BiPredicate<@NonNull Slurry, @NonNull AutomationType> canInsert,
              Predicate<@NonNull Slurry> validator, @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
            super(multiblock, tile, capacity, canExtract, canInsert, validator, attributeValidator, listener);
        }
    }
}
package mekanism.api.chemical.merged;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MergedChemicalTank {

    public static MergedChemicalTank create(IGasTank gasTank, IInfusionTank infusionTank, IPigmentTank pigmentTank, ISlurryTank slurryTank) {
        Objects.requireNonNull(gasTank, "Gas tank cannot be null");
        Objects.requireNonNull(infusionTank, "Infusion tank cannot be null");
        Objects.requireNonNull(pigmentTank, "Pigment tank cannot be null");
        Objects.requireNonNull(slurryTank, "Slurry tank cannot be null");
        return new MergedChemicalTank(gasTank, infusionTank, pigmentTank, slurryTank);
    }

    private final Map<ChemicalTankType<?, ?, ?>, IChemicalTank<?, ?>> tankMap = new HashMap<>();

    private MergedChemicalTank(IChemicalTank<?, ?>... allTanks) {
        this(null, allTanks);
    }

    protected MergedChemicalTank(@Nullable BooleanSupplier extraCheck, IChemicalTank<?, ?>... allTanks) {
        for (ChemicalTankType<?, ?, ?> type : ChemicalTankType.TYPES) {
            boolean handled = false;
            for (IChemicalTank<?, ?> tank : allTanks) {
                if (type.canHandle(tank)) {
                    //TODO: Improve this so it doesn't have to loop nearly as much?
                    List<IChemicalTank<?, ?>> otherTanks = Arrays.stream(allTanks).filter(otherTank -> tank != otherTank).collect(Collectors.toList());
                    BooleanSupplier insertionCheck;
                    if (extraCheck == null) {
                        insertionCheck = () -> otherTanks.stream().allMatch(IChemicalTank::isEmpty);
                    } else {
                        insertionCheck = () -> extraCheck.getAsBoolean() && otherTanks.stream().allMatch(IChemicalTank::isEmpty);
                    }
                    tankMap.put(type, type.createWrapper(this, tank, insertionCheck));
                    handled = true;
                    break;
                }
            }
            if (!handled) {
                throw new IllegalArgumentException("No chemical tank supplied for type: " + type);
            }
        }
    }

    public Collection<IChemicalTank<?, ?>> getAllTanks() {
        return tankMap.values();
    }

    public IChemicalTank<?, ?> getTankForType(ChemicalType chemicalType) {
        if (chemicalType == ChemicalType.GAS) {
            return getGasTank();
        } else if (chemicalType == ChemicalType.INFUSION) {
            return getInfusionTank();
        } else if (chemicalType == ChemicalType.PIGMENT) {
            return getPigmentTank();
        } else if (chemicalType == ChemicalType.SLURRY) {
            return getSlurryTank();
        }
        throw new IllegalStateException("Unknown chemical type");
    }

    public final IGasTank getGasTank() {
        return (IGasTank) tankMap.get(ChemicalTankType.GAS);
    }

    public final IInfusionTank getInfusionTank() {
        return (IInfusionTank) tankMap.get(ChemicalTankType.INFUSE_TYPE);
    }

    public final IPigmentTank getPigmentTank() {
        return (IPigmentTank) tankMap.get(ChemicalTankType.PIGMENT);
    }

    public final ISlurryTank getSlurryTank() {
        return (ISlurryTank) tankMap.get(ChemicalTankType.SLURRY);
    }

    public Current getCurrent() {
        if (!getGasTank().isEmpty()) {
            return Current.GAS;
        } else if (!getInfusionTank().isEmpty()) {
            return Current.INFUSION;
        } else if (!getPigmentTank().isEmpty()) {
            return Current.PIGMENT;
        } else if (!getSlurryTank().isEmpty()) {
            return Current.SLURRY;
        }
        return Current.EMPTY;
    }

    public IChemicalTank<?, ?> getTankFromCurrent(Current current) {
        switch (current) {
            case GAS:
                return getGasTank();
            case INFUSION:
                return getInfusionTank();
            case PIGMENT:
                return getPigmentTank();
            case SLURRY:
                return getSlurryTank();
        }
        throw new IllegalStateException("Unknown chemical type");
    }

    public enum Current {
        EMPTY,
        GAS,
        INFUSION,
        PIGMENT,
        SLURRY
    }

    @FunctionalInterface
    private interface IWrapperCreator<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>> {

        TANK create(MergedChemicalTank mergedTank, TANK tank, BooleanSupplier insertCheck);
    }

    private static class ChemicalTankType<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>> {

        private static final List<ChemicalTankType<?, ?, ?>> TYPES = new ArrayList<>();
        private static final ChemicalTankType<Gas, GasStack, IGasTank> GAS = new ChemicalTankType<>("gas", GasTankWrapper::new, tank -> tank instanceof IGasTank);
        private static final ChemicalTankType<InfuseType, InfusionStack, IInfusionTank> INFUSE_TYPE = new ChemicalTankType<>("infusion", InfusionTankWrapper::new, tank -> tank instanceof IInfusionTank);
        private static final ChemicalTankType<Pigment, PigmentStack, IPigmentTank> PIGMENT = new ChemicalTankType<>("pigment", PigmentTankWrapper::new, tank -> tank instanceof IPigmentTank);
        private static final ChemicalTankType<Slurry, SlurryStack, ISlurryTank> SLURRY = new ChemicalTankType<>("slurry", SlurryTankWrapper::new, tank -> tank instanceof ISlurryTank);

        private final IWrapperCreator<CHEMICAL, STACK, TANK> tankWrapper;
        private final Predicate<IChemicalTank<?, ?>> tankValidator;
        private final String type;

        ChemicalTankType(String type, IWrapperCreator<CHEMICAL, STACK, TANK> tankWrapper, Predicate<IChemicalTank<?, ?>> tankValidator) {
            this.type = type;
            this.tankWrapper = tankWrapper;
            this.tankValidator = tankValidator;
            //Add to known types
            TYPES.add(this);
        }

        private boolean canHandle(IChemicalTank<?, ?> tank) {
            return tankValidator.test(tank);
        }

        /**
         * It is assumed that {@link #canHandle(IChemicalTank)} is called before this method
         */
        public TANK createWrapper(MergedChemicalTank mergedTank, IChemicalTank<?, ?> tank, BooleanSupplier insertCheck) {
            return tankWrapper.create(mergedTank, (TANK) tank, insertCheck);
        }

        @Override
        public String toString() {
            return type;
        }
    }

    private static class GasTankWrapper extends ChemicalTankWrapper<Gas, GasStack> implements IGasTank {

        public GasTankWrapper(MergedChemicalTank mergedTank, IGasTank internal, BooleanSupplier insertCheck) {
            super(mergedTank, internal, insertCheck);
        }
    }

    private static class InfusionTankWrapper extends ChemicalTankWrapper<InfuseType, InfusionStack> implements IInfusionTank {

        public InfusionTankWrapper(MergedChemicalTank mergedTank, IInfusionTank internal, BooleanSupplier insertCheck) {
            super(mergedTank, internal, insertCheck);
        }
    }

    private static class PigmentTankWrapper extends ChemicalTankWrapper<Pigment, PigmentStack> implements IPigmentTank {

        public PigmentTankWrapper(MergedChemicalTank mergedTank, IPigmentTank internal, BooleanSupplier insertCheck) {
            super(mergedTank, internal, insertCheck);
        }
    }

    private static class SlurryTankWrapper extends ChemicalTankWrapper<Slurry, SlurryStack> implements ISlurryTank {

        public SlurryTankWrapper(MergedChemicalTank mergedTank, ISlurryTank internal, BooleanSupplier insertCheck) {
            super(mergedTank, internal, insertCheck);
        }
    }
}
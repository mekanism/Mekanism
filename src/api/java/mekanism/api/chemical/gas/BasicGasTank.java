package mekanism.api.chemical.gas;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.inventory.AutomationType;
import net.minecraft.nbt.CompoundNBT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BasicGasTank extends BasicChemicalTank<Gas, GasStack> implements IGasHandler {

    public static final Predicate<@NonNull Gas> alwaysTrue = stack -> true;
    public static final Predicate<@NonNull Gas> alwaysFalse = stack -> false;
    public static final BiPredicate<@NonNull Gas, @NonNull AutomationType> alwaysTrueBi = (stack, automationType) -> true;
    public static final BiPredicate<@NonNull Gas, @NonNull AutomationType> manualOnly = (stack, automationType) -> automationType == AutomationType.MANUAL;
    public static final BiPredicate<@NonNull Gas, @NonNull AutomationType> internalOnly = (stack, automationType) -> automationType == AutomationType.INTERNAL;

    @Nullable
    private final IMekanismGasHandler gasHandler;

    public static BasicGasTank create(int capacity, @Nullable IMekanismGasHandler gasHandler) {
        //TODO: Validate capacity is positive
        return new BasicGasTank(capacity, alwaysTrueBi, alwaysTrueBi, alwaysTrue, gasHandler);
    }

    public static BasicGasTank create(int capacity, Predicate<@NonNull Gas> canExtract, Predicate<@NonNull Gas> canInsert, @Nullable IMekanismGasHandler gasHandler) {
        return create(capacity, canExtract, canInsert, alwaysTrue, gasHandler);
    }

    public static BasicGasTank input(int capacity, Predicate<@NonNull Gas> validator, @Nullable IMekanismGasHandler gasHandler) {
        //TODO: Validate capacity is positive
        Objects.requireNonNull(validator, "Gas validity check cannot be null");
        return new BasicGasTank(capacity, manualOnly, alwaysTrueBi, validator, gasHandler);
    }

    public static BasicGasTank input(int capacity, Predicate<@NonNull Gas> canInsert, Predicate<@NonNull Gas> validator, @Nullable IMekanismGasHandler gasHandler) {
        //TODO: Validate capacity is positive
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Gas validity check cannot be null");
        return new BasicGasTank(capacity, alwaysTrue, canInsert, validator, gasHandler);
    }

    public static BasicGasTank output(int capacity, @Nullable IMekanismGasHandler gasHandler) {
        //TODO: Validate capacity is positive
        return new BasicGasTank(capacity, alwaysTrueBi, internalOnly, alwaysTrue, gasHandler);
    }

    public static BasicGasTank ejectOutput(int capacity, @Nullable IMekanismGasHandler gasHandler) {
        //TODO: Validate capacity is positive
        return new BasicGasTank(capacity, internalOnly, internalOnly, alwaysTrue, gasHandler);
    }

    public static BasicGasTank create(int capacity, Predicate<@NonNull Gas> canExtract, Predicate<@NonNull Gas> canInsert, Predicate<@NonNull Gas> validator,
          @Nullable IMekanismGasHandler gasHandler) {
        //TODO: Validate capacity is positive
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Gas validity check cannot be null");
        return new BasicGasTank(capacity, canExtract, canInsert, validator, gasHandler);
    }

    protected BasicGasTank(int capacity, Predicate<@NonNull Gas> canExtract, Predicate<@NonNull Gas> canInsert, Predicate<@NonNull Gas> validator,
          @Nullable IMekanismGasHandler gasHandler) {
        this(capacity, (stack, automationType) -> automationType == AutomationType.MANUAL || canExtract.test(stack), (stack, automationType) -> canInsert.test(stack),
              validator, gasHandler);
    }

    protected BasicGasTank(int capacity, BiPredicate<@NonNull Gas, @NonNull AutomationType> canExtract, BiPredicate<@NonNull Gas, @NonNull AutomationType> canInsert,
          Predicate<@NonNull Gas> validator, @Nullable IMekanismGasHandler gasHandler) {
        super(capacity, canExtract, canInsert, validator);
        this.gasHandler = gasHandler;
    }

    @Override
    public GasStack getEmptyStack() {
        return GasStack.EMPTY;
    }

    @Override
    public GasStack createStack(GasStack stored, int size) {
        return new GasStack(stored, size);
    }

    @Override
    public void onContentsChanged() {
        if (gasHandler != null) {
            gasHandler.onContentsChanged();
        }
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if (nbt.contains("stored")) {
            setStackUnchecked(GasStack.readFromNBT(nbt.getCompound("stored")));
        }
    }

    @Override
    public int getGasTankCount() {
        return 1;
    }

    @Override
    public GasStack getGasInTank(int tank) {
        return tank == 0 ? getStack() : getEmptyStack();
    }

    @Override
    public void setGasInTank(int tank, GasStack stack) {
        if (tank == 0) {
            setStack(stack);
        }
    }

    @Override
    public int getGasTankCapacity(int tank) {
        return tank == 0 ? getCapacity() : 0;
    }

    @Override
    public boolean isGasValid(int tank, GasStack stack) {
        return tank == 0 && isValid(stack);
    }

    @Override
    public GasStack insertGas(int tank, GasStack stack, Action action) {
        return tank == 0 ? insert(stack, action, AutomationType.EXTERNAL) : stack;
    }

    @Override
    public GasStack extractGas(int tank, int amount, Action action) {
        return tank == 0 ? extract(amount, action, AutomationType.EXTERNAL) : getEmptyStack();
    }
}
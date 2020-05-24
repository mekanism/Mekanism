package mekanism.api.chemical.gas;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.inventory.AutomationType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants.NBT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BasicGasTank extends BasicChemicalTank<Gas, GasStack> implements IGasHandler, IGasTank {

    public static final Predicate<@NonNull Gas> alwaysTrue = stack -> true;
    public static final Predicate<@NonNull Gas> alwaysFalse = stack -> false;
    public static final BiPredicate<@NonNull Gas, @NonNull AutomationType> alwaysTrueBi = (stack, automationType) -> true;
    public static final BiPredicate<@NonNull Gas, @NonNull AutomationType> internalOnly = (stack, automationType) -> automationType == AutomationType.INTERNAL;
    public static final BiPredicate<@NonNull Gas, @NonNull AutomationType> notExternal = (stack, automationType) -> automationType != AutomationType.EXTERNAL;

    @Nullable
    private final IMekanismGasHandler gasHandler;

    public static BasicGasTank createDummy(long capacity) {
        return create(capacity, alwaysTrueBi, alwaysTrueBi, alwaysTrue, ChemicalAttributeValidator.ALWAYS_ALLOW, null);
    }

    public static BasicGasTank create(long capacity, @Nullable IMekanismGasHandler gasHandler) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        return new BasicGasTank(capacity, alwaysTrueBi, alwaysTrueBi, alwaysTrue, gasHandler);
    }

    public static BasicGasTank create(long capacity, Predicate<@NonNull Gas> canExtract, Predicate<@NonNull Gas> canInsert, @Nullable IMekanismGasHandler gasHandler) {
        return create(capacity, canExtract, canInsert, alwaysTrue, gasHandler);
    }

    public static BasicGasTank create(long capacity, Predicate<@NonNull Gas> validator, @Nullable IMekanismGasHandler gasHandler) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(validator, "Infuse type validity check cannot be null");
        return new BasicGasTank(capacity, alwaysTrueBi, alwaysTrueBi, validator, gasHandler);
    }

    public static BasicGasTank input(long capacity, Predicate<@NonNull Gas> validator, @Nullable IMekanismGasHandler gasHandler) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(validator, "Gas validity check cannot be null");
        return new BasicGasTank(capacity, notExternal, alwaysTrueBi, validator, gasHandler);
    }

    public static BasicGasTank input(long capacity, Predicate<@NonNull Gas> canInsert, Predicate<@NonNull Gas> validator, @Nullable IMekanismGasHandler gasHandler) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Gas validity check cannot be null");
        return new BasicGasTank(capacity, notExternal, (stack, automationType) -> canInsert.test(stack), validator, gasHandler);
    }

    public static BasicGasTank output(long capacity, @Nullable IMekanismGasHandler gasHandler) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        return new BasicGasTank(capacity, alwaysTrueBi, internalOnly, alwaysTrue, ChemicalAttributeValidator.ALWAYS_ALLOW, gasHandler);
    }

    public static BasicGasTank ejectOutput(long capacity, @Nullable IMekanismGasHandler gasHandler) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        return new BasicGasTank(capacity, internalOnly, internalOnly, alwaysTrue, ChemicalAttributeValidator.ALWAYS_ALLOW, gasHandler);
    }

    public static BasicGasTank create(long capacity, Predicate<@NonNull Gas> canExtract, Predicate<@NonNull Gas> canInsert, Predicate<@NonNull Gas> validator,
          @Nullable IMekanismGasHandler gasHandler) {
        return create(capacity, canExtract, canInsert, validator, null, gasHandler);
    }

    public static BasicGasTank create(long capacity, BiPredicate<@NonNull Gas, @NonNull AutomationType> canExtract,
          BiPredicate<@NonNull Gas, @NonNull AutomationType> canInsert, Predicate<@NonNull Gas> validator, @Nullable IMekanismGasHandler gasHandler) {
        return create(capacity, canExtract, canInsert, validator, null, gasHandler);
    }

    public static BasicGasTank create(long capacity, Predicate<@NonNull Gas> canExtract, Predicate<@NonNull Gas> canInsert, Predicate<@NonNull Gas> validator,
          @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IMekanismGasHandler gasHandler) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Gas validity check cannot be null");
        return new BasicGasTank(capacity, canExtract, canInsert, validator, attributeValidator, gasHandler);
    }

    public static BasicGasTank create(long capacity, BiPredicate<@NonNull Gas, @NonNull AutomationType> canExtract, BiPredicate<@NonNull Gas, @NonNull AutomationType> canInsert,
          Predicate<@NonNull Gas> validator, @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IMekanismGasHandler gasHandler) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Gas validity check cannot be null");
        return new BasicGasTank(capacity, canExtract, canInsert, validator, attributeValidator, gasHandler);
    }

    protected BasicGasTank(long capacity, Predicate<@NonNull Gas> canExtract, Predicate<@NonNull Gas> canInsert, Predicate<@NonNull Gas> validator,
          @Nullable IMekanismGasHandler gasHandler) {
        this(capacity, (stack, automationType) -> automationType == AutomationType.MANUAL || canExtract.test(stack), (stack, automationType) -> canInsert.test(stack),
              validator, gasHandler);
    }

    protected BasicGasTank(long capacity, BiPredicate<@NonNull Gas, @NonNull AutomationType> canExtract, BiPredicate<@NonNull Gas, @NonNull AutomationType> canInsert,
          Predicate<@NonNull Gas> validator, @Nullable IMekanismGasHandler gasHandler) {
        this(capacity, canExtract, canInsert, validator, null, gasHandler);
    }

    protected BasicGasTank(long capacity, Predicate<@NonNull Gas> canExtract, Predicate<@NonNull Gas> canInsert, Predicate<@NonNull Gas> validator,
          @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IMekanismGasHandler gasHandler) {
        this(capacity, (stack, automationType) -> automationType == AutomationType.MANUAL || canExtract.test(stack), (stack, automationType) -> canInsert.test(stack), validator,
              attributeValidator, gasHandler);
    }

    protected BasicGasTank(long capacity, BiPredicate<@NonNull Gas, @NonNull AutomationType> canExtract, BiPredicate<@NonNull Gas, @NonNull AutomationType> canInsert,
          Predicate<@NonNull Gas> validator, @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IMekanismGasHandler gasHandler) {
        super(capacity, canExtract, canInsert, validator, attributeValidator);
        this.gasHandler = gasHandler;
    }

    @Override
    public void onContentsChanged() {
        if (gasHandler != null) {
            gasHandler.onContentsChanged();
        }
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if (nbt.contains(NBTConstants.STORED, NBT.TAG_COMPOUND)) {
            setStackUnchecked(GasStack.readFromNBT(nbt.getCompound(NBTConstants.STORED)));
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
    public long getGasTankCapacity(int tank) {
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
    public GasStack extractGas(int tank, long amount, Action action) {
        return tank == 0 ? extract(amount, action, AutomationType.EXTERNAL) : getEmptyStack();
    }
}
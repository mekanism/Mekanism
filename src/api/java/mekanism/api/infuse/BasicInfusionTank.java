package mekanism.api.infuse;

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
public class BasicInfusionTank extends BasicChemicalTank<InfuseType, InfusionStack> implements IInfusionHandler {

    public static final Predicate<@NonNull InfuseType> alwaysTrue = stack -> true;
    public static final Predicate<@NonNull InfuseType> alwaysFalse = stack -> false;
    public static final BiPredicate<@NonNull InfuseType, @NonNull AutomationType> alwaysTrueBi = (stack, automationType) -> true;
    public static final BiPredicate<@NonNull InfuseType, @NonNull AutomationType> manualOnly = (stack, automationType) -> automationType == AutomationType.MANUAL;
    public static final BiPredicate<@NonNull InfuseType, @NonNull AutomationType> internalOnly = (stack, automationType) -> automationType == AutomationType.INTERNAL;

    @Nullable
    private final IMekanismInfusionHandler infusionHandler;

    public static BasicInfusionTank create(int capacity, @Nullable IMekanismInfusionHandler infusionHandler) {
        return create(capacity, alwaysTrue, infusionHandler);
    }

    public static BasicInfusionTank create(int capacity, Predicate<@NonNull InfuseType> validator, @Nullable IMekanismInfusionHandler infusionHandler) {
        //TODO: Validate capacity is positive
        Objects.requireNonNull(validator, "Infuse type validity check cannot be null");
        return new BasicInfusionTank(capacity, alwaysTrueBi, alwaysTrueBi, validator, infusionHandler);
    }

    public static BasicInfusionTank create(int capacity, Predicate<@NonNull InfuseType> canExtract, Predicate<@NonNull InfuseType> canInsert,
          @Nullable IMekanismInfusionHandler infusionHandler) {
        return create(capacity, canExtract, canInsert, alwaysTrue, infusionHandler);
    }

    public static BasicInfusionTank input(int capacity, Predicate<@NonNull InfuseType> validator, @Nullable IMekanismInfusionHandler infusionHandler) {
        //TODO: Validate capacity is positive
        Objects.requireNonNull(validator, "Infuse type validity check cannot be null");
        return new BasicInfusionTank(capacity, manualOnly, alwaysTrueBi, validator, infusionHandler);
    }

    public static BasicInfusionTank create(int capacity, Predicate<@NonNull InfuseType> canExtract, Predicate<@NonNull InfuseType> canInsert,
          Predicate<@NonNull InfuseType> validator, @Nullable IMekanismInfusionHandler infusionHandler) {
        //TODO: Validate capacity is positive
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Infuse type validity check cannot be null");
        return new BasicInfusionTank(capacity, canExtract, canInsert, validator, infusionHandler);
    }

    protected BasicInfusionTank(int capacity, Predicate<@NonNull InfuseType> canExtract, Predicate<@NonNull InfuseType> canInsert, Predicate<@NonNull InfuseType> validator,
          @Nullable IMekanismInfusionHandler infusionHandler) {
        this(capacity, (stack, automationType) -> automationType == AutomationType.MANUAL || canExtract.test(stack), (stack, automationType) -> canInsert.test(stack),
              validator, infusionHandler);
    }

    protected BasicInfusionTank(int capacity, BiPredicate<@NonNull InfuseType, @NonNull AutomationType> canExtract, BiPredicate<@NonNull InfuseType, @NonNull AutomationType> canInsert,
          Predicate<@NonNull InfuseType> validator, @Nullable IMekanismInfusionHandler infusionHandler) {
        super(capacity, canExtract, canInsert, validator);
        this.infusionHandler = infusionHandler;
    }

    @Override
    public InfusionStack getEmptyStack() {
        return InfusionStack.EMPTY;
    }

    @Override
    public InfusionStack createStack(InfusionStack stored, int size) {
        return new InfusionStack(stored, size);
    }

    @Override
    public void onContentsChanged() {
        if (infusionHandler != null) {
            infusionHandler.onContentsChanged();
        }
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if (nbt.contains("stored")) {
            setStackUnchecked(InfusionStack.readFromNBT(nbt.getCompound("stored")));
        }
    }

    @Override
    public int getInfusionTankCount() {
        return 1;
    }

    @Override
    public InfusionStack getInfusionInTank(int tank) {
        return tank == 0 ? getStack() : getEmptyStack();
    }

    @Override
    public void setInfusionInTank(int tank, InfusionStack stack) {
        if (tank == 0) {
            setStack(stack);
        }
    }

    @Override
    public int getInfusionTankCapacity(int tank) {
        return tank == 0 ? getCapacity() : 0;
    }

    @Override
    public boolean isInfusionValid(int tank, InfusionStack stack) {
        return tank == 0 && isValid(stack);
    }

    @Override
    public InfusionStack insertInfusion(int tank, InfusionStack stack, Action action) {
        return tank == 0 ? insert(stack, action, AutomationType.EXTERNAL) : stack;
    }

    @Override
    public InfusionStack extractInfusion(int tank, int amount, Action action) {
        return tank == 0 ? extract(amount, action, AutomationType.EXTERNAL) : getEmptyStack();
    }
}
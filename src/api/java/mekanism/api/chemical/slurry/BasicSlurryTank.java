package mekanism.api.chemical.slurry;

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
public class BasicSlurryTank extends BasicChemicalTank<Slurry, SlurryStack> implements ISlurryHandler, ISlurryTank {

    public static final Predicate<@NonNull Slurry> alwaysTrue = stack -> true;
    public static final BiPredicate<@NonNull Slurry, @NonNull AutomationType> alwaysTrueBi = (stack, automationType) -> true;
    public static final BiPredicate<@NonNull Slurry, @NonNull AutomationType> internalOnly = (stack, automationType) -> automationType == AutomationType.INTERNAL;
    public static final BiPredicate<@NonNull Slurry, @NonNull AutomationType> notExternal = (stack, automationType) -> automationType != AutomationType.EXTERNAL;

    @Nullable
    private final IMekanismSlurryHandler slurryHandler;

    public static BasicSlurryTank create(long capacity, @Nullable IMekanismSlurryHandler slurryHandler) {
        return create(capacity, alwaysTrue, slurryHandler);
    }

    public static BasicSlurryTank create(long capacity, Predicate<@NonNull Slurry> validator, @Nullable IMekanismSlurryHandler slurryHandler) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(validator, "Slurry validity check cannot be null");
        return new BasicSlurryTank(capacity, alwaysTrueBi, alwaysTrueBi, validator, slurryHandler);
    }

    public static BasicSlurryTank create(long capacity, Predicate<@NonNull Slurry> canExtract, Predicate<@NonNull Slurry> canInsert,
          @Nullable IMekanismSlurryHandler slurryHandler) {
        return create(capacity, canExtract, canInsert, alwaysTrue, slurryHandler);
    }

    public static BasicSlurryTank input(long capacity, Predicate<@NonNull Slurry> validator, @Nullable IMekanismSlurryHandler slurryHandler) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(validator, "Slurry validity check cannot be null");
        return new BasicSlurryTank(capacity, notExternal, alwaysTrueBi, validator, slurryHandler);
    }

    public static BasicSlurryTank create(long capacity, Predicate<@NonNull Slurry> canExtract, Predicate<@NonNull Slurry> canInsert,
          Predicate<@NonNull Slurry> validator, @Nullable IMekanismSlurryHandler slurryHandler) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Slurry validity check cannot be null");
        return new BasicSlurryTank(capacity, canExtract, canInsert, validator, slurryHandler);
    }

    public static BasicSlurryTank create(long capacity, BiPredicate<@NonNull Slurry, @NonNull AutomationType> canExtract,
          BiPredicate<@NonNull Slurry, @NonNull AutomationType> canInsert, Predicate<@NonNull Slurry> validator, @Nullable IMekanismSlurryHandler slurryHandler) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Slurry validity check cannot be null");
        return new BasicSlurryTank(capacity, canExtract, canInsert, validator, slurryHandler);
    }

    protected BasicSlurryTank(long capacity, Predicate<@NonNull Slurry> canExtract, Predicate<@NonNull Slurry> canInsert, Predicate<@NonNull Slurry> validator,
          @Nullable IMekanismSlurryHandler slurryHandler) {
        this(capacity, (stack, automationType) -> automationType == AutomationType.MANUAL || canExtract.test(stack), (stack, automationType) -> canInsert.test(stack),
              validator, slurryHandler);
    }

    protected BasicSlurryTank(long capacity, BiPredicate<@NonNull Slurry, @NonNull AutomationType> canExtract,
          BiPredicate<@NonNull Slurry, @NonNull AutomationType> canInsert, Predicate<@NonNull Slurry> validator, @Nullable IMekanismSlurryHandler slurryHandler) {
        this(capacity, canExtract, canInsert, validator, null, slurryHandler);
    }

    protected BasicSlurryTank(long capacity, BiPredicate<@NonNull Slurry, @NonNull AutomationType> canExtract, BiPredicate<@NonNull Slurry, @NonNull AutomationType> canInsert,
          Predicate<@NonNull Slurry> validator, @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IMekanismSlurryHandler slurryHandler) {
        super(capacity, canExtract, canInsert, validator, attributeValidator);
        this.slurryHandler = slurryHandler;
    }

    @Override
    public void onContentsChanged() {
        if (slurryHandler != null) {
            slurryHandler.onContentsChanged();
        }
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if (nbt.contains(NBTConstants.STORED, NBT.TAG_COMPOUND)) {
            setStackUnchecked(SlurryStack.readFromNBT(nbt.getCompound(NBTConstants.STORED)));
        }
    }

    @Override
    public int getSlurryTankCount() {
        return 1;
    }

    @Override
    public SlurryStack getSlurryInTank(int tank) {
        return tank == 0 ? getStack() : getEmptyStack();
    }

    @Override
    public void setSlurryInTank(int tank, SlurryStack stack) {
        if (tank == 0) {
            setStack(stack);
        }
    }

    @Override
    public long getSlurryTankCapacity(int tank) {
        return tank == 0 ? getCapacity() : 0;
    }

    @Override
    public boolean isSlurryValid(int tank, SlurryStack stack) {
        return tank == 0 && isValid(stack);
    }

    @Override
    public SlurryStack insertSlurry(int tank, SlurryStack stack, Action action) {
        return tank == 0 ? insert(stack, action, AutomationType.EXTERNAL) : stack;
    }

    @Override
    public SlurryStack extractSlurry(int tank, long amount, Action action) {
        return tank == 0 ? extract(amount, action, AutomationType.EXTERNAL) : getEmptyStack();
    }
}
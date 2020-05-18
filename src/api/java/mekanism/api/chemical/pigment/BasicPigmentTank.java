package mekanism.api.chemical.pigment;

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
public class BasicPigmentTank extends BasicChemicalTank<Pigment, PigmentStack> implements IPigmentHandler, IPigmentTank {

    public static final Predicate<@NonNull Pigment> alwaysTrue = stack -> true;
    public static final BiPredicate<@NonNull Pigment, @NonNull AutomationType> alwaysTrueBi = (stack, automationType) -> true;
    public static final BiPredicate<@NonNull Pigment, @NonNull AutomationType> internalOnly = (stack, automationType) -> automationType == AutomationType.INTERNAL;
    public static final BiPredicate<@NonNull Pigment, @NonNull AutomationType> notExternal = (stack, automationType) -> automationType != AutomationType.EXTERNAL;

    @Nullable
    private final IMekanismPigmentHandler pigmentHandler;

    public static BasicPigmentTank create(long capacity, @Nullable IMekanismPigmentHandler pigmentHandler) {
        return create(capacity, alwaysTrue, pigmentHandler);
    }

    public static BasicPigmentTank create(long capacity, Predicate<@NonNull Pigment> validator, @Nullable IMekanismPigmentHandler pigmentHandler) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(validator, "Pigment validity check cannot be null");
        return new BasicPigmentTank(capacity, alwaysTrueBi, alwaysTrueBi, validator, pigmentHandler);
    }

    public static BasicPigmentTank create(long capacity, Predicate<@NonNull Pigment> canExtract, Predicate<@NonNull Pigment> canInsert,
          @Nullable IMekanismPigmentHandler pigmentHandler) {
        return create(capacity, canExtract, canInsert, alwaysTrue, pigmentHandler);
    }

    public static BasicPigmentTank input(long capacity, Predicate<@NonNull Pigment> validator, @Nullable IMekanismPigmentHandler pigmentHandler) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(validator, "Pigment validity check cannot be null");
        return new BasicPigmentTank(capacity, notExternal, alwaysTrueBi, validator, pigmentHandler);
    }

    public static BasicPigmentTank create(long capacity, Predicate<@NonNull Pigment> canExtract, Predicate<@NonNull Pigment> canInsert,
          Predicate<@NonNull Pigment> validator, @Nullable IMekanismPigmentHandler pigmentHandler) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Pigment validity check cannot be null");
        return new BasicPigmentTank(capacity, canExtract, canInsert, validator, pigmentHandler);
    }

    public static BasicPigmentTank create(long capacity, BiPredicate<@NonNull Pigment, @NonNull AutomationType> canExtract,
          BiPredicate<@NonNull Pigment, @NonNull AutomationType> canInsert, Predicate<@NonNull Pigment> validator, @Nullable IMekanismPigmentHandler pigmentHandler) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Pigment validity check cannot be null");
        return new BasicPigmentTank(capacity, canExtract, canInsert, validator, pigmentHandler);
    }

    protected BasicPigmentTank(long capacity, Predicate<@NonNull Pigment> canExtract, Predicate<@NonNull Pigment> canInsert, Predicate<@NonNull Pigment> validator,
          @Nullable IMekanismPigmentHandler pigmentHandler) {
        this(capacity, (stack, automationType) -> automationType == AutomationType.MANUAL || canExtract.test(stack), (stack, automationType) -> canInsert.test(stack),
              validator, pigmentHandler);
    }

    protected BasicPigmentTank(long capacity, BiPredicate<@NonNull Pigment, @NonNull AutomationType> canExtract,
          BiPredicate<@NonNull Pigment, @NonNull AutomationType> canInsert, Predicate<@NonNull Pigment> validator, @Nullable IMekanismPigmentHandler pigmentHandler) {
        super(capacity, canExtract, canInsert, validator, null);
        this.pigmentHandler = pigmentHandler;
    }

    protected BasicPigmentTank(long capacity, BiPredicate<@NonNull Pigment, @NonNull AutomationType> canExtract, BiPredicate<@NonNull Pigment, @NonNull AutomationType> canInsert,
          Predicate<@NonNull Pigment> validator, @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IMekanismPigmentHandler pigmentHandler) {
        super(capacity, canExtract, canInsert, validator, attributeValidator);
        this.pigmentHandler = pigmentHandler;
    }

    @Override
    public PigmentStack getEmptyStack() {
        return PigmentStack.EMPTY;
    }

    @Override
    public PigmentStack createStack(PigmentStack stored, long size) {
        return new PigmentStack(stored, size);
    }

    @Override
    public void onContentsChanged() {
        if (pigmentHandler != null) {
            pigmentHandler.onContentsChanged();
        }
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if (nbt.contains(NBTConstants.STORED, NBT.TAG_COMPOUND)) {
            setStackUnchecked(PigmentStack.readFromNBT(nbt.getCompound(NBTConstants.STORED)));
        }
    }

    @Override
    public int getPigmentTankCount() {
        return 1;
    }

    @Override
    public PigmentStack getPigmentInTank(int tank) {
        return tank == 0 ? getStack() : getEmptyStack();
    }

    @Override
    public void setPigmentInTank(int tank, PigmentStack stack) {
        if (tank == 0) {
            setStack(stack);
        }
    }

    @Override
    public long getPigmentTankCapacity(int tank) {
        return tank == 0 ? getCapacity() : 0;
    }

    @Override
    public boolean isPigmentValid(int tank, PigmentStack stack) {
        return tank == 0 && isValid(stack);
    }

    @Override
    public PigmentStack insertPigment(int tank, PigmentStack stack, Action action) {
        return tank == 0 ? insert(stack, action, AutomationType.EXTERNAL) : stack;
    }

    @Override
    public PigmentStack extractPigment(int tank, long amount, Action action) {
        return tank == 0 ? extract(amount, action, AutomationType.EXTERNAL) : getEmptyStack();
    }
}
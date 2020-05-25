package mekanism.api.chemical.infuse;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.inventory.AutomationType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants.NBT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BasicInfusionTank extends BasicChemicalTank<InfuseType, InfusionStack, IInfusionTank, IMekanismInfusionHandler> implements IInfusionHandler, IInfusionTank {

    public static final Predicate<@NonNull InfuseType> alwaysTrue = stack -> true;
    public static final BiPredicate<@NonNull InfuseType, @NonNull AutomationType> alwaysTrueBi = (stack, automationType) -> true;
    public static final BiPredicate<@NonNull InfuseType, @NonNull AutomationType> internalOnly = (stack, automationType) -> automationType == AutomationType.INTERNAL;
    public static final BiPredicate<@NonNull InfuseType, @NonNull AutomationType> notExternal = (stack, automationType) -> automationType != AutomationType.EXTERNAL;

    public static BasicInfusionTank create(long capacity, @Nullable IMekanismInfusionHandler infusionHandler) {
        return create(capacity, alwaysTrue, infusionHandler);
    }

    public static BasicInfusionTank create(long capacity, Predicate<@NonNull InfuseType> validator, @Nullable IMekanismInfusionHandler infusionHandler) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(validator, "Infuse type validity check cannot be null");
        return new BasicInfusionTank(capacity, alwaysTrueBi, alwaysTrueBi, validator, infusionHandler);
    }

    public static BasicInfusionTank create(long capacity, Predicate<@NonNull InfuseType> canExtract, Predicate<@NonNull InfuseType> canInsert,
          @Nullable IMekanismInfusionHandler infusionHandler) {
        return create(capacity, canExtract, canInsert, alwaysTrue, infusionHandler);
    }

    public static BasicInfusionTank input(long capacity, Predicate<@NonNull InfuseType> validator, @Nullable IMekanismInfusionHandler infusionHandler) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(validator, "Infuse type validity check cannot be null");
        return new BasicInfusionTank(capacity, notExternal, alwaysTrueBi, validator, infusionHandler);
    }

    public static BasicInfusionTank create(long capacity, Predicate<@NonNull InfuseType> canExtract, Predicate<@NonNull InfuseType> canInsert,
          Predicate<@NonNull InfuseType> validator, @Nullable IMekanismInfusionHandler infusionHandler) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Infuse type validity check cannot be null");
        return new BasicInfusionTank(capacity, canExtract, canInsert, validator, infusionHandler);
    }

    public static BasicInfusionTank create(long capacity, BiPredicate<@NonNull InfuseType, @NonNull AutomationType> canExtract, BiPredicate<@NonNull InfuseType, @NonNull AutomationType> canInsert,
          Predicate<@NonNull InfuseType> validator, @Nullable IMekanismInfusionHandler infusionHandler) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Infuse type validity check cannot be null");
        return new BasicInfusionTank(capacity, canExtract, canInsert, validator, infusionHandler);
    }

    protected BasicInfusionTank(long capacity, Predicate<@NonNull InfuseType> canExtract, Predicate<@NonNull InfuseType> canInsert, Predicate<@NonNull InfuseType> validator,
          @Nullable IMekanismInfusionHandler infusionHandler) {
        this(capacity, (stack, automationType) -> automationType == AutomationType.MANUAL || canExtract.test(stack), (stack, automationType) -> canInsert.test(stack),
              validator, infusionHandler);
    }

    protected BasicInfusionTank(long capacity, BiPredicate<@NonNull InfuseType, @NonNull AutomationType> canExtract, BiPredicate<@NonNull InfuseType, @NonNull AutomationType> canInsert,
          Predicate<@NonNull InfuseType> validator, @Nullable IMekanismInfusionHandler infusionHandler) {
        this(capacity, canExtract, canInsert, validator, null, infusionHandler);
    }

    protected BasicInfusionTank(long capacity, BiPredicate<@NonNull InfuseType, @NonNull AutomationType> canExtract, BiPredicate<@NonNull InfuseType, @NonNull AutomationType> canInsert,
          Predicate<@NonNull InfuseType> validator, @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IMekanismInfusionHandler infusionHandler) {
        super(capacity, canExtract, canInsert, validator, attributeValidator, infusionHandler);
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if (nbt.contains(NBTConstants.STORED, NBT.TAG_COMPOUND)) {
            setStackUnchecked(InfusionStack.readFromNBT(nbt.getCompound(NBTConstants.STORED)));
        }
    }

    @Override
    public InfusionStack getEmptyStack() {
        return InfusionStack.EMPTY;
    }
}
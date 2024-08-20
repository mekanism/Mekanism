package mekanism.api.chemical;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.functions.ConstantPredicates;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class BasicChemicalTank implements IChemicalTank, IChemicalHandler {

    /**
     * A predicate that returns {@code true} for any input.
     *
     * @since 10.7.0 Previously was in ChemicalTankBuilder
     */
    public static final Predicate<Chemical> alwaysTrue = ConstantPredicates.alwaysTrue();
    /**
     * A predicate that returns {@code false} for any input.
     *
     * @since 10.7.0 Previously was in ChemicalTankBuilder
     */
    public static final Predicate<Chemical> alwaysFalse = ConstantPredicates.alwaysFalse();
    /**
     * A bi predicate that returns {@code true} for any input.
     *
     * @since 10.7.0 Previously was in ChemicalTankBuilder
     */
    public static final BiPredicate<Chemical, @NotNull AutomationType> alwaysTrueBi = ConstantPredicates.alwaysTrueBi();
    /**
     * A bi predicate that returns {@code true} for any input when the automation type is internal.
     *
     * @since 10.7.0 Previously was in ChemicalTankBuilder
     */
    public static final BiPredicate<Chemical, @NotNull AutomationType> internalOnly = ConstantPredicates.internalOnly();
    /**
     * A bi predicate that returns {@code true} for any input when the automation type is not external.
     *
     * @since 10.7.0 Previously was in ChemicalTankBuilder
     */
    public static final BiPredicate<Chemical, @NotNull AutomationType> notExternal = ConstantPredicates.notExternal();
    /**
     * A bi predicate that returns {@code true} for any input when the automation type is manual.
     *
     * @since 10.7.0 Previously was in ChemicalTankBuilder
     */
    public static final BiPredicate<Chemical, @NotNull AutomationType> manualOnly = ConstantPredicates.manualOnly();

    /**
     * Creates a tank with a given capacity, and content listener, using the default attribute validator {@link ChemicalAttributeValidator#DEFAULT}.
     *
     * @param capacity Tank capacity.
     * @param listener Contents change listener.
     *
     * @since 10.7.0 Previously was in ChemicalTankBuilder
     */
    public static IChemicalTank create(long capacity, @Nullable IContentsListener listener) {
        return createWithValidator(capacity, null, listener);
    }

    /**
     * Creates a tank with a given capacity, attribute validator, and content listener.
     *
     * @param capacity           Tank capacity.
     * @param attributeValidator Chemical Attribute Validator, or {@code null} to fall back to {@link ChemicalAttributeValidator#DEFAULT}.
     * @param listener           Contents change listener.
     *
     * @since 10.7.0 Previously was in ChemicalTankBuilder
     */
    public static IChemicalTank createWithValidator(long capacity, @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        return new BasicChemicalTank(capacity, alwaysTrueBi, alwaysTrueBi, alwaysTrue, attributeValidator, listener);
    }

    /**
     * Creates a tank with a given capacity, and content listener, that allows chemicals with any attributes.
     *
     * @param capacity Tank capacity.
     * @param listener Contents change listener.
     *
     * @since 10.7.0 Previously was in ChemicalTankBuilder
     */
    public static IChemicalTank createAllValid(long capacity, @Nullable IContentsListener listener) {
        return createWithValidator(capacity, ChemicalAttributeValidator.ALWAYS_ALLOW, listener);
    }

    /**
     * Creates a tank with a given capacity, extract predicate, insert predicate, and content listener, using the default attribute validator
     * {@link ChemicalAttributeValidator#DEFAULT}.
     *
     * @param capacity   Tank capacity.
     * @param canExtract Extract predicate.
     * @param canInsert  Insert predicate.
     * @param listener   Contents change listener.
     *
     * @implNote The created tank will always allow {@link AutomationType#MANUAL} extraction, and allow any {@link AutomationType} to insert into it.
     * @since 10.7.0 Previously was in ChemicalTankBuilder
     */
    public static IChemicalTank create(long capacity, Predicate<Chemical> canExtract, Predicate<Chemical> canInsert, @Nullable IContentsListener listener) {
        return create(capacity, canExtract, canInsert, alwaysTrue, listener);
    }

    /**
     * Creates a tank with a given capacity, validation predicate, and content listener, using the default attribute validator
     * {@link ChemicalAttributeValidator#DEFAULT}.
     *
     * @param capacity  Tank capacity.
     * @param validator Validation predicate.
     * @param listener  Contents change listener.
     *
     * @since 10.7.0 Previously was in ChemicalTankBuilder
     */
    public static IChemicalTank create(long capacity, Predicate<Chemical> validator, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(validator, "Chemical validity check cannot be null");
        return new BasicChemicalTank(capacity, alwaysTrueBi, alwaysTrueBi, validator, null, listener);
    }

    /**
     * Creates an input tank with a given capacity, validation predicate, and content listener, using the default attribute validator
     * {@link ChemicalAttributeValidator#DEFAULT}. Input tanks don't allow for external ({@link AutomationType#EXTERNAL}) extraction.
     *
     * @param capacity  Tank capacity.
     * @param validator Validation predicate.
     * @param listener  Contents change listener.
     *
     * @since 10.7.0 Previously was in ChemicalTankBuilder
     */
    public static IChemicalTank input(long capacity, Predicate<Chemical> validator, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(validator, "Chemical validity check cannot be null");
        return new BasicChemicalTank(capacity, notExternal, alwaysTrueBi, validator, null, listener);
    }

    /**
     * Creates an input tank with a given capacity, insertion predicate, validation predicate, and content listener, using the default attribute validator
     * {@link ChemicalAttributeValidator#DEFAULT}. Input tanks don't allow for external ({@link AutomationType#EXTERNAL}) extraction.
     *
     * @param capacity  Tank capacity.
     * @param canInsert Insert predicate.
     * @param validator Validation predicate.
     * @param listener  Contents change listener.
     *
     * @since 10.7.0 Previously was in ChemicalTankBuilder
     */
    public static IChemicalTank input(long capacity, Predicate<Chemical> canInsert, Predicate<Chemical> validator, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Chemical validity check cannot be null");
        return new BasicChemicalTank(capacity, notExternal, (stack, automationType) -> canInsert.test(stack), validator, null, listener);
    }

    /**
     * Creates an output tank with a given capacity, and content listener, that allows chemicals with any attributes. Output tanks only allow for internal
     * ({@link AutomationType#INTERNAL}) insertion.
     *
     * @param capacity Tank capacity.
     * @param listener Contents change listener.
     *
     * @since 10.7.0 Previously was in ChemicalTankBuilder
     */
    public static IChemicalTank output(long capacity, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        return new BasicChemicalTank(capacity, alwaysTrueBi, internalOnly, alwaysTrue, ChemicalAttributeValidator.ALWAYS_ALLOW, listener);
    }

    /**
     * Creates a tank with a given capacity, extract predicate, insert predicate, validation predicate, and content listener, using the default attribute validator
     * {@link ChemicalAttributeValidator#DEFAULT}.
     *
     * @param capacity   Tank capacity.
     * @param canExtract Extract predicate.
     * @param canInsert  Insert predicate.
     * @param validator  Validation predicate.
     * @param listener   Contents change listener.
     *
     * @implNote The created tank will always allow {@link AutomationType#MANUAL} extraction, and allow any {@link AutomationType} to insert into it.
     * @since 10.7.0 Previously was in ChemicalTankBuilder
     */
    public static IChemicalTank create(long capacity, Predicate<Chemical> canExtract, Predicate<Chemical> canInsert, Predicate<Chemical> validator,
          @Nullable IContentsListener listener) {
        return create(capacity, canExtract, canInsert, validator, null, listener);
    }

    /**
     * Creates a tank with a given capacity, extract predicate, insert predicate, validation predicate, and content listener, using the default attribute validator
     * {@link ChemicalAttributeValidator#DEFAULT}.
     *
     * @param capacity   Tank capacity.
     * @param canExtract Extract predicate.
     * @param canInsert  Insert predicate.
     * @param validator  Validation predicate.
     * @param listener   Contents change listener.
     *
     * @since 10.7.0 Previously was in ChemicalTankBuilder
     */
    public static IChemicalTank create(long capacity, BiPredicate<Chemical, @NotNull AutomationType> canExtract,
          BiPredicate<Chemical, @NotNull AutomationType> canInsert, Predicate<Chemical> validator, @Nullable IContentsListener listener) {
        return create(capacity, canExtract, canInsert, validator, null, listener);
    }

    /**
     * Creates a tank with a given capacity, extract predicate, insert predicate, validation predicate, attribute validator, and content listener.
     *
     * @param capacity           Tank capacity.
     * @param canExtract         Extract predicate.
     * @param canInsert          Insert predicate.
     * @param validator          Validation predicate.
     * @param attributeValidator Chemical Attribute Validator, or {@code null} to fall back to {@link ChemicalAttributeValidator#DEFAULT}.
     * @param listener           Contents change listener.
     *
     * @implNote The created tank will always allow {@link AutomationType#MANUAL} extraction, and allow any {@link AutomationType} to insert into it.
     * @since 10.7.0 Previously was in ChemicalTankBuilder
     */
    public static IChemicalTank create(long capacity, Predicate<Chemical> canExtract, Predicate<Chemical> canInsert, Predicate<Chemical> validator,
          @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Chemical validity check cannot be null");
        return new BasicChemicalTank(capacity, (stack, automationType) -> automationType == AutomationType.MANUAL || canExtract.test(stack),
              (stack, automationType) -> canInsert.test(stack), validator, attributeValidator, listener);
    }

    /**
     * Creates a tank with a given capacity, extract predicate, insert predicate, validation predicate, attribute validator, and content listener.
     *
     * @param capacity           Tank capacity.
     * @param canExtract         Extract predicate.
     * @param canInsert          Insert predicate.
     * @param validator          Validation predicate.
     * @param attributeValidator Chemical Attribute Validator, or {@code null} to fall back to {@link ChemicalAttributeValidator#DEFAULT}.
     * @param listener           Contents change listener.
     *
     * @since 10.7.0 Previously was in ChemicalTankBuilder
     */
    public static IChemicalTank create(long capacity, BiPredicate<Chemical, @NotNull AutomationType> canExtract, BiPredicate<Chemical, @NotNull AutomationType> canInsert,
          Predicate<Chemical> validator, @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be at least zero");
        }
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Chemical validity check cannot be null");
        return new BasicChemicalTank(capacity, canExtract, canInsert, validator, attributeValidator, listener);
    }

    private final Predicate<Chemical> validator;
    protected final BiPredicate<Chemical, @NotNull AutomationType> canExtract;
    protected final BiPredicate<Chemical, @NotNull AutomationType> canInsert;
    @Nullable
    private final ChemicalAttributeValidator attributeValidator;
    private final long capacity;
    /**
     * @apiNote This is only protected for direct querying access. To modify this stack the external methods or {@link #setStackUnchecked(ChemicalStack)} should be used
     * instead.
     */
    protected ChemicalStack stored;
    @Nullable
    private final IContentsListener listener;

    protected BasicChemicalTank(long capacity, BiPredicate<Chemical, @NotNull AutomationType> canExtract,
          BiPredicate<Chemical, @NotNull AutomationType> canInsert, Predicate<Chemical> validator,
          @Nullable ChemicalAttributeValidator attributeValidator, @Nullable IContentsListener listener) {
        this.capacity = capacity;
        this.canExtract = canExtract;
        this.canInsert = canInsert;
        this.validator = validator;
        this.attributeValidator = attributeValidator;
        this.listener = listener;
        this.stored = ChemicalStack.EMPTY;
    }

    @Override
    public ChemicalStack getStack() {
        return stored;
    }

    @Override
    public void setStack(ChemicalStack stack) {
        setStack(stack, true);
    }

    /**
     * Helper method to allow easily setting a rate at which chemicals can be inserted into this {@link BasicChemicalTank}.
     *
     * @param automationType The automation type to limit the rate by or null if we don't have access to an automation type.
     *
     * @return The rate this tank can insert/extract at.
     *
     * @implNote By default, this returns {@link Long#MAX_VALUE} to not actually limit the tank's rate. By default, this is also ignored for direct setting of the
     * stack/stack size
     * @since 10.6.0, previously was combined with {@link #getExtractRate(AutomationType)} as a method named getRate
     */
    protected long getInsertRate(@Nullable AutomationType automationType) {
        return Long.MAX_VALUE;
    }

    /**
     * Helper method to allow easily setting a rate at which chemicals can be extracted from this {@link BasicChemicalTank}.
     *
     * @param automationType The automation type to limit the rate by or null if we don't have access to an automation type.
     *
     * @return The rate this tank can insert/extract at.
     *
     * @implNote By default, this returns {@link Long#MAX_VALUE} to not actually limit the tank's rate. By default, this is also ignored for direct setting of the
     * stack/stack size
     * @since 10.6.0, previously was combined with {@link #getInsertRate(AutomationType)} as a method named getRate
     */
    protected long getExtractRate(@Nullable AutomationType automationType) {
        return Long.MAX_VALUE;
    }

    @Override
    public void setStackUnchecked(ChemicalStack stack) {
        setStack(stack, false);
    }

    private void setStack(ChemicalStack stack, boolean validateStack) {
        if (stack.isEmpty()) {
            if (stored.isEmpty()) {
                //If we are already empty just exit, to not fire onContentsChanged
                return;
            }
            stored = ChemicalStack.EMPTY;
        } else if (!validateStack || isValid(stack)) {
            stored = stack.copy();
        } else {
            //Throws a RuntimeException as specified is allowed when something unexpected happens
            // As setStack is more meant to be used as an internal method
            throw new RuntimeException("Invalid chemical for tank: " + stack.getTypeRegistryName() + " " + stack.getAmount());
        }
        onContentsChanged();
    }

    @Override
    public ChemicalStack insert(ChemicalStack stack, Action action, AutomationType automationType) {
        boolean sameType = false;
        if (stack.isEmpty() || !(isEmpty() || (sameType = isTypeEqual(stack)))) {
            //"Fail quick" if the given stack is empty
            return stack;
        }
        long needed = Math.min(getInsertRate(automationType), getNeeded());
        if (needed <= 0) {
            //Fail if we are a full tank or our rate is zero
            return stack;
        }
        if (!isValid(stack) || !canInsert.test(stack.getChemical(), automationType)) {
            //we can never insert the chemical or currently are unable to insert it
            return stack;
        }
        long toAdd = Math.min(stack.getAmount(), needed);
        if (action.execute()) {
            //If we want to actually insert the chemical, then update the current chemical
            if (sameType) {
                //We can just grow our stack by the amount we want to increase it
                stored.grow(toAdd);
                onContentsChanged();
            } else {
                //If we are not the same type then we have to copy the stack and set it
                // Just set it unchecked as we have already validated it
                // Note: this also will mark that the contents changed
                setStackUnchecked(stack.copyWithAmount(toAdd));
            }
        }
        return stack.copyWithAmount(stack.getAmount() - toAdd);
    }

    @Override
    public ChemicalStack extract(long amount, Action action, AutomationType automationType) {
        if (isEmpty() || amount < 1 || !canExtract.test(stored.getChemical(), automationType)) {
            //"Fail quick" if we don't can never extract from this tank, have a chemical stored, or the amount being requested is less than one
            return ChemicalStack.EMPTY;
        }
        //Note: While we technically could just return the stack itself if we are removing all that we have, it would require a lot more checks
        // We also are limiting it by the rate this tank has
        long size = Math.min(Math.min(getExtractRate(automationType), getStored()), amount);
        if (size == 0) {
            return ChemicalStack.EMPTY;
        }
        ChemicalStack ret = stored.copyWithAmount(size);
        if (!ret.isEmpty() && action.execute()) {
            //If shrink gets the size to zero it will update the empty state so that isEmpty() returns true.
            stored.shrink(ret.getAmount());
            onContentsChanged();
        }
        return ret;
    }

    @Override
    public boolean isValid(ChemicalStack stack) {
        return getAttributeValidator().process(stack) && validator.test(stack.getChemical());
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten so that if we decide to change to returning a cached/copy of our stack in {@link #getStack()}, we can optimize out the copying, and can also
     * directly modify our stack instead of having to make a copy.
     */
    @Override
    public long setStackSize(long amount, Action action) {
        if (isEmpty()) {
            return 0;
        } else if (amount <= 0) {
            if (action.execute()) {
                setEmpty();
            }
            return 0;
        }
        long maxStackSize = getCapacity();
        if (amount > maxStackSize) {
            amount = maxStackSize;
        }
        if (getStored() == amount || action.simulate()) {
            //If our size is not changing, or we are only simulating the change, don't do anything
            return amount;
        }
        stored.setAmount(amount);
        onContentsChanged();
        return amount;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten so that we can make this obey the rate limit our tank may have
     */
    @Override
    public long growStack(long amount, Action action) {
        long current = getStored();
        if (current == 0) {
            //"Fail quick" if our stack is empty, so we can't grow it
            return 0;
        } else if (amount > 0) {
            //Cap adding amount at how much we need, so that we don't risk long overflow
            //If we are increasing the stack's size, use the insert rate
            amount = Math.min(Math.min(amount, getNeeded()), getInsertRate(null));
        } else if (amount < 0) {
            //If we are decreasing the stack's size, use the extract rate
            amount = Math.max(amount, -getExtractRate(null));
        }
        long newSize = setStackSize(current + amount, action);
        return newSize - current;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten so that if we decide to change to returning a cached/copy of our stack in {@link #getStack()}, we can optimize out the copying.
     */
    @Override
    public boolean isEmpty() {
        return stored.isEmpty();
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten so that if we decide to change to returning a cached/copy of our stack in {@link #getStack()}, we can optimize out the copying.
     */
    @Override
    public long getStored() {
        return stored.getAmount();
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten so that if we decide to change to returning a cached/copy of our stack in {@link #getStack()}, we can optimize out the copying.
     */
    @Override
    public Chemical getType() {
        return stored.getChemical();
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten so that if we decide to change to returning a cached/copy of our stack in {@link #getStack()}, we can optimize out the copying.
     */
    @Override
    public boolean isTypeEqual(ChemicalStack other) {
        return ChemicalStack.isSameChemical(stored, other);
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten so that if we decide to change to returning a cached/copy of our stack in {@link #getStack()}, we can optimize out the copying.
     */
    @Override
    public boolean isTypeEqual(Chemical other) {
        return stored.is(other);
    }

    @Override
    public long getCapacity() {
        return capacity;
    }

    @Override
    public void onContentsChanged() {
        if (listener != null) {
            listener.onContentsChanged();
        }
    }

    @Override
    public ChemicalAttributeValidator getAttributeValidator() {
        return attributeValidator == null ? IChemicalTank.super.getAttributeValidator() : attributeValidator;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten so that if we decide to change to returning a cached/copy of our stack in {@link #getStack()}, we can optimize out the copying.
     */
    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        if (!isEmpty()) {
            nbt.put(SerializationConstants.STORED, stored.save(provider));
        }
        return nbt;
    }

    @Override
    public int getChemicalTanks() {
        return 1;
    }

    @Override
    public ChemicalStack getChemicalInTank(int tank) {
        return tank == 0 ? getStack() : ChemicalStack.EMPTY;
    }

    @Override
    public void setChemicalInTank(int tank, ChemicalStack stack) {
        if (tank == 0) {
            setStack(stack);
        }
    }

    @Override
    public long getChemicalTankCapacity(int tank) {
        return tank == 0 ? getCapacity() : 0;
    }

    @Override
    public boolean isValid(int tank, ChemicalStack stack) {
        return tank == 0 && isValid(stack);
    }

    @Override
    public ChemicalStack insertChemical(int tank, ChemicalStack stack, Action action) {
        return tank == 0 ? insertChemical(stack, action) : stack;
    }

    @Override
    public ChemicalStack insertChemical(ChemicalStack stack, Action action) {
        return insert(stack, action, AutomationType.EXTERNAL);
    }

    @Override
    public ChemicalStack extractChemical(int tank, long amount, Action action) {
        return tank == 0 ? extractChemical(amount, action) : ChemicalStack.EMPTY;
    }

    @Override
    public ChemicalStack extractChemical(long amount, Action action) {
        return extract(amount, action, AutomationType.EXTERNAL);
    }

    @Override
    public ChemicalStack extractChemical(ChemicalStack stack, Action action) {
        return isTypeEqual(stack) ? extractChemical(stack.getAmount(), action) : ChemicalStack.EMPTY;
    }
}
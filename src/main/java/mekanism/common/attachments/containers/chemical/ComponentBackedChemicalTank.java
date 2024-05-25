package mekanism.common.attachments.containers.chemical;

import java.util.function.BiPredicate;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.common.attachments.containers.ComponentBackedContainer;
import mekanism.common.attachments.containers.IAttachedContainers;
import mekanism.common.util.ChemicalUtil;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault//TODO - 1.20.5: BasicChemicalTank also implement IChemicalHandler. Do we want to be doing that here?
public abstract class ComponentBackedChemicalTank<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      ATTACHED extends IAttachedContainers<STACK, ATTACHED>> extends ComponentBackedContainer<STACK, ATTACHED> implements IChemicalTank<CHEMICAL, STACK> {

    private final BiPredicate<@NotNull CHEMICAL, @NotNull AutomationType> canExtract;
    private final BiPredicate<@NotNull CHEMICAL, @NotNull AutomationType> canInsert;
    private final Predicate<@NotNull CHEMICAL> validator;
    @Nullable
    private final ChemicalAttributeValidator attributeValidator;
    private final LongSupplier capacity;
    private final LongSupplier rate;

    protected ComponentBackedChemicalTank(ItemStack attachedTo, int tankIndex, BiPredicate<@NotNull CHEMICAL, @NotNull AutomationType> canExtract,
          BiPredicate<@NotNull CHEMICAL, @NotNull AutomationType> canInsert, Predicate<@NotNull CHEMICAL> validator, LongSupplier rate, LongSupplier capacity,
          @Nullable ChemicalAttributeValidator attributeValidator) {
        super(attachedTo, tankIndex);
        this.canExtract = canExtract;
        this.canInsert = canInsert;
        this.validator = validator;
        this.capacity = capacity;
        this.rate = rate;
        this.attributeValidator = attributeValidator;
    }

    @Override
    protected STACK copy(STACK toCopy) {
        return ChemicalUtil.copy(toCopy);
    }

    @Override
    protected boolean isEmpty(STACK value) {
        return value.isEmpty();
    }

    @Override
    public STACK getStack() {
        //TODO - 1.20.5: Similar to getBasicInventorySlot do we want to reduce calls to this? Probably (We mostly do so, but we probably want to add a note here)
        ATTACHED attachedChemicals = getAttached();
        return attachedChemicals == null ? getEmptyStack() : getContents(attachedChemicals);
    }

    @Override
    public void setStack(STACK stack) {
        //TODO - 1.20.5: I think just always setting it as unchecked is fine as we are doing that for items regardless
        // but we may want to validate it?
        setStackUnchecked(stack);
    }

    @Override
    public void setStackUnchecked(STACK stack) {
        setContents(stack);
    }

    @Override
    public ChemicalAttributeValidator getAttributeValidator() {
        return attributeValidator == null ? IChemicalTank.super.getAttributeValidator() : attributeValidator;
    }

    protected long getRate(@Nullable AutomationType automationType) {
        //Allow unknown or manual interaction to bypass rate limit for the item
        return automationType == null || automationType == AutomationType.MANUAL ? Long.MAX_VALUE : rate.getAsLong();
    }

    @Override
    public long getCapacity() {
        return capacity.getAsLong();
    }

    @Override
    public boolean isValid(STACK stack) {
        return getAttributeValidator().process(stack) && validator.test(stack.getChemical());
    }

    @Override
    public STACK insert(STACK stack, Action action, AutomationType automationType) {
        //TODO - 1.20.5: Items do the is valid and canInsert check after checking the needed amount. Should we do the same for fluids
        // or should items have the order flipped? In general calculating the needed amount is likely cheaper which is likely why items do it first
        if (stack.isEmpty() || !isValid(stack) || !canInsert.test(stack.getChemical(), automationType)) {
            //"Fail quick" if the given stack is empty, or we can never insert the fluid or currently are unable to insert it
            return stack;
        }
        ATTACHED attachedChemicals = getAttached();
        if (attachedChemicals == null) {
            //"Fail quick" if we can't have a stack
            return stack;
        }
        STACK stored = getContents(attachedChemicals);
        long needed = Math.min(getRate(automationType), getNeeded(stored));
        if (needed <= 0) {
            //Fail if we are a full tank or our rate is zero
            return stack;
        } else if (stored.isEmpty() || ChemicalStack.isSameChemical(stored, stack)) {
            long toAdd = Math.min(stack.getAmount(), needed);
            if (action.execute()) {
                //Note: We let setStack handle updating the backing holding stack
                // We use stored.getAmount + toAdd so that if we are empty we end up at toAdd
                // but if we aren't then we grow by the given amount
                setContents(attachedChemicals, createStack(stack, stored.getAmount() + toAdd));
            }
            return createStack(stack, stack.getAmount() - toAdd);
        }
        //If we didn't accept this fluid, then just return the given stack
        return stack;
    }

    @Override
    public final STACK extract(long amount, Action action, AutomationType automationType) {
        if (amount < 1) {
            //"Fail quick" if the amount being requested is less than one
            return getEmptyStack();
        }
        ATTACHED attachedChemicals = getAttached();
        if (attachedChemicals == null) {
            //"Fail quick" if we can't have a stack
            return getEmptyStack();
        }
        return extract(attachedChemicals, getContents(attachedChemicals), amount, action, automationType);
    }

    protected STACK extract(ATTACHED attachedChemicals, STACK stored, long amount, Action action, AutomationType automationType) {
        if (amount < 1 || stored.isEmpty() || !canExtract.test(stored.getChemical(), automationType)) {
            //"Fail quick" if we don't can never extract from this tank, have a fluid stored, or the amount being requested is less than one
            return getEmptyStack();
        }
        //Note: While we technically could just return the stack itself if we are removing all that we have, it would require a lot more checks
        // We also are limiting it by the rate this tank has
        long size = Math.min(Math.min(getRate(automationType), stored.getAmount()), amount);
        if (size == 0) {
            return getEmptyStack();
        }
        STACK ret = createStack(stored, size);
        if (!ret.isEmpty() && action.execute()) {
            //Note: We let setStack handle updating the backing holding stack
            setContents(attachedChemicals, createStack(stored, stored.getAmount() - ret.getAmount()));
        }
        return ret;
    }

    @Override
    public final long setStackSize(long amount, Action action) {
        ATTACHED attachedChemicals = getAttached();
        if (attachedChemicals == null) {
            //"Fail quick" if we can't have a stack
            return 0;
        }
        return setStackSize(attachedChemicals, getContents(attachedChemicals), amount, action);
    }

    protected long setStackSize(ATTACHED attachedChemicals, STACK stored, long amount, Action action) {
        if (stored.isEmpty()) {
            return 0;
        } else if (amount <= 0) {
            if (action.execute()) {
                setContents(attachedChemicals, getEmptyStack());
            }
            return 0;
        }
        long maxStackSize = getCapacity();
        if (amount > maxStackSize) {
            amount = maxStackSize;
        }
        if (stored.getAmount() == amount || action.simulate()) {
            //If our size is not changing, or we are only simulating the change, don't do anything
            return amount;
        }
        setContents(attachedChemicals, createStack(stored, amount));
        return amount;
    }

    @Override
    public long growStack(long amount, Action action) {
        ATTACHED attachedChemicals = getAttached();
        if (attachedChemicals == null) {
            //"Fail quick" if we can't have a stack
            return 0;
        }
        STACK stored = getContents(attachedChemicals);
        long current = stored.getAmount();
        if (amount > 0) {
            //Cap adding amount at how much we need, so that we don't risk integer overflow
            amount = Math.min(Math.min(amount, getNeeded(stored)), getRate(null));
        } else if (amount < 0) {
            amount = Math.max(amount, -getRate(null));
        }
        long newSize = setStackSize(attachedChemicals, stored, current + amount, action);
        return newSize - current;
    }

    protected long getNeeded(STACK stored) {
        //Skip the stack lookup for getNeeded
        return Math.max(0, getCapacity() - stored.getAmount());
    }

    @Override
    public CompoundTag serializeNBT(Provider provider) {
        //TODO - 1.20.5: This is a copy of BasicChemicalTank#serializeNBT. We might need to also grab the specific overrides of
        // that method as special component backed inventory slots, that then access and put that other data as a different component?
        CompoundTag nbt = new CompoundTag();
        STACK stored = getStack();
        if (!stored.isEmpty()) {
            nbt.put(SerializationConstants.STORED, stored.save(provider));
        }
        return nbt;
    }
}
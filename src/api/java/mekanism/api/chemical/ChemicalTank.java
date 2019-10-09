package mekanism.api.chemical;

import javax.annotation.Nonnull;
import mekanism.api.Action;
import net.minecraft.nbt.CompoundNBT;

public abstract class ChemicalTank<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> {

    @Nonnull
    private STACK stored;
    private int capacity;

    /**
     * Creates a tank with a defined capacity.
     *
     * @param capacity - the maximum amount of a chemical this ChemicalTank can hold
     */
    public ChemicalTank(int capacity) {
        this.capacity = capacity;
        stored = getEmptyStack();
    }

    //TODO: Is there a better way to make this super class know about the empty stack?
    @Nonnull
    protected abstract STACK getEmptyStack();

    /**
     * Helper method to create a new stack based on our current stack, but with a given size.
     */
    @Nonnull
    protected abstract STACK createStack(STACK stored, int size);

    /**
     * @param resource Maximum amount of a chemical to be removed from the container.
     * @param action   If SIMULATE, the drain will only be simulated.
     *
     * @return ChemicalStack representing the chemical that was removed (or would be, if simulated) from the tank.
     */
    @Nonnull
    public STACK drain(STACK resource, Action action) {
        if (isEmpty() || resource.isEmpty() || !resource.isTypeEqual(stored)) {
            return getEmptyStack();
        }
        return drain(resource.getAmount(), action);
    }

    /**
     * Draws a specified amount of a chemical out of this tank.
     *
     * @param amount Maximum amount of the chemical to be removed from the container.
     * @param action If SIMULATE, the drain will only be simulated.
     *
     * @return Amount of the chemical that was removed (or would be, if simulated) from the tank.
     */
    //TODO: Go through and evaluate if some of these should call the method that takes a stack as a param
    @Nonnull
    public STACK drain(int amount, Action action) {
        if (isEmpty()) {
            return getEmptyStack();
        }
        STACK ret = createStack(stored, Math.min(getStored(), amount));
        if (!ret.isEmpty() && action.execute()) {
            stored.shrink(ret.getAmount());
        }
        return ret;
    }

    /**
     * Adds a specified amount of a chemical to this tank.
     *
     * @param resource ChemicalStack attempting to fill the tank.
     * @param action   If SIMULATE, the fill will only be simulated.
     *
     * @return Amount of the chemical that was accepted (or would be, if simulated) by the tank.
     */
    public int fill(@Nonnull STACK resource, Action action) {
        if (resource.isEmpty()) {
            return 0;
        }
        //This can be used for both being empty, and  when not, as getNeeded() returns the capacity when empty
        int amount = Math.min(getNeeded(), resource.getAmount());
        if (isEmpty()) {
            if (action.execute()) {
                stored = createStack(resource, amount);
            }
        } else if (!stored.isTypeEqual(resource)) {
            return 0;
        } else if (action.execute()) {
            stored.grow(amount);
        }
        return amount;
    }

    /**
     * If this ChemicalTank can receive the specified type of chemical. Will return false if this tank is full.
     *
     * @param chemical - The chemical to check
     *
     * @return if this ChemicalTank can accept the defined chemical
     */
    //TODO: Is there really a use for this, or are things that are using it doing so incorrectly and really mean to be using canReceiveType
    //TODO: Rename to canFill
    public boolean canReceive(@Nonnull CHEMICAL chemical) {
        return getNeeded() > 0 && canReceiveType(chemical);
    }

    /**
     * If this ChemicalTank can receive the specified type of chemical. Will return TRUE even if this tank is full.
     *
     * @param chemical - The chemical to check
     *
     * @return if this ChemicalTank can accept the defined chemical
     */
    //TODO: Rename to canFillType
    public boolean canReceiveType(@Nonnull CHEMICAL chemical) {
        return stored.isEmpty() || stored.isTypeEqual(chemical);
    }

    /**
     * If this ChemicalTank can be drawn of the specified type of chemical. Will return false if this tank is empty.
     *
     * @param chemical - The chemical to check
     *
     * @return if this ChemicalTank can be drawn of the defined chemical
     */
    //TODO: Rename to canDrain
    public boolean canDraw(@Nonnull CHEMICAL chemical) {
        return !stored.isEmpty() && stored.isTypeEqual(chemical);
    }

    /**
     * Gets the amount of chemical needed by this ChemicalTank.
     *
     * @return Amount of chemical needed
     */
    public int getNeeded() {
        return Math.max(0, getCapacity() - getStored());
    }

    /**
     * Gets the capacity of this tank.
     *
     * @return - capacity
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Sets the capacity of this tank.
     */
    public ChemicalTank<CHEMICAL, STACK> setCapacity(int capacity) {
        this.capacity = capacity;
        return this;
    }

    /**
     * Gets the ChemicalStack held by this ChemicalTank.
     *
     * @return - ChemicalStack held by this tank
     */
    //TODO: Update javadocs so that it specifies not to modify this stack similar to how it is stated in GasTankInfo?
    @Nonnull
    public STACK getStack() {
        return stored;
    }

    /**
     * Sets this tank's ChemicalStack value to a new value. Will cap the amount to this ChemicalTank's capacity.
     *
     * @param stack - value to set this tank's ChemicalStack value to
     */
    public void setStack(@Nonnull STACK stack) {
        stored = stack;
        //TODO: Remove this extra check? FluidTank does not have it, I believe so that it can load and not accidentally void excess
        // We first need to go through and check the places it is called to make sure none are passing more than max size relying on the fact
        // that it will cap it here
        if (!stored.isEmpty()) {
            stored.setAmount(Math.min(getCapacity(), stored.getAmount()));
        }
    }

    public void setEmpty() {
        setStack(getEmptyStack());
    }

    /**
     * Gets the type of chemical currently stored in this ChemicalTank.
     *
     * @return chemical type contained
     */
    //TODO: Evaluate all calls of this and see if they can be implemented in a "better" way
    @Nonnull
    public CHEMICAL getType() {
        return stored.getType();
    }

    /**
     * Gets the amount of chemical stored by this ChemicalTank.
     *
     * @return amount of chemical stored
     */
    public int getStored() {
        return stored.getAmount();
    }

    public boolean isEmpty() {
        return stored.isEmpty();
    }

    /**
     * Writes this tank to a defined tag compound.
     *
     * @param nbtTags - tag compound to write to
     *
     * @return tag compound with this tank's data
     */
    public CompoundNBT write(CompoundNBT nbtTags) {
        if (!stored.isEmpty()) {
            nbtTags.put("stored", stored.write(new CompoundNBT()));
        }
        return nbtTags;
    }

    /**
     * Reads this tank's data from a defined tag compound.
     *
     * @param nbtTags - tag compound to read from
     */
    public abstract ChemicalTank<CHEMICAL, STACK> read(CompoundNBT nbtTags);
}
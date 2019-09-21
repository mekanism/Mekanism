package mekanism.api.gas;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.MekanismAPI;
import net.minecraft.nbt.CompoundNBT;

/**
 * An optional way of managing and/or storing gasses. Would be very useful in TileEntity and Entity gas storage.
 *
 * @author aidancbrady
 */
public class GasTank implements GasTankInfo {

    @Nonnull
    private GasStack stored = GasStack.EMPTY;
    private int maxGas;

    private GasTank() {
    }

    /**
     * Creates a tank with a defined capacity.
     *
     * @param max - the maximum amount of gas this GasTank can hold
     */
    public GasTank(int max) {
        maxGas = max;
    }

    /**
     * Returns the tank stored in the defined tag compound, or null if it doesn't exist.
     *
     * @param nbtTags - tag compound to read from
     *
     * @return tank stored in the tag compound
     */
    @Nullable
    public static GasTank readFromNBT(CompoundNBT nbtTags) {
        if (nbtTags == null || nbtTags.isEmpty()) {
            return null;
        }
        GasTank tank = new GasTank();
        tank.read(nbtTags);
        return tank;
    }

    /**
     * Draws a specified amount of gas out of this tank.
     *
     * @param amount - amount to draw
     * @param doDraw - if the gas should actually be removed from this tank
     *
     * @return gas taken from this GasTank as a GasStack value
     */
    @Nonnull
    public GasStack draw(int amount, boolean doDraw) {
        //TODO: Replace things like doDraw with InputAction based off of FluidAction
        if (stored.isEmpty()) {
            return GasStack.EMPTY;
        }

        GasStack ret = new GasStack(stored, Math.min(getStored(), amount));
        if (!ret.isEmpty() && doDraw) {
            stored.shrink(ret.getAmount());
        }
        return ret;
    }

    /**
     * Adds a specified amount of gas to this tank.
     *
     * @param amount    - the GasStack for this tank to receive
     * @param doReceive - if the gas should actually be added to this tank
     *
     * @return the amount of gas accepted by this tank
     */
    public int receive(@Nonnull GasStack amount, boolean doReceive) {
        if (amount.isEmpty() || !stored.isGasEqual(amount)) {
            return 0;
        }
        int toFill = Math.min(getNeeded(), amount.getAmount());
        if (doReceive) {
            if (stored.isEmpty()) {
                stored = new GasStack(amount, toFill);
            } else {
                stored.grow(toFill);
            }
        }
        return toFill;
    }

    /**
     * If this GasTank can receive the specified type of gas. Will return false if this tank does not need anymore gas.
     *
     * @param gas - gas to check
     *
     * @return if this GasTank can accept the defined gas
     */
    public boolean canReceive(@Nonnull Gas gas) {
        return getNeeded() > 0 && canReceiveType(gas);
    }

    /**
     * If this GasTank can receive the specified type of gas. Will return TRUE if this tank does not need anymore gas.
     *
     * @param gas - gas to check
     *
     * @return if this GasTank can accept the defined gas
     */
    public boolean canReceiveType(@Nonnull Gas gas) {
        return stored.isEmpty() || gas == MekanismAPI.EMPTY_GAS || stored.isGasEqual(gas);
    }

    /**
     * If this GasTank can be drawn of the specified type of gas. Will return false if this tank does not contain any gas.
     *
     * @param gas - gas to check
     *
     * @return if this GasTank can be drawn of the defined gas
     */
    public boolean canDraw(@Nonnull Gas gas) {
        return !stored.isEmpty() && stored.isGasEqual(gas);
    }

    /**
     * Gets the amount of gas needed by this GasTank.
     *
     * @return Amount of gas needed
     */
    public int getNeeded() {
        return getMaxGas() - getStored();
    }

    /**
     * Gets the maximum amount of gas this tank can hold.
     *
     * @return - max gas
     */
    @Override
    public int getMaxGas() {
        return maxGas;
    }

    /**
     * Sets the maximum amount of gas this tank can hold
     */
    public void setMaxGas(int capacity) {
        maxGas = capacity;
    }

    /**
     * Gets the GasStack held by this GasTank.
     *
     * @return - GasStack held by this tank
     */
    //TODO: Should we make sure this is not a valid entry point for modifying the tank's contents? (Just to ensure that the max gas gets obeyed)
    @Nonnull
    @Override
    public GasStack getGas() {
        return stored;
    }

    /**
     * Sets this tank's GasStack value to a new value. Will cap the amount to this GasTank's capacity.
     *
     * @param stack - value to set this tank's GasStack value to
     */
    public void setGas(@Nonnull GasStack stack) {
        stored = stack;
        if (!stored.isEmpty()) {
            stored.setAmount(Math.min(getMaxGas(), stored.getAmount()));
        }
    }

    /**
     * Gets the type of gas currently stored in this GasTank.
     *
     * @return gas type contained
     */
    @Nonnull
    public Gas getGasType() {
        return stored.getGas();
    }

    /**
     * Gets the amount of gas stored by this GasTank.
     *
     * @return amount of gas stored
     */
    @Override
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
        nbtTags.putInt("maxGas", maxGas);
        return nbtTags;
    }

    /**
     * Reads this tank's data from a defined tag compound.
     *
     * @param nbtTags - tag compound to read from
     */
    public void read(CompoundNBT nbtTags) {
        if (nbtTags.contains("stored")) {
            stored = GasStack.readFromNBT(nbtTags.getCompound("stored"));
        }
        //todo: this is weird, remove in v10?
        if (nbtTags.contains("maxGas") && nbtTags.getInt("maxGas") != 0) {
            maxGas = nbtTags.getInt("maxGas");
        }
    }
}
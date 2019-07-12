package mekanism.api.gas;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.nbt.NBTTagCompound;

/**
 * GasStack - a specified amount of a defined Gas with certain properties.
 *
 * @author aidancbrady
 */
public class GasStack {

    public int amount;

    @Nonnull
    private Gas type;

    /**
     * Creates a new GasStack with a defined gas ID and quantity.
     *
     * @param id       - gas ID to associate this GasStack to, will perform a GasRegistry lookup in the constructor
     * @param quantity - amount of gas to be referenced in this GasStack
     */
    public GasStack(int id, int quantity) {
        type = Objects.requireNonNull(GasRegistry.getGas(id));
        amount = quantity;
    }

    /**
     * Creates a new GasStack with a defined Gas type and quantity.
     *
     * @param gas      - gas type of the stack
     * @param quantity - amount of gas to be referenced in this GasStack
     */
    public GasStack(@Nonnull Gas gas, int quantity) {
        type = Objects.requireNonNull(gas);
        amount = quantity;
    }

    private GasStack() {
    }

    /**
     * Returns the GasStack stored in the defined tag compound, or null if it doesn't exist.
     *
     * @param nbtTags - tag compound to read from
     *
     * @return GasStack stored in the tag compound
     */
    @Nullable
    public static GasStack readFromNBT(NBTTagCompound nbtTags) {
        if (nbtTags == null || nbtTags.isEmpty()) {
            return null;
        }

        GasStack stack = new GasStack();
        stack.read(nbtTags);

        if (stack.getGas() == null || stack.amount <= 0) {
            return null;
        }

        return stack;
    }

    /**
     * Gets the Gas type of this GasStack.
     *
     * @return this GasStack's Gas type
     */
    public Gas getGas() {
        return type;
    }

    public GasStack withAmount(int newAmount) {
        amount = newAmount;
        return this;
    }

    /**
     * Writes this GasStack to a defined tag compound.
     *
     * @param nbtTags - tag compound to write to
     *
     * @return tag compound with this GasStack's data
     */
    public NBTTagCompound write(NBTTagCompound nbtTags) {
        type.write(nbtTags);
        nbtTags.setInteger("amount", amount);

        return nbtTags;
    }

    /**
     * Reads this GasStack's data from a defined tag compound.
     *
     * @param nbtTags - tag compound to read from
     */
    public void read(NBTTagCompound nbtTags) {
        type = Gas.readFromNBT(nbtTags);
        amount = nbtTags.getInteger("amount");
    }

    /**
     * Returns a copied form of this GasStack.
     *
     * @return copied GasStack
     */
    public GasStack copy() {
        return new GasStack(type, amount);
    }

    /**
     * Whether or not this GasStack's gas type is equal to the other defined GasStack.
     *
     * @param stack - GasStack to check
     *
     * @return if the GasStacks contain the same gas type
     */
    public boolean isGasEqual(GasStack stack) {
        return stack != null && getGas() == stack.getGas();
    }

    @Override
    public String toString() {
        return "[" + type + ", " + amount + "]";
    }

    @Override
    public int hashCode() {
        return type == null ? 0 : type.getID();
    }
}
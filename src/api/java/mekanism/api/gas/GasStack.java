package mekanism.api.gas;

import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import mekanism.api.providers.IGasProvider;
import mekanism.api.text.IHasTranslationKey;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.registries.IRegistryDelegate;

/**
 * GasStack - a specified amount of a defined Gas with certain properties.
 *
 * @author aidancbrady
 */
public class GasStack implements IHasTranslationKey {

    public static final GasStack EMPTY = new GasStack(MekanismAPI.EMPTY_GAS, 0);

    private boolean isEmpty;
    private int amount;
    private IRegistryDelegate<Gas> gasDelegate;

    /**
     * Creates a new GasStack with a defined Gas type and quantity.
     *
     * @param gasProvider - provides the gas type of the stack
     * @param amount      - amount of gas to be referenced in this GasStack
     */
    //TODO: Get rid of uses of this that are just stack.getGas() and use below helper method, to make it so if we ever add NBT or other stuff
    // it can copy it easier
    public GasStack(@Nonnull IGasProvider gasProvider, int amount) {
        Gas gas = gasProvider.getGas();
        if (MekanismAPI.GAS_REGISTRY.getKey(gas) == null) {
            MekanismAPI.logger.fatal("Failed attempt to create a GasStack for an unregistered Gas {} (type {})", gas.getRegistryName(), gas.getClass().getName());
            throw new IllegalArgumentException("Cannot create a GasStack from an unregistered gas");
        }
        this.gasDelegate = gas.delegate;
        this.amount = amount;
        updateEmpty();
    }

    public GasStack(@Nonnull GasStack stack, int amount) {
        this(stack.getGas(), amount);
    }

    /**
     * Returns the GasStack stored in the defined tag compound, or null if it doesn't exist.
     *
     * @param nbtTags - tag compound to read from
     *
     * @return GasStack stored in the tag compound
     */
    @Nonnull
    public static GasStack readFromNBT(CompoundNBT nbtTags) {
        if (nbtTags == null || nbtTags.isEmpty()) {
            return EMPTY;
        }
        Gas type = Gas.readFromNBT(nbtTags);
        if (type == MekanismAPI.EMPTY_GAS) {
            return EMPTY;
        }
        int amount = nbtTags.getInt("amount");
        if (amount <= 0) {
            return EMPTY;
        }
        return new GasStack(type, amount);
    }

    /**
     * Gets the Gas type of this GasStack.
     *
     * @return this GasStack's Gas type
     */
    @Nonnull
    public final Gas getGas() {
        return isEmpty ? MekanismAPI.EMPTY_GAS : getRawGas();
    }

    //TODO: Remove this
    @Deprecated
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
    public CompoundNBT write(CompoundNBT nbtTags) {
        getGas().write(nbtTags);
        nbtTags.putInt("amount", amount);
        return nbtTags;
    }

    public void writeToPacket(PacketBuffer buf) {
        buf.writeRegistryId(getGas());
        buf.writeVarInt(getAmount());
    }

    public static GasStack readFromPacket(PacketBuffer buf) {
        Gas gas = buf.readRegistryId();
        int amount = buf.readVarInt();
        if (gas == MekanismAPI.EMPTY_GAS) {
            return EMPTY;
        }
        return new GasStack(gas, amount);
    }

    /**
     * Returns a copied form of this GasStack.
     *
     * @return copied GasStack
     */
    public GasStack copy() {
        return new GasStack(this::getGas, amount);
    }

    /**
     * Whether or not this GasStack's gas type is equal to the other defined GasStack.
     *
     * @param stack - GasStack to check
     *
     * @return if the GasStacks contain the same gas type
     */
    //TODO: Use this in places we compare manually
    public boolean isTypeEqual(@Nonnull GasStack stack) {
        return isTypeEqual(stack.getGas());
    }

    public boolean isTypeEqual(@Nonnull Gas gas) {
        return getGas() == gas;
    }

    public final Gas getRawGas() {
        return gasDelegate.get();
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    protected void updateEmpty() {
        isEmpty = getRawGas() == MekanismAPI.EMPTY_GAS || amount <= 0;
    }

    public int getAmount() {
        return isEmpty ? 0 : amount;
    }

    public void setAmount(int amount) {
        if (getRawGas() == MekanismAPI.EMPTY_GAS) {
            throw new IllegalStateException("Can't modify the empty stack.");
        }
        this.amount = amount;
        updateEmpty();
    }

    public void grow(int amount) {
        setAmount(this.amount + amount);
    }

    public void shrink(int amount) {
        setAmount(this.amount - amount);
    }

    @Override
    public String toString() {
        return "[" + getGas() + ", " + amount + "]";
    }

    public ITextComponent getDisplayName() {
        //Wrapper to get display name of the gas type easier
        return getGas().getDisplayName();
    }

    @Override
    public String getTranslationKey() {
        //Wrapper to get translation key of the gas type easier
        return getGas().getTranslationKey();
    }

    /**
     * Determines if the Gases are equal and this stack is larger.
     *
     * @return true if this GasStack contains the other GasStack (same gas and >= amount)
     */
    public boolean containsGas(@Nonnull GasStack other) {
        return isTypeEqual(other) && amount >= other.amount;
    }

    /**
     * Determines if the gases and amounts are all equal.
     *
     * @param other - the GasStack for comparison
     *
     * @return true if the two GasStacks are exactly the same
     */
    public boolean isGasStackIdentical(GasStack other) {
        return isTypeEqual(other) && amount == other.amount;
    }

    //TODO: Method to check gas in an itemstack (capabilities instead of IGasItem)

    @Override
    public final int hashCode() {
        int code = 1;
        code = 31 * code + getGas().hashCode();
        code = 31 * code + amount;
        return code;
    }

    /**
     * Default equality comparison for a GasStack. Same functionality as isGasEqual().
     *
     * This is included for use in data structures.
     */
    //TODO: Is this a problem that it does not check size
    @Override
    public final boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof GasStack) {
            return isTypeEqual((GasStack) o);
        }
        return false;
    }
}
package mekanism.api.chemical.gas;

import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.providers.IGasProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.registries.IRegistryDelegate;

/**
 * GasStack - a specified amount of a defined Gas with certain properties.
 *
 * @author aidancbrady
 */
public class GasStack extends ChemicalStack<Gas> {

    public static final GasStack EMPTY = new GasStack(MekanismAPI.EMPTY_GAS, 0);

    /**
     * Creates a new GasStack with a defined Gas type and quantity.
     *
     * @param gasProvider - provides the gas type of the stack
     * @param amount      - amount of gas to be referenced in this GasStack
     */
    //TODO: Get rid of uses of this that are just stack.getGas() and use below helper method, to make it so if we ever add NBT or other stuff
    // it can copy it easier
    public GasStack(@Nonnull IGasProvider gasProvider, int amount) {
        super(gasProvider.getGas(), amount);
    }

    public GasStack(@Nonnull GasStack stack, int amount) {
        this(stack.getType(), amount);
    }

    @Nonnull
    @Override
    protected IRegistryDelegate<Gas> getDelegate(Gas gas) {
        if (MekanismAPI.GAS_REGISTRY.getKey(gas) == null) {
            MekanismAPI.logger.fatal("Failed attempt to create a GasStack for an unregistered Gas {} (type {})", gas.getRegistryName(), gas.getClass().getName());
            throw new IllegalArgumentException("Cannot create a GasStack from an unregistered gas");
        }
        return gas.delegate;
    }

    @Nonnull
    @Override
    protected Gas getEmptyChemical() {
        return MekanismAPI.EMPTY_GAS;
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
        if (type.isEmptyType()) {
            return EMPTY;
        }
        int amount = nbtTags.getInt(NBTConstants.AMOUNT);
        if (amount <= 0) {
            return EMPTY;
        }
        return new GasStack(type, amount);
    }

    public static GasStack readFromPacket(PacketBuffer buf) {
        Gas gas = buf.readRegistryId();
        int amount = buf.readVarInt();
        if (gas.isEmptyType()) {
            return EMPTY;
        }
        return new GasStack(gas, amount);
    }

    /**
     * Returns a copied form of this GasStack.
     *
     * @return copied GasStack
     */
    @Nonnull
    @Override
    public GasStack copy() {
        return new GasStack(this, getAmount());
    }

    //TODO: Method to check gas in an itemstack (capabilities instead of IGasItem)

    @Override
    public int hashCode() {
        int code = 1;
        code = 31 * code + getType().hashCode();
        code = 31 * code + getAmount();
        return code;
    }

    /**
     * Default equality comparison for a GasStack. Same functionality as isGasEqual().
     *
     * This is included for use in data structures.
     */
    //TODO: Is this a problem that it does not check size
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof GasStack) {
            return isTypeEqual((GasStack) o);
        }
        return false;
    }
}
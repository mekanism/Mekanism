package mekanism.api.chemical.gas;

import mekanism.api.MekanismAPI;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.providers.IGasProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

/**
 * GasStack - a specified amount of a defined Gas with certain properties.
 *
 * @author aidancbrady
 */
@NothingNullByDefault
public final class GasStack extends ChemicalStack<Gas> {

    /**
     * Empty GasStack instance.
     */
    public static final GasStack EMPTY = new GasStack(MekanismAPI.EMPTY_GAS, 0);

    /**
     * Creates a new GasStack with a defined Gas type and quantity.
     *
     * @param gasProvider - provides the gas type of the stack
     * @param amount      - amount of gas to be referenced in this GasStack
     */
    public GasStack(IGasProvider gasProvider, long amount) {
        super(gasProvider.getChemical(), amount);
    }

    /**
     * Creates a new GasStack with a defined Gas type and quantity.
     *
     * @param gasHolder - provides the gas type of the stack
     * @param amount    - amount of gas to be referenced in this GasStack
     *
     * @since 10.5.0
     */
    public GasStack(Holder<Gas> gasHolder, long amount) {
        this(gasHolder.value(), amount);
    }

    public GasStack(GasStack stack, long amount) {
        this(stack.getType(), amount);
    }

    @Override
    protected Registry<Gas> getRegistry() {
        return MekanismAPI.GAS_REGISTRY;
    }

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
    public static GasStack readFromNBT(@Nullable CompoundTag nbtTags) {
        if (nbtTags == null || nbtTags.isEmpty()) {
            return EMPTY;
        }
        Gas type = Gas.readFromNBT(nbtTags);
        if (type.isEmptyType()) {
            return EMPTY;
        }
        long amount = nbtTags.getLong(NBTConstants.AMOUNT);
        if (amount <= 0) {
            return EMPTY;
        }
        return new GasStack(type, amount);
    }

    public static GasStack readFromPacket(FriendlyByteBuf buf) {
        Gas gas = buf.readById(MekanismAPI.GAS_REGISTRY::byId);
        if (gas == null || gas.isEmptyType()) {
            return EMPTY;
        }
        return new GasStack(gas, buf.readVarLong());
    }

    /**
     * Returns a copied form of this GasStack.
     *
     * @return copied GasStack
     */
    @Override
    public GasStack copy() {
        if (isEmpty()) {
            return EMPTY;
        }
        return new GasStack(this, getAmount());
    }
}
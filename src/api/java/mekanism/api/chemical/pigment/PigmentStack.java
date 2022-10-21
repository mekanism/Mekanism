package mekanism.api.chemical.pigment;

import mekanism.api.MekanismAPI;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.providers.IPigmentProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public final class PigmentStack extends ChemicalStack<Pigment> {

    /**
     * Empty PigmentStack instance.
     */
    public static final PigmentStack EMPTY = new PigmentStack(MekanismAPI.EMPTY_PIGMENT, 0);

    /**
     * Creates a new PigmentStack with a defined pigment type and quantity.
     *
     * @param pigmentProvider - provides the pigment type of the stack
     * @param amount          - amount of the pigment to be referenced in this PigmentStack
     */
    public PigmentStack(IPigmentProvider pigmentProvider, long amount) {
        super(pigmentProvider.getChemical(), amount);
    }

    public PigmentStack(PigmentStack stack, long amount) {
        this(stack.getType(), amount);
    }

    @Override
    protected IForgeRegistry<Pigment> getRegistry() {
        return MekanismAPI.pigmentRegistry();
    }

    @Override
    protected Pigment getEmptyChemical() {
        return MekanismAPI.EMPTY_PIGMENT;
    }

    /**
     * Returns the PigmentStack stored in the defined tag compound, or null if it doesn't exist.
     *
     * @param nbtTags - tag compound to read from
     *
     * @return PigmentStack stored in the tag compound
     */
    public static PigmentStack readFromNBT(@Nullable CompoundTag nbtTags) {
        if (nbtTags == null || nbtTags.isEmpty()) {
            return EMPTY;
        }
        Pigment type = Pigment.readFromNBT(nbtTags);
        if (type.isEmptyType()) {
            return EMPTY;
        }
        long amount = nbtTags.getLong(NBTConstants.AMOUNT);
        if (amount <= 0) {
            return EMPTY;
        }
        return new PigmentStack(type, amount);
    }

    public static PigmentStack readFromPacket(FriendlyByteBuf buf) {
        Pigment pigment = buf.readRegistryIdSafe(Pigment.class);
        if (pigment.isEmptyType()) {
            return EMPTY;
        }
        return new PigmentStack(pigment, buf.readVarLong());
    }

    /**
     * Returns a copied form of this PigmentStack.
     *
     * @return copied PigmentStack
     */
    @Override
    public PigmentStack copy() {
        if (isEmpty()) {
            return EMPTY;
        }
        return new PigmentStack(this, getAmount());
    }
}
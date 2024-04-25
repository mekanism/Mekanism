package mekanism.api.chemical.slurry;

import mekanism.api.MekanismAPI;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.providers.ISlurryProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public final class SlurryStack extends ChemicalStack<Slurry> {

    /**
     * Empty SlurryStack instance.
     */
    public static final SlurryStack EMPTY = new SlurryStack(MekanismAPI.EMPTY_SLURRY, 0);

    /**
     * Creates a new SlurryStack with a defined slurry type and quantity.
     *
     * @param slurryProvider - provides the slurry type of the stack
     * @param amount         - amount of the slurry to be referenced in this SlurryStack
     */
    public SlurryStack(ISlurryProvider slurryProvider, long amount) {
        super(slurryProvider.getChemical(), amount);
    }

    /**
     * Creates a new SlurryStack with a defined slurry type and quantity.
     *
     * @param slurryHolder - provides the slurry type of the stack
     * @param amount       - amount of the slurry to be referenced in this SlurryStack
     *
     * @since 10.5.0
     */
    public SlurryStack(Holder<Slurry> slurryHolder, long amount) {
        this(slurryHolder.value(), amount);
    }

    public SlurryStack(SlurryStack stack, long amount) {
        this(stack.getType(), amount);
    }

    @Override
    protected Registry<Slurry> getRegistry() {
        return MekanismAPI.SLURRY_REGISTRY;
    }

    @Override
    protected Slurry getEmptyChemical() {
        return MekanismAPI.EMPTY_SLURRY;
    }

    /**
     * Returns the SlurryStack stored in the defined tag compound, or null if it doesn't exist.
     *
     * @param nbtTags - tag compound to read from
     *
     * @return SlurryStack stored in the tag compound
     */
    public static SlurryStack readFromNBT(@Nullable CompoundTag nbtTags) {
        if (nbtTags == null || nbtTags.isEmpty()) {
            return EMPTY;
        }
        Slurry type = Slurry.readFromNBT(nbtTags);
        if (type.isEmptyType()) {
            return EMPTY;
        }
        long amount = nbtTags.getLong(NBTConstants.AMOUNT);
        if (amount <= 0) {
            return EMPTY;
        }
        return new SlurryStack(type, amount);
    }

    public static SlurryStack readFromPacket(FriendlyByteBuf buf) {
        Slurry slurry = buf.readById(MekanismAPI.SLURRY_REGISTRY::byId);
        if (slurry == null || slurry.isEmptyType()) {
            return EMPTY;
        }
        return new SlurryStack(slurry, buf.readVarLong());
    }

    /**
     * Returns a copied form of this SlurryStack.
     *
     * @return copied SlurryStack
     */
    @Override
    public SlurryStack copy() {
        if (isEmpty()) {
            return EMPTY;
        }
        return new SlurryStack(this, getAmount());
    }
}
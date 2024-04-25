package mekanism.api.chemical.infuse;

import mekanism.api.MekanismAPI;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.providers.IInfuseTypeProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public final class InfusionStack extends ChemicalStack<InfuseType> {

    /**
     * Empty InfusionStack instance.
     */
    public static final InfusionStack EMPTY = new InfusionStack(MekanismAPI.EMPTY_INFUSE_TYPE, 0);

    /**
     * Creates a new InfusionStack with a defined infusion type and quantity.
     *
     * @param infuseTypeProvider - provides the infusion type of the stack
     * @param amount             - amount of the infusion type to be referenced in this InfusionStack
     */
    public InfusionStack(IInfuseTypeProvider infuseTypeProvider, long amount) {
        super(infuseTypeProvider.getChemical(), amount);
    }

    /**
     * Creates a new InfusionStack with a defined infusion type and quantity.
     *
     * @param infuseTypeHolder - provides the infusion type of the stack
     * @param amount           - amount of the infusion type to be referenced in this InfusionStack
     *
     * @since 10.5.0
     */
    public InfusionStack(Holder<InfuseType> infuseTypeHolder, long amount) {
        this(infuseTypeHolder.value(), amount);
    }

    public InfusionStack(InfusionStack stack, long amount) {
        this(stack.getType(), amount);
    }

    @Override
    protected Registry<InfuseType> getRegistry() {
        return MekanismAPI.INFUSE_TYPE_REGISTRY;
    }

    @Override
    protected InfuseType getEmptyChemical() {
        return MekanismAPI.EMPTY_INFUSE_TYPE;
    }

    /**
     * Returns the InfusionStack stored in the defined tag compound, or null if it doesn't exist.
     *
     * @param nbtTags - tag compound to read from
     *
     * @return InfusionStack stored in the tag compound
     */
    public static InfusionStack readFromNBT(@Nullable CompoundTag nbtTags) {
        if (nbtTags == null || nbtTags.isEmpty()) {
            return EMPTY;
        }
        InfuseType type = InfuseType.readFromNBT(nbtTags);
        if (type.isEmptyType()) {
            return EMPTY;
        }
        long amount = nbtTags.getLong(NBTConstants.AMOUNT);
        if (amount <= 0) {
            return EMPTY;
        }
        return new InfusionStack(type, amount);
    }

    public static InfusionStack readFromPacket(FriendlyByteBuf buf) {
        InfuseType infuseType = buf.readById(MekanismAPI.INFUSE_TYPE_REGISTRY::byId);
        if (infuseType == null || infuseType.isEmptyType()) {
            return EMPTY;
        }
        return new InfusionStack(infuseType, buf.readVarLong());
    }

    /**
     * Returns a copied form of this InfusionStack.
     *
     * @return copied InfusionStack
     */
    @Override
    public InfusionStack copy() {
        if (isEmpty()) {
            return EMPTY;
        }
        return new InfusionStack(this, getAmount());
    }
}
package mekanism.api.chemical.infuse;

import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.providers.IInfuseTypeProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.registries.IRegistryDelegate;

/**
 * InfuseObject - an object associated with an ItemStack that can modify a Metallurgic Infuser's internal infuse.
 *
 * @author AidanBrady
 */
public class InfusionStack extends ChemicalStack<InfuseType> {

    public static final InfusionStack EMPTY = new InfusionStack(MekanismAPI.EMPTY_INFUSE_TYPE, 0);

    /**
     * Creates a new InfusionStack with a defined infusion type and quantity.
     *
     * @param infuseTypeProvider - provides the infusion type of the stack
     * @param amount             - amount of the infusion type to be referenced in this InfusionStack
     */
    public InfusionStack(@Nonnull IInfuseTypeProvider infuseTypeProvider, long amount) {
        super(infuseTypeProvider.getInfuseType(), amount);
    }

    public InfusionStack(@Nonnull InfusionStack stack, long amount) {
        this(stack.getType(), amount);
    }

    @Nonnull
    @Override
    protected IRegistryDelegate<InfuseType> getDelegate(InfuseType infuseType) {
        if (MekanismAPI.INFUSE_TYPE_REGISTRY.getKey(infuseType) == null) {
            MekanismAPI.logger.fatal("Failed attempt to create a InfusionStack for an unregistered InfuseType {} (type {})", infuseType.getRegistryName(),
                  infuseType.getClass().getName());
            throw new IllegalArgumentException("Cannot create a InfusionStack from an unregistered infusion type");
        }
        return infuseType.delegate;
    }

    @Nonnull
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
    @Nonnull
    public static InfusionStack readFromNBT(CompoundNBT nbtTags) {
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

    public static InfusionStack readFromPacket(PacketBuffer buf) {
        InfuseType infuseType = buf.readRegistryId();
        long amount = buf.readVarLong();
        if (infuseType.isEmptyType()) {
            return EMPTY;
        }
        return new InfusionStack(infuseType, amount);
    }

    /**
     * Returns a copied form of this InfusionStack.
     *
     * @return copied InfusionStack
     */
    @Nonnull
    @Override
    public InfusionStack copy() {
        return new InfusionStack(this, getAmount());
    }

    /**
     * Default equality comparison for a InfusionStack. Same functionality as isInfusionEqual().
     *
     * This is included for use in data structures.
     */
    @Override
    public final boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof InfusionStack) {
            return isTypeEqual((InfusionStack) o);
        }
        return false;
    }
}
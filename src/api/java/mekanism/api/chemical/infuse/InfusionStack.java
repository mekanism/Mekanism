package mekanism.api.chemical.infuse;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.MekanismAPI;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.providers.IInfuseTypeProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.registries.IRegistryDelegate;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class InfusionStack extends ChemicalStack<InfuseType> {

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

    public InfusionStack(InfusionStack stack, long amount) {
        this(stack.getType(), amount);
    }

    @Override
    protected IRegistryDelegate<InfuseType> getDelegate(InfuseType infuseType) {
        if (MekanismAPI.infuseTypeRegistry().getKey(infuseType) == null) {
            MekanismAPI.logger.fatal("Failed attempt to create a InfusionStack for an unregistered InfuseType {} (type {})", infuseType.getRegistryName(),
                  infuseType.getClass().getName());
            throw new IllegalArgumentException("Cannot create a InfusionStack from an unregistered infusion type");
        }
        return infuseType.delegate;
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
    public static InfusionStack readFromNBT(@Nullable CompoundNBT nbtTags) {
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
    @Override
    public InfusionStack copy() {
        return new InfusionStack(this, getAmount());
    }
}
package mekanism.api.infuse;

import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import mekanism.api.providers.IInfuseTypeProvider;
import mekanism.api.text.IHasTranslationKey;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.registries.IRegistryDelegate;

/**
 * InfuseObject - an object associated with an ItemStack that can modify a Metallurgic Infuser's internal infuse.
 *
 * @author AidanBrady
 */
//TODO: Rename to InfuseStack and make the variables not be public
public class InfusionStack implements InfusionContainer, IHasTranslationKey {

    public static final InfusionStack EMPTY = new InfusionStack(MekanismAPI.EMPTY_INFUSE_TYPE, 0);

    private boolean isEmpty;
    private int amount;
    private IRegistryDelegate<InfuseType> infusionDelegate;

    /**
     * Creates a new InfusionStack with a defined infusion type and quantity.
     *
     * @param infuseTypeProvider - provides the infusion type of the stack
     * @param amount             - amount of the infusion type to be referenced in this InfusionStack
     */
    public InfusionStack(@Nonnull IInfuseTypeProvider infuseTypeProvider, int amount) {
        InfuseType infuseType = infuseTypeProvider.getInfuseType();
        if (MekanismAPI.INFUSE_TYPE_REGISTRY.getKey(infuseType) == null) {
            MekanismAPI.logger.fatal("Failed attempt to create a InfusionStack for an unregistered InfuseType {} (type {})", infuseType.getRegistryName(),
                  infuseType.getClass().getName());
            throw new IllegalArgumentException("Cannot create a InfusionStack from an unregistered infusion type");
        }
        this.infusionDelegate = infuseType.delegate;
        this.amount = amount;
        updateEmpty();
    }

    public InfusionStack(@Nonnull InfusionStack stack, int amount) {
        this(stack.getType(), amount);
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
        if (type == MekanismAPI.EMPTY_INFUSE_TYPE) {
            return EMPTY;
        }
        int amount = nbtTags.getInt("amount");
        if (amount <= 0) {
            return EMPTY;
        }
        return new InfusionStack(type, amount);
    }

    /**
     * Gets the InfuseType type of this InfusionStack.
     *
     * @return this InfusionStack's InfuseType type
     */
    @Nonnull
    @Override
    public final InfuseType getType() {
        return isEmpty ? MekanismAPI.EMPTY_INFUSE_TYPE : getRawType();
    }

    /**
     * Writes this InfusionStack to a defined tag compound.
     *
     * @param nbtTags - tag compound to write to
     *
     * @return tag compound with this InfusionStack's data
     */
    public CompoundNBT write(CompoundNBT nbtTags) {
        getType().write(nbtTags);
        nbtTags.putInt("amount", amount);
        return nbtTags;
    }

    public void writeToPacket(PacketBuffer buf) {
        buf.writeRegistryId(getType());
        buf.writeVarInt(getAmount());
    }

    public static InfusionStack readFromPacket(PacketBuffer buf) {
        InfuseType infuseType = buf.readRegistryId();
        int amount = buf.readVarInt();
        if (infuseType == MekanismAPI.EMPTY_INFUSE_TYPE) {
            return EMPTY;
        }
        return new InfusionStack(infuseType, amount);
    }

    /**
     * Returns a copied form of this InfusionStack.
     *
     * @return copied InfusionStack
     */
    public InfusionStack copy() {
        return new InfusionStack(this::getType, amount);
    }

    /**
     * Whether or not this InfusionStack's infusion type is equal to the other defined InfusionStack.
     *
     * @param stack - InfusionStack to check
     *
     * @return if the InfusionStacks contain the same infusion type
     */
    public boolean isInfusionEqual(@Nonnull InfusionStack stack) {
        return isInfusionEqual(stack.getType());
    }

    //TODO: Rename to isTypeEqual??
    public boolean isInfusionEqual(@Nonnull InfuseType infuseType) {
        return getType() == infuseType;
    }

    public final InfuseType getRawType() {
        return infusionDelegate.get();
    }

    @Override
    public boolean isEmpty() {
        return isEmpty;
    }

    protected void updateEmpty() {
        isEmpty = getRawType() == MekanismAPI.EMPTY_INFUSE_TYPE || amount <= 0;
    }

    @Override
    public int getAmount() {
        return isEmpty ? 0 : amount;
    }

    public void setAmount(int amount) {
        if (getRawType() == MekanismAPI.EMPTY_INFUSE_TYPE) {
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
        return "[" + getType() + ", " + amount + "]";
    }

    public ITextComponent getDisplayName() {
        //Wrapper to get display name of the infusion type easier
        return getType().getDisplayName();
    }

    @Override
    public String getTranslationKey() {
        //Wrapper to get translation key of the infusion type easier
        return getType().getTranslationKey();
    }

    /**
     * Determines if the infusion types are equal and this stack is larger.
     *
     * @return true if this InfusionStack contains the other InfusionStack (same infusion type and >= amount)
     */
    public boolean containsInfusion(@Nonnull InfusionStack other) {
        return isInfusionEqual(other) && amount >= other.amount;
    }

    /**
     * Determines if the infusion types and amounts are all equal.
     *
     * @param other - the InfusionStack for comparison
     *
     * @return true if the two InfusionStacks are exactly the same
     */
    public boolean isInfusionStackIdentical(InfusionStack other) {
        return isInfusionEqual(other) && amount == other.amount;
    }

    //TODO: Method to check infuse type an itemstack can produce/stores? Could allow for a "tank" that can store infusion

    @Override
    public final int hashCode() {
        int code = 1;
        code = 31 * code + getType().hashCode();
        code = 31 * code + amount;
        return code;
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
            return isInfusionEqual((InfusionStack) o);
        }
        return false;
    }
}
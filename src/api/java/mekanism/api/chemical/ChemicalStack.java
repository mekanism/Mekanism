package mekanism.api.chemical;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.Objects;
import java.util.function.BiFunction;
import mekanism.api.JsonConstants;
import mekanism.api.NBTConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import mekanism.api.chemical.attribute.IChemicalAttributeContainer;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.IHasTranslationKey;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class ChemicalStack<CHEMICAL extends Chemical<CHEMICAL>> implements IHasTextComponent, IHasTranslationKey, IChemicalAttributeContainer<ChemicalStack<CHEMICAL>> {

    protected static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> MapCodec<STACK> codec(Codec<CHEMICAL> chemicalCodec, String chemicalField,
          BiFunction<CHEMICAL, Long, STACK> constructor) {
        return RecordCodecBuilder.mapCodec(i -> i.group(
              chemicalCodec.fieldOf(chemicalField).forGetter(ChemicalStack::getRaw),
              SerializerHelper.POSITIVE_LONG_CODEC.fieldOf(JsonConstants.AMOUNT).forGetter(ChemicalStack::getAmount)
        ).apply(i, constructor));
    }

    private boolean isEmpty;
    private long amount;
    private final CHEMICAL chemical;

    protected ChemicalStack(CHEMICAL chemical, long amount) {
        Objects.requireNonNull(chemical, "Cannot create a ChemicalStack from a null chemical");
        //TODO - 1.20.5: Update based on changes to FluidStack to mirror them more appropriately
        /*if (!getRegistry().containsValue(chemical)) {
            throw new IllegalArgumentException("Cannot create a ChemicalStack from an unregistered Chemical");
        }*/
        this.chemical = chemical;
        this.amount = amount;
        updateEmpty();
    }

    /**
     * Registry the chemical is a part of.
     */
    protected abstract Registry<CHEMICAL> getRegistry();

    /**
     * Helper ot get the empty version of this chemical.
     */
    protected abstract CHEMICAL getEmptyChemical();

    /**
     * Copies this chemical stack into a new chemical stack.
     */
    public abstract ChemicalStack<CHEMICAL> copy();

    /**
     * Gets the chemical represented by this stack.
     *
     * @return Backing chemical.
     */
    public final CHEMICAL getType() {
        return isEmpty ? getEmptyChemical() : getRaw();
    }

    /**
     * Whether this ChemicalStack's chemical type is equal to the other defined ChemicalStack.
     *
     * @param stack - ChemicalStack to check
     *
     * @return if the ChemicalStacks contain the same chemical type
     */
    public boolean isTypeEqual(ChemicalStack<CHEMICAL> stack) {
        return isTypeEqual(stack.getType());
    }

    /**
     * Whether this ChemicalStack's chemical type is equal to the other defined Chemical.
     *
     * @param chemical - Chemical to check
     *
     * @return if the ChemicalStack's type is the same as the given chemical
     */
    public boolean isTypeEqual(CHEMICAL chemical) {
        return getType() == chemical;
    }

    /**
     * Helper to retrieve the registry name of the stored chemical. This is equivalent to calling {@code getType().getRegistryName()}
     *
     * @return The registry name of the stored chemical.
     */
    public ResourceLocation getTypeRegistryName() {
        return getType().getRegistryName();
    }

    /**
     * Helper to get the tint of the stored chemical. This is equivalent to calling {@code getType().getTint()}
     *
     * @return The tint of the stored chemical.
     *
     * @apiNote Does not have any special handling for when the stack is empty.
     */
    public int getChemicalTint() {
        return getType().getTint();
    }

    /**
     * Helper to get the color representation of the stored chemical. This is equivalent to calling {@code getType().getColorRepresentation()} and is used for things like
     * durability bars of chemical tanks.
     *
     * @return The color representation of the stored chemical.
     *
     * @apiNote Does not have any special handling for when the stack is empty.
     */
    public int getChemicalColorRepresentation() {
        return getType().getColorRepresentation();
    }

    private CHEMICAL getRaw() {
        return chemical;
    }

    /**
     * Gets whether this chemical stack is empty.
     *
     * @return {@code true} if this stack is empty, {@code false} otherwise.
     */
    public boolean isEmpty() {
        return isEmpty;
    }

    protected void updateEmpty() {
        isEmpty = getRaw().isEmptyType() || amount <= 0;
    }

    /**
     * Gets the size of this chemical stack.
     *
     * @return The size of this chemical stack or zero if it is empty
     */
    public long getAmount() {
        return isEmpty ? 0 : amount;
    }

    /**
     * Sets this stack's amount to the given amount.
     *
     * @param amount The amount to set this stack's amount to.
     */
    public void setAmount(long amount) {
        if (getRaw().isEmptyType()) {
            throw new IllegalStateException("Can't modify the empty stack.");
        }
        this.amount = amount;
        updateEmpty();
    }

    /**
     * Grows this stack's amount by the given amount.
     *
     * @param amount The amount to grow this stack by.
     *
     * @apiNote Negative values are valid and will instead shrink the stack.
     * @implNote No checks are made to ensure that the long does not overflow.
     */
    public void grow(long amount) {
        setAmount(this.amount + amount);
    }

    /**
     * Shrinks this stack's amount by the given amount.
     *
     * @param amount The amount to shrink this stack by.
     *
     * @apiNote Negative values are valid and will instead grow the stack.
     * @implNote No checks are made to ensure that the long does not underflow.
     */
    public void shrink(long amount) {
        setAmount(this.amount - amount);
    }

    @Override
    public boolean has(Class<? extends ChemicalAttribute> type) {
        return getType().has(type);
    }

    /**
     * Helper to check if this chemical is radioactive without having to look it up from the attributes.
     *
     * @return {@code true} if this chemical is radioactive.
     *
     * @since 10.5.15
     */
    public boolean isRadioactive() {
        return getType().isRadioactive();
    }

    @Nullable
    @Override
    public <ATTRIBUTE extends ChemicalAttribute> ATTRIBUTE get(Class<ATTRIBUTE> type) {
        return getType().get(type);
    }

    @Override
    public Collection<ChemicalAttribute> getAttributes() {
        return getType().getAttributes();
    }

    @Override
    public Collection<Class<? extends ChemicalAttribute>> getAttributeTypes() {
        return getType().getAttributeTypes();
    }

    @Override
    public int hashCode() {
        int code = 1;
        code = 31 * code + getType().hashCode();
        code = 31 * code + Long.hashCode(getAmount());
        return code;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChemicalStack<?> other = (ChemicalStack<?>) o;
        return getType() == other.getType() && getAmount() == other.getAmount();
    }

    @Override
    public String toString() {
        return "[" + getType() + ", " + amount + "]";
    }

    @Override
    public Component getTextComponent() {
        //Wrapper to get display name of the chemical type easier
        return getType().getTextComponent();
    }

    @Override
    public String getTranslationKey() {
        //Wrapper to get translation key of the chemical type easier
        return getType().getTranslationKey();
    }

    /**
     * Determines if the Chemicals are equal and this stack is larger.
     *
     * @return true if this ChemicalStack contains the other ChemicalStack (same chemical and >= amount)
     */
    public boolean contains(ChemicalStack<CHEMICAL> other) {
        return isTypeEqual(other) && amount >= other.amount;
    }

    /**
     * Determines if the chemicals and amounts are all equal.
     *
     * @param other - the ChemicalStack for comparison
     *
     * @return true if the two ChemicalStacks are exactly the same
     */
    public boolean isStackIdentical(ChemicalStack<CHEMICAL> other) {
        return isTypeEqual(other) && amount == other.amount;
    }

    /**
     * Writes this ChemicalStack to a defined tag compound.
     *
     * @param nbtTags - tag compound to write to
     *
     * @return tag compound with this ChemicalStack's data
     */
    public CompoundTag write(CompoundTag nbtTags) {
        getType().write(nbtTags);
        nbtTags.putLong(NBTConstants.AMOUNT, getAmount());
        return nbtTags;
    }

    /**
     * Writes this ChemicalStack to a Packet Buffer.
     *
     * @param buffer - Buffer to write to.
     */
    public void writeToPacket(FriendlyByteBuf buffer) {
        buffer.writeById(getRegistry()::getId, getType());
        if (!isEmpty()) {
            buffer.writeVarLong(getAmount());
        }
    }
}
package mekanism.api.chemical;

import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.IHasTranslationKey;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.registries.IRegistryDelegate;

public abstract class ChemicalStack<CHEMICAL extends Chemical<CHEMICAL>> implements IHasTextComponent, IHasTranslationKey {

    private boolean isEmpty;
    private int amount;
    @Nonnull
    private IRegistryDelegate<CHEMICAL> chemicalDelegate;

    protected ChemicalStack(@Nonnull CHEMICAL chemical, int amount) {
        this.chemicalDelegate = getDelegate(chemical);
        this.amount = amount;
        updateEmpty();
    }

    /**
     * Used for checking the chemical is valid and registered.
     */
    @Nonnull
    protected abstract IRegistryDelegate<CHEMICAL> getDelegate(CHEMICAL chemical);

    //TODO: Is there a better way to make this super class know about the empty chemical instance and empty stack?
    @Nonnull
    protected abstract CHEMICAL getEmptyChemical();

    @Nonnull
    public abstract ChemicalStack<CHEMICAL> copy();

    //TODO: decide on a good name, or maybe just make it be .get()
    @Nonnull
    public final CHEMICAL getType() {
        return isEmpty ? getEmptyChemical() : getRaw();
    }

    /**
     * Whether or not this ChemicalStack's chemical type is equal to the other defined ChemicalStack.
     *
     * @param stack - ChemicalStack to check
     *
     * @return if the ChemicalStacks contain the same chemical type
     */
    //TODO: Use this in places we compare manually
    public boolean isTypeEqual(@Nonnull ChemicalStack<CHEMICAL> stack) {
        return isTypeEqual(stack.getType());
    }

    public boolean isTypeEqual(@Nonnull CHEMICAL chemical) {
        return getType() == chemical;
    }

    @Nonnull
    public final CHEMICAL getRaw() {
        return chemicalDelegate.get();
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    protected void updateEmpty() {
        isEmpty = getRaw().isEmptyType() || amount <= 0;
    }

    public int getAmount() {
        return isEmpty ? 0 : amount;
    }

    public void setAmount(int amount) {
        if (getRaw().isEmptyType()) {
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

    /**
     * Whether this stack's chemical has an attribute of a certain type.
     * @param type attribute type to check
     * @return if this chemical has the attribute
     */
    public boolean has(Class<? extends ChemicalAttribute> type) {
        return getType().has(type);
    }

    /**
     * Gets the attribute instance of a certain type, or null if it doesn't exist.
     * @param type attribute type to get
     * @return attribute instance
     */
    @Nullable
    public <T extends ChemicalAttribute> T get(Class<T> type) {
        return getType().get(type);
    }

    /**
     * Gets all attribute instances associated with this chemical's type.
     * @return collection of attribute instances
     */
    public Collection<ChemicalAttribute> getAttributes() {
        return getType().getAttributes();
    }

    /**
     * Gets all attribute types associated with this chemical's type.
     * @return collection of attribute types
     */
    public Collection<Class<? extends ChemicalAttribute>> getAttributeTypes() {
        return getType().getAttributeTypes();
    }

    @Override
    public String toString() {
        return "[" + getType() + ", " + amount + "]";
    }

    @Override
    public ITextComponent getTextComponent() {
        //Wrapper to get display name of the chemical type easier
        return getType().getTextComponent();
    }

    //TODO: Make sure we use getTextComponent where we can instead of the translation key (might already be done)
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
    public boolean contains(@Nonnull ChemicalStack<CHEMICAL> other) {
        return isTypeEqual(other) && amount >= other.amount;
    }

    /**
     * Determines if the chemicals and amounts are all equal.
     *
     * @param other - the ChemicalStack for comparison
     *
     * @return true if the two ChemicalStacks are exactly the same
     */
    public boolean isStackIdentical(@Nonnull ChemicalStack<CHEMICAL> other) {
        return isTypeEqual(other) && amount == other.amount;
    }

    /**
     * Writes this ChemicalStack to a defined tag compound.
     *
     * @param nbtTags - tag compound to write to
     *
     * @return tag compound with this GasStack's data
     */
    public CompoundNBT write(CompoundNBT nbtTags) {
        getType().write(nbtTags);
        nbtTags.putInt(NBTConstants.AMOUNT, getAmount());
        return nbtTags;
    }

    public void writeToPacket(PacketBuffer buf) {
        buf.writeRegistryId(getType());
        buf.writeVarInt(getAmount());
    }
}
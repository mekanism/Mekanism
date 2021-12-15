package mekanism.api.chemical.merged;

import java.util.Objects;
import javax.annotation.Nullable;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.text.IHasTextComponent;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;

public class BoxedChemicalStack implements IHasTextComponent {

    /**
     * Empty Boxed Chemical Stack instance.
     */
    public static final BoxedChemicalStack EMPTY = new BoxedChemicalStack(ChemicalType.GAS, GasStack.EMPTY);

    /**
     * Boxes a Chemical Stack.
     *
     * @param chemicalStack Chemical Stack to box.
     *
     * @return Boxed Chemical Stack.
     */
    public static BoxedChemicalStack box(ChemicalStack<?> chemicalStack) {
        if (chemicalStack.isEmpty()) {
            return EMPTY;
        }
        return new BoxedChemicalStack(ChemicalType.getTypeFor(chemicalStack), chemicalStack);
    }

    /**
     * Reads a Boxed Chemical Stack from a CompoundNBT.
     *
     * @param nbt NBT.
     *
     * @return Boxed Chemical Stack.
     */
    public static BoxedChemicalStack read(@Nullable CompoundNBT nbt) {
        ChemicalType chemicalType = ChemicalType.fromNBT(nbt);
        ChemicalStack<?> stack = null;
        if (chemicalType == ChemicalType.GAS) {
            stack = GasStack.readFromNBT(nbt);
        } else if (chemicalType == ChemicalType.INFUSION) {
            stack = InfusionStack.readFromNBT(nbt);
        } else if (chemicalType == ChemicalType.PIGMENT) {
            stack = PigmentStack.readFromNBT(nbt);
        } else if (chemicalType == ChemicalType.SLURRY) {
            stack = SlurryStack.readFromNBT(nbt);
        }
        return chemicalType == null || stack == null ? EMPTY : new BoxedChemicalStack(chemicalType, stack);
    }

    private final ChemicalType chemicalType;
    private final ChemicalStack<?> chemicalStack;

    private BoxedChemicalStack(ChemicalType chemicalType, ChemicalStack<?> chemicalStack) {
        this.chemicalType = chemicalType;
        this.chemicalStack = chemicalStack;
    }

    /**
     * Gets the boxed type of this stack.
     */
    public BoxedChemical getType() {
        if (isEmpty()) {
            return BoxedChemical.EMPTY;
        }
        return new BoxedChemical(chemicalType, chemicalStack.getType());
    }

    /**
     * Gets the chemical type of this stack.
     */
    public ChemicalType getChemicalType() {
        return chemicalType;
    }

    /**
     * Gets whether this boxed chemical stack is empty.
     *
     * @return {@code true} if this stack is empty, {@code false} otherwise.
     */
    public boolean isEmpty() {
        return this == EMPTY || chemicalStack.isEmpty();
    }

    /**
     * Writes this BoxedChemicalStack to a defined tag compound.
     *
     * @param nbt - tag compound to write to
     *
     * @return tag compound with this BoxedChemicalStack's data
     */
    public CompoundNBT write(CompoundNBT nbt) {
        chemicalType.write(nbt);
        chemicalStack.write(nbt);
        return nbt;
    }

    /**
     * Gets the internal chemical stack that was boxed.
     */
    public ChemicalStack<?> getChemicalStack() {
        return chemicalStack;
    }

    @Override
    public ITextComponent getTextComponent() {
        return chemicalStack.getTextComponent();
    }

    /**
     * Copies this boxed chemical stack into a new boxed chemical stack.
     */
    public BoxedChemicalStack copy() {
        return new BoxedChemicalStack(chemicalType, chemicalStack.copy());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BoxedChemicalStack other = (BoxedChemicalStack) o;
        return chemicalType == other.chemicalType && chemicalStack.equals(other.chemicalStack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chemicalType, chemicalStack);
    }
}
package mekanism.api.chemical.merged;

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

    //TODO: Make a subclass for the empty implementation?
    public static final BoxedChemicalStack EMPTY = new BoxedChemicalStack(ChemicalType.GAS, GasStack.EMPTY);

    public static BoxedChemicalStack box(ChemicalStack<?> chemicalStack) {
        return new BoxedChemicalStack(ChemicalType.getTypeFor(chemicalStack), chemicalStack);
    }

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

    public BoxedChemical getType() {
        return new BoxedChemical(chemicalType, chemicalStack.getType());
    }

    public ChemicalType getChemicalType() {
        return chemicalType;
    }

    public boolean isEmpty() {
        return this == EMPTY || chemicalStack.isEmpty();
    }

    public CompoundNBT write(CompoundNBT nbt) {
        chemicalType.write(nbt);
        chemicalStack.write(nbt);
        return nbt;
    }

    public ChemicalStack<?> getChemicalStack() {
        return chemicalStack;
    }

    @Override
    public ITextComponent getTextComponent() {
        return chemicalStack.getTextComponent();
    }
}
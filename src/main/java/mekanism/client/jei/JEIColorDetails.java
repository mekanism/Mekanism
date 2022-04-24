package mekanism.client.jei;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.client.gui.element.progress.GuiProgress.ColorDetails;

public abstract class JEIColorDetails<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> implements ColorDetails {

    protected final STACK empty;
    public STACK ingredient;

    protected JEIColorDetails(STACK empty) {
        this.empty = empty;
        this.ingredient = this.empty;
    }

    public void reset() {
        this.ingredient = this.empty;
    }

    protected int getColor(STACK ingredient) {
        return getColor(ingredient.getChemicalColorRepresentation());
    }

    protected int getColor(int tint) {
        if ((tint & 0xFF000000) == 0) {
            return 0xFF000000 | tint;
        }
        return tint;
    }
}
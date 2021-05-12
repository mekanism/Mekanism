package mekanism.client.jei;

import javax.annotation.Nullable;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.client.gui.element.progress.GuiProgress.ColorDetails;
import mezz.jei.api.gui.ingredient.IGuiIngredient;

public abstract class JEIColorDetails<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> implements ColorDetails {

    private final STACK empty;

    protected JEIColorDetails(STACK empty) {
        this.empty = empty;
    }

    protected STACK getCurrent(@Nullable IGuiIngredient<STACK> ingredient) {
        if (ingredient == null) {
            return empty;
        }
        STACK stack = ingredient.getDisplayedIngredient();
        return stack == null ? empty : stack;
    }

    protected int getColor(@Nullable IGuiIngredient<STACK> ingredient) {
        return getColor(getCurrent(ingredient).getChemicalColorRepresentation());
    }

    protected int getColor(int tint) {
        if ((tint & 0xFF000000) == 0) {
            return 0xFF000000 | tint;
        }
        return tint;
    }
}
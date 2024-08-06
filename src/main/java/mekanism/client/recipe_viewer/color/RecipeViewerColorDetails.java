package mekanism.client.recipe_viewer.color;

import java.util.function.Supplier;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.client.gui.element.progress.GuiProgress.ColorDetails;

public abstract class RecipeViewerColorDetails<CHEMICAL extends Chemical, STACK extends ChemicalStack> implements ColorDetails {

    protected final Supplier<STACK> empty;
    public Supplier<STACK> ingredient;

    protected RecipeViewerColorDetails(Supplier<STACK> empty) {
        this.empty = empty;
        setIngredient(this.empty);
    }

    public void setIngredient(STACK ingredient) {
        setIngredient(() -> ingredient);
    }

    public void setIngredient(Supplier<STACK> ingredient) {
        this.ingredient = ingredient;
    }

    public void reset() {
        setIngredient(empty);
    }

    protected int getColor(Supplier<STACK> ingredient) {
        return getColor(ingredient.get());
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
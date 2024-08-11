package mekanism.client.recipe_viewer.color;

import java.util.function.Supplier;
import mekanism.api.chemical.ChemicalStack;
import mekanism.client.gui.element.progress.GuiProgress.ColorDetails;

public abstract class RecipeViewerColorDetails implements ColorDetails {

    protected final Supplier<ChemicalStack> empty;
    public Supplier<ChemicalStack> ingredient;

    protected RecipeViewerColorDetails(Supplier<ChemicalStack> empty) {
        this.empty = empty;
        setIngredient(this.empty);
    }

    public void setIngredient(ChemicalStack ingredient) {
        setIngredient(() -> ingredient);
    }

    public void setIngredient(Supplier<ChemicalStack> ingredient) {
        this.ingredient = ingredient;
    }

    public void reset() {
        setIngredient(empty);
    }

    protected int getColor(Supplier<ChemicalStack> ingredient) {
        return getColor(ingredient.get());
    }

    protected int getColor(ChemicalStack ingredient) {
        return getColor(ingredient.getChemicalColorRepresentation());
    }

    protected int getColor(int tint) {
        if ((tint & 0xFF000000) == 0) {
            return 0xFF000000 | tint;
        }
        return tint;
    }
}
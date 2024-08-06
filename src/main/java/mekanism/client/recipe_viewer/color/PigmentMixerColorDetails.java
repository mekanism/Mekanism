package mekanism.client.recipe_viewer.color;

import java.util.function.Supplier;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;

public class PigmentMixerColorDetails extends RecipeViewerColorDetails<Chemical, ChemicalStack> {

    private Supplier<ChemicalStack> outputIngredient;

    public PigmentMixerColorDetails() {
        super(() -> ChemicalStack.EMPTY);
        setOutputIngredient(empty);
    }

    @Override
    public void reset() {
        super.reset();
        setOutputIngredient(empty);
    }

    @Override
    public int getColorFrom() {
        return getColor(ingredient);
    }

    @Override
    public int getColorTo() {
        return getColor(outputIngredient);
    }

    public void setOutputIngredient(Supplier<ChemicalStack> outputIngredient) {
        this.outputIngredient = outputIngredient;
    }
}
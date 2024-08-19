package mekanism.client.recipe_viewer.color;

import java.util.function.Supplier;
import mekanism.api.chemical.ChemicalStack;

public class PigmentMixerColorDetails extends RecipeViewerColorDetails {

    private Supplier<ChemicalStack> outputIngredient;

    public PigmentMixerColorDetails() {
        setOutputIngredient(EMPTY);
    }

    @Override
    public void reset() {
        super.reset();
        setOutputIngredient(EMPTY);
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
package mekanism.client.recipe_viewer.color;

import java.util.function.Supplier;
import mekanism.api.chemical.ChemicalStack;

public class PigmentMixerColorDetails extends RecipeViewerColorDetails {

    private final Supplier<ChemicalStack> defaultOutput;
    private Supplier<ChemicalStack> outputIngredient;

    public PigmentMixerColorDetails() {
        this(EMPTY);
    }

    public PigmentMixerColorDetails(Supplier<ChemicalStack> defaultOutput) {
        this.defaultOutput = defaultOutput;
        setOutputIngredient(defaultOutput);
    }

    @Override
    public void reset() {
        super.reset();
        setOutputIngredient(defaultOutput);
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
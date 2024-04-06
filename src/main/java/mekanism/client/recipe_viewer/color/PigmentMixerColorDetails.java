package mekanism.client.recipe_viewer.color;

import java.util.function.Supplier;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;

public class PigmentMixerColorDetails extends RecipeViewerColorDetails<Pigment, PigmentStack> {

    private Supplier<PigmentStack> outputIngredient;

    public PigmentMixerColorDetails() {
        super(() -> PigmentStack.EMPTY);
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

    public void setOutputIngredient(Supplier<PigmentStack> outputIngredient) {
        this.outputIngredient = outputIngredient;
    }
}
package mekanism.client.recipe_viewer.color;

import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;

public class PigmentExtractorColorDetails extends RecipeViewerColorDetails<Pigment, PigmentStack> {

    public PigmentExtractorColorDetails() {
        super(() -> PigmentStack.EMPTY);
    }

    @Override
    public int getColorFrom() {
        return 0xFFFFFFFF;
    }

    @Override
    public int getColorTo() {
        return getColor(ingredient);
    }
}
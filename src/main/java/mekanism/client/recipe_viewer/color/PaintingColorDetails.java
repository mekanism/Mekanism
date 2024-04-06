package mekanism.client.recipe_viewer.color;

import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;

public class PaintingColorDetails extends RecipeViewerColorDetails<Pigment, PigmentStack> {

    public PaintingColorDetails() {
        super(() -> PigmentStack.EMPTY);
    }

    @Override
    public int getColorFrom() {
        return getColor(ingredient);
    }

    @Override
    public int getColorTo() {
        return 0xFFFFFFFF;
    }
}
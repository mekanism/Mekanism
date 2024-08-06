package mekanism.client.recipe_viewer.color;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;

public class PigmentExtractorColorDetails extends RecipeViewerColorDetails<Chemical, ChemicalStack> {

    public PigmentExtractorColorDetails() {
        super(() -> ChemicalStack.EMPTY);
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
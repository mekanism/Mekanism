package mekanism.client.recipe_viewer.color;

import mekanism.api.chemical.ChemicalStack;

public class PaintingColorDetails extends RecipeViewerColorDetails {

    public PaintingColorDetails() {
        super(() -> ChemicalStack.EMPTY);
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
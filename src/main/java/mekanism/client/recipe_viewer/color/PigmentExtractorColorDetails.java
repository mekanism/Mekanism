package mekanism.client.recipe_viewer.color;

public class PigmentExtractorColorDetails extends RecipeViewerColorDetails {

    @Override
    public int getColorFrom() {
        return 0xFFFFFFFF;
    }

    @Override
    public int getColorTo() {
        return getColor(ingredient);
    }
}
package mekanism.client.recipe_viewer.color;

public class PaintingColorDetails extends RecipeViewerColorDetails {

    @Override
    public int getColorFrom() {
        return getColor(ingredient);
    }

    @Override
    public int getColorTo() {
        return 0xFFFFFFFF;
    }
}
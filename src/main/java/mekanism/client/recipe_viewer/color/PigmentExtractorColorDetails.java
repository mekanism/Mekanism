package mekanism.client.recipe_viewer.color;

import net.minecraft.util.CommonColors;

public class PigmentExtractorColorDetails extends RecipeViewerColorDetails {

    @Override
    public int getColorFrom() {
        return CommonColors.WHITE;
    }

    @Override
    public int getColorTo() {
        return getColor(ingredient);
    }
}
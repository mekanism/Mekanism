package mekanism.client.recipe_viewer.color;

import net.minecraft.util.CommonColors;

public class PaintingColorDetails extends RecipeViewerColorDetails {

    @Override
    public int getColorFrom() {
        return getColor(ingredient);
    }

    @Override
    public int getColorTo() {
        return CommonColors.WHITE;
    }
}
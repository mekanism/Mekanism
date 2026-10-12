package mekanism.client.recipe_viewer.jei;

import java.util.Collections;
import java.util.List;
import mekanism.client.recipe_viewer.INamedRVRecipe;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public abstract class SyntheticRecipeCategory<RECIPE> extends BaseRecipeCategory<RECIPE> {

    protected SyntheticRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<RECIPE> recipeType) {
        super(helper, recipeType);
    }

    protected SyntheticRecipeCategory(IGuiHelper helper, IRecipeType<RECIPE> recipeType, Component component, IDrawable icon, int xOffset, int yOffset, int width, int height) {
        super(helper, recipeType, component, icon, xOffset, yOffset, width, height);
    }

    @Nullable
    @Override
    public Identifier getIdentifier(RECIPE recipe) {
        return recipe instanceof INamedRVRecipe namedRecipe ? namedRecipe.id() : null;
    }

    @Override
    public List<Identifier> getSourceRecipeIds(RECIPE recipe) {
        //Short circuit as the generated recipe id is synthetic
        return Collections.emptyList();
    }
}
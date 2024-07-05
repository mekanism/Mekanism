package mekanism.client.recipe_viewer.jei;

import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

public abstract class HolderRecipeCategory<RECIPE extends Recipe<?>> extends BaseRecipeCategory<RecipeHolder<RECIPE>> {

    protected HolderRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<RECIPE> recipeType) {
        super(helper, MekanismJEI.holderRecipeType(recipeType), recipeType.getTextComponent(), createIcon(helper, recipeType), recipeType.xOffset(), recipeType.yOffset(), recipeType.width(), recipeType.height());
    }

    @NotNull
    @Override
    public ResourceLocation getRegistryName(RecipeHolder<RECIPE> recipe) {
        return recipe.id();
    }
}
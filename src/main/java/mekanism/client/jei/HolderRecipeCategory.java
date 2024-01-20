package mekanism.client.jei;

import mekanism.api.providers.IItemProvider;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public abstract class HolderRecipeCategory<RECIPE extends Recipe<?>> extends BaseRecipeCategory<RecipeHolder<RECIPE>> {

    protected HolderRecipeCategory(IGuiHelper helper, MekanismJEIRecipeType<RECIPE> recipeType, IItemProvider provider, int xOffset, int yOffset, int width, int height) {
        super(helper, MekanismJEI.holderRecipeType(recipeType), provider, xOffset, yOffset, width, height);
    }

    protected HolderRecipeCategory(IGuiHelper helper, MekanismJEIRecipeType<RECIPE> recipeType, Component component, IDrawable icon, int xOffset, int yOffset, int width,
          int height) {
        super(helper, MekanismJEI.holderRecipeType(recipeType), component, icon, xOffset, yOffset, width, height);
    }
}
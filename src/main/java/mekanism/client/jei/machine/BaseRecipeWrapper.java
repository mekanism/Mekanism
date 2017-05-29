package mekanism.client.jei.machine;

import mekanism.client.jei.BaseRecipeCategory;
import mezz.jei.api.recipe.BlankRecipeWrapper;

public abstract class BaseRecipeWrapper extends BlankRecipeWrapper
{
	public abstract BaseRecipeCategory getCategory();
}

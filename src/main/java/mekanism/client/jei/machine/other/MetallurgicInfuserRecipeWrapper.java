package mekanism.client.jei.machine.other;

import mekanism.client.render.MekanismRenderer;
import mekanism.common.recipe.machines.MetallurgicInfuserRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MetallurgicInfuserRecipeWrapper implements IRecipeWrapper
{
	private final MetallurgicInfuserRecipe recipe;
	
	public MetallurgicInfuserRecipeWrapper(MetallurgicInfuserRecipe r)
	{
		recipe = r;
	}
	
	@Override
	public void getIngredients(IIngredients ingredients)
	{
		List<ItemStack> inputStacks = Collections.singletonList(recipe.recipeInput.inputStack);
		List<ItemStack> infuseStacks = MetallurgicInfuserRecipeCategory.getInfuseStacks(recipe.getInput().infuse.type);
		
		ingredients.setInput(ItemStack.class, recipe.recipeInput.inputStack);
		ingredients.setInputLists(ItemStack.class, Arrays.asList(inputStacks, infuseStacks));
		ingredients.setOutput(ItemStack.class, recipe.recipeOutput.output);
	}

	public MetallurgicInfuserRecipe getRecipe()
	{
		return recipe;
	}

	@Override
	public void drawInfo(Minecraft mc, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		if (mc.currentScreen != null) {
			mc.renderEngine.bindTexture(MekanismRenderer.getBlocksTexture());
			mc.currentScreen.drawTexturedModalRect(2, 2, recipe.getInput().infuse.type.sprite, 4, 52);
		}
	}
}

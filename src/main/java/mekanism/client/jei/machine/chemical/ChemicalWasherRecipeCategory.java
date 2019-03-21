package mekanism.client.jei.machine.chemical;

import mekanism.api.gas.GasStack;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.machines.WasherRecipe;
import mekanism.common.util.LangUtils;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiIngredientGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

public class ChemicalWasherRecipeCategory extends BaseRecipeCategory
{
	private final IDrawable background;

	@Nullable
	private WasherRecipe tempRecipe;
	
	public ChemicalWasherRecipeCategory(IGuiHelper helper)
	{
		super(helper, "mekanism:gui/nei/GuiChemicalWasher.png", Recipe.CHEMICAL_WASHER.jeiRecipeUid, "tile.MachineBlock2.ChemicalWasher.name", null);

		xOffset = 3;
		yOffset = 3;
		
		background = guiHelper.createDrawable(new ResourceLocation(guiTexture), xOffset, yOffset, 170, 70);
	}
	
	@Override
	public void drawExtras(Minecraft minecraft) 
	{
		super.drawExtras(minecraft);
		
		drawTexturedRect(61-xOffset, 39-yOffset, 176, 63, 55, 8);
	}
	
	@Override
	public IDrawable getBackground() 
	{
		return background;
	}
	
	@Override
	public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper, IIngredients ingredients) 
	{
		if(!(recipeWrapper instanceof ChemicalWasherRecipeWrapper))
		{
			return;
		}

		tempRecipe = ((ChemicalWasherRecipeWrapper)recipeWrapper).getRecipe();
		
		IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
		
		fluidStacks.init(0, true, 6-xOffset, 5-yOffset, 16, 58, 1, false, fluidOverlayLarge);
		fluidStacks.set(0, ingredients.getInputs(VanillaTypes.FLUID).get(0));
		fluidStacks.addTooltipCallback((index, input, ingredient, tooltip) -> tooltip.set(1, LangUtils.localizeWithFormat("mekanism.jei.amount.per.tick", tooltip.get(1))));
		
		IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.GAS_INGREDIENT_TYPE);
		
		initGas(gasStacks, 0, true, 27-xOffset, 14-yOffset, 16, 58, tempRecipe.getInput().ingredient, true);
		initGas(gasStacks, 1, false, 134-xOffset, 14-yOffset, 16, 58, tempRecipe.getOutput().output, true);
	}
}

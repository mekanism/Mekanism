package mekanism.client.jei.machine;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.machines.ChanceMachineRecipe;
import mekanism.common.recipe.outputs.ChanceOutput;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;

public class ChanceMachineRecipeWrapper extends BlankRecipeWrapper
{
	public ChanceMachineRecipe recipe;
	
	public ChanceMachineRecipeCategory category;
	
	public ChanceMachineRecipeWrapper(ChanceMachineRecipe r, ChanceMachineRecipeCategory c)
	{
		recipe = r;
		category = c;
	}
	
	@Nonnull
	@Override
	public List<ItemStack> getInputs()
	{
		return Arrays.asList(((ItemStackInput)recipe.getInput()).ingredient);
	}

	@Nonnull
	@Override
	public List<ItemStack> getOutputs()
	{
		ChanceOutput output = (ChanceOutput)recipe.getOutput();
		
		return Arrays.asList(output.primaryOutput, output.secondaryOutput);
	}
	
	@Override
	public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY)
	{
		ChanceOutput output = (ChanceOutput)recipe.getOutput();
		
		if(output.hasSecondary())
		{
			FontRenderer fontRendererObj = minecraft.fontRendererObj;
			fontRendererObj.drawString(Math.round(output.secondaryChance*100) + "%", 104, 41, 0x404040, false);
		}
	}
}

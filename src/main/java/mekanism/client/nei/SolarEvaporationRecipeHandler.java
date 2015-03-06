package mekanism.client.nei;

import static codechicken.lib.gui.GuiDraw.changeTexture;
import static codechicken.lib.gui.GuiDraw.drawTexturedModalRect;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import mekanism.client.gui.GuiSolarEvaporationController;
import mekanism.common.ObfuscatedNames;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.machines.SolarEvaporationRecipe;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class SolarEvaporationRecipeHandler extends BaseRecipeHandler
{
	private int ticksPassed;

	public static int xOffset = 5;
	public static int yOffset = 12;

	@Override
	public String getRecipeName()
	{
		return MekanismUtils.localize("gui.solarEvaporationController.short");
	}

	@Override
	public String getOverlayIdentifier()
	{
		return "solarevaporation";
	}

	@Override
	public String getGuiTexture()
	{
		return "mekanism:gui/nei/GuiSolarEvaporationController.png";
	}

	@Override
	public Class getGuiClass()
	{
		return GuiSolarEvaporationController.class;
	}

	public String getRecipeId()
	{
		return "mekanism.solarevaporation";
	}

	public Collection<SolarEvaporationRecipe> getRecipes()
	{
		return Recipe.SOLAR_EVAPORATION_PLANT.get().values();
	}
	
	@Override
	public void loadTransferRects()
	{
		transferRects.add(new TemplateRecipeHandler.RecipeTransferRect(new Rectangle(49-xOffset, 20-yOffset, 78, 38), getRecipeId(), new Object[0]));
	}

	@Override
	public void drawBackground(int i)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		changeTexture(getGuiTexture());
		drawTexturedModalRect(-2, 0, 3, yOffset, 170, 62);
	}

	@Override
	public void drawExtras(int i)
	{
		CachedIORecipe recipe = (CachedIORecipe)arecipes.get(i);

		drawProgressBar(49-xOffset, 64-yOffset, 176, 59, 78, 8, ticksPassed < 20 ? ticksPassed % 20 / 20.0F : 1.0F, 0);

		if(recipe.fluidInput != null)
		{
			displayGauge(58, 7-xOffset, 14-yOffset, 176, 0, 58, recipe.fluidInput, null);
		}

		if(recipe.fluidOutput != null)
		{
			displayGauge(58, 153-xOffset, 14-yOffset, 176, 0, 58, recipe.fluidOutput, null);
		}
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		ticksPassed++;
	}

	@Override
	public void loadCraftingRecipes(String outputId, Object... results)
	{
		if(outputId.equals(getRecipeId()))
		{
			for(SolarEvaporationRecipe irecipe : getRecipes())
			{
				arecipes.add(new CachedIORecipe(irecipe));
			}
		}
		else if(outputId.equals("fluid") && results.length == 1 && results[0] instanceof FluidStack)
		{
			for(SolarEvaporationRecipe irecipe : getRecipes())
			{
				if(((FluidStack)results[0]).isFluidEqual(irecipe.recipeOutput.output))
				{
					arecipes.add(new CachedIORecipe(irecipe));
				}
			}
		}
		else {
			super.loadCraftingRecipes(outputId, results);
		}
	}

	@Override
	public void loadUsageRecipes(String inputId, Object... ingredients)
	{
		if(inputId.equals("fluid") && ingredients.length == 1 && ingredients[0] instanceof FluidStack)
		{
			for(SolarEvaporationRecipe irecipe : getRecipes())
			{
				if(irecipe.recipeInput.ingredient.isFluidEqual((FluidStack)ingredients[0]))
				{
					arecipes.add(new CachedIORecipe(irecipe));
				}
			}
		}
		else {
			super.loadUsageRecipes(inputId, ingredients);
		}
	}

	@Override
	public List<String> handleTooltip(GuiRecipe gui, List<String> currenttip, int recipe)
	{
		Point point = GuiDraw.getMousePosition();

		int xAxis = point.x-(Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiLeft);
		int yAxis = point.y-(Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiTop);

		if(xAxis >= 7 && xAxis <= 23 && yAxis >= 14+4 && yAxis <= 72+4)
		{
			currenttip.add(LangUtils.localizeFluidStack(((CachedIORecipe) arecipes.get(recipe)).fluidInput));
		}
		else if(xAxis >= 153 && xAxis <= 169 && yAxis >= 14+4 && yAxis <= 72+4)
		{
			currenttip.add(LangUtils.localizeFluidStack(((CachedIORecipe) arecipes.get(recipe)).fluidOutput));
		}

		return super.handleTooltip(gui, currenttip, recipe);
	}

	@Override
	public boolean keyTyped(GuiRecipe gui, char keyChar, int keyCode, int recipe)
	{
		Point point = GuiDraw.getMousePosition();

		int xAxis = point.x-(Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiLeft);
		int yAxis = point.y-(Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiTop);

		FluidStack stack = null;

		if(xAxis >= 7 && xAxis <= 23 && yAxis >= 14+4 && yAxis <= 72+4)
		{
			stack = ((CachedIORecipe)arecipes.get(recipe)).fluidInput;
		}
		else if(xAxis >= 153 && xAxis <= 169 && yAxis >= 14+4 && yAxis <= 72+4)
		{
			stack = ((CachedIORecipe)arecipes.get(recipe)).fluidOutput;
		}

		if(stack != null)
		{
			if(keyCode == NEIClientConfig.getKeyBinding("gui.recipe"))
			{
				if(doFluidLookup(stack, false))
				{
					return true;
				}
			}
			else if(keyCode == NEIClientConfig.getKeyBinding("gui.usage"))
			{
				if(doFluidLookup(stack, true))
				{
					return true;
				}
			}
		}

		return super.keyTyped(gui, keyChar, keyCode, recipe);
	}

	@Override
	public boolean mouseClicked(GuiRecipe gui, int button, int recipe)
	{
		Point point = GuiDraw.getMousePosition();

		int xAxis = point.x - (Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiLeft);
		int yAxis = point.y - (Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiTop);

		FluidStack stack = null;

		if(xAxis >= 7 && xAxis <= 23 && yAxis >= 14+4 && yAxis <= 72+4)
		{
			stack = ((CachedIORecipe)arecipes.get(recipe)).fluidInput;
		}
		else if(xAxis >= 153 && xAxis <= 169 && yAxis >= 14+4 && yAxis <= 72+4)
		{
			stack = ((CachedIORecipe)arecipes.get(recipe)).fluidOutput;
		}

		if(stack != null)
		{
			if(button == 0)
			{
				if(doFluidLookup(stack, false))
				{
					return true;
				}
			}
			else if(button == 1)
			{
				if(doFluidLookup(stack, true))
				{
					return true;
				}
			}
		}

		return super.mouseClicked(gui, button, recipe);
	}

	@Override
	public int recipiesPerPage()
	{
		return 1;
	}

	@Override
	public void addGuiElements()
	{

	}

	public class CachedIORecipe extends TemplateRecipeHandler.CachedRecipe
	{
		public FluidStack fluidInput;
		public FluidStack fluidOutput;

		@Override
		public PositionedStack getResult()
		{
			return null;
		}

		public CachedIORecipe(FluidStack input, FluidStack output)
		{
			fluidInput = input;
			fluidOutput = output;
		}

		public CachedIORecipe(SolarEvaporationRecipe recipe)
		{
			this((FluidStack)recipe.recipeInput.ingredient, (FluidStack)recipe.recipeOutput.output);
		}
	}
}

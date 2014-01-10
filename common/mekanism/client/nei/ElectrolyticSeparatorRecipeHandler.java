package mekanism.client.nei;

import static codechicken.core.gui.GuiDraw.changeTexture;
import static codechicken.core.gui.GuiDraw.drawTexturedModalRect;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import mekanism.api.ChemicalPair;
import mekanism.api.gas.GasStack;
import mekanism.client.gui.GuiElectrolyticSeparator;
import mekanism.common.ObfuscatedNames;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import codechicken.core.gui.GuiDraw;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class ElectrolyticSeparatorRecipeHandler extends BaseRecipeHandler
{
	private int ticksPassed;
	
	public static int xOffset = 5;
	public static int yOffset = 9;
	
	@Override
	public String getRecipeName()
	{
		return "Electrolytic Separator";
	}
	
	@Override
	public String getOverlayIdentifier()
	{
		return "electrolyticseparator";
	}
	
	@Override
	public String getGuiTexture()
	{
		return "mekanism:gui/GuiElectrolyticSeparator.png";
	}
	
	@Override
	public Class getGuiClass()
	{
		return GuiElectrolyticSeparator.class;
	}

	public String getRecipeId()
	{
		return "mekanism.electrolyticseparator";
	}

	public Set<Entry<FluidStack, ChemicalPair>> getRecipes()
	{
		return Recipe.ELECTROLYTIC_SEPARATOR.get().entrySet();
	}

	@Override
	public void drawBackground(int i)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		changeTexture(getGuiTexture());
		drawTexturedModalRect(-1, 0, 4, yOffset, 167, 62);
	}

	@Override
	public void drawExtras(int i)
	{
		CachedIORecipe recipe = (CachedIORecipe)arecipes.get(i);
		
		float f = ticksPassed <= 20 ? ticksPassed / 20.0F : 1.0F;
		drawProgressBar(165-xOffset, 17-yOffset, 176, 0, 4, 52, f, 3);
		
		if(recipe.fluidInput != null)
		{
        	displayGauge(58, 6-xOffset, 11-yOffset, 176, 68, 58, recipe.fluidInput, null);
		}
		
		if(recipe.outputPair.leftGas != null)
		{
        	displayGauge(28, 59-xOffset, 19-yOffset, 176, 68, 28, null, recipe.outputPair.leftGas);
		}
		
		if(recipe.outputPair.rightGas != null)
		{
        	displayGauge(28, 101-xOffset, 19-yOffset, 176, 68, 28, null, recipe.outputPair.rightGas);
		}
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		ticksPassed++;
	}

	@Override
	public void loadTransferRects()
	{
		transferRects.add(new TemplateRecipeHandler.RecipeTransferRect(new Rectangle(80-xOffset, 30-yOffset, 16, 6), getRecipeId(), new Object[0]));
	}

	@Override
	public void loadCraftingRecipes(String outputId, Object... results)
	{
		if(outputId.equals(getRecipeId()))
		{
			for(Map.Entry irecipe : getRecipes())
			{
				arecipes.add(new CachedIORecipe(irecipe));
			}
		}
		else if(outputId.equals("gas") && results.length == 1 && results[0] instanceof GasStack)
		{
			for(Map.Entry<FluidStack, ChemicalPair> irecipe : getRecipes())
			{
				if(irecipe.getValue().containsType((GasStack)results[0]))
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
			for(Map.Entry<FluidStack, ChemicalPair> irecipe : getRecipes())
			{
				if(irecipe.getKey().isFluidEqual((FluidStack)ingredients[0]))
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
		
		if(xAxis >= 6 && xAxis <= 22 && yAxis >= 11+7 && yAxis <= 69+7)
		{
			currenttip.add(((CachedIORecipe)arecipes.get(recipe)).fluidInput.getFluid().getLocalizedName());
		}
		else if(xAxis >= 59 && xAxis <= 75 && yAxis >= 19+7 && yAxis <= 47+7)
		{
			currenttip.add(((CachedIORecipe)arecipes.get(recipe)).outputPair.leftGas.getGas().getLocalizedName());
		}
		else if(xAxis >= 101 && xAxis <= 117 && yAxis >= 19+7 && yAxis <= 47+7)
		{
			currenttip.add(((CachedIORecipe)arecipes.get(recipe)).outputPair.rightGas.getGas().getLocalizedName());
		}
		
		return super.handleTooltip(gui, currenttip, recipe);
	}

	@Override
	public boolean keyTyped(GuiRecipe gui, char keyChar, int keyCode, int recipe)
	{
		Point point = GuiDraw.getMousePosition();
		
		int xAxis = point.x-(Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiLeft);
		int yAxis = point.y-(Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiTop);
		
		GasStack gas = null;
		FluidStack fluid = null;
		
		if(xAxis >= 6 && xAxis <= 22 && yAxis >= 11+7 && yAxis <= 22+7)
		{
			fluid = ((CachedIORecipe)arecipes.get(recipe)).fluidInput;
		}
		else if(xAxis >= 59 && xAxis <= 75 && yAxis >= 19+7 && yAxis <= 47+7)
		{
			gas = ((CachedIORecipe)arecipes.get(recipe)).outputPair.leftGas;
		}
		else if(xAxis >= 101 && xAxis <= 117 && yAxis >= 19+7 && yAxis <= 47+7)
		{
			gas = ((CachedIORecipe)arecipes.get(recipe)).outputPair.rightGas;
		}
		
		if(gas != null)
		{
			if(keyCode == NEIClientConfig.getKeyBinding("gui.recipe"))
			{
				if(doGasLookup(gas, false))
				{
					return true;
				}
			}
			else if(keyCode == NEIClientConfig.getKeyBinding("gui.usage"))
			{
				if(doGasLookup(gas, true))
				{
					return true;
				}
			}
		}
		else if(fluid != null)
		{
			if(keyCode == NEIClientConfig.getKeyBinding("gui.recipe"))
			{
				if(doFluidLookup(fluid, false))
				{
					return true;
				}
			}
			else if(keyCode == NEIClientConfig.getKeyBinding("gui.usage"))
			{
				if(doFluidLookup(fluid, true))
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
		
		GasStack gas = null;
		FluidStack fluid = null;
		
		if(xAxis >= 6 && xAxis <= 22 && yAxis >= 11+7 && yAxis <= 22+7)
		{
			fluid = ((CachedIORecipe)arecipes.get(recipe)).fluidInput;
		}
		else if(xAxis >= 59 && xAxis <= 75 && yAxis >= 19+7 && yAxis <= 47+7)
		{
			gas = ((CachedIORecipe)arecipes.get(recipe)).outputPair.leftGas;
		}
		else if(xAxis >= 101 && xAxis <= 117 && yAxis >= 19+7 && yAxis <= 47+7)
		{
			gas = ((CachedIORecipe)arecipes.get(recipe)).outputPair.rightGas;
		}
		
		if(gas != null)
		{
			if(button == 0)
			{
				if(doGasLookup(gas, false))
				{
					return true;
				}
			}
			else if(button == 1)
			{
				if(doGasLookup(gas, true))
				{
					return true;
				}
			}
		}
		else if(fluid != null)
		{
			if(button == 0)
			{
				if(doFluidLookup(fluid, false))
				{
					return true;
				}
			}
			else if(button == 1)
			{
				if(doFluidLookup(fluid, true))
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

	public class CachedIORecipe extends TemplateRecipeHandler.CachedRecipe
	{
		public FluidStack fluidInput;
		public ChemicalPair outputPair;

		@Override
		public PositionedStack getResult()
		{
			return null;
		}

		public CachedIORecipe(FluidStack input, ChemicalPair pair)
		{
			fluidInput = input;
			outputPair = pair;
		}

		public CachedIORecipe(Map.Entry recipe)
		{
			this((FluidStack)recipe.getKey(), (ChemicalPair)recipe.getValue());
		}
	}
}

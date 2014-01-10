package mekanism.client.nei;

import static codechicken.core.gui.GuiDraw.changeTexture;
import static codechicken.core.gui.GuiDraw.drawTexturedModalRect;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import mekanism.api.gas.GasStack;
import mekanism.client.gui.GuiChemicalOxidizer;
import mekanism.common.ObfuscatedNames;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import codechicken.core.gui.GuiDraw;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class ChemicalOxidizerRecipeHandler extends BaseRecipeHandler
{
	private int ticksPassed;
	
	public int xOffset = 5;
	public int yOffset = 12;
	
	@Override
	public String getRecipeName()
	{
		return "Chemical Oxidizer";
	}
	
	@Override
	public String getOverlayIdentifier()
	{
		return "chemicaloxidizer";
	}
	
	@Override
	public String getGuiTexture()
	{
		return "mekanism:gui/GuiChemicalOxidizer.png";
	}
	
	@Override
	public Class getGuiClass()
	{
		return GuiChemicalOxidizer.class;
	}

	public String getRecipeId()
	{
		return "mekanism.chemicaloxidizer";
	}

	public Set<Entry<ItemStack, GasStack>> getRecipes()
	{
		return Recipe.CHEMICAL_OXIDIZER.get().entrySet();
	}

	@Override
	public void drawBackground(int i)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		changeTexture(getGuiTexture());
		drawTexturedModalRect(0, 0, xOffset, yOffset, 147, 62);
	}

	@Override
	public void drawExtras(int i)
	{
		GasStack gas = ((CachedIORecipe)arecipes.get(i)).outputStack;
		
		float f = ticksPassed % 20 / 20.0F;
		drawProgressBar(64-xOffset, 40-yOffset, 176, 63, 48, 8, f, 0);
		
		if(gas != null)
		{
        	displayGauge(58, 134-xOffset, 14-yOffset, 176, 4, 58, null, gas);
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
		transferRects.add(new TemplateRecipeHandler.RecipeTransferRect(new Rectangle(64-5, 40-12, 48, 8), getRecipeId(), new Object[0]));
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
			for(Map.Entry<ItemStack, GasStack> irecipe : getRecipes())
			{
				if(((GasStack)results[0]).isGasEqual(irecipe.getValue()))
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
	public List<String> handleTooltip(GuiRecipe gui, List<String> currenttip, int recipe)
	{
		Point point = GuiDraw.getMousePosition();
		
		int xAxis = point.x-(Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiLeft);
		int yAxis = point.y-(Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiTop);
		
		if(xAxis >= 134 && xAxis <= 150 && yAxis >= 14+4 && yAxis <= 72+4)
		{
			currenttip.add(((CachedIORecipe)arecipes.get(recipe)).outputStack.getGas().getLocalizedName());
		}
		
		return super.handleTooltip(gui, currenttip, recipe);
	}
	
	@Override
	public boolean keyTyped(GuiRecipe gui, char keyChar, int keyCode, int recipe)
	{
		Point point = GuiDraw.getMousePosition();
		
		int xAxis = point.x-(Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiLeft);
		int yAxis = point.y-(Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiTop);
		
		if(xAxis >= 134 && xAxis <= 150 && yAxis >= 14+4 && yAxis <= 72+4)
		{
			GasStack stack = ((CachedIORecipe)arecipes.get(recipe)).outputStack;
			
			if(keyCode == NEIClientConfig.getKeyBinding("gui.recipe"))
			{
				if(doGasLookup(stack, false))
				{
					return true;
				}
			}
			else if(keyCode == NEIClientConfig.getKeyBinding("gui.usage"))
			{
				if(doGasLookup(stack, true))
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
		
		if(xAxis >= 134 && xAxis <= 150 && yAxis >= 14+4 && yAxis <= 72+4)
		{
			GasStack stack = ((CachedIORecipe)arecipes.get(recipe)).outputStack;
			
			if(button == 0)
			{
				if(doGasLookup(stack, false))
				{
					return true;
				}
			}
			else if(button == 1)
			{
				if(doGasLookup(stack, true))
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
		return 2;
	}

	@Override
	public void loadUsageRecipes(ItemStack ingredient)
	{
		for(Map.Entry irecipe : getRecipes())
		{
			if(NEIServerUtils.areStacksSameTypeCrafting((ItemStack)irecipe.getKey(), ingredient))
			{
				arecipes.add(new CachedIORecipe(irecipe));
			}
		}
	}

	public class CachedIORecipe extends TemplateRecipeHandler.CachedRecipe
	{
		public PositionedStack inputStack;
		public GasStack outputStack;

		@Override
		public PositionedStack getIngredient()
		{
			return inputStack;
		}

		@Override
		public PositionedStack getResult()
		{
			return null;
		}

		public CachedIORecipe(ItemStack input, GasStack output)
		{
			inputStack = new PositionedStack(input, 26-xOffset, 36-yOffset);
			outputStack = output;
		}

		public CachedIORecipe(Map.Entry recipe)
		{
			this((ItemStack)recipe.getKey(), (GasStack)recipe.getValue());
		}
	}
}

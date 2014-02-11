package mekanism.client.nei;

import static codechicken.core.gui.GuiDraw.changeTexture;
import static codechicken.core.gui.GuiDraw.drawTexturedModalRect;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.client.gui.GuiChemicalCrystalizer;
import mekanism.client.nei.MachineRecipeHandler.CachedIORecipe;
import mekanism.common.ObfuscatedNames;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import codechicken.core.gui.GuiDraw;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class ChemicalCrystalizerRecipeHandler extends BaseRecipeHandler
{
	private int ticksPassed;
	
	public static int xOffset = 5;
	public static int yOffset = 3;
	
	@Override
	public String getRecipeName()
	{
		return MekanismUtils.localize("tile.MachineBlock2.ChemicalCrystalizer.name");
	}
	
	@Override
	public String getOverlayIdentifier()
	{
		return "chemicalcrystalizer";
	}
	
	@Override
	public String getGuiTexture()
	{
		return "mekanism:gui/nei/GuiChemicalCrystalizer.png";
	}
	
	@Override
	public Class getGuiClass()
	{
		return GuiChemicalCrystalizer.class;
	}

	public String getRecipeId()
	{
		return "mekanism.chemicalcrystalizer";
	}

	public Set<Entry<GasStack, ItemStack>> getRecipes()
	{
		return Recipe.CHEMICAL_CRYSTALIZER.get().entrySet();
	}

	@Override
	public void drawBackground(int i)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		changeTexture(getGuiTexture());
		drawTexturedModalRect(-2, 0, 3, yOffset, 147, 79);
	}

	@Override
	public void drawExtras(int i)
	{
		GasStack gas = ((CachedIORecipe)arecipes.get(i)).inputStack;
		
		float f = ticksPassed % 20 / 20.0F;
		drawProgressBar(53-xOffset, 61-yOffset, 176, 63, 48, 8, f, 0);
		
		if(gas != null)
		{
        	displayGauge(58, 6-xOffset, 5-yOffset, 176, 4, 58, null, gas);
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
		transferRects.add(new TemplateRecipeHandler.RecipeTransferRect(new Rectangle(53-xOffset, 61-yOffset, 48, 8), getRecipeId(), new Object[0]));
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
		else {
			super.loadCraftingRecipes(outputId, results);
		}
	}

	@Override
	public void loadCraftingRecipes(ItemStack result)
	{
		for(Map.Entry irecipe : getRecipes())
		{
			if(NEIServerUtils.areStacksSameTypeCrafting((ItemStack)irecipe.getValue(), result))
			{
				arecipes.add(new CachedIORecipe(irecipe));
			}
		}
	}
	
	@Override
	public List<String> handleTooltip(GuiRecipe gui, List<String> currenttip, int recipe)
	{
		Point point = GuiDraw.getMousePosition();
		
		int xAxis = point.x-(Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiLeft);
		int yAxis = point.y-(Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiTop);
		
		if(xAxis >= 6 && xAxis <= 22 && yAxis >= 5+13 && yAxis <= 63+13)
		{
			currenttip.add(((CachedIORecipe)arecipes.get(recipe)).inputStack.getGas().getLocalizedName());
		}
		
		return super.handleTooltip(gui, currenttip, recipe);
	}
	
	@Override
	public boolean keyTyped(GuiRecipe gui, char keyChar, int keyCode, int recipe)
	{
		Point point = GuiDraw.getMousePosition();
		
		int xAxis = point.x-(Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiLeft);
		int yAxis = point.y-(Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiTop);
		
		GasStack stack = null;
		
		if(xAxis >= 6 && xAxis <= 22 && yAxis >= 5+13 && yAxis <= 63+13)
		{
			stack = ((CachedIORecipe)arecipes.get(recipe)).inputStack;
		}
		
		if(stack != null)
		{
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
		
		GasStack stack = null;
		
		if(xAxis >= 6 && xAxis <= 22 && yAxis >= 5+13 && yAxis <= 63+13)
		{
			stack = ((CachedIORecipe)arecipes.get(recipe)).inputStack;
		}
		
		if(stack != null)
		{
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
		return 1;
	}

	@Override
	public void loadUsageRecipes(String inputId, Object... ingredients)
	{
		if(inputId.equals("gas") && ingredients.length == 1 && ingredients[0] instanceof GasStack)
		{
			for(Map.Entry<GasStack, ItemStack> irecipe : getRecipes())
			{
				if(irecipe.getKey().isGasEqual((GasStack)ingredients[0]))
				{
					arecipes.add(new CachedIORecipe(irecipe));
				}
			}
		}
		else {
			super.loadUsageRecipes(inputId, ingredients);
		}
	}

	public class CachedIORecipe extends TemplateRecipeHandler.CachedRecipe
	{
		public GasStack inputStack;
		public PositionedStack outputStack;

		@Override
		public PositionedStack getResult()
		{
			return outputStack;
		}

		public CachedIORecipe(GasStack input, ItemStack output)
		{
			inputStack = input;
			outputStack = new PositionedStack(output, 131-xOffset, 57-yOffset);
		}

		public CachedIORecipe(Map.Entry recipe)
		{
			this((GasStack)recipe.getKey(), (ItemStack)recipe.getValue());
		}
	}
}

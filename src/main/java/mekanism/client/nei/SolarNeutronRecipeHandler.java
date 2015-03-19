package mekanism.client.nei;

import static codechicken.lib.gui.GuiDraw.changeTexture;
import static codechicken.lib.gui.GuiDraw.drawTexturedModalRect;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.List;

import mekanism.api.gas.GasStack;
import mekanism.client.gui.GuiSolarNeutronActivator;
import mekanism.client.nei.RotaryCondensentratorRecipeHandler.CachedIORecipe;
import mekanism.common.ObfuscatedNames;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.machines.SolarNeutronRecipe;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class SolarNeutronRecipeHandler extends BaseRecipeHandler
{
	private int ticksPassed;

	public static int xOffset = 5;
	public static int yOffset = 12;

	@Override
	public String getRecipeName()
	{
		return MekanismUtils.localize("tile.MachineBlock3.SolarNeutronActivator.name");
	}

	@Override
	public String getOverlayIdentifier()
	{
		return "solarneutron";
	}

	@Override
	public String getGuiTexture()
	{
		return "mekanism:gui/nei/GuiSolarNeutronActivator.png";
	}

	@Override
	public Class getGuiClass()
	{
		return GuiSolarNeutronActivator.class;
	}

	public String getRecipeId()
	{
		return "mekanism.solarneutron";
	}

	public Collection<SolarNeutronRecipe> getRecipes()
	{
		return Recipe.SOLAR_NEUTRON_ACTIVATOR.get().values();
	}

	@Override
	public void drawBackground(int i)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		changeTexture(getGuiTexture());
		drawTexturedModalRect(-2, 0, 3, yOffset, 170, 70);
	}

	@Override
	public void drawExtras(int i)
	{
		CachedIORecipe recipe = (CachedIORecipe)arecipes.get(i);

		drawTexturedModalRect(64-xOffset, 39-yOffset, 176, 58, 55, 8);

		if(recipe.inputStack != null)
		{
			displayGauge(58, 26-xOffset, 14-yOffset, 176, 0, 58, null, recipe.inputStack);
		}

		if(recipe.inputStack != null)
		{
			displayGauge(58, 134-xOffset, 14-yOffset, 176, 0, 58, null, recipe.outputStack);
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
		transferRects.add(new TemplateRecipeHandler.RecipeTransferRect(new Rectangle(64-xOffset, 39-yOffset, 48, 8), getRecipeId(), new Object[0]));
	}

	@Override
	public void loadCraftingRecipes(String outputId, Object... results)
	{
		if(outputId.equals(getRecipeId()))
		{
			for(SolarNeutronRecipe irecipe : getRecipes())
			{
				arecipes.add(new CachedIORecipe(irecipe));
			}
		}
		else if(outputId.equals("gas") && results.length == 1 && results[0] instanceof GasStack)
		{
			for(SolarNeutronRecipe irecipe : getRecipes())
			{
				if(((GasStack)results[0]).isGasEqual(irecipe.getOutput().output))
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
		if(inputId.equals("gas") && ingredients.length == 1 && ingredients[0] instanceof GasStack)
		{
			for(SolarNeutronRecipe irecipe : getRecipes())
			{
				if(irecipe.getInput().ingredient.isGasEqual((GasStack)ingredients[0]))
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

		if(xAxis >= 26 && xAxis <= 42 && yAxis >= 14+4 && yAxis <= 72+4)
		{
			currenttip.add(LangUtils.localizeGasStack(((CachedIORecipe)arecipes.get(recipe)).inputStack));
		}
		else if(xAxis >= 134 && xAxis <= 150 && yAxis >= 14+4 && yAxis <= 72+4)
		{
			currenttip.add(LangUtils.localizeGasStack(((CachedIORecipe)arecipes.get(recipe)).outputStack));
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

		if(xAxis >= 26 && xAxis <= 42 && yAxis >= 14+4 && yAxis <= 72+4)
		{
			gas = ((CachedIORecipe)arecipes.get(recipe)).inputStack;
		}
		else if(xAxis >= 134 && xAxis <= 150 && yAxis >= 14+4 && yAxis <= 72+4)
		{
			gas = ((CachedIORecipe)arecipes.get(recipe)).outputStack;
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

		return super.keyTyped(gui, keyChar, keyCode, recipe);
	}

	@Override
	public boolean mouseClicked(GuiRecipe gui, int button, int recipe)
	{
		Point point = GuiDraw.getMousePosition();

		int xAxis = point.x - (Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiLeft);
		int yAxis = point.y - (Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiTop);

		GasStack gas = null;

		if(xAxis >= 26 && xAxis <= 42 && yAxis >= 14+18 && yAxis <= 72+18)
		{
			gas = ((CachedIORecipe)arecipes.get(recipe)).inputStack;
		}
		else if(xAxis >= 134 && xAxis <= 150 && yAxis >= 14+18 && yAxis <= 72+18)
		{
			gas = ((CachedIORecipe)arecipes.get(recipe)).outputStack;
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
		public GasStack inputStack;
		public GasStack outputStack;

		@Override
		public PositionedStack getResult()
		{
			return null;
		}

		public CachedIORecipe(GasStack input, GasStack output)
		{
			inputStack = input;
			outputStack = output;
		}

		public CachedIORecipe(SolarNeutronRecipe recipe)
		{
			this(recipe.getInput().ingredient, recipe.getOutput().output);
		}
	}
}

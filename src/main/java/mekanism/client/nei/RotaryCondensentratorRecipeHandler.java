package mekanism.client.nei;

import java.awt.*;
import java.util.List;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.client.gui.GuiRotaryCondensentrator;
import mekanism.common.ObfuscatedNames;
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

import static codechicken.lib.gui.GuiDraw.changeTexture;
import static codechicken.lib.gui.GuiDraw.drawString;
import static codechicken.lib.gui.GuiDraw.drawTexturedModalRect;

public class RotaryCondensentratorRecipeHandler extends BaseRecipeHandler
{
	private int ticksPassed;

	public static int xOffset = 5;
	public static int yOffset = 12;

	@Override
	public String getRecipeName()
	{
		return MekanismUtils.localize("nei.rotaryCondensentrator");
	}

	@Override
	public String getOverlayIdentifier()
	{
		return "rotarycondensentrator";
	}

	@Override
	public String getGuiTexture()
	{
		return "mekanism:gui/nei/GuiRotaryCondensentrator.png";
	}

	@Override
	public Class getGuiClass()
	{
		return GuiRotaryCondensentrator.class;
	}

	public String getRecipeId()
	{
		return "mekanism.rotarycondensentrator";
	}

	@Override
	public void drawBackground(int i)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		changeTexture(getGuiTexture());
		drawTexturedModalRect(-2, 0, 3, yOffset, 170, 71);
	}

	@Override
	public void drawExtras(int i)
	{
		CachedIORecipe recipe = (CachedIORecipe)arecipes.get(i);

		if(recipe.type)
		{
			drawTexturedModalRect(64-xOffset, 39-yOffset, 176, 123, 48, 8);
		}
		else {
			drawTexturedModalRect(64-xOffset, 39-yOffset, 176, 115, 48, 8);
		}

		if(recipe.gasStack != null)
		{
			displayGauge(58, 26-xOffset, 14-yOffset, 176, 40, 58, null, recipe.gasStack);
		}

		if(recipe.fluidStack != null)
		{
			displayGauge(58, 134-xOffset, 14-yOffset, 176, 40, 58, recipe.fluidStack, null);
		}

		drawString(recipe.type ? MekanismUtils.localize("gui.condensentrating") : MekanismUtils.localize("gui.decondensentrating"), 6-xOffset, 74-yOffset, 0x404040, false);
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
			for(Gas gas : GasRegistry.getRegisteredGasses())
			{
				if(gas.hasFluid())
				{
					arecipes.add(new CachedIORecipe(new GasStack(gas, 1), new FluidStack(gas.getFluid(), 1), true));
					arecipes.add(new CachedIORecipe(new GasStack(gas, 1), new FluidStack(gas.getFluid(), 1), false));
				}
			}
		}
		else if(outputId.equals("gas") && results.length == 1 && results[0] instanceof GasStack)
		{
			GasStack gas = (GasStack)results[0];

			if(gas.getGas().hasFluid())
			{
				arecipes.add(new CachedIORecipe(new GasStack(gas.getGas(), 1), new FluidStack(gas.getGas().getFluid(), 1), false));
			}
		}
		else if(outputId.equals("fluid") && results.length == 1 && results[0] instanceof FluidStack)
		{
			FluidStack fluid = (FluidStack)results[0];
			Gas gas = GasRegistry.getGas(fluid.getFluid());

			if(gas != null)
			{
				arecipes.add(new CachedIORecipe(new GasStack(gas, 1), new FluidStack(fluid.getFluid(), 1), true));
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
			GasStack gas = (GasStack)ingredients[0];

			if(gas.getGas().hasFluid())
			{
				arecipes.add(new CachedIORecipe(new GasStack(gas.getGas(), 1), new FluidStack(gas.getGas().getFluid(), 1), true));
			}
		}
		else if(inputId.equals("fluid") && ingredients.length == 1 && ingredients[0] instanceof FluidStack)
		{
			FluidStack fluid = (FluidStack)ingredients[0];
			Gas gas = GasRegistry.getGas(fluid.getFluid());

			if(gas != null)
			{
				arecipes.add(new CachedIORecipe(new GasStack(gas, 1), new FluidStack(fluid.getFluid(), 1), false));
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
			currenttip.add(LangUtils.localizeGasStack(((CachedIORecipe)arecipes.get(recipe)).gasStack));
		}
		else if(xAxis >= 134 && xAxis <= 150 && yAxis >= 14+4 && yAxis <= 72+4)
		{
			currenttip.add(LangUtils.localizeFluidStack(((CachedIORecipe)arecipes.get(recipe)).fluidStack));
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

		if(xAxis >= 26 && xAxis <= 42 && yAxis >= 14+4 && yAxis <= 72+4)
		{
			gas = ((CachedIORecipe)arecipes.get(recipe)).gasStack;
		}
		else if(xAxis >= 134 && xAxis <= 150 && yAxis >= 14+4 && yAxis <= 72+4)
		{
			fluid = ((CachedIORecipe)arecipes.get(recipe)).fluidStack;
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

		if(xAxis >= 26 && xAxis <= 42 && yAxis >= 14+18 && yAxis <= 72+18)
		{
			gas = ((CachedIORecipe)arecipes.get(recipe)).gasStack;
		}
		else if(xAxis >= 134 && xAxis <= 150 && yAxis >= 14+18 && yAxis <= 72+18)
		{
			fluid = ((CachedIORecipe)arecipes.get(recipe)).fluidStack;
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

	@Override
	public void addGuiElements()
	{

	}

	public class CachedIORecipe extends TemplateRecipeHandler.CachedRecipe
	{
		public GasStack gasStack;
		public FluidStack fluidStack;

		/* true = condensentrating, false = decondensentrating */
		public boolean type;

		@Override
		public PositionedStack getResult()
		{
			return null;
		}

		public CachedIORecipe(GasStack gas, FluidStack fluid, boolean b)
		{
			gasStack = gas;
			fluidStack = fluid;
			type = b;
		}
	}
}

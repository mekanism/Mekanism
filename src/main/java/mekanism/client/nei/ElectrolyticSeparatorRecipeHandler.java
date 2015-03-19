package mekanism.client.nei;

import static codechicken.lib.gui.GuiDraw.changeTexture;
import static codechicken.lib.gui.GuiDraw.drawTexturedModalRect;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import mekanism.api.gas.GasStack;
import mekanism.client.gui.GuiElectrolyticSeparator;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiFluidGauge;
import mekanism.client.gui.element.GuiGasGauge;
import mekanism.client.gui.element.GuiGauge;
import mekanism.client.gui.element.GuiPowerBar;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiPowerBar.IPowerInfoHandler;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.common.ObfuscatedNames;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.FluidInput;
import mekanism.common.recipe.machines.SeparatorRecipe;
import mekanism.common.recipe.outputs.ChemicalPairOutput;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class ElectrolyticSeparatorRecipeHandler extends BaseRecipeHandler
{
	private int ticksPassed;
	
	public GuiFluidGauge fluidInput;
	public GuiGasGauge leftGas;
	public GuiGasGauge rightGas;

	public static int xOffset = 5;
	public static int yOffset = 9;
	
	@Override
	public void addGuiElements()
	{
		guiElements.add(fluidInput = GuiFluidGauge.getDummy(GuiGauge.Type.STANDARD, this, MekanismUtils.getResource(ResourceType.GUI, "GuiElectrolyticSeparator.png"), 5, 10));
		guiElements.add(leftGas = GuiGasGauge.getDummy(GuiGauge.Type.SMALL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiElectrolyticSeparator.png"), 58, 18));
		guiElements.add(rightGas = GuiGasGauge.getDummy(GuiGauge.Type.SMALL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiElectrolyticSeparator.png"), 100, 18));
		guiElements.add(new GuiPowerBar(this, new IPowerInfoHandler() {
			@Override
			public double getLevel()
			{
				return ticksPassed <= 20 ? ticksPassed / 20.0F : 1.0F;
			}
		}, MekanismUtils.getResource(ResourceType.GUI, stripTexture()), 164, 15));
		
		guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiElectrolyticSeparator.png"), 25, 34));
		guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiElectrolyticSeparator.png"), 58, 51));
		guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiElectrolyticSeparator.png"), 100, 51));
		guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiElectrolyticSeparator.png"), 142, 34).with(SlotOverlay.POWER));

		guiElements.add(new GuiProgress(new IProgressInfoHandler()
		{
			@Override
			public double getProgress()
			{
				return 1;
			}
		}, ProgressBar.BI, this, MekanismUtils.getResource(ResourceType.GUI, "GuiElectrolyticSeparator.png"), 78, 29));
	}

	@Override
	public String getRecipeName()
	{
		return MekanismUtils.localize("tile.MachineBlock2.ElectrolyticSeparator.name");
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

	public Collection<SeparatorRecipe> getRecipes()
	{
		return Recipe.ELECTROLYTIC_SEPARATOR.get().values();
	}

	@Override
	public void drawBackground(int i)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		changeTexture(getGuiTexture());
		drawTexturedModalRect(-1, 0, 4, yOffset, 167, 62);
		
		for(GuiElement e : guiElements)
		{
			e.renderBackground(0, 0, -xOffset, -yOffset);
		}
	}

	@Override
	public void drawExtras(int i)
	{
		CachedIORecipe recipe = (CachedIORecipe)arecipes.get(i);

		if(recipe.fluidInput != null)
		{
			fluidInput.setDummyType(recipe.fluidInput.ingredient.getFluid());
			fluidInput.renderScale(0, 0, -xOffset, -yOffset);
		}

		if(recipe.outputPair.leftGas != null)
		{
			displayGauge(28, 59-xOffset, 19-yOffset, 176, 68, 28, null, recipe.outputPair.leftGas);
			leftGas.setDummyType(recipe.outputPair.leftGas.getGas());
			leftGas.renderScale(0, 0, -xOffset, -yOffset);
		}

		if(recipe.outputPair.rightGas != null)
		{
			displayGauge(28, 101-xOffset, 19-yOffset, 176, 68, 28, null, recipe.outputPair.rightGas);
			rightGas.setDummyType(recipe.outputPair.rightGas.getGas());
			rightGas.renderScale(0, 0, -xOffset, -yOffset);
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
			for(SeparatorRecipe irecipe : getRecipes())
			{
				arecipes.add(new CachedIORecipe(irecipe));
			}
		}
		else if(outputId.equals("gas") && results.length == 1 && results[0] instanceof GasStack)
		{
			for(SeparatorRecipe irecipe : getRecipes())
			{
				if(irecipe.recipeOutput.containsType((GasStack)results[0]))
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
			for(SeparatorRecipe irecipe : getRecipes())
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

		if(xAxis >= 6 && xAxis <= 22 && yAxis >= 11+7 && yAxis <= 69+7)
		{
			currenttip.add(LangUtils.localizeFluidStack(((CachedIORecipe)arecipes.get(recipe)).fluidInput.ingredient));
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

		if(xAxis >= 6 && xAxis <= 22 && yAxis >= 11+7 && yAxis <= 69+7)
		{
			fluid = ((CachedIORecipe)arecipes.get(recipe)).fluidInput.ingredient;
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

		if(xAxis >= 6 && xAxis <= 22 && yAxis >= 11+7 && yAxis <= 69+7)
		{
			fluid = ((CachedIORecipe)arecipes.get(recipe)).fluidInput.ingredient;
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
		public FluidInput fluidInput;
		public ChemicalPairOutput outputPair;

		@Override
		public PositionedStack getResult()
		{
			return null;
		}

		public CachedIORecipe(FluidInput input, ChemicalPairOutput pair)
		{
			fluidInput = input;
			outputPair = pair;
		}

		public CachedIORecipe(SeparatorRecipe recipe)
		{
			this(recipe.recipeInput, recipe.recipeOutput);
		}
	}
}

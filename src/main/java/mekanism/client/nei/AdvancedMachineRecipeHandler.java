package mekanism.client.nei;

import java.awt.*;
import java.util.Collection;
import java.util.List;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiPowerBar;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiPowerBar.IPowerInfoHandler;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.common.ObfuscatedNames;
import mekanism.common.recipe.inputs.AdvancedMachineInput;
import mekanism.common.recipe.machines.AdvancedMachineRecipe;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.TemplateRecipeHandler;
import static codechicken.lib.gui.GuiDraw.changeTexture;
import static codechicken.lib.gui.GuiDraw.drawTexturedModalRect;

public abstract class AdvancedMachineRecipeHandler extends BaseRecipeHandler
{
	private int ticksPassed;

	public abstract String getRecipeId();

	public abstract Collection<? extends AdvancedMachineRecipe> getRecipes();

	public abstract List<ItemStack> getFuelStacks(Gas gasType);
	
	public abstract ProgressBar getProgressType();
	
	@Override
	public void addGuiElements()
	{
		guiElements.add(new GuiSlot(SlotType.INPUT, this, MekanismUtils.getResource(ResourceType.GUI, stripTexture()), 55, 16));
		guiElements.add(new GuiSlot(SlotType.POWER, this, MekanismUtils.getResource(ResourceType.GUI, stripTexture()), 30, 34).with(SlotOverlay.POWER));
		guiElements.add(new GuiSlot(SlotType.EXTRA, this, MekanismUtils.getResource(ResourceType.GUI, stripTexture()), 55, 52));
		guiElements.add(new GuiSlot(SlotType.OUTPUT_LARGE, this, MekanismUtils.getResource(ResourceType.GUI, stripTexture()), 111, 30));
		
		guiElements.add(new GuiPowerBar(this, new IPowerInfoHandler() {
			@Override
			public double getLevel()
			{
				return ticksPassed <= 20 ? ticksPassed / 20.0F : 1.0F;
			}
		}, MekanismUtils.getResource(ResourceType.GUI, stripTexture()), 164, 15));
		guiElements.add(new GuiProgress(new IProgressInfoHandler()
		{
			@Override
			public double getProgress()
			{
				return ticksPassed >= 40 ? (ticksPassed - 40) % 20 / 20.0F : 0.0F;
			}
		}, getProgressType(), this, MekanismUtils.getResource(ResourceType.GUI, stripTexture()), 77, 37));
	}

	@Override
	public void drawBackground(int i)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		changeTexture(getGuiTexture());
		drawTexturedModalRect(12, 0, 28, 5, 144, 68);
		
		for(GuiElement e : guiElements)
		{
			e.renderBackground(0, 0, -16, -5);
		}
	}

	@Override
	public void drawExtras(int i)
	{
		CachedIORecipe recipe = (CachedIORecipe)arecipes.get(i);

		if(recipe.input.gasType != null && ticksPassed >= 20)
		{
			int displayInt = ticksPassed < 40 ? (ticksPassed-20)*12 / 20 : 12;
			displayGauge(45, 32 + 12 - displayInt, 6, displayInt, new GasStack(recipe.input.gasType, 1));
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
		transferRects.add(new TemplateRecipeHandler.RecipeTransferRect(new Rectangle(63, 34, 24, 7), getRecipeId(), new Object[0]));
	}

	@Override
	public void loadCraftingRecipes(String outputId, Object... results)
	{
		if(outputId.equals(getRecipeId()))
		{
			for(AdvancedMachineRecipe<?> irecipe : getRecipes())
			{
				arecipes.add(new CachedIORecipe(irecipe, getFuelStacks(irecipe.getInput().gasType)));
			}
		}
		else {
			super.loadCraftingRecipes(outputId, results);
		}
	}

	@Override
	public void loadCraftingRecipes(ItemStack result)
	{
		for(AdvancedMachineRecipe<?> irecipe : getRecipes())
		{
			if(NEIServerUtils.areStacksSameTypeCrafting(irecipe.getOutput().output, result))
			{
				arecipes.add(new CachedIORecipe(irecipe, getFuelStacks(irecipe.getInput().gasType)));
			}
		}
	}
	
	@Override
	public String getGuiTexture()
	{
		return "mekanism:gui/GuiAdvancedMachine.png";
	}

	@Override
	public void loadUsageRecipes(String inputId, Object... ingredients)
	{
		if(inputId.equals("gas") && ingredients.length == 1 && ingredients[0] instanceof GasStack)
		{
			for(AdvancedMachineRecipe<?> irecipe : getRecipes())
			{
				if(irecipe.getInput().gasType == ((GasStack)ingredients[0]).getGas())
				{
					arecipes.add(new CachedIORecipe(irecipe, getFuelStacks(irecipe.getInput().gasType)));
				}
			}
		}
		else {
			super.loadUsageRecipes(inputId, ingredients);
		}
	}

	@Override
	public void loadUsageRecipes(ItemStack ingredient)
	{
		for(AdvancedMachineRecipe<?> irecipe : getRecipes())
		{
			if(NEIServerUtils.areStacksSameTypeCrafting(irecipe.getInput().itemStack, ingredient))
			{
				arecipes.add(new CachedIORecipe(irecipe, getFuelStacks(irecipe.getInput().gasType)));
			}
		}
	}

	@Override
	public List<String> handleTooltip(GuiRecipe gui, List<String> currenttip, int recipe)
	{
		Point point = GuiDraw.getMousePosition();
		Point offset = gui.getRecipePosition(recipe);

		int xAxis = point.x-(Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiLeft)-offset.x;
		int yAxis = point.y-(Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiTop)-offset.y;

		if(xAxis >= 45 && xAxis <= 51 && yAxis >= 33 && yAxis <= 45)
		{
			currenttip.add(((CachedIORecipe)arecipes.get(recipe)).input.gasType.getLocalizedName());
		}

		return super.handleTooltip(gui, currenttip, recipe);
	}

	@Override
	public boolean keyTyped(GuiRecipe gui, char keyChar, int keyCode, int recipe)
	{
		Point point = GuiDraw.getMousePosition();
		Point offset = gui.getRecipePosition(recipe);

		int xAxis = point.x-(Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiLeft)-offset.x;
		int yAxis = point.y-(Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiTop)-offset.y;

		GasStack stack = null;

		if(xAxis >= 45 && xAxis <= 51 && yAxis >= 33 && yAxis <= 45)
		{
			stack = new GasStack(((CachedIORecipe)arecipes.get(recipe)).input.gasType, 1);
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
		Point offset = gui.getRecipePosition(recipe);

		int xAxis = point.x-(Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiLeft)-offset.x;
		int yAxis = point.y-(Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiTop)-offset.y;

		GasStack stack = null;

		if(xAxis >= 45 && xAxis <= 51 && yAxis >= 33 && yAxis <= 45)
		{
			stack = new GasStack(((CachedIORecipe)arecipes.get(recipe)).input.gasType, 1);
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

	public class CachedIORecipe extends TemplateRecipeHandler.CachedRecipe
	{
		public List<ItemStack> fuelStacks;

		public AdvancedMachineInput input;

		public PositionedStack outputStack;

		@Override
		public PositionedStack getIngredient()
		{
			return new PositionedStack(input.itemStack, 40, 12);
		}

		@Override
		public PositionedStack getResult()
		{
			return outputStack;
		}

		@Override
		public PositionedStack getOtherStack()
		{
			return new PositionedStack(fuelStacks.get(cycleticks/40 % fuelStacks.size()), 40, 48);
		}

		public CachedIORecipe(AdvancedMachineInput adv, ItemStack output, List<ItemStack> fuels)
		{
			input = adv;
			outputStack = new PositionedStack(output, 100, 30);
			fuelStacks = fuels;
		}

		public CachedIORecipe(AdvancedMachineRecipe<?> recipe, List<ItemStack> fuels)
		{
			this(recipe.getInput(), recipe.getOutput().output, fuels);
		}
	}
}

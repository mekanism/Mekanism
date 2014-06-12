package mekanism.client.nei;

import static codechicken.lib.gui.GuiDraw.changeTexture;
import static codechicken.lib.gui.GuiDraw.drawTexturedModalRect;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import mekanism.api.AdvancedInput;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.common.ObfuscatedNames;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.TemplateRecipeHandler;

public abstract class AdvancedMachineRecipeHandler extends BaseRecipeHandler
{
	private int ticksPassed;

	public abstract String getRecipeId();

	public abstract Set<Entry<AdvancedInput, ItemStack>> getRecipes();

	public abstract List<ItemStack> getFuelStacks(Gas gasType);

	@Override
	public void drawBackground(int i)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		changeTexture(getGuiTexture());
		drawTexturedModalRect(12, 0, 28, 5, 144, 68);
	}

	@Override
	public void drawExtras(int i)
	{
		CachedIORecipe recipe = (CachedIORecipe)arecipes.get(i);

		float f = ticksPassed >= 20 ? (ticksPassed - 20) % 20 / 20.0F : 0;
		drawProgressBar(63, 34, 176, 0, 24, 7, f, 0);

		if(recipe.input.gasType != null)
		{
			int displayInt = ticksPassed < 20 ? ticksPassed*12 / 20 : 12;
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
			for(Map.Entry<AdvancedInput, ItemStack> irecipe : getRecipes())
			{
				arecipes.add(new CachedIORecipe(irecipe, getFuelStacks(irecipe.getKey().gasType)));
			}
		}
		else {
			super.loadCraftingRecipes(outputId, results);
		}
	}

	@Override
	public void loadCraftingRecipes(ItemStack result)
	{
		for(Map.Entry<AdvancedInput, ItemStack> irecipe : getRecipes())
		{
			if(NEIServerUtils.areStacksSameTypeCrafting((ItemStack)irecipe.getValue(), result))
			{
				arecipes.add(new CachedIORecipe(irecipe, getFuelStacks(irecipe.getKey().gasType)));
			}
		}
	}

	@Override
	public void loadUsageRecipes(String inputId, Object... ingredients)
	{
		if(inputId.equals("gas") && ingredients.length == 1 && ingredients[0] instanceof GasStack)
		{
			for(Map.Entry<AdvancedInput, ItemStack> irecipe : getRecipes())
			{
				if(irecipe.getKey().gasType == ((GasStack)ingredients[0]).getGas())
				{
					arecipes.add(new CachedIORecipe(irecipe, getFuelStacks(irecipe.getKey().gasType)));
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
		for(Map.Entry<AdvancedInput, ItemStack> irecipe : getRecipes())
		{
			if(NEIServerUtils.areStacksSameTypeCrafting(irecipe.getKey().itemStack, ingredient))
			{
				arecipes.add(new CachedIORecipe(irecipe, getFuelStacks(irecipe.getKey().gasType)));
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

	@Override
	public void addGuiElements()
	{

	}

	public class CachedIORecipe extends TemplateRecipeHandler.CachedRecipe
	{
		public List<ItemStack> fuelStacks;

		public AdvancedInput input;

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

		public CachedIORecipe(AdvancedInput adv, ItemStack output, List<ItemStack> fuels)
		{
			input = adv;
			outputStack = new PositionedStack(output, 100, 30);
			fuelStacks = fuels;
		}

		public CachedIORecipe(Map.Entry recipe, List<ItemStack> fuels)
		{
			this((AdvancedInput)recipe.getKey(), (ItemStack)recipe.getValue(), fuels);
		}
	}
}

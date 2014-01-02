package mekanism.client.nei;

import static codechicken.core.gui.GuiDraw.changeTexture;
import static codechicken.core.gui.GuiDraw.drawTexturedModalRect;

import java.awt.Rectangle;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;

public abstract class AdvancedMachineRecipeHandler extends TemplateRecipeHandler
{
	private int ticksPassed;

	public abstract String getRecipeId();

	public abstract Set<Entry<ItemStack, ItemStack>> getRecipes();
	
	public abstract List<ItemStack> getFuelStacks();

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
		float f = ticksPassed >= 40 ? (ticksPassed - 40) % 20 / 20.0F : 0.0F;
		drawProgressBar(63, 34, 176, 0, 24, 7, f, 0);
		
		f = ticksPassed >= 20 && ticksPassed < 40 ? (ticksPassed - 20) % 20 / 20.0F : 1.0F;
		if(ticksPassed < 20) f = 0.0F;
		drawProgressBar(45, 32, 176, 7, 5, 12, f, 3);
		
		f = ticksPassed <= 20 ? ticksPassed / 20.0F : 1.0F;
		drawProgressBar(149, 12, 176, 19, 4, 52, f, 3);
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
			for(Map.Entry irecipe : getRecipes())
			{
				arecipes.add(new CachedIORecipe(irecipe, getFuelStacks()));
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
				arecipes.add(new CachedIORecipe(irecipe, getFuelStacks()));
			}
		}
	}

	@Override
	public void loadUsageRecipes(ItemStack ingredient)
	{
		for(Map.Entry irecipe : getRecipes())
		{
			if(NEIServerUtils.areStacksSameTypeCrafting((ItemStack)irecipe.getKey(), ingredient))
			{
				arecipes.add(new CachedIORecipe(irecipe, getFuelStacks()));
			}
		}
	}

	public class CachedIORecipe extends TemplateRecipeHandler.CachedRecipe
	{
		public List<ItemStack> fuelStacks;
		
		public PositionedStack inputStack;
		public PositionedStack outputStack;

		@Override
		public PositionedStack getIngredient()
		{
			return inputStack;
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

		public CachedIORecipe(ItemStack input, ItemStack output, List<ItemStack> fuels)
		{
			super();
			inputStack = new PositionedStack(input, 40, 12);
			outputStack = new PositionedStack(output, 100, 30);
			fuelStacks = fuels;
		}

		public CachedIORecipe(Map.Entry recipe, List<ItemStack> fuels)
		{
			this((ItemStack)recipe.getKey(), (ItemStack)recipe.getValue(), fuels);
		}
	}
}

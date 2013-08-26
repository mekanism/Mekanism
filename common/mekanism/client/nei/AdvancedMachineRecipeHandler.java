package mekanism.client.nei;

import java.awt.Rectangle;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;

import static codechicken.core.gui.GuiDraw.*;

public abstract class AdvancedMachineRecipeHandler extends TemplateRecipeHandler
{
	private int ticksPassed;

	public abstract String getRecipeId();
	
	public abstract ItemStack getFuelStack();

	public abstract Set<Entry<ItemStack, ItemStack>> getRecipes();

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
		drawProgressBar(63, 34, 176 + 26, 0, 24, 7, f, 0);
		
		f = ticksPassed >= 20 && ticksPassed < 40 ? (ticksPassed - 20) % 20 / 20.0F : 1.0F;
		if(ticksPassed < 20) f = 0.0F;
		drawProgressBar(45, 32, 176 + 26, 7, 5, 12, f, 3);
		
		f = ticksPassed <= 20 ? ticksPassed / 20.0F : 1.0F;
		drawProgressBar(149, 12, 176 + 26, 19, 4, 52, f, 3);
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
				arecipes.add(new CachedIORecipe(irecipe, getFuelStack()));
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
				arecipes.add(new CachedIORecipe(irecipe, getFuelStack()));
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
				arecipes.add(new CachedIORecipe(irecipe, getFuelStack()));
			}
		}
	}

	public class CachedIORecipe extends TemplateRecipeHandler.CachedRecipe
	{
		public PositionedStack inputStack;
		public PositionedStack outputStack;
		public PositionedStack fuelStack;

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
			return fuelStack;
		}

		public CachedIORecipe(ItemStack input, ItemStack output, ItemStack fuel)
		{
			super();
			inputStack = new PositionedStack(input, 40, 12);
			outputStack = new PositionedStack(output, 100, 30);
			fuelStack = new PositionedStack(fuel, 40, 48);
		}

		public CachedIORecipe(Map.Entry recipe, ItemStack fuel)
		{
			this((ItemStack)recipe.getKey(), (ItemStack)recipe.getValue(), fuel);
		}
	}
}

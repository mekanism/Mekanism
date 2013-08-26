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

public abstract class MachineRecipeHandler extends TemplateRecipeHandler
{
	private int ticksPassed;

	public abstract String getRecipeId();

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
		float f = ticksPassed >= 20 ? (ticksPassed - 20) % 20 / 20.0F : 0.0F;
		drawProgressBar(63, 34, 176 + 26, 0, 24, 7, f, 0);
		f = ticksPassed <= 20 ? ticksPassed / 20.0F : 1.0F;
		drawProgressBar(149, 12, 176 + 26, 7, 4, 52, f, 3);
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
		public PositionedStack input;
		public PositionedStack output;

		@Override
		public PositionedStack getIngredient()
		{
			return input;
		}

		@Override
		public PositionedStack getResult()
		{
			return output;
		}

		public CachedIORecipe(ItemStack itemstack, ItemStack itemstack1)
		{
			super();
			input = new PositionedStack(itemstack, 40, 12);
			output = new PositionedStack(itemstack1, 100, 30);
		}

		public CachedIORecipe(Map.Entry recipe)
		{
			this((ItemStack)recipe.getKey(), (ItemStack)recipe.getValue());
		}
	}
}

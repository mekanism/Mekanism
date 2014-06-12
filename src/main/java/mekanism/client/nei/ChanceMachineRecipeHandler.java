package mekanism.client.nei;

import static codechicken.lib.gui.GuiDraw.changeTexture;
import static codechicken.lib.gui.GuiDraw.drawTexturedModalRect;
import static codechicken.lib.gui.GuiDraw.drawString;

import java.awt.Rectangle;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import mekanism.api.ChanceOutput;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;

public abstract class ChanceMachineRecipeHandler extends TemplateRecipeHandler
{
	private int ticksPassed;

	public abstract String getRecipeId();

	public abstract Set<Entry<ItemStack, ChanceOutput>> getRecipes();

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

		float f = ticksPassed >= 20 ? (ticksPassed - 20) % 20 / 20.0F : 0.0F;
		drawProgressBar(63, 34, 176, 0, 24, 7, f, 0);
		f = ticksPassed <= 20 ? ticksPassed / 20.0F : 1.0F;
		drawProgressBar(149, 12, 176, 7, 4, 52, f, 3);

		if(recipe.output.hasSecondary())
		{
			drawString(Math.round(recipe.output.secondaryChance*100) + "%", 116, 52, 0x404040, false);
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
		for(Map.Entry<ItemStack, ChanceOutput> irecipe : getRecipes())
		{
			if(irecipe.getValue().hasPrimary() && NEIServerUtils.areStacksSameTypeCrafting(irecipe.getValue().primaryOutput, result))
			{
				arecipes.add(new CachedIORecipe(irecipe));
			}
			else if(irecipe.getValue().hasSecondary() && NEIServerUtils.areStacksSameTypeCrafting(irecipe.getValue().secondaryOutput, result))
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
		public ChanceOutput output;

		@Override
		public PositionedStack getIngredient()
		{
			return input;
		}

		@Override
		public PositionedStack getResult()
		{
			if(output.hasPrimary())
			{
				return new PositionedStack(output.primaryOutput, 100, 30);
			}

			return null;
		}

		@Override
		public PositionedStack getOtherStack()
		{
			if(output.hasSecondary())
			{
				return new PositionedStack(output.secondaryOutput, 116, 30);
			}

			return null;
		}

		public CachedIORecipe(ItemStack itemstack, ChanceOutput chance)
		{
			input = new PositionedStack(itemstack, 40, 12);
			output = chance;
		}

		public CachedIORecipe(Map.Entry recipe)
		{
			this((ItemStack)recipe.getKey(), (ChanceOutput)recipe.getValue());
		}
	}
}

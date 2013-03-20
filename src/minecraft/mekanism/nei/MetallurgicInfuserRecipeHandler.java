package mekanism.nei;

import java.awt.Rectangle;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import mekanism.api.InfusionInput;
import mekanism.api.InfusionOutput;
import mekanism.api.InfusionType;
import mekanism.client.GuiMetallurgicInfuser;
import mekanism.common.Mekanism;
import mekanism.common.MekanismUtils;
import mekanism.common.TileEntityMetallurgicInfuser;
import mekanism.common.RecipeHandler.Recipe;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.forge.GuiContainerManager;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class MetallurgicInfuserRecipeHandler extends TemplateRecipeHandler
{
	int ticksPassed;
	
	@Override
	public String getRecipeName()
	{
		return "Metallurgic Infuser";
	}
	
	@Override
	public String getOverlayIdentifier()
	{
		return "infuser";
	}
	
	@Override
	public String getGuiTexture()
	{
		return "/mods/mekanism/gui/GuiMetallurgicInfuser.png";
	}
	
	@Override
	public Class getGuiClass()
	{
		return GuiMetallurgicInfuser.class;
	}

	public String getRecipeId()
	{
		return "mekanism.infuser";
	}
	
	public int getInfuseX(InfusionType type)
	{
		switch(type)
		{
			case COAL:
				return 4;
			case TIN:
				return 0;
			case DIAMOND:
				return 4;
			case BIO:
				return 8;
			default:
				return 0;
		}
	}
	
	public int getInfuseY(InfusionType type)
	{
		switch(type)
		{
			case COAL:
				return 0;
			case TIN:
				return 52;
			case DIAMOND:
				return 52;
			case BIO:
				return 0;
			default:
				return 0;
		}
	}
	
	public InfusionType getFromFuel(ItemStack itemStack)
	{
		if(MekanismUtils.getInfuseObject(itemStack) != null)
		{
			return MekanismUtils.getInfuseObject(itemStack).type;
		}
		
		return null;
	}
	
	public ItemStack getInfuseStack(InfusionType type)
	{
		switch(type)
		{
			case COAL:
				return new ItemStack(Mekanism.CompressedCarbon);
			case TIN:
				return new ItemStack(Mekanism.Dust, 1, 7);
			case DIAMOND:
				return new ItemStack(Mekanism.Dust, 1, 4);
			case BIO:
				try {
					return new ItemStack(MekanismGenerators.BioFuel);
				} catch(Throwable t) {}
			default:
				return null;
		}
	}

	public Set<Entry<InfusionInput, InfusionOutput>> getRecipes()
	{
		return Recipe.METALLURGIC_INFUSER.get().entrySet();
	}

	@Override
	public void drawBackground(GuiContainerManager guimanager, int i)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		guimanager.bindTexture(getGuiTexture());
		guimanager.drawTexturedModalRect(0, 0, 5, 15, 166, 56);
	}

	@Override
	public void drawExtras(GuiContainerManager guimanager, int i)
	{
		float f = ticksPassed >= 40 ? (ticksPassed - 40) % 20 / 20.0F : 0.0F;
		drawProgressBar(guimanager, 67, 32, 176 + 26, 104, 32, 8, f, 0);
		
		f = ticksPassed >= 20 && ticksPassed < 40 ? (ticksPassed - 20) % 20 / 20.0F : 1.0F;
		if(ticksPassed < 20) f = 0.0F;
		int infuseX = 176 + 26 + getInfuseX(getFromFuel(getOtherStacks(i).get(0).item));
		int infuseY = getInfuseY(getFromFuel(getOtherStacks(i).get(0).item));
		
		drawProgressBar(guimanager, 2, 2, infuseX, infuseY, 4, 52, f, 3);
		
		f = ticksPassed <= 20 ? ticksPassed / 20.0F : 1.0F;
		drawProgressBar(guimanager, 160, 2, 176 + 26, 0, 4, 52, f, 3);
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
		transferRects.add(new TemplateRecipeHandler.RecipeTransferRect(new Rectangle(67, 32, 32, 8), getRecipeId(), new Object[0]));
	}

	@Override
	public void loadCraftingRecipes(String outputId, Object... results)
	{
		if(outputId.equals(getRecipeId()))
		{
			for(Map.Entry irecipe : getRecipes())
			{
				arecipes.add(new CachedIORecipe(irecipe, getInfuseStack(((InfusionInput)irecipe.getKey()).infusionType)));
			}
		}
		else {
			super.loadCraftingRecipes(outputId, results);
		}
	}
	
	@Override
	public int recipiesPerPage()
	{
		return 2;
	}

	@Override
	public void loadCraftingRecipes(ItemStack result)
	{
		for(Map.Entry irecipe : getRecipes())
		{
			if(NEIServerUtils.areStacksSameTypeCrafting(((InfusionOutput)irecipe.getValue()).resource, result))
			{
				arecipes.add(new CachedIORecipe(irecipe, getInfuseStack(((InfusionInput)irecipe.getKey()).infusionType)));
			}
		}
	}

	@Override
	public void loadUsageRecipes(ItemStack ingredient)
	{
		for(Map.Entry irecipe : getRecipes())
		{
			if(NEIServerUtils.areStacksSameTypeCrafting(((InfusionInput)irecipe.getKey()).inputSlot, ingredient))
			{
				arecipes.add(new CachedIORecipe(irecipe, getInfuseStack(((InfusionInput)irecipe.getKey()).infusionType)));
			}
		}
	}

	public class CachedIORecipe extends TemplateRecipeHandler.CachedRecipe
	{
		public PositionedStack inputStack;
		public PositionedStack outputStack;
		public PositionedStack infuseStack;

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
			return infuseStack;
		}

		public CachedIORecipe(ItemStack input, ItemStack output, ItemStack infuse)
		{
			super();
			inputStack = new PositionedStack(input, 46, 28);
			outputStack = new PositionedStack(output, 104, 28);
			infuseStack = new PositionedStack(infuse, 12, 20);
		}

		public CachedIORecipe(Map.Entry recipe, ItemStack infuse)
		{
			this(((InfusionInput)recipe.getKey()).inputSlot, ((InfusionOutput)recipe.getValue()).resource, infuse);
		}
	}
}

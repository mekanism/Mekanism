package mekanism.client.nei;

import java.awt.Rectangle;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import mekanism.api.infuse.InfuseObject;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.infuse.InfuseType;
import mekanism.api.infuse.InfusionInput;
import mekanism.api.infuse.InfusionOutput;
import mekanism.client.gui.GuiMetallurgicInfuser;
import mekanism.common.RecipeHandler.Recipe;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import static codechicken.core.gui.GuiDraw.*;

public class MetallurgicInfuserRecipeHandler extends TemplateRecipeHandler
{
	private int ticksPassed;
	
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
		return "mekanism:gui/GuiMetallurgicInfuser.png";
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
	
	public ItemStack getInfuseStack(InfuseType type)
	{
		for(Map.Entry<ItemStack, InfuseObject> obj : InfuseRegistry.getObjectMap().entrySet())
		{
			if(obj.getValue().type == type)
			{
				return obj.getKey();
			}
		}
		
		return null;
	}

	public Set<Entry<InfusionInput, InfusionOutput>> getRecipes()
	{
		return Recipe.METALLURGIC_INFUSER.get().entrySet();
	}

	@Override
	public void drawBackground(int i)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		changeTexture(getGuiTexture());
		drawTexturedModalRect(0, 0, 5, 15, 166, 56);
	}

	@Override
	public void drawExtras(int i)
	{
		InfuseType type = ((CachedIORecipe)arecipes.get(i)).infusionType;
		
		float f = ticksPassed >= 40 ? (ticksPassed - 40) % 20 / 20.0F : 0.0F;
		drawProgressBar(67, 32, 176 + 26, 52, 32, 8, f, 0);
		
		f = ticksPassed >= 20 && ticksPassed < 40 ? (ticksPassed - 20) % 20 / 20.0F : 1.0F;
		if(ticksPassed < 20) f = 0.0F;
		
		f = ticksPassed <= 20 ? ticksPassed / 20.0F : 1.0F;
		drawProgressBar(160, 2, 176 + 26, 0, 4, 52, f, 3);
		
		changeTexture(type.texture);
		drawProgressBar(2, 2, type.texX, type.texY, 4, 52, f, 3);
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
				arecipes.add(new CachedIORecipe(irecipe, getInfuseStack(((InfusionInput)irecipe.getKey()).infusionType), ((InfusionInput)irecipe.getKey()).infusionType));
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
				arecipes.add(new CachedIORecipe(irecipe, getInfuseStack(((InfusionInput)irecipe.getKey()).infusionType), ((InfusionInput)irecipe.getKey()).infusionType));
			}
		}
	}

	@Override
	public void loadUsageRecipes(ItemStack ingredient)
	{
		for(Map.Entry irecipe : getRecipes())
		{
			if(NEIServerUtils.areStacksSameTypeCrafting(((InfusionInput)irecipe.getKey()).inputStack, ingredient))
			{
				arecipes.add(new CachedIORecipe(irecipe, getInfuseStack(((InfusionInput)irecipe.getKey()).infusionType), ((InfusionInput)irecipe.getKey()).infusionType));
			}
		}
	}

	public class CachedIORecipe extends TemplateRecipeHandler.CachedRecipe
	{
		public PositionedStack inputStack;
		public PositionedStack outputStack;
		public PositionedStack infuseStack;
		
		public InfuseType infusionType;

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

		public CachedIORecipe(ItemStack input, ItemStack output, ItemStack infuse, InfuseType type)
		{
			super();
			
			inputStack = new PositionedStack(input, 46, 28);
			outputStack = new PositionedStack(output, 104, 28);
			
			if(infuse != null)
			{
				infuseStack = new PositionedStack(infuse, 12, 20);
			}
			
			infusionType = type;
		}

		public CachedIORecipe(Map.Entry recipe, ItemStack infuse, InfuseType type)
		{
			this(((InfusionInput)recipe.getKey()).inputStack, ((InfusionOutput)recipe.getValue()).resource, infuse, type);
		}
	}
}

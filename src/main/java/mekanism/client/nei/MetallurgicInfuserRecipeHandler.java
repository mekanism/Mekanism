package mekanism.client.nei;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import mekanism.api.infuse.InfuseObject;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.infuse.InfuseType;
import mekanism.client.gui.GuiMetallurgicInfuser;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiPowerBar;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiPowerBar.IPowerInfoHandler;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.machines.MetallurgicInfuserRecipe;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import static codechicken.lib.gui.GuiDraw.changeTexture;
import static codechicken.lib.gui.GuiDraw.drawTexturedModalRect;

public class MetallurgicInfuserRecipeHandler extends BaseRecipeHandler
{
	private int ticksPassed;
	
	@Override
	public void addGuiElements()
	{
		guiElements.add(new GuiSlot(SlotType.EXTRA, this, MekanismUtils.getResource(ResourceType.GUI, stripTexture()), 16, 34));
		guiElements.add(new GuiSlot(SlotType.INPUT, this, MekanismUtils.getResource(ResourceType.GUI, stripTexture()), 50, 42));
		guiElements.add(new GuiSlot(SlotType.POWER, this, MekanismUtils.getResource(ResourceType.GUI, stripTexture()), 142, 34).with(SlotOverlay.POWER));
		guiElements.add(new GuiSlot(SlotType.OUTPUT, this, MekanismUtils.getResource(ResourceType.GUI, stripTexture()), 108, 42));

		guiElements.add(new GuiPowerBar(this, new IPowerInfoHandler() {
			@Override
			public double getLevel()
			{
				return ticksPassed <= 20 ? ticksPassed / 20.0F : 1.0F;
			}
		}, MekanismUtils.getResource(ResourceType.GUI, stripTexture()), 164, 15));
		guiElements.add(new GuiProgress(new IProgressInfoHandler() {
			@Override
			public double getProgress()
			{
				return ticksPassed >= 40 ? (ticksPassed - 40) % 20 / 20.0F : 0.0F;
			}
		}, ProgressBar.MEDIUM, this, MekanismUtils.getResource(ResourceType.GUI, stripTexture()), 70, 46));
	}

	@Override
	public String getRecipeName()
	{
		return MekanismUtils.localize("tile.MachineBlock.MetallurgicInfuser.name");
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

	public List<ItemStack> getInfuseStacks(InfuseType type)
	{
		List<ItemStack> ret = new ArrayList<ItemStack>();

		for(Map.Entry<ItemStack, InfuseObject> obj : InfuseRegistry.getObjectMap().entrySet())
		{
			if(obj.getValue().type == type)
			{
				ret.add(obj.getKey());
			}
		}

		return ret;
	}

	public Collection<MetallurgicInfuserRecipe> getRecipes()
	{
		return Recipe.METALLURGIC_INFUSER.get().values();
	}

	@Override
	public void drawBackground(int i)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		changeTexture(getGuiTexture());
		drawTexturedModalRect(0, 0, 5, 15, 166, 56);
		
		for(GuiElement e : guiElements)
		{
			e.renderBackground(0, 0, -5, -15);
		}
	}

	@Override
	public void drawExtras(int i)
	{
		InfuseType type = ((CachedIORecipe)arecipes.get(i)).infusionType;

		float f = ticksPassed >= 20 && ticksPassed < 40 ? (ticksPassed - 20) % 20 / 20.0F : 1.0F;
		if(ticksPassed < 20) f = 0.0F;

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
			for(MetallurgicInfuserRecipe irecipe : getRecipes())
			{
				arecipes.add(new CachedIORecipe(irecipe, getInfuseStacks(irecipe.getInput().infuse.type), irecipe.getInput().infuse.type));
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
		for(MetallurgicInfuserRecipe irecipe : getRecipes())
		{
			if(NEIServerUtils.areStacksSameTypeCrafting(irecipe.getOutput().output, result))
			{
				arecipes.add(new CachedIORecipe(irecipe, getInfuseStacks(irecipe.getInput().infuse.type), irecipe.getInput().infuse.type));
			}
		}
	}

	@Override
	public void loadUsageRecipes(ItemStack ingredient)
	{
		for(MetallurgicInfuserRecipe irecipe : getRecipes())
		{
			if(NEIServerUtils.areStacksSameTypeCrafting(irecipe.getInput().inputStack, ingredient))
			{
				arecipes.add(new CachedIORecipe(irecipe, getInfuseStacks(irecipe.getInput().infuse.type), irecipe.getInput().infuse.type));
			}
			
			List<ItemStack> infuses;
			
			for(ItemStack stack : getInfuseStacks(irecipe.getInput().infuse.type)) 
			{
				if(NEIServerUtils.areStacksSameTypeCrafting(stack, ingredient))
				{
					infuses = new ArrayList<ItemStack>();
					infuses.add(stack);
					arecipes.add(new CachedIORecipe(irecipe, infuses, irecipe.getInput().infuse.type));
				}
			}
		}
	}

	public class CachedIORecipe extends TemplateRecipeHandler.CachedRecipe
	{
		public List<ItemStack> infuseStacks;

		public PositionedStack inputStack;
		public PositionedStack outputStack;

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
			return new PositionedStack(infuseStacks.get(cycleticks/40 % infuseStacks.size()), 12, 20);
		}

		public CachedIORecipe(ItemStack input, ItemStack output, List<ItemStack> infuses, InfuseType type)
		{
			super();

			inputStack = new PositionedStack(input, 46, 28);
			outputStack = new PositionedStack(output, 104, 28);

			infuseStacks = infuses;

			infusionType = type;
		}

		public CachedIORecipe(MetallurgicInfuserRecipe recipe, List<ItemStack> infuses, InfuseType type)
		{
			this(recipe.getInput().inputStack, recipe.getOutput().output, infuses, type);
		}
	}
}

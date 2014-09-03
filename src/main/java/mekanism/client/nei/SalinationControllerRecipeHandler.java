package mekanism.client.nei;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import mekanism.client.gui.GuiSalinationController;
import mekanism.common.ObfuscatedNames;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.TemplateRecipeHandler;

import static codechicken.lib.gui.GuiDraw.changeTexture;
import static codechicken.lib.gui.GuiDraw.drawTexturedModalRect;

public class SalinationControllerRecipeHandler extends BaseRecipeHandler
{
	private int ticksPassed;

	private static Map<FluidStack, FluidStack> recipes = new HashMap<FluidStack, FluidStack>();

	public static int xOffset = 5;
	public static int yOffset = 12;

	static
	{
		if(recipes.isEmpty())
		{
			recipes.put(new FluidStack(FluidRegistry.WATER, 1), new FluidStack(FluidRegistry.getFluid("brine"), 1));
		}
	}

	@Override
	public String getRecipeName()
	{
		return MekanismUtils.localize("tile.BasicBlock.SalinationController.name");
	}

	@Override
	public String getOverlayIdentifier()
	{
		return "salinationcontroller";
	}

	@Override
	public String getGuiTexture()
	{
		return "mekanism:gui/nei/GuiSalinationController.png";
	}

	@Override
	public Class getGuiClass()
	{
		return GuiSalinationController.class;
	}

	public String getRecipeId()
	{
		return "mekanism.salinationcontroller";
	}

	public Set<Entry<FluidStack, FluidStack>> getRecipes()
	{
		return recipes.entrySet();
	}

	@Override
	public void drawBackground(int i)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		changeTexture(getGuiTexture());
		drawTexturedModalRect(-2, 0, 3, yOffset, 170, 62);
	}

	@Override
	public void drawExtras(int i)
	{
		CachedIORecipe recipe = (CachedIORecipe)arecipes.get(i);

		drawProgressBar(49-xOffset, 64-yOffset, 176, 59, 78, 8, ticksPassed < 20 ? ticksPassed % 20 / 20.0F : 1.0F, 0);

		if(recipe.fluidInput != null)
		{
			displayGauge(58, 7-xOffset, 14-yOffset, 176, 0, 58, recipe.fluidInput, null);
		}

		if(recipe.fluidOutput != null)
		{
			displayGauge(58, 153-xOffset, 14-yOffset, 176, 0, 58, recipe.fluidOutput, null);
		}
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		ticksPassed++;
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
		else if(outputId.equals("fluid") && results.length == 1 && results[0] instanceof FluidStack)
		{
			for(Map.Entry<FluidStack, FluidStack> irecipe : getRecipes())
			{
				if(((FluidStack)results[0]).isFluidEqual(irecipe.getValue()))
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
			for(Map.Entry<FluidStack, FluidStack> irecipe : getRecipes())
			{
				if(irecipe.getKey().isFluidEqual((FluidStack)ingredients[0]))
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

		if(xAxis >= 7 && xAxis <= 23 && yAxis >= 14+4 && yAxis <= 72+4)
		{
			currenttip.add(LangUtils.localizeFluidStack(((CachedIORecipe) arecipes.get(recipe)).fluidInput));
		}
		else if(xAxis >= 153 && xAxis <= 169 && yAxis >= 14+4 && yAxis <= 72+4)
		{
			currenttip.add(LangUtils.localizeFluidStack(((CachedIORecipe) arecipes.get(recipe)).fluidOutput));
		}

		return super.handleTooltip(gui, currenttip, recipe);
	}

	@Override
	public boolean keyTyped(GuiRecipe gui, char keyChar, int keyCode, int recipe)
	{
		Point point = GuiDraw.getMousePosition();

		int xAxis = point.x-(Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiLeft);
		int yAxis = point.y-(Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiTop);

		FluidStack stack = null;

		if(xAxis >= 7 && xAxis <= 23 && yAxis >= 14+4 && yAxis <= 72+4)
		{
			stack = ((CachedIORecipe)arecipes.get(recipe)).fluidInput;
		}
		else if(xAxis >= 153 && xAxis <= 169 && yAxis >= 14+4 && yAxis <= 72+4)
		{
			stack = ((CachedIORecipe)arecipes.get(recipe)).fluidOutput;
		}

		if(stack != null)
		{
			if(keyCode == NEIClientConfig.getKeyBinding("gui.recipe"))
			{
				if(doFluidLookup(stack, false))
				{
					return true;
				}
			}
			else if(keyCode == NEIClientConfig.getKeyBinding("gui.usage"))
			{
				if(doFluidLookup(stack, true))
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

		FluidStack stack = null;

		if(xAxis >= 7 && xAxis <= 23 && yAxis >= 14+4 && yAxis <= 72+4)
		{
			stack = ((CachedIORecipe)arecipes.get(recipe)).fluidInput;
		}
		else if(xAxis >= 153 && xAxis <= 169 && yAxis >= 14+4 && yAxis <= 72+4)
		{
			stack = ((CachedIORecipe)arecipes.get(recipe)).fluidOutput;
		}

		if(stack != null)
		{
			if(button == 0)
			{
				if(doFluidLookup(stack, false))
				{
					return true;
				}
			}
			else if(button == 1)
			{
				if(doFluidLookup(stack, true))
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

	@Override
	public void addGuiElements()
	{

	}

	public class CachedIORecipe extends TemplateRecipeHandler.CachedRecipe
	{
		public FluidStack fluidInput;
		public FluidStack fluidOutput;

		@Override
		public PositionedStack getResult()
		{
			return null;
		}

		public CachedIORecipe(FluidStack input, FluidStack output)
		{
			fluidInput = input;
			fluidOutput = output;
		}

		public CachedIORecipe(Map.Entry recipe)
		{
			this((FluidStack)recipe.getKey(), (FluidStack)recipe.getValue());
		}
	}
}

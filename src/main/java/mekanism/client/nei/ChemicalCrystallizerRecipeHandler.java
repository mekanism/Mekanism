package mekanism.client.nei;

import static codechicken.lib.gui.GuiDraw.changeTexture;
import static codechicken.lib.gui.GuiDraw.drawTexturedModalRect;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.List;

import mekanism.api.gas.GasStack;
import mekanism.client.gui.GuiChemicalCrystallizer;
import mekanism.common.ObfuscatedNames;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.machines.CrystallizerRecipe;
import mekanism.common.util.LangUtils;
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

public class ChemicalCrystallizerRecipeHandler extends BaseRecipeHandler
{
	private int ticksPassed;

	public static int xOffset = 5;
	public static int yOffset = 3;

	@Override
	public String getRecipeName()
	{
		return MekanismUtils.localize("tile.MachineBlock2.ChemicalCrystallizer.name");
	}

	@Override
	public String getOverlayIdentifier()
	{
		return "chemicalcrystallizer";
	}

	@Override
	public String getGuiTexture()
	{
		return "mekanism:gui/nei/GuiChemicalCrystallizer.png";
	}

	@Override
	public Class getGuiClass()
	{
		return GuiChemicalCrystallizer.class;
	}

	public String getRecipeId()
	{
		return "mekanism.chemicalcrystallizer";
	}

	public Collection<CrystallizerRecipe> getRecipes()
	{
		return Recipe.CHEMICAL_CRYSTALLIZER.get().values();
	}

	@Override
	public void drawBackground(int i)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		changeTexture(getGuiTexture());
		drawTexturedModalRect(-2, 0, 3, yOffset, 147, 79);
	}

	@Override
	public void drawExtras(int i)
	{
		GasStack gas = ((CachedIORecipe)arecipes.get(i)).inputStack;

		float f = ticksPassed % 20 / 20.0F;
		drawProgressBar(53-xOffset, 61-yOffset, 176, 63, 48, 8, f, 0);

		if(gas != null)
		{
			displayGauge(58, 6-xOffset, 5-yOffset, 176, 4, 58, null, gas);
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
		transferRects.add(new TemplateRecipeHandler.RecipeTransferRect(new Rectangle(53-xOffset, 61-yOffset, 48, 8), getRecipeId(), new Object[0]));
	}

	@Override
	public void loadCraftingRecipes(String outputId, Object... results)
	{
		if(outputId.equals(getRecipeId()))
		{
			for(CrystallizerRecipe irecipe : getRecipes())
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
		for(CrystallizerRecipe irecipe : getRecipes())
		{
			if(NEIServerUtils.areStacksSameTypeCrafting(irecipe.getOutput().output, result))
			{
				arecipes.add(new CachedIORecipe(irecipe));
			}
		}
	}

	@Override
	public List<String> handleTooltip(GuiRecipe gui, List<String> currenttip, int recipe)
	{
		Point point = GuiDraw.getMousePosition();

		int xAxis = point.x-(Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiLeft);
		int yAxis = point.y-(Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiTop);

		if(xAxis >= 6 && xAxis <= 22 && yAxis >= 5+13 && yAxis <= 63+13)
		{
			currenttip.add(LangUtils.localizeGasStack(((CachedIORecipe)arecipes.get(recipe)).inputStack));
		}

		return super.handleTooltip(gui, currenttip, recipe);
	}

	@Override
	public boolean keyTyped(GuiRecipe gui, char keyChar, int keyCode, int recipe)
	{
		Point point = GuiDraw.getMousePosition();

		int xAxis = point.x-(Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiLeft);
		int yAxis = point.y-(Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiTop);

		GasStack stack = null;

		if(xAxis >= 6 && xAxis <= 22 && yAxis >= 5+13 && yAxis <= 63+13)
		{
			stack = ((CachedIORecipe)arecipes.get(recipe)).inputStack;
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

		int xAxis = point.x - (Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiLeft);
		int yAxis = point.y - (Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiTop);

		GasStack stack = null;

		if(xAxis >= 6 && xAxis <= 22 && yAxis >= 5+13 && yAxis <= 63+13)
		{
			stack = ((CachedIORecipe)arecipes.get(recipe)).inputStack;
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
	public int recipiesPerPage()
	{
		return 1;
	}

	@Override
	public void loadUsageRecipes(String inputId, Object... ingredients)
	{
		if(inputId.equals("gas") && ingredients.length == 1 && ingredients[0] instanceof GasStack)
		{
			for(CrystallizerRecipe irecipe : getRecipes())
			{
				if(irecipe.getInput().ingredient.isGasEqual((GasStack)ingredients[0]))
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
	public void addGuiElements()
	{

	}

	public class CachedIORecipe extends TemplateRecipeHandler.CachedRecipe
	{
		public GasStack inputStack;
		public PositionedStack outputStack;

		@Override
		public PositionedStack getResult()
		{
			return outputStack;
		}

		public CachedIORecipe(GasStack input, ItemStack output)
		{
			inputStack = input;
			outputStack = new PositionedStack(output, 131-xOffset, 57-yOffset);
		}

		public CachedIORecipe(CrystallizerRecipe recipe)
		{
			this(recipe.getInput().ingredient, recipe.getOutput().output);
		}
	}
}

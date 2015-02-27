package mekanism.client.nei;

import static codechicken.lib.gui.GuiDraw.changeTexture;
import static codechicken.lib.gui.GuiDraw.drawTexturedModalRect;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.List;

import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.client.gui.GuiChemicalDissolutionChamber;
import mekanism.common.ObfuscatedNames;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.machines.DissolutionRecipe;
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

public class ChemicalDissolutionChamberRecipeHandler extends BaseRecipeHandler
{
	private int ticksPassed;

	public static int xOffset = 5;
	public static int yOffset = 3;

	@Override
	public String getRecipeName()
	{
		return MekanismUtils.localize("gui.chemicalDissolutionChamber.short");
	}

	@Override
	public String getOverlayIdentifier()
	{
		return "chemicaldissolutionchamber";
	}

	@Override
	public String getGuiTexture()
	{
		return "mekanism:gui/nei/GuiChemicalDissolutionChamber.png";
	}

	@Override
	public Class getGuiClass()
	{
		return GuiChemicalDissolutionChamber.class;
	}

	public String getRecipeId()
	{
		return "mekanism.chemicaldissolutionchamber";
	}

	public Collection<DissolutionRecipe> getRecipes()
	{
		return Recipe.CHEMICAL_DISSOLUTION_CHAMBER.get().values();
	}

	@Override
	public void drawBackground(int i)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		changeTexture(getGuiTexture());
		drawTexturedModalRect(-2, 0, 3, yOffset, 170, 79);
	}

	@Override
	public void drawExtras(int i)
	{
		GasStack gas = ((CachedIORecipe)arecipes.get(i)).outputStack;

		float f = ticksPassed % 20 / 20.0F;
		drawProgressBar(64-xOffset, 40-yOffset, 176, 63, 48, 8, f, 0);

		displayGauge(58, 6-xOffset, 5-yOffset, 176, 4, 58, null, new GasStack(GasRegistry.getGas("sulfuricAcid"), 1));

		if(gas != null)
		{
			displayGauge(58, 134-xOffset, 14-yOffset, 176, 4, 58, null, gas);
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
		transferRects.add(new TemplateRecipeHandler.RecipeTransferRect(new Rectangle(64-xOffset, 40-yOffset, 48, 8), getRecipeId(), new Object[0]));
	}

	@Override
	public void loadCraftingRecipes(String outputId, Object... results)
	{
		if(outputId.equals(getRecipeId()))
		{
			for(DissolutionRecipe irecipe : getRecipes())
			{
				arecipes.add(new CachedIORecipe(irecipe));
			}
		}
		else if(outputId.equals("gas") && results.length == 1 && results[0] instanceof GasStack)
		{
			for(DissolutionRecipe irecipe : getRecipes())
			{
				if(((GasStack)results[0]).isGasEqual(irecipe.getOutput().output))
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
	public List<String> handleTooltip(GuiRecipe gui, List<String> currenttip, int recipe)
	{
		Point point = GuiDraw.getMousePosition();

		int xAxis = point.x-(Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiLeft);
		int yAxis = point.y-(Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiTop);

		if(xAxis >= 6 && xAxis <= 22 && yAxis >= 5+13 && yAxis <= 63+13)
		{
			currenttip.add(GasRegistry.getGas("sulfuricAcid").getLocalizedName());
		}
		else if(xAxis >= 134 && xAxis <= 150 && yAxis >= 14+4 && yAxis <= 72+4)
		{
			currenttip.add(LangUtils.localizeGasStack(((CachedIORecipe)arecipes.get(recipe)).outputStack));
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
			stack = new GasStack(GasRegistry.getGas("sulfuricAcid"), 1);
		}
		else if(xAxis >= 134 && xAxis <= 150 && yAxis >= 14+4 && yAxis <= 72+4)
		{
			stack = ((CachedIORecipe)arecipes.get(recipe)).outputStack;
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
			stack = new GasStack(GasRegistry.getGas("sulfuricAcid"), 1);
		}
		else if(xAxis >= 134 && xAxis <= 150 && yAxis >= 14+4 && yAxis <= 72+4)
		{
			stack = ((CachedIORecipe)arecipes.get(recipe)).outputStack;
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
	public void loadUsageRecipes(ItemStack ingredient)
	{
		for(DissolutionRecipe irecipe : getRecipes())
		{
			if(NEIServerUtils.areStacksSameTypeCrafting((ItemStack)irecipe.getInput().ingredient, ingredient))
			{
				arecipes.add(new CachedIORecipe(irecipe));
			}
		}
	}

	@Override
	public void addGuiElements()
	{

	}

	public class CachedIORecipe extends TemplateRecipeHandler.CachedRecipe
	{
		public PositionedStack inputStack;
		public GasStack outputStack;

		@Override
		public PositionedStack getIngredient()
		{
			return inputStack;
		}

		@Override
		public PositionedStack getResult()
		{
			return null;
		}

		public CachedIORecipe(ItemStack input, GasStack output)
		{
			inputStack = new PositionedStack(input, 26-xOffset, 36-yOffset);
			outputStack = output;
		}

		public CachedIORecipe(DissolutionRecipe recipe)
		{
			this(recipe.getInput().ingredient, recipe.getOutput().output);
		}
	}
}

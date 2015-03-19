package mekanism.client.nei;

import java.awt.*;
import java.util.Collection;
import java.util.List;

import mekanism.api.gas.GasStack;
import mekanism.client.gui.GuiChemicalInfuser;
import mekanism.common.ObfuscatedNames;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.ChemicalPairInput;
import mekanism.common.recipe.machines.ChemicalInfuserRecipe;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.inventory.GuiContainer;

import org.lwjgl.opengl.GL11;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.TemplateRecipeHandler;
import static codechicken.lib.gui.GuiDraw.changeTexture;
import static codechicken.lib.gui.GuiDraw.drawTexturedModalRect;

public class ChemicalInfuserRecipeHandler extends BaseRecipeHandler
{
	private int ticksPassed;

	public static int xOffset = 5;
	public static int yOffset = 3;

	@Override
	public String getRecipeName()
	{
		return MekanismUtils.localize("tile.MachineBlock2.ChemicalInfuser.name");
	}

	@Override
	public String getOverlayIdentifier()
	{
		return "chemicalinfuser";
	}

	@Override
	public String getGuiTexture()
	{
		return "mekanism:gui/nei/GuiChemicalInfuser.png";
	}

	@Override
	public Class getGuiClass()
	{
		return GuiChemicalInfuser.class;
	}

	public String getRecipeId()
	{
		return "mekanism.chemicalinfuser";
	}

	public Collection<ChemicalInfuserRecipe> getRecipes()
	{
		return Recipe.CHEMICAL_INFUSER.get().values();
	}

	@Override
	public void drawBackground(int i)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		changeTexture(getGuiTexture());
		drawTexturedModalRect(-2, 0, 3, yOffset, 170, 80);
	}

	@Override
	public void drawExtras(int i)
	{
		CachedIORecipe recipe = (CachedIORecipe)arecipes.get(i);

		drawTexturedModalRect(47-xOffset, 39-yOffset, 176, 71, 28, 8);
		drawTexturedModalRect(101-xOffset, 39-yOffset, 176, 63, 28, 8);

		if(recipe.chemicalInput.leftGas != null)
		{
			displayGauge(58, 26-xOffset, 14-yOffset, 176, 4, 58, null, recipe.chemicalInput.leftGas);
		}

		if(recipe.outputStack != null)
		{
			displayGauge(58, 80-xOffset, 5-yOffset, 176, 4, 58, null, recipe.outputStack);
		}

		if(recipe.chemicalInput.rightGas != null)
		{
			displayGauge(58, 134-xOffset, 14-yOffset, 176, 4, 58, null, recipe.chemicalInput.rightGas);
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
		transferRects.add(new TemplateRecipeHandler.RecipeTransferRect(new Rectangle(47-xOffset, 39-yOffset, 28, 8), getRecipeId(), new Object[0]));
		transferRects.add(new TemplateRecipeHandler.RecipeTransferRect(new Rectangle(101-xOffset, 39-yOffset, 28, 8), getRecipeId(), new Object[0]));
	}

	@Override
	public void loadCraftingRecipes(String outputId, Object... results)
	{
		if(outputId.equals(getRecipeId()))
		{
			for(ChemicalInfuserRecipe irecipe : getRecipes())
			{
				arecipes.add(new CachedIORecipe(irecipe));
			}
		}
		else if(outputId.equals("gas") && results.length == 1 && results[0] instanceof GasStack)
		{
			for(ChemicalInfuserRecipe irecipe : getRecipes())
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
	public void loadUsageRecipes(String inputId, Object... ingredients)
	{
		if(inputId.equals("gas") && ingredients.length == 1 && ingredients[0] instanceof GasStack)
		{
			for(ChemicalInfuserRecipe irecipe : getRecipes())
			{
				if(irecipe.getInput().containsType((GasStack)ingredients[0]))
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

		if(xAxis >= 26 && xAxis <= 42 && yAxis >= 14+13 && yAxis <= 72+13)
		{
			currenttip.add(LangUtils.localizeGasStack(((CachedIORecipe)arecipes.get(recipe)).chemicalInput.leftGas));
		}
		else if(xAxis >= 80 && xAxis <= 96 && yAxis >= 5+13 && yAxis <= 63+13)
		{
			currenttip.add(LangUtils.localizeGasStack(((CachedIORecipe)arecipes.get(recipe)).outputStack));
		}
		else if(xAxis >= 134 && xAxis <= 150 && yAxis >= 14+13 && yAxis <= 72+13)
		{
			currenttip.add(LangUtils.localizeGasStack(((CachedIORecipe)arecipes.get(recipe)).chemicalInput.rightGas));
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

		if(xAxis >= 26 && xAxis <= 42 && yAxis >= 14+13 && yAxis <= 72+13)
		{
			stack = ((CachedIORecipe)arecipes.get(recipe)).chemicalInput.leftGas;
		}
		else if(xAxis >= 80 && xAxis <= 96 && yAxis >= 5+13 && yAxis <= 63+13)
		{
			stack = ((CachedIORecipe)arecipes.get(recipe)).outputStack;
		}
		else if(xAxis >= 134 && xAxis <= 150 && yAxis >= 14+13 && yAxis <= 72+13)
		{
			stack = ((CachedIORecipe)arecipes.get(recipe)).chemicalInput.rightGas;
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

		if(xAxis >= 26 && xAxis <= 42 && yAxis >= 14+13 && yAxis <= 72+13)
		{
			stack = ((CachedIORecipe)arecipes.get(recipe)).chemicalInput.leftGas;
		}
		else if(xAxis >= 80 && xAxis <= 96 && yAxis >= 5+13 && yAxis <= 63+13)
		{
			stack = ((CachedIORecipe)arecipes.get(recipe)).outputStack;
		}
		else if(xAxis >= 134 && xAxis <= 150 && yAxis >= 14+13 && yAxis <= 72+13)
		{
			stack = ((CachedIORecipe)arecipes.get(recipe)).chemicalInput.rightGas;
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
	public void addGuiElements()
	{

	}

	public class CachedIORecipe extends TemplateRecipeHandler.CachedRecipe
	{
		public ChemicalPairInput chemicalInput;
		public GasStack outputStack;

		@Override
		public PositionedStack getResult()
		{
			return null;
		}

		public CachedIORecipe(ChemicalPairInput input, GasStack output)
		{
			chemicalInput = input;
			outputStack = output;
		}

		public CachedIORecipe(ChemicalInfuserRecipe recipe)
		{
			this(recipe.getInput(), recipe.getOutput().output);
		}
	}
}

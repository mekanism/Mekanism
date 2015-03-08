package mekanism.client.nei;

import static codechicken.lib.gui.GuiDraw.changeTexture;
import static codechicken.lib.gui.GuiDraw.drawTexturedModalRect;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.List;

import mekanism.api.gas.GasStack;
import mekanism.client.gui.GuiChemicalOxidizer;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiGasGauge;
import mekanism.client.gui.element.GuiGauge;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.common.ObfuscatedNames;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.machines.OxidationRecipe;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class ChemicalOxidizerRecipeHandler extends BaseRecipeHandler
{
	private int ticksPassed;
	
	public GuiGasGauge gasOutput;

	public static int xOffset = 5;
	public static int yOffset = 12;
	
	@Override
	public void addGuiElements()
	{
		guiElements.add(gasOutput = GuiGasGauge.getDummy(GuiGauge.Type.STANDARD, this, MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalOxidizer.png"), 133, 13));

		guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalOxidizer.png"), 154, 4).with(SlotOverlay.POWER));
		guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalOxidizer.png"), 25, 35));
		guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalOxidizer.png"), 154, 24).with(SlotOverlay.PLUS));

		guiElements.add(new GuiProgress(new IProgressInfoHandler()
		{
			@Override
			public double getProgress()
			{
				return ticksPassed >= 20 ? (ticksPassed - 20) % 20 / 20.0F : 0.0F;
			}
		}, ProgressBar.LARGE_RIGHT, this, MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalOxidizer.png"), 62, 39));
	}

	@Override
	public String getRecipeName()
	{
		return MekanismUtils.localize("tile.MachineBlock2.ChemicalOxidizer.name");
	}

	@Override
	public String getOverlayIdentifier()
	{
		return "chemicaloxidizer";
	}

	@Override
	public String getGuiTexture()
	{
		return "mekanism:gui/GuiChemicalOxidizer.png";
	}

	@Override
	public Class getGuiClass()
	{
		return GuiChemicalOxidizer.class;
	}

	public String getRecipeId()
	{
		return "mekanism.chemicaloxidizer";
	}

	public Collection<OxidationRecipe> getRecipes()
	{
		return Recipe.CHEMICAL_OXIDIZER.get().values();
	}

	@Override
	public void drawBackground(int i)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		changeTexture(getGuiTexture());
		drawTexturedModalRect(0, 0, xOffset, yOffset, 147, 62);
		
		for(GuiElement e : guiElements)
		{
			e.renderBackground(0, 0, -xOffset, -yOffset);
		}
	}

	@Override
	public void drawExtras(int i)
	{
		CachedIORecipe recipe = (CachedIORecipe)arecipes.get(i);

		if(recipe.outputStack != null)
		{
			gasOutput.setDummyType(recipe.outputStack.getGas());
			gasOutput.renderScale(0, 0, -xOffset, -yOffset);
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
			for(OxidationRecipe irecipe : getRecipes())
			{
				arecipes.add(new CachedIORecipe(irecipe));
			}
		}
		else if(outputId.equals("gas") && results.length == 1 && results[0] instanceof GasStack)
		{
			for(OxidationRecipe irecipe : getRecipes())
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
		Point offset = gui.getRecipePosition(recipe);

		int xAxis = point.x-(Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiLeft)-offset.x;
		int yAxis = point.y-(Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiTop)-offset.y;

		if(xAxis >= 134-5 && xAxis <= 150-5 && yAxis >= 14-11 && yAxis <= 72-11)
		{
			currenttip.add(LangUtils.localizeGasStack(((CachedIORecipe)arecipes.get(recipe)).outputStack));
		}

		return super.handleTooltip(gui, currenttip, recipe);
	}

	@Override
	public boolean keyTyped(GuiRecipe gui, char keyChar, int keyCode, int recipe)
	{
		Point point = GuiDraw.getMousePosition();
		Point offset = gui.getRecipePosition(recipe);

		int xAxis = point.x-(Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiLeft)-offset.x;
		int yAxis = point.y-(Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiTop)-offset.y;

		if(xAxis >= 134-5 && xAxis <= 150-5 && yAxis >= 14-11 && yAxis <= 72-11)
		{
			GasStack stack = ((CachedIORecipe)arecipes.get(recipe)).outputStack;

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
		Point offset = gui.getRecipePosition(recipe);

		int xAxis = point.x - (Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiLeft)-offset.x;
		int yAxis = point.y - (Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiTop)-offset.y;

		if(xAxis >= 134-5 && xAxis <= 150-5 && yAxis >= 14-11 && yAxis <= 72-11)
		{
			GasStack stack = ((CachedIORecipe)arecipes.get(recipe)).outputStack;

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
		return 2;
	}

	@Override
	public void loadUsageRecipes(ItemStack ingredient)
	{
		for(OxidationRecipe irecipe : getRecipes())
		{
			if(NEIServerUtils.areStacksSameTypeCrafting(irecipe.getInput().ingredient, ingredient))
			{
				arecipes.add(new CachedIORecipe(irecipe));
			}
		}
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

		public CachedIORecipe(OxidationRecipe recipe)
		{
			this(recipe.getInput().ingredient, recipe.getOutput().output);
		}
	}
}

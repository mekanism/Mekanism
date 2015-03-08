package mekanism.client.nei;

import static codechicken.lib.gui.GuiDraw.changeTexture;
import static codechicken.lib.gui.GuiDraw.drawTexturedModalRect;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import mekanism.api.gas.GasStack;
import mekanism.client.gui.GuiPRC;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiFluidGauge;
import mekanism.client.gui.element.GuiGasGauge;
import mekanism.client.gui.element.GuiGauge;
import mekanism.client.gui.element.GuiPowerBar;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiPowerBar.IPowerInfoHandler;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.common.ObfuscatedNames;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.PressurizedInput;
import mekanism.common.recipe.machines.PressurizedRecipe;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class PRCRecipeHandler extends BaseRecipeHandler
{
	private int ticksPassed;
	
	public GuiFluidGauge fluidInput;
	public GuiGasGauge gasInput;
	public GuiGasGauge gasOutput;
	
	public static int xOffset = 5;
	public static int yOffset = 11;
	
	@Override
	public void addGuiElements()
	{
		guiElements.add(new GuiSlot(SlotType.INPUT, this, MekanismUtils.getResource(ResourceType.GUI, stripTexture()), 53, 34));
		guiElements.add(new GuiSlot(SlotType.POWER, this, MekanismUtils.getResource(ResourceType.GUI, stripTexture()), 140, 18).with(SlotOverlay.POWER));
		guiElements.add(new GuiSlot(SlotType.OUTPUT, this, MekanismUtils.getResource(ResourceType.GUI, stripTexture()), 115, 34));
		
		guiElements.add(fluidInput = GuiFluidGauge.getDummy(GuiGauge.Type.STANDARD, this, MekanismUtils.getResource(ResourceType.GUI, "GuiPRC.png"), 5, 10));
		guiElements.add(gasInput = GuiGasGauge.getDummy(GuiGauge.Type.STANDARD, this, MekanismUtils.getResource(ResourceType.GUI, "GuiPRC.png"), 28, 10));
		guiElements.add(gasOutput = GuiGasGauge.getDummy(GuiGauge.Type.SMALL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiPRC.png"), 140, 40));

		guiElements.add(new GuiPowerBar(this, new IPowerInfoHandler() {
			@Override
			public double getLevel()
			{
				return ticksPassed <= 20 ? ticksPassed / 20.0F : 1.0F;
			}
		}, MekanismUtils.getResource(ResourceType.GUI, stripTexture()), 164, 15));
		guiElements.add(new GuiProgress(new IProgressInfoHandler()
		{
			@Override
			public double getProgress()
			{
				return ticksPassed >= 20 ? (ticksPassed - 20) % 20 / 20.0F : 0.0F;
			}
		}, getProgressType(), this, MekanismUtils.getResource(ResourceType.GUI, stripTexture()), 75, 37));
	}
	
	public ProgressBar getProgressType()
	{
		return ProgressBar.MEDIUM;
	}
	
	public Set<Entry<PressurizedInput, PressurizedRecipe>> getRecipes()
	{
		return Recipe.PRESSURIZED_REACTION_CHAMBER.get().entrySet();
	}
	
	@Override
	public void drawBackground(int i)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		changeTexture(getGuiTexture());
		drawTexturedModalRect(-2, 0, 3, yOffset, 170, 68);
		
		for(GuiElement e : guiElements)
		{
			e.renderBackground(0, 0, -xOffset, -yOffset);
		}
	}
	
	@Override
	public void loadTransferRects()
	{
		transferRects.add(new TemplateRecipeHandler.RecipeTransferRect(new Rectangle(75-xOffset, 37-yOffset, 36, 10), getRecipeId(), new Object[0]));
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		ticksPassed++;
	}
	
	@Override
	public String getRecipeName() 
	{
		return MekanismUtils.localize("tile.MachineBlock2.PressurizedReactionChamber.short.name");
	}
	
	@Override
	public Class getGuiClass()
	{
		return GuiPRC.class;
	}
	
	@Override
	public String getOverlayIdentifier()
	{
		return "prc";
	}
	
	@Override
	public int recipiesPerPage()
	{
		return 2;
	}
	
	public String getRecipeId()
	{
		return "mekanism.prc";
	}
	
	@Override
	public String getGuiTexture() 
	{
		return "mekanism:gui/nei/GuiPRC.png";
	}
	
	@Override
	public void drawExtras(int i)
	{
		CachedIORecipe recipe = (CachedIORecipe)arecipes.get(i);
		
		if(recipe.pressurizedRecipe.getInput().getFluid() != null)
		{
			fluidInput.setDummyType(recipe.pressurizedRecipe.getInput().getFluid().getFluid());
			fluidInput.renderScale(0, 0, -xOffset, -yOffset);
		}

		if(recipe.pressurizedRecipe.getInput().getGas() != null)
		{
			gasInput.setDummyType(recipe.pressurizedRecipe.getInput().getGas().getGas());
			gasInput.renderScale(0, 0, -xOffset, -yOffset);
		}

		if(recipe.pressurizedRecipe.getOutput().getGasOutput() != null)
		{
			gasOutput.setDummyType(recipe.pressurizedRecipe.getOutput().getGasOutput().getGas());
			gasOutput.renderScale(0, 0, -xOffset, -yOffset);
		}
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
		else if(outputId.equals("gas") && results.length == 1 && results[0] instanceof GasStack)
		{
			for(Map.Entry<PressurizedInput, PressurizedRecipe> irecipe : getRecipes())
			{
				if(irecipe.getValue().getOutput().getGasOutput().isGasEqual((GasStack)results[0]))
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
	public void loadCraftingRecipes(ItemStack result)
	{
		for(Map.Entry<PressurizedInput, PressurizedRecipe> irecipe : getRecipes())
		{
			if(NEIServerUtils.areStacksSameTypeCrafting(irecipe.getValue().getOutput().getItemOutput(), result))
			{
				arecipes.add(new CachedIORecipe(irecipe));
			}
		}
	}

	@Override
	public void loadUsageRecipes(String inputId, Object... ingredients)
	{
		if(inputId.equals("gas") && ingredients.length == 1 && ingredients[0] instanceof GasStack)
		{
			for(Map.Entry<PressurizedInput, PressurizedRecipe> irecipe : getRecipes())
			{
				if(irecipe.getKey().containsType((GasStack)ingredients[0]))
				{
					arecipes.add(new CachedIORecipe(irecipe));
				}
			}
		}
		else if(inputId.equals("fluid") && ingredients.length == 1 && ingredients[0] instanceof FluidStack)
		{
			for(Map.Entry<PressurizedInput, PressurizedRecipe> irecipe : getRecipes())
			{
				if(irecipe.getKey().containsType((FluidStack)ingredients[0]))
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
	public void loadUsageRecipes(ItemStack ingredient)
	{
		for(Map.Entry<PressurizedInput, PressurizedRecipe> irecipe : getRecipes())
		{
			if(NEIServerUtils.areStacksSameTypeCrafting(irecipe.getKey().getSolid(), ingredient))
			{
				arecipes.add(new CachedIORecipe(irecipe));
			}
		}
	}

	@Override
	public List<String> handleTooltip(GuiRecipe gui, List<String> currenttip, int recipe)
	{
		Point point = GuiDraw.getMousePosition();
		Point offset = gui.getRecipePosition(recipe);

		int xAxis = point.x-(Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiLeft)-offset.x;
		int yAxis = point.y-(Integer)MekanismUtils.getPrivateValue(gui, GuiContainer.class, ObfuscatedNames.GuiContainer_guiTop)-offset.y;

		if(xAxis >= 6-5 && xAxis <= 22-5 && yAxis >= 11-10 && yAxis <= 69-10)
		{
			currenttip.add(LangUtils.localizeFluidStack(((CachedIORecipe)arecipes.get(recipe)).pressurizedRecipe.getInput().getFluid()));
		}
		else if(xAxis >= 29-5 && xAxis <= 45-5 && yAxis >= 11-10 && yAxis <= 69-10)
		{
			currenttip.add(LangUtils.localizeGasStack(((CachedIORecipe)arecipes.get(recipe)).pressurizedRecipe.getInput().getGas()));
		}
		else if(xAxis >= 141-5 && xAxis <= 157-5 && yAxis >= 41-10 && yAxis <= 69-10)
		{
			currenttip.add(LangUtils.localizeGasStack(((CachedIORecipe)arecipes.get(recipe)).pressurizedRecipe.getOutput().getGasOutput()));
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

		GasStack gas = null;
		FluidStack fluid = null;

		if(xAxis >= 6-5 && xAxis <= 22-5 && yAxis >= 11-10 && yAxis <= 69-10)
		{
			fluid = ((CachedIORecipe)arecipes.get(recipe)).pressurizedRecipe.getInput().getFluid();
		}
		else if(xAxis >= 29-5 && xAxis <= 45-5 && yAxis >= 11-10 && yAxis <= 69-10)
		{
			gas = ((CachedIORecipe)arecipes.get(recipe)).pressurizedRecipe.getInput().getGas();
		}
		else if(xAxis >= 141-5 && xAxis <= 157-5 && yAxis >= 41-10 && yAxis <= 69-10)
		{
			gas = ((CachedIORecipe)arecipes.get(recipe)).pressurizedRecipe.getOutput().getGasOutput();
		}

		if(gas != null)
		{
			if(keyCode == NEIClientConfig.getKeyBinding("gui.recipe"))
			{
				if(doGasLookup(gas, false))
				{
					return true;
				}
			}
			else if(keyCode == NEIClientConfig.getKeyBinding("gui.usage"))
			{
				if(doGasLookup(gas, true))
				{
					return true;
				}
			}
		}
		else if(fluid != null)
		{
			if(keyCode == NEIClientConfig.getKeyBinding("gui.recipe"))
			{
				if(doFluidLookup(fluid, false))
				{
					return true;
				}
			}
			else if(keyCode == NEIClientConfig.getKeyBinding("gui.usage"))
			{
				if(doFluidLookup(fluid, true))
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

		GasStack gas = null;
		FluidStack fluid = null;

		if(xAxis >= 6-5 && xAxis <= 22-5 && yAxis >= 11-10 && yAxis <= 69-10)
		{
			fluid = ((CachedIORecipe)arecipes.get(recipe)).pressurizedRecipe.getInput().getFluid();
		}
		else if(xAxis >= 29-5 && xAxis <= 45-5 && yAxis >= 11-10 && yAxis <= 69-10)
		{
			gas = ((CachedIORecipe)arecipes.get(recipe)).pressurizedRecipe.getInput().getGas();
		}
		else if(xAxis >= 141-5 && xAxis <= 157-5 && yAxis >= 41-10 && yAxis <= 69-10)
		{
			gas = ((CachedIORecipe)arecipes.get(recipe)).pressurizedRecipe.getOutput().getGasOutput();
		}

		if(gas != null)
		{
			if(button == 0)
			{
				if(doGasLookup(gas, false))
				{
					return true;
				}
			}
			else if(button == 1)
			{
				if(doGasLookup(gas, true))
				{
					return true;
				}
			}
		}
		else if(fluid != null)
		{
			if(button == 0)
			{
				if(doFluidLookup(fluid, false))
				{
					return true;
				}
			}
			else if(button == 1)
			{
				if(doFluidLookup(fluid, true))
				{
					return true;
				}
			}
		}

		return super.mouseClicked(gui, button, recipe);
	}
	
	public class CachedIORecipe extends TemplateRecipeHandler.CachedRecipe
	{
		public PressurizedRecipe pressurizedRecipe;
		
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

		public CachedIORecipe(PressurizedRecipe recipe)
		{
			super();
			
			pressurizedRecipe = recipe;
			
			input = new PositionedStack(recipe.getInput().getSolid(), 54-xOffset, 35-yOffset);
			output = new PositionedStack(recipe.getOutput().getItemOutput(), 116-xOffset, 35-yOffset);
		}

		public CachedIORecipe(Map.Entry recipe)
		{
			this((PressurizedRecipe)recipe.getValue());
		}
	}
}

package mekanism.client.jei;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mekanism.api.gas.GasStack;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiGauge;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.jei.gas.GasStackRenderer;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiIngredientGroup;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

public abstract class BaseRecipeCategory implements IRecipeCategory, IGuiWrapper
{
	public static final GuiDummy gui = new GuiDummy();
	
	public IGuiHelper guiHelper;
	
	public String recipeName;
	public String unlocalizedName;
	
	public String guiTexture;
	public ResourceLocation guiLocation;
	
	public ProgressBar progressBar;
	public ITickTimer timer;
	
	public int xOffset = 28;
	public int yOffset = 16;
	
	public IDrawable fluidOverlayLarge;
	public IDrawable fluidOverlaySmall;
	
	public BaseRecipeCategory(IGuiHelper helper, String gui, String name, String unlocalized, ProgressBar progress)
	{
		guiHelper = helper;
		guiTexture = gui;
		guiLocation = new ResourceLocation(guiTexture);
		
		progressBar = progress;
		
		recipeName = name;
		unlocalizedName = unlocalized;
		
		timer = helper.createTickTimer(20, 20, false);
		
		fluidOverlayLarge = guiHelper.createDrawable(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, GuiGauge.Type.STANDARD.textureLocation), 19, 1, 16, 59);
		fluidOverlaySmall = guiHelper.createDrawable(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, GuiGauge.Type.STANDARD.textureLocation), 19, 1, 16, 29);
		
		addGuiElements();
	}
	
	@Override
	public String getUid() 
	{
		return "mekanism." + recipeName;
	}

	@Override
	public String getTitle()
	{
		return LangUtils.localize(unlocalizedName);
	}
	
	@Override
	public void drawExtras(Minecraft minecraft) 
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		changeTexture(guiLocation);
		
		for(GuiElement e : guiElements)
		{
			e.renderBackground(0, 0, -xOffset, -yOffset);
		}
	}
	
	@Override
	public void drawTexturedRect(int x, int y, int u, int v, int w, int h) 
	{
		gui.drawTexturedModalRect(x, y, u, v, w, h);
	}

	@Override
	public void drawTexturedRectFromIcon(int x, int y, TextureAtlasSprite icon, int w, int h) 
	{
		gui.drawTexturedModalRect(x, y, icon, w, h);
	}

	@Override
	public void displayTooltip(String s, int xAxis, int yAxis) {}

	@Override
	public void displayTooltips(List<String> list, int xAxis, int yAxis) {}

	@Override
	public FontRenderer getFont() 
	{
		return null;
	}
	
	public void displayGauge(int length, int xPos, int yPos, int overlayX, int overlayY, int scale, FluidStack fluid, GasStack gas)
	{
		if(fluid == null && gas == null)
		{
			return;
		}

		int start = 0;

		while(true)
		{
			int renderRemaining = 0;

			if(scale > 16)
			{
				renderRemaining = 16;
				scale -= 16;
			}
			else {
				renderRemaining = scale;
				scale = 0;
			}

			changeTexture(MekanismRenderer.getBlocksTexture());

			if(fluid != null)
			{
				gui.drawTexturedModalRect(xPos, yPos + length - renderRemaining - start, MekanismRenderer.getFluidTexture(fluid.getFluid(), FluidType.STILL), 16, 16 - (16 - renderRemaining));
			}
			else if(gas != null)
			{
				gui.drawTexturedModalRect(xPos, yPos + length - renderRemaining - start, gas.getGas().getSprite(), 16, 16 - (16 - renderRemaining));
			}

			start+=16;

			if(renderRemaining == 0 || scale == 0)
			{
				break;
			}
		}

		changeTexture(guiLocation);
		gui.drawTexturedModalRect(xPos, yPos, overlayX, overlayY, 16, length+1);
	}
	
	public void displayGauge(int xPos, int yPos, int sizeX, int sizeY, GasStack gas)
	{
		if(gas == null)
		{
			return;
		}

		changeTexture(MekanismRenderer.getBlocksTexture());
		gui.drawTexturedModalRect(xPos, yPos, gas.getGas().getSprite(), sizeX, sizeY);
	}
	
	public String stripTexture()
	{
		return guiTexture.replace("mekanism:gui/", "");
	}

	public void changeTexture(ResourceLocation texture) 
	{
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
	}
	
	public Set<GuiElement> guiElements = new HashSet<GuiElement>();
	
	public void addGuiElements() {}
	
	public static class GuiDummy extends Gui {}
	
	@Override
	public IDrawable getIcon() 
	{
		return null;
	}

	@Override
	public List getTooltipStrings(int mouseX, int mouseY) 
	{
		return Collections.emptyList();
	}
	
	protected void initGas(IGuiIngredientGroup<GasStack> group, int slot, boolean input, int x, int y, int width, int height, GasStack stack, boolean overlay)
	{
		if(stack == null) return;
		
		IDrawable fluidOverlay = height > 50 ? fluidOverlayLarge : fluidOverlaySmall;
		
		GasStackRenderer renderer = new GasStackRenderer(stack.amount, false, width, height, overlay ? fluidOverlay : null);
		group.init(slot, input, renderer, x, y, width, height, 0, 0);
		group.set(slot, stack);
		group.addTooltipCallback((index, isInput, ingredient, tooltip) -> tooltip.remove(1));
	}
}

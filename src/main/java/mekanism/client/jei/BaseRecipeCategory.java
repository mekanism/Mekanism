package mekanism.client.jei;

import mekanism.api.gas.GasStack;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiGauge;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.jei.gas.GasStackRenderer;
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
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class BaseRecipeCategory implements IRecipeCategory, IGuiWrapper
{
	private static final GuiDummy gui = new GuiDummy();
	
	protected IGuiHelper guiHelper;
	
	private String recipeName;
	private String unlocalizedName;
	
	protected String guiTexture;
	protected ResourceLocation guiLocation;

	@Nullable
	protected ProgressBar progressBar;
	protected ITickTimer timer;
	
	protected int xOffset = 28;
	protected int yOffset = 16;
	
	protected IDrawable fluidOverlayLarge;
	protected IDrawable fluidOverlaySmall;

	protected Set<GuiElement> guiElements = new HashSet<>();
	
	public BaseRecipeCategory(IGuiHelper helper, String gui, String name, String unlocalized, @Nullable ProgressBar progress)
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
	public String getModName()
	{
		return "Mekanism";
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
	
	public String stripTexture()
	{
		return guiTexture.replace("mekanism:gui/", "");
	}

	public void changeTexture(ResourceLocation texture)
	{
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
	}
	
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
	
	protected void initGas(IGuiIngredientGroup<GasStack> group, int slot, boolean input, int x, int y, int width, int height, @Nullable GasStack stack, boolean overlay)
	{
		if(stack == null) return;
		
		IDrawable fluidOverlay = height > 50 ? fluidOverlayLarge : fluidOverlaySmall;
		
		GasStackRenderer renderer = new GasStackRenderer(stack.amount, false, width, height, overlay ? fluidOverlay : null);
		group.init(slot, input, renderer, x, y, width, height, 0, 0);
		group.set(slot, stack);
		group.addTooltipCallback((index, isInput, ingredient, tooltip) -> tooltip.remove(1));
	}
}

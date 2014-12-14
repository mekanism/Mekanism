package mekanism.client.gui;

import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidTank;

public class GuiFluidGauge extends GuiGauge<Fluid>
{
	IFluidInfoHandler infoHandler;

	public GuiFluidGauge(IFluidInfoHandler handler, Type type, IGuiWrapper gui, ResourceLocation def, int x, int y)
	{
		super(type, gui, def, x, y);

		infoHandler = handler;
	}
	
	public static GuiFluidGauge getDummy(Type type, IGuiWrapper gui, ResourceLocation def, int x, int y)
	{
		GuiFluidGauge gauge = new GuiFluidGauge(null, type, gui, def, x, y);
		gauge.dummy = true;
		
		return gauge;
	}

	@Override
	public int getScaledLevel()
	{
		if(dummy)
		{
			return height-2;
		}
		
		return infoHandler.getTank().getFluid() != null ? infoHandler.getTank().getFluidAmount()*(height-2) / infoHandler.getTank().getCapacity() : 0;
	}

	@Override
	public TextureAtlasSprite getIcon()
	{
		return null;
/*
		if(dummy)
		{
			return dummyType.getIcon();
		}
		
		return infoHandler.getTank().getFluid().getFluid().getIcon();
*/
	}

	@Override
	public String getTooltipText()
	{
		if(dummy)
		{
			return dummyType.getLocalizedName(null);
		}
		
		return infoHandler.getTank().getFluid() != null ? LangUtils.localizeFluidStack(infoHandler.getTank().getFluid()) + ": " + infoHandler.getTank().getFluidAmount() + "mB" : MekanismUtils.localize("gui.empty");
	}

	public static interface IFluidInfoHandler
	{
		public FluidTank getTank();
	}
}

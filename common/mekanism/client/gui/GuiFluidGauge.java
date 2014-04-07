package mekanism.client.gui;

import mekanism.common.util.MekanismUtils;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidTank;

public class GuiFluidGauge extends GuiGauge
{
	IFluidInfoHandler infoHandler;

	public GuiFluidGauge(IFluidInfoHandler handler, Type type, GuiMekanism gui, TileEntity tile, ResourceLocation def, int x, int y)
	{
		super(type, gui, tile, def, x, y);

		infoHandler = handler;
	}

	@Override
	public int getScaledLevel()
	{
		return infoHandler.getTank().getFluid() != null ? infoHandler.getTank().getFluidAmount()*(height-2) / infoHandler.getTank().getCapacity() : 0;
	}

	@Override
	public Icon getIcon()
	{
		return infoHandler.getTank().getFluid().getFluid().getIcon();
	}

	@Override
	public String getTooltipText()
	{
		return infoHandler.getTank().getFluid() != null ? infoHandler.getTank().getFluid().getFluid().getLocalizedName() + ": " + infoHandler.getTank().getFluidAmount() + "mB" : MekanismUtils.localize("gui.empty");
	}

	public static interface IFluidInfoHandler
	{
		public FluidTank getTank();
	}
}

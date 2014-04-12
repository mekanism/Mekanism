package mekanism.client.gui;

import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;

public class GuiEnergyGauge extends GuiGauge
{
	IEnergyInfoHandler infoHandler;

	public GuiEnergyGauge(IEnergyInfoHandler handler, Type type, GuiMekanism gui, TileEntity tile, ResourceLocation def, int x, int y)
	{
		super(type, gui, tile, def, x, y);

		infoHandler = handler;
	}

	@Override
	public int getScaledLevel()
	{
		return (int)(infoHandler.getEnergyStorage().getEnergy()*(height-2) / infoHandler.getEnergyStorage().getMaxEnergy());
	}

	@Override
	public Icon getIcon()
	{
		return MekanismRenderer.energyIcon;
	}

	@Override
	public String getTooltipText()
	{
		return infoHandler.getEnergyStorage().getEnergy() > 0 ? MekanismUtils.getEnergyDisplay(infoHandler.getEnergyStorage().getEnergy()) : MekanismUtils.localize("gui.empty");
	}

	public static interface IEnergyInfoHandler
	{
		public IStrictEnergyStorage getEnergyStorage();
	}
}

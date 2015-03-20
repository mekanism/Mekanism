package mekanism.client.gui.element;

import java.util.Arrays;

import mekanism.api.Coord4D;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.Mekanism;
import mekanism.common.base.ITankManager;
import mekanism.common.item.ItemGaugeDropper;
import mekanism.common.network.PacketDropperUse.DropperUseMessage;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
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
	public TransmissionType getTransmission()
	{
		return TransmissionType.FLUID;
	}
	
	@Override
	public void mouseClicked(int xAxis, int yAxis, int button)
	{
		if(xAxis >= xLocation + 1 && xAxis <= xLocation + width - 1 && yAxis >= yLocation + 1 && yAxis <= yLocation + height - 1)
		{
			ItemStack stack = mc.thePlayer.inventory.getItemStack();
			
			if(guiObj instanceof GuiMekanism && stack != null && stack.getItem() instanceof ItemGaugeDropper)
			{
				TileEntity tile = ((GuiMekanism)guiObj).getTileEntity();
				
				if(tile instanceof ITankManager)
				{
					int index = Arrays.asList(((ITankManager)tile).getTanks()).indexOf(infoHandler.getTank());
					
					if(index != -1)
					{
						Mekanism.packetHandler.sendToServer(new DropperUseMessage(Coord4D.get(tile), button, index));
					}
				}
			}
		}
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
	public IIcon getIcon()
	{
		if(dummy)
		{
			return dummyType.getIcon();
		}
		
		return infoHandler.getTank().getFluid().getFluid().getIcon();
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

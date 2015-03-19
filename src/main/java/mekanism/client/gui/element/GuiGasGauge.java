package mekanism.client.gui.element;

import java.util.Arrays;

import mekanism.api.Coord4D;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasTank;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.Mekanism;
import mekanism.common.base.ITankManager;
import mekanism.common.item.ItemGaugeDropper;
import mekanism.common.network.PacketDropperUse.DropperUseMessage;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

public class GuiGasGauge extends GuiGauge<Gas>
{
	IGasInfoHandler infoHandler;

	public GuiGasGauge(IGasInfoHandler handler, Type type, IGuiWrapper gui, ResourceLocation def, int x, int y)
	{
		super(type, gui, def, x, y);

		infoHandler = handler;
	}
	
	public static GuiGasGauge getDummy(Type type, IGuiWrapper gui, ResourceLocation def, int x, int y)
	{
		GuiGasGauge gauge = new GuiGasGauge(null, type, gui, def, x, y);
		gauge.dummy = true;
		
		return gauge;
	}
	
	@Override
	public TransmissionType getTransmission()
	{
		return TransmissionType.GAS;
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
		
		return infoHandler.getTank().getGas() != null ? infoHandler.getTank().getStored()*(height-2) / infoHandler.getTank().getMaxGas() : 0;
	}

	@Override
	public IIcon getIcon()
	{
		if(dummy)
		{
			return dummyType.getIcon();
		}
		
		return (infoHandler.getTank() != null && infoHandler.getTank().getGas() != null && infoHandler.getTank().getGas().getGas() != null) ? infoHandler.getTank().getGas().getGas().getIcon() : null;
	}

	@Override
	public String getTooltipText()
	{
		if(dummy)
		{
			return dummyType.getLocalizedName();
		}
		
		return (infoHandler.getTank().getGas() != null) ? infoHandler.getTank().getGas().getGas().getLocalizedName() + ": " + infoHandler.getTank().getStored() : MekanismUtils.localize("gui.empty");
	}

	public static interface IGasInfoHandler
	{
		public GasTank getTank();
	}
}

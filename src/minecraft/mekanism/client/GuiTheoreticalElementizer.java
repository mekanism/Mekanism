package mekanism.client;

import org.lwjgl.opengl.GL11;

import mekanism.common.ContainerAdvancedElectricMachine;
import mekanism.common.EnumColor;
import mekanism.common.MekanismUtils;
import mekanism.common.TileEntityTheoreticalElementizer;
import net.minecraft.src.*;

public class GuiTheoreticalElementizer extends GuiAdvancedElectricMachine
{
    public GuiTheoreticalElementizer(InventoryPlayer inventory, TileEntityTheoreticalElementizer tentity)
    {
        super(inventory, tentity);
    }

    protected void drawGuiContainerForegroundLayer(int par1, int par2)
    {
    	super.drawGuiContainerForegroundLayer(par1, par2);
    	String displayText = "";
        if(tileEntity.isActive)
        {
        	if(tileEntity.currentTicksRequired == 1000)
        	{
        		displayText = "Status: " + Double.toString(Math.round(tileEntity.operatingTicks/10)).replace(".0", "") + "%";
        	}
        	else {
        		displayText = "Status: " + Integer.toString((int)(tileEntity.operatingTicks/8.5)).replace(".0", "") + "%";
        	}
        }
        else {
        	displayText = "Status: " + EnumColor.DARK_RED + "Off";
        }
        fontRenderer.drawString(displayText, 80, 60, 0x404040);
    }
}

package net.uberkat.obsidian.client;

import org.lwjgl.opengl.GL11;
import net.minecraft.src.*;
import net.uberkat.obsidian.common.ContainerAdvancedElectricMachine;
import net.uberkat.obsidian.common.EnumColor;
import net.uberkat.obsidian.common.ObsidianUtils;
import net.uberkat.obsidian.common.TileEntityTheoreticalElementizer;

public class GuiTheoreticalElementizer extends GuiAdvancedElectricMachine
{
    public GuiTheoreticalElementizer(InventoryPlayer inventory, TileEntityTheoreticalElementizer tentity)
    {
        super(inventory, tentity);
    }

    protected void drawGuiContainerForegroundLayer()
    {
    	super.drawGuiContainerForegroundLayer();
    	String displayText = "";
        if(tileEntity.isActive)
        {
        	displayText = "Status: " + Integer.toString(tileEntity.operatingTicks/10) + "%";
        }
        else {
        	displayText = "Status: " + EnumColor.DARK_RED.code + "Off";
        }
        fontRenderer.drawString(displayText, 80, 60, 0x404040);
    }
}

package mekanism.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mekanism.api.EnumColor;
import mekanism.common.MekanismUtils;
import mekanism.common.TileEntityTheoreticalElementizer;
import net.minecraft.entity.player.InventoryPlayer;

@SideOnly(Side.CLIENT)
public class GuiTheoreticalElementizer extends GuiAdvancedElectricMachine
{
    public GuiTheoreticalElementizer(InventoryPlayer inventory, TileEntityTheoreticalElementizer tentity)
    {
        super(inventory, tentity);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2)
    {
    	super.drawGuiContainerForegroundLayer(par1, par2);
    	String displayText = "";
    	
        if(tileEntity.isActive)
        {
        	displayText = "Status: " + (int)(((float)tileEntity.operatingTicks/MekanismUtils.getTicks(tileEntity.speedMultiplier, tileEntity.TICKS_REQUIRED))*100) + "%";
        }
        else {
        	displayText = "Status: " + EnumColor.DARK_RED + "Off";
        }
        fontRenderer.drawString(displayText, 80, 60, 0x404040);
    }
}

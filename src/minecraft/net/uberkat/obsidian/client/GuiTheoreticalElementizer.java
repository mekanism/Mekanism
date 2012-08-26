package net.uberkat.obsidian.client;

import org.lwjgl.opengl.GL11;
import net.minecraft.src.*;
import net.uberkat.obsidian.common.BlockTheoreticalElementizer;
import net.uberkat.obsidian.common.ContainerTheoreticalElementizer;
import net.uberkat.obsidian.common.TileEntityTheoreticalElementizer;

public class GuiTheoreticalElementizer extends GuiContainer
{
    private TileEntityTheoreticalElementizer machineInventory;

    public GuiTheoreticalElementizer(InventoryPlayer par1InventoryPlayer, TileEntityTheoreticalElementizer par2TileEntityTheoreticalElementizer)
    {
        super(new ContainerTheoreticalElementizer(par1InventoryPlayer, par2TileEntityTheoreticalElementizer));
        machineInventory = par2TileEntityTheoreticalElementizer;
    }

    /**
     * Draw the foreground layer for the GuiContainer (everythin in front of the items)
     */
    protected void drawGuiContainerForegroundLayer()
    {
    	String displayText = "";
        fontRenderer.drawString("Theoretical Elementizer", 32, 6, 0x404040);
        fontRenderer.drawString("Inventory", 8, (ySize - 96) + 2, 0x404040);
        if(machineInventory.machineBurnTime > 0)
        {
        	displayText = "Status: " + Integer.toString(machineInventory.machineCookTime/10) + "%";
        }
        else {
        	displayText = "Status: ¤4Off";
        }
        fontRenderer.drawString(displayText, 80, 60, 0x404040);
    }

    /**
     * Draw the background layer for the GuiContainer (everything behind the items)
     */
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
    {
        int var4 = this.mc.renderEngine.getTexture("/gui/GuiElementizer.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(var4);
        int var5 = (this.width - this.xSize) / 2;
        int var6 = (this.height - this.ySize) / 2;
        drawTexturedModalRect(var5, var6, 0, 0, this.xSize, this.ySize);
        int var7;

        if (this.machineInventory.isBurning())
        {
            var7 = this.machineInventory.getBurnTimeRemainingScaled(12);
            this.drawTexturedModalRect(var5 + 56, var6 + 36 + 12 - var7, 176, 12 - var7, 14, var7 + 2);
        }

        var7 = this.machineInventory.getCookProgressScaled(24);
        this.drawTexturedModalRect(var5 + 79, var6 + 34, 176, 14, var7 + 1, 16);
    }
}

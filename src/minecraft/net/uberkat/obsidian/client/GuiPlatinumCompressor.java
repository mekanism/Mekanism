package net.uberkat.obsidian.client;

import org.lwjgl.opengl.GL11;
import net.minecraft.src.*;
import net.uberkat.obsidian.common.BlockPlatinumCompressor;
import net.uberkat.obsidian.common.ContainerPlatinumCompressor;
import net.uberkat.obsidian.common.TileEntityPlatinumCompressor;

public class GuiPlatinumCompressor extends GuiContainer
{
    private TileEntityPlatinumCompressor machineInventory;

    public GuiPlatinumCompressor(InventoryPlayer par1InventoryPlayer, TileEntityPlatinumCompressor par2TileEntityPlatinumCompressor)
    {
        super(new ContainerPlatinumCompressor(par1InventoryPlayer, par2TileEntityPlatinumCompressor));
        machineInventory = par2TileEntityPlatinumCompressor;
    }

    /**
     * Draw the foreground layer for the GuiContainer (everythin in front of the items)
     */
    protected void drawGuiContainerForegroundLayer()
    {
        fontRenderer.drawString("Platinum Compressor", 45, 6, 0x404040);
        fontRenderer.drawString("Inventory", 8, (ySize - 96) + 2, 0x404040);
    }

    /**
     * Draw the background layer for the GuiContainer (everything behind the items)
     */
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
    {
        int var4 = this.mc.renderEngine.getTexture("/gui/GuiCompressor.png");
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

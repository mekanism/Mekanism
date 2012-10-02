package net.uberkat.obsidian.client;

import org.lwjgl.opengl.GL11;
import net.minecraft.src.*;
import net.uberkat.obsidian.common.ContainerElectricMachine;
import net.uberkat.obsidian.common.TileEntityEnrichmentChamber;

public class GuiEnrichmentChamber extends GuiContainer
{
    private TileEntityEnrichmentChamber machineInventory;

    public GuiEnrichmentChamber(InventoryPlayer par1InventoryPlayer, TileEntityEnrichmentChamber par2TileEntityEnrichmentChamber)
    {
        super(new ContainerElectricMachine(par1InventoryPlayer, par2TileEntityEnrichmentChamber));
        machineInventory = par2TileEntityEnrichmentChamber;
    }

    /**
     * Draw the foreground layer for the GuiContainer (everythin in front of the items)
     */
    protected void drawGuiContainerForegroundLayer()
    {
        fontRenderer.drawString("Enrichment Chamber", 45, 6, 0x404040);
        fontRenderer.drawString("Inventory", 8, (ySize - 96) + 2, 0x404040);
    }

    /**
     * Draw the background layer for the GuiContainer (everything behind the items)
     */
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
    {
        int var4 = mc.renderEngine.getTexture("/gui/GuiChamber.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(var4);
        int var5 = (width - xSize) / 2;
        int var6 = (height - ySize) / 2;
        drawTexturedModalRect(var5, var6, 0, 0, xSize, ySize);
        int var7;

        var7 = machineInventory.getScaledChargeLevel(12);
        drawTexturedModalRect(var5 + 56, var6 + 36 + 12 - var7, 176, 12 - var7, 14, var7 + 2);

        var7 = machineInventory.getScaledProgress(24);
        drawTexturedModalRect(var5 + 79, var6 + 34, 176, 14, var7 + 1, 16);
    }
}

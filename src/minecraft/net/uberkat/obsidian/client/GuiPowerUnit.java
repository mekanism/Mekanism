package net.uberkat.obsidian.client;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.*;
import net.uberkat.obsidian.common.ContainerPowerUnit;
import net.uberkat.obsidian.common.ObsidianUtils;
import net.uberkat.obsidian.common.TileEntityPowerUnit;

public class GuiPowerUnit extends GuiContainer
{
	private TileEntityPowerUnit tileEntity;
	
	private int guiWidth;
	private int guiHeight;
	
	public GuiPowerUnit(InventoryPlayer inventory, TileEntityPowerUnit tentity)
	{
		super(new ContainerPowerUnit(inventory, tentity));
		tileEntity = tentity;
	}
	
	protected void drawGuiContainerForegroundLayer(int par1, int par2)
	{
		String capacityInfo = "Stored: " + ObsidianUtils.getDisplayedEnergyNoColor(tileEntity.energyStored) + "/" + ObsidianUtils.getDisplayedEnergyNoColor(tileEntity.MAX_ENERGY);
		String outputInfo = "Output: " + ObsidianUtils.getDisplayedEnergyNoColor(tileEntity.output) + "/t";
		fontRenderer.drawString(tileEntity.getInvName(), 43, 6, 0x404040);
		fontRenderer.drawString(capacityInfo, 45, 42, 0x404040);
		fontRenderer.drawString(outputInfo, 45, 52, 0x404040);
		fontRenderer.drawString(StatCollector.translateToLocal("container.inventory"), 8, ySize - 96 + 2, 0x404040);
	}
	
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
    {
        int texture = mc.renderEngine.getTexture("/gui/GuiPowerUnit.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(texture);
        
        guiWidth = (width - xSize) / 2;
        guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        int scale = (int)(((double)tileEntity.energyStored / tileEntity.MAX_ENERGY) * 72);
        drawTexturedModalRect(guiWidth + 65, guiHeight + 17, 176, 0, scale, 20);
    }
}

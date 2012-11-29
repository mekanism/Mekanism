package mekanism.client;

import org.lwjgl.opengl.GL11;

import universalelectricity.core.electricity.ElectricInfo;
import universalelectricity.core.electricity.ElectricInfo.ElectricUnit;

import mekanism.common.ContainerEnergyCube;
import mekanism.common.MekanismUtils;
import mekanism.common.TileEntityEnergyCube;
import net.minecraft.src.*;

public class GuiEnergyCube extends GuiContainer
{
	private TileEntityEnergyCube tileEntity;
	
	private int guiWidth;
	private int guiHeight;
	
	public GuiEnergyCube(InventoryPlayer inventory, TileEntityEnergyCube tentity)
	{
		super(new ContainerEnergyCube(inventory, tentity));
		tileEntity = tentity;
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2)
	{
		String capacityInfo = ElectricInfo.getDisplayShort(tileEntity.electricityStored, ElectricUnit.JOULES) + "/" + ElectricInfo.getDisplayShort(tileEntity.tier.MAX_ELECTRICITY, ElectricUnit.JOULES);
		String outputInfo = "Out: " + tileEntity.output + "w";
		fontRenderer.drawString(tileEntity.tier.name + " Energy Cube", 43, 6, 0x404040);
		fontRenderer.drawString(capacityInfo, 45, 40, 0x404040);
		fontRenderer.drawString(outputInfo, 45, 49, 0x404040);
		fontRenderer.drawString(StatCollector.translateToLocal("container.inventory"), 8, ySize - 96 + 2, 0x404040);
	}
	
	@Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
    {
        int texture = mc.renderEngine.getTexture("/resources/mekanism/gui/GuiPowerUnit.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(texture);
        
        guiWidth = (width - xSize) / 2;
        guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        int scale = (int)(((double)tileEntity.electricityStored / tileEntity.tier.MAX_ELECTRICITY) * 72);
        drawTexturedModalRect(guiWidth + 65, guiHeight + 17, 176, 0, scale, 20);
    }
}

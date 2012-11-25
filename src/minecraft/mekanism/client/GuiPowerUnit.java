package mekanism.client;

import org.lwjgl.opengl.GL11;

import universalelectricity.core.electricity.ElectricInfo;
import universalelectricity.core.electricity.ElectricInfo.ElectricUnit;

import mekanism.common.ContainerPowerUnit;
import mekanism.common.MekanismUtils;
import mekanism.common.TileEntityPowerUnit;
import net.minecraft.src.*;

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
	
	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2)
	{
		String capacityInfo = ElectricInfo.getDisplayShort(tileEntity.electricityStored, ElectricUnit.JOULES) + "/" + ElectricInfo.getDisplayShort(tileEntity.MAX_ELECTRICITY, ElectricUnit.JOULES);
		String outputInfo = "Out: " + tileEntity.output + "w";
		fontRenderer.drawString(tileEntity.getInvName(), 43, 6, 0x404040);
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
        int scale = (int)(((double)tileEntity.electricityStored / tileEntity.MAX_ELECTRICITY) * 72);
        drawTexturedModalRect(guiWidth + 65, guiHeight + 17, 176, 0, scale, 20);
    }
}

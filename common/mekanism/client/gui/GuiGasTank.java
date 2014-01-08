package mekanism.client.gui;

import mekanism.common.inventory.container.ContainerGasTank;
import mekanism.common.tile.TileEntityGasTank;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiGasTank extends GuiMekanism
{
	public TileEntityGasTank tileEntity;
	
	public GuiGasTank(InventoryPlayer inventory, TileEntityGasTank tentity)
	{
		super(new ContainerGasTank(inventory, tentity));
		tileEntity = tentity;
		guiElements.add(new GuiRedstoneControl(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiGasTank.png")));
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
			
		String capacityInfo = tileEntity.gasTank.getStored() + "/" + tileEntity.MAX_GAS;
		
		fontRenderer.drawString(tileEntity.getInvName(), 43, 6, 0x404040);
		fontRenderer.drawString(capacityInfo, 45, 40, 0x404040);
		fontRenderer.drawString("Gas: " + (tileEntity.gasTank.getGas() != null ? tileEntity.gasTank.getGas().getGas().getLocalizedName() : "None"), 45, 49, 0x404040);
		fontRenderer.drawString(MekanismUtils.localize("container.inventory"), 8, ySize - 96 + 2, 0x404040);
		
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}
	
	@Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
    {
		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
		
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiGasTank.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        
		if(tileEntity.gasTank.getGas() != null)
		{
	        int scale = (int)(((double)tileEntity.gasTank.getStored() / tileEntity.MAX_GAS) * 72);
	        drawTexturedModalRect(guiWidth + 65, guiHeight + 17, 176, 0, scale, 20);
		}
    }
}

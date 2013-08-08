package mekanism.client;

import mekanism.common.ContainerGasTank;
import mekanism.common.MekanismUtils;
import mekanism.common.TileEntityGasTank;
import mekanism.common.MekanismUtils.ResourceType;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiGasTank extends GuiContainer
{
	public TileEntityGasTank tileEntity;
	
	public GuiRedstoneControl redstoneControl;
	
	public GuiGasTank(InventoryPlayer inventory, TileEntityGasTank tentity)
	{
		super(new ContainerGasTank(inventory, tentity));
		tileEntity = tentity;
		redstoneControl = new GuiRedstoneControl(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiGasTank.png"));
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
			
		String capacityInfo = tileEntity.gasStored + "/" + tileEntity.MAX_GAS;
		
		fontRenderer.drawString("Gas Tank", 43, 6, 0x404040);
		fontRenderer.drawString(capacityInfo, 45, 40, 0x404040);
		fontRenderer.drawString("Gas: " + tileEntity.gasType.name, 45, 49, 0x404040);
		fontRenderer.drawString(StatCollector.translateToLocal("container.inventory"), 8, ySize - 96 + 2, 0x404040);
		
		redstoneControl.renderForeground(xAxis, yAxis);
	}
	
	@Override
    protected void drawGuiContainerBackgroundLayer(float par1, int mouseX, int mouseY)
    {
		mc.renderEngine.func_110577_a(MekanismUtils.getResource(ResourceType.GUI, "GuiGasTank.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        
        int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
        
        int scale = (int)(((double)tileEntity.gasStored / tileEntity.MAX_GAS) * 72);
        drawTexturedModalRect(guiWidth + 65, guiHeight + 17, 176, 0, scale, 20);
        
        redstoneControl.renderBackground(xAxis, yAxis, guiWidth, guiHeight);
    }
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button)
	{
		super.mouseClicked(mouseX, mouseY, button);
		
		if(button == 0)
		{
			int xAxis = (mouseX - (width - xSize) / 2);
			int yAxis = (mouseY - (height - ySize) / 2);
			
			redstoneControl.mouseClicked(xAxis, yAxis);
		}
	}
}

package mekanism.client;

import mekanism.common.ContainerRobitMain;
import mekanism.common.EntityRobit;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.opengl.GL11;

import universalelectricity.core.electricity.ElectricityDisplay;
import universalelectricity.core.electricity.ElectricityDisplay.ElectricUnit;

public class GuiRobitMain extends GuiContainer
{
	public EntityRobit robit;
    
    public GuiRobitMain(InventoryPlayer inventory, EntityRobit entity)
    {
    	super(new ContainerRobitMain(inventory, entity));
    	xSize += 25;
    	robit = entity;
    }
	
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
    	fontRenderer.drawString("Robit", 76, 6, 0x404040);
    	fontRenderer.drawString("Inventory", 8, (ySize - 96) + 3, 0x404040);
    	fontRenderer.drawString("Hi, I'm Robit!", 29, 18, 0x00CD00);
    	fontRenderer.drawString("Energy: " + ElectricityDisplay.getDisplayShort(robit.getEnergy(), ElectricUnit.JOULES), 29, 36, 0x00CD00);
    	fontRenderer.drawString("Following: " + robit.getFollowing(), 29, 45, 0x00CD00);
    	fontRenderer.drawString("Owner: " + robit.getOwnerName(), 29, 54, 0x00CD00);
    	
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
    	
		if(xAxis >= 20 && xAxis <= 24 && yAxis >= 17 && yAxis <= 70)
		{
			drawCreativeTabHoveringText(ElectricityDisplay.getDisplayShort(robit.getEnergy(), ElectricUnit.JOULES), xAxis, yAxis);
		}
		else if(xAxis >= 152 && xAxis <= 170 && yAxis >= 53 && yAxis <= 71)
		{
			drawCreativeTabHoveringText("Toggle 'follow' mode", xAxis, yAxis);
		}
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int mouseX, int mouseY)
    {
        mc.renderEngine.bindTexture("/mods/mekanism/gui/GuiRobitMain.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
        
		if(xAxis >= 179 && xAxis <= 197 && yAxis >= 10 && yAxis <= 28)
		{
			drawTexturedModalRect(guiWidth + 179, guiHeight + 10, 176 + 25, 0, 18, 18);
		}
		else {
			drawTexturedModalRect(guiWidth + 179, guiHeight + 10, 176 + 25, 18, 18, 18);
		}
		
		if(xAxis >= 179 && xAxis <= 197 && yAxis >= 30 && yAxis <= 48)
		{
			drawTexturedModalRect(guiWidth + 179, guiHeight + 30, 176 + 25, 36, 18, 18);
		}
		else {
			drawTexturedModalRect(guiWidth + 179, guiHeight + 30, 176 + 25, 54, 18, 18);
		}
		
		if(xAxis >= 179 && xAxis <= 197 && yAxis >= 50 && yAxis <= 68)
		{
			drawTexturedModalRect(guiWidth + 179, guiHeight + 50, 176 + 25, 72, 18, 18);
		}
		else {
			drawTexturedModalRect(guiWidth + 179, guiHeight + 50, 176 + 25, 90, 18, 18);
		}
		
		if(xAxis >= 179 && xAxis <= 197 && yAxis >= 70 && yAxis <= 88)
		{
			drawTexturedModalRect(guiWidth + 179, guiHeight + 70, 176 + 25, 108, 18, 18);
		}
		else {
			drawTexturedModalRect(guiWidth + 179, guiHeight + 70, 176 + 25, 126, 18, 18);
		}
		
		if(xAxis >= 179 && xAxis <= 197 && yAxis >= 90 && yAxis <= 108)
		{
			drawTexturedModalRect(guiWidth + 179, guiHeight + 90, 176 + 25, 144, 18, 18);
		}
		else {
			drawTexturedModalRect(guiWidth + 179, guiHeight + 90, 176 + 25, 162, 18, 18);
		}
		
		if(xAxis >= 152 && xAxis <= 170 && yAxis >= 53 && yAxis <= 71)
		{
			drawTexturedModalRect(guiWidth + 152, guiHeight + 53, 176 + 25, 180, 18, 18);
		}
		else {
			drawTexturedModalRect(guiWidth + 152, guiHeight + 53, 176 + 25, 198, 18, 18);
		}
		
		int displayInt;
		
        displayInt = getScaledEnergyLevel(53);
        drawTexturedModalRect(guiWidth + 20, guiHeight + 17 + 53 - displayInt, 176 + 25 + 18, 36 + 53 - displayInt, 4, displayInt);
    }
    
	private int getScaledEnergyLevel(int i)
	{
		return (int)(robit.getEnergy()*i / robit.MAX_ELECTRICITY);
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button)
	{
		super.mouseClicked(mouseX, mouseY, button);
		
		if(button == 0)
		{
			int xAxis = (mouseX - (width - xSize) / 2);
			int yAxis = (mouseY - (height - ySize) / 2);
			
			if(xAxis >= 179 && xAxis <= 197 && yAxis >= 10 && yAxis <= 28)
			{	
				mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
			}
			else if(xAxis >= 179 && xAxis <= 197 && yAxis >= 30 && yAxis <= 48)
			{
				mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
				PacketHandler.sendRobitGui(1, robit.entityId);
				mc.thePlayer.openGui(Mekanism.instance, 22, mc.theWorld, robit.entityId, 0, 0);
			}
			else if(xAxis >= 179 && xAxis <= 197 && yAxis >= 50 && yAxis <= 68)
			{
				mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
				PacketHandler.sendRobitGui(2, robit.entityId);
				mc.thePlayer.openGui(Mekanism.instance, 23, mc.theWorld, robit.entityId, 0, 0);
			}
			else if(xAxis >= 179 && xAxis <= 197 && yAxis >= 70 && yAxis <= 88)
			{
				mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
				PacketHandler.sendRobitGui(3, robit.entityId);
				mc.thePlayer.openGui(Mekanism.instance, 24, mc.theWorld, robit.entityId, 0, 0);
			}
			else if(xAxis >= 179 && xAxis <= 197 && yAxis >= 90 && yAxis <= 108)
			{
				mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
				PacketHandler.sendRobitGui(4, robit.entityId);
				mc.thePlayer.openGui(Mekanism.instance, 25, mc.theWorld, robit.entityId, 0, 0);
			}
			else if(xAxis >= 152 && xAxis <= 170 && yAxis >= 53 && yAxis <= 71)
			{
				mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
				PacketHandler.sendFollowUpdate(!robit.getFollowing(), robit.entityId);
			}
		}
	}
}

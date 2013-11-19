package mekanism.client.gui;

import java.util.ArrayList;

import mekanism.api.Object3D;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.inventory.container.ContainerDigitalMiner;
import mekanism.common.network.PacketDigitalMinerGui;
import mekanism.common.network.PacketDigitalMinerGui.MinerGuiPacket;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tileentity.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import universalelectricity.core.electricity.ElectricityDisplay;
import universalelectricity.core.electricity.ElectricityDisplay.ElectricUnit;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiDigitalMiner extends GuiMekanism
{
	public TileEntityDigitalMiner tileEntity;

    public GuiDigitalMiner(InventoryPlayer inventory, TileEntityDigitalMiner tentity)
    {
        super(new ContainerDigitalMiner(inventory, tentity));
        tileEntity = tentity;
        
        guiElements.add(new GuiRedstoneControl(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiDigitalMiner.png")));
        guiElements.add(new GuiUpgradeManagement(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiDigitalMiner.png")));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
    	super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    	
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
		
        fontRenderer.drawString(tileEntity.fullName, 45, 6, 0x404040);
        fontRenderer.drawString("Inventory", 8, (ySize - 96) + 2, 0x404040);
        
    	if(tileEntity.replaceStack != null)
		{
			GL11.glPushMatrix();
			GL11.glEnable(GL11.GL_LIGHTING);
			itemRenderer.renderItemAndEffectIntoGUI(fontRenderer, mc.getTextureManager(), tileEntity.replaceStack, 144, 27);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glPopMatrix();
		}
    
		if(xAxis >= 164 && xAxis <= 168 && yAxis >= 25 && yAxis <= 77)
		{
			drawCreativeTabHoveringText(ElectricityDisplay.getDisplayShort(tileEntity.getEnergyStored(), ElectricUnit.JOULES), xAxis, yAxis);
		}
		
		if(xAxis >= 147 && xAxis <= 161 && yAxis >= 47 && yAxis <= 61)
		{
			drawCreativeTabHoveringText("Auto-eject", xAxis, yAxis);
		}
		
		if(xAxis >= 147 && xAxis <= 161 && yAxis >= 63 && yAxis <= 77)
		{
			drawCreativeTabHoveringText("Auto-pull", xAxis, yAxis);
		}
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
		
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiDigitalMiner.png"));
	    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	    int guiWidth = (width - xSize) / 2;
	    int guiHeight = (height - ySize) / 2;
	    drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
	    
	    int xAxis = mouseX - guiWidth;
		int yAxis = mouseY - guiHeight;
		
	    int displayInt;
	    
	    displayInt = tileEntity.getScaledEnergyLevel(52);
	    drawTexturedModalRect(guiWidth + 164, guiHeight + 25 + 52 - displayInt, 176, 52 - displayInt, 4, displayInt);
	    
		if(xAxis >= 147 && xAxis <= 161 && yAxis >= 47 && yAxis <= 61)
		{
			drawTexturedModalRect(guiWidth + 147, guiHeight + 47, 176 + 4, 0, 14, 14);
		}
		else {
			drawTexturedModalRect(guiWidth + 147, guiHeight + 47, 176 + 4, 14, 14, 14);
		}
		
		if(xAxis >= 147 && xAxis <= 161 && yAxis >= 63 && yAxis <= 77)
		{
			drawTexturedModalRect(guiWidth + 147, guiHeight + 63, 176 + 4 + 14, 0, 14, 14);
		}
		else {
			drawTexturedModalRect(guiWidth + 147, guiHeight + 63, 176 + 4 + 14, 14, 14, 14);
		}
	}
	
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button)
    {
        super.mouseClicked(mouseX, mouseY, button);
        
    	if(button == 0)
		{
			int xAxis = (mouseX - (width - xSize) / 2);
			int yAxis = (mouseY - (height - ySize) / 2);
			
			if(xAxis >= 147 && xAxis <= 161 && yAxis >= 47 && yAxis <= 61)
			{
				mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
				
				ArrayList data = new ArrayList();
				data.add(0);
				
				PacketHandler.sendPacket(Transmission.SERVER, new PacketTileEntity().setParams(Object3D.get(tileEntity), data));
			}
			
			if(xAxis >= 147 && xAxis <= 161 && yAxis >= 63 && yAxis <= 77)
			{
				mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
				
				ArrayList data = new ArrayList();
				data.add(1);
				
				PacketHandler.sendPacket(Transmission.SERVER, new PacketTileEntity().setParams(Object3D.get(tileEntity), data));
			}
			
			if(xAxis >= 12 && xAxis <= 28 && yAxis >= 19 && yAxis <= 35)
			{
				ItemStack stack = mc.thePlayer.inventory.getItemStack();
				
				if(stack != null && !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
				{
					tileEntity.replaceStack = stack.copy();
					tileEntity.replaceStack.stackSize = 1;
				}
				else if(stack == null && Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
				{
					tileEntity.replaceStack = null;
				}
				
	           	mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
			}
		}
    }
}

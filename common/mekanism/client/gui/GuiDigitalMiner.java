package mekanism.client.gui;

import java.util.ArrayList;

import mekanism.api.Object3D;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.inventory.container.ContainerDigitalMiner;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tileentity.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemBlock;
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
	
	public GuiButton startButton;
	public GuiButton stopButton;
	public GuiButton configButton;

    public GuiDigitalMiner(InventoryPlayer inventory, TileEntityDigitalMiner tentity)
    {
        super(new ContainerDigitalMiner(inventory, tentity));
        tileEntity = tentity;
        
        guiElements.add(new GuiRedstoneControl(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiDigitalMiner.png")));
        guiElements.add(new GuiUpgradeManagement(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiDigitalMiner.png")));
        
        ySize+=64;
    }
    
	@Override
	public void initGui()
	{
		super.initGui();
		
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
		
		buttonList.clear();
		startButton = new GuiButton(0, guiWidth + 69, guiHeight + 17, 60, 20, "Start");
		
		if(tileEntity.searcher.finished == true && tileEntity.running)
		{
			startButton.enabled = false;
		}
		
		stopButton = new GuiButton(1, guiWidth + 69, guiHeight + 37, 60, 20, "Stop");
		
		if(tileEntity.searcher.finished == false || !tileEntity.running)
		{
			stopButton.enabled = false;
		}
		
		configButton = new GuiButton(2, guiWidth + 69, guiHeight + 57, 60, 20, "Config");
		
		if(tileEntity.searcher.finished == true)
		{
			configButton.enabled = false;
		}
		
		buttonList.add(startButton);
		buttonList.add(stopButton);
		buttonList.add(configButton);
	}
	
	@Override
	public void updateScreen()
	{
		super.updateScreen();
		
		if(tileEntity.searcher.finished == true && tileEntity.running)
		{
			startButton.enabled = false;
		}
		
		if(tileEntity.searcher.finished == false || !tileEntity.running)
		{
			stopButton.enabled = false;
		}
		
		if(tileEntity.searcher.finished == true)
		{
			configButton.enabled = false;
		}
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
		
		if(xAxis >= 144 && xAxis <= 160 && yAxis >= 27 && yAxis <= 43)
		{
			drawCreativeTabHoveringText("Replace block", xAxis, yAxis);
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
		
		if(xAxis >= 144 && xAxis <= 160 && yAxis >= 27 && yAxis <= 43)
		{
			GL11.glPushMatrix();
	        GL11.glDisable(GL11.GL_LIGHTING);
	        GL11.glDisable(GL11.GL_DEPTH_TEST);
	        
	        int x = guiWidth + 144;
	        int y = guiHeight + 27;
	        drawGradientRect(x, y, x + 16, y + 16, -2130706433, -2130706433);
	        
	        GL11.glEnable(GL11.GL_LIGHTING);
	        GL11.glEnable(GL11.GL_DEPTH_TEST);
	        GL11.glPopMatrix();
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
			
			if(xAxis >= 144 && xAxis <= 160 && yAxis >= 27 && yAxis <= 43)
			{
				boolean doNull = false;
				ItemStack stack = mc.thePlayer.inventory.getItemStack();
				ItemStack toUse = null;
				
				if(stack != null && !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
				{
					if(stack.getItem() instanceof ItemBlock)
					{
						toUse = stack.copy();
					}
				}
				else if(stack == null && Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
				{
					doNull = true;
				}
				
				if(stack != null || doNull)
				{
					ArrayList data = new ArrayList();
					data.add(2);
					
					if(stack != null)
					{
						data.add(false);
						data.add(stack.itemID);
						data.add(stack.getItemDamage());
					}
					else {
						data.add(true);
					}
					
					PacketHandler.sendPacket(Transmission.SERVER, new PacketTileEntity().setParams(Object3D.get(tileEntity), data));
				}
				
	           	mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
			}
		}
    }
}

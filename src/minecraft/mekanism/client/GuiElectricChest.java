package mekanism.client;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;

import universalelectricity.core.item.IItemElectric;

import mekanism.api.EnumColor;
import mekanism.common.ContainerElectricChest;
import mekanism.common.IElectricChest;
import mekanism.common.InventoryElectricChest;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.TileEntityElectricChest;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class GuiElectricChest extends GuiContainer
{
	public TileEntityElectricChest tileEntity;
	public IInventory itemInventory;
	public boolean isBlock;
	
	public GuiElectricChest(InventoryPlayer inventory, TileEntityElectricChest tentity)
    {
        super(new ContainerElectricChest(inventory, tentity, null, true));
        xSize+=26;
        ySize+=64;
        tileEntity = tentity;
        isBlock = true;
    }
	
	public GuiElectricChest(InventoryPlayer inventory, IInventory inv)
    {
        super(new ContainerElectricChest(inventory, null, inv, false));
        xSize+=26;
        ySize+=64;
        itemInventory = inv;
        isBlock = false;
    }
	
	@Override
	public void onGuiClosed() 
	{
		super.onGuiClosed();
		
		if(!isBlock)
		{
			mc.sndManager.playSoundFX("random.chestclosed", 1.0F, 1.0F);
		}
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		
		int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        
		buttonList.clear();
		buttonList.add(new GuiButton(0, guiWidth + 93, guiHeight + 4, 76, 20, "Edit Password"));
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton)
	{
		if(guibutton.id == 0)
		{
			if(isBlock)
			{
				mc.thePlayer.openGui(Mekanism.instance, 20, mc.theWorld, tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
			}
			else {
				FMLClientHandler.instance().displayGuiScreen(mc.thePlayer, new GuiPasswordModify(((InventoryElectricChest)itemInventory).itemStack));
			}
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2)
    {
        fontRenderer.drawString("Electric Chest", 8, 6, 0x404040);
        fontRenderer.drawString(getLocked() ? EnumColor.DARK_RED + "Locked" : EnumColor.BRIGHT_GREEN + "Unlocked", 97, 137, 0x404040);
        fontRenderer.drawString("Inventory", 8, (ySize - 96) + 2, 0x404040);
    }
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button)
	{
		super.mouseClicked(mouseX, mouseY, button);
		
		if(button == 0)
		{
			int xAxis = (mouseX - (width - xSize) / 2);
			int yAxis = (mouseY - (height - ySize) / 2);
			
			if(xAxis >= 179 && xAxis <= 197 && yAxis >= 88 && yAxis <= 106)
			{	
				mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
				PacketHandler.sendLockChange(tileEntity, !getLocked(), isBlock);
				
				if(!isBlock)
				{
					ItemStack stack = ((InventoryElectricChest)itemInventory).itemStack;
					((IElectricChest)stack.getItem()).setLocked(stack, !getLocked());
				}
			}
		}
	}

	@Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
    {
        mc.renderEngine.bindTexture("/mods/mekanism/gui/GuiElectricChest.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        
		int xAxis = (par2 - (width - xSize) / 2);
		int yAxis = (par3 - (height - ySize) / 2);
        
		if(xAxis >= 179 && xAxis <= 197 && yAxis >= 88 && yAxis <= 106)
		{
			drawTexturedModalRect(guiWidth + 179, guiHeight + 88, 176 + 26, 52, 18, 18);
		}
		else {
			drawTexturedModalRect(guiWidth + 179, guiHeight + 88, 176 + 26, 70, 18, 18);
		}
        
        int displayInt = getScale();
        drawTexturedModalRect(guiWidth + 180, guiHeight + 32 + 52 - displayInt, 176 + 26, 52 - displayInt, 4, displayInt);
    }
	
	public boolean getLocked()
	{
		if(isBlock)
		{
			return tileEntity.locked;
		}
		else {
			ItemStack stack = ((InventoryElectricChest)itemInventory).itemStack;
			return ((IElectricChest)stack.getItem()).getLocked(stack);
		}
	}
	
	public int getScale()
	{
		if(isBlock)
		{
			return tileEntity.getScaledEnergyLevel(52);
		}
		else {
			ItemStack stack = ((InventoryElectricChest)itemInventory).itemStack;
			return (int)(((IItemElectric)stack.getItem()).getJoules(stack)*52 / ((IItemElectric)stack.getItem()).getMaxJoules(stack));
		}
	}
}

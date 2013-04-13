package mekanism.client;

import mekanism.common.EnumPacketType;
import mekanism.common.ItemPortableTeleporter;
import mekanism.common.PacketHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiPortableTeleporter extends GuiScreen
{
	public EntityPlayer entityPlayer;
    public ItemStack itemStack;
    
    public int xSize = 176;
    public int ySize = 166;

    public GuiPortableTeleporter(EntityPlayer player, ItemStack itemstack)
    {
    	entityPlayer = player;
    	itemStack = itemstack;
    }
    
    @Override
    public void initGui()
    {
    	buttonList.clear();
    	buttonList.add(new GuiButton(0, 173, 105, 80, 20, "Teleport"));
    }

    @Override
    public void drawScreen(int i, int j, float f)
    {
    	if(mc.thePlayer.getCurrentEquippedItem() != null && mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemPortableTeleporter)
    	{
    		itemStack = mc.thePlayer.getCurrentEquippedItem();
    	}
        
    	mc.renderEngine.bindTexture("/mods/mekanism/gui/GuiPortableTeleporter.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        
        int displayInt;
        
        displayInt = getYAxisForNumber(((ItemPortableTeleporter)itemStack.getItem()).getDigit(itemStack, 0));
        drawTexturedModalRect(guiWidth + 23, guiHeight + 44, 176, displayInt, 13, 13);
        
        displayInt = getYAxisForNumber(((ItemPortableTeleporter)itemStack.getItem()).getDigit(itemStack, 1));
        drawTexturedModalRect(guiWidth + 62, guiHeight + 44, 176, displayInt, 13, 13);
        
        displayInt = getYAxisForNumber(((ItemPortableTeleporter)itemStack.getItem()).getDigit(itemStack, 2));
        drawTexturedModalRect(guiWidth + 101, guiHeight + 44, 176, displayInt, 13, 13);
        
        displayInt = getYAxisForNumber(((ItemPortableTeleporter)itemStack.getItem()).getDigit(itemStack, 3));
        drawTexturedModalRect(guiWidth + 140, guiHeight + 44, 176, displayInt, 13, 13);
        
        ItemPortableTeleporter item = (ItemPortableTeleporter)itemStack.getItem();
        
        ((GuiButton)buttonList.get(0)).xPosition = guiWidth+48;
        ((GuiButton)buttonList.get(0)).yPosition = guiHeight+68;
        
        fontRenderer.drawString("Portable Teleporter", guiWidth+39, guiHeight+6, 0x404040);
        fontRenderer.drawString(item.getStatusAsString(item.getStatus(itemStack)), guiWidth+53, guiHeight+19, 0x00CD00);
        super.drawScreen(i, j, f);
    }
    
	@Override
	protected void actionPerformed(GuiButton guibutton)
	{
		if(guibutton.id == 0)
		{
			PacketHandler.sendPacketDataInt(EnumPacketType.PORTABLE_TELEPORT, 0);
			mc.setIngameFocus();
		}
	}
    
	@Override
    protected void mouseClicked(int x, int y, int button)
    {
		super.mouseClicked(x, y, button);
		
		int xAxis = (x - (width - xSize) / 2);
		int yAxis = (y - (height - ySize) / 2);
		
		if(xAxis > 23 && xAxis < 37 && yAxis > 44 && yAxis < 58)
		{
			PacketHandler.sendDigitUpdate(0, getIncrementedNumber(((ItemPortableTeleporter)itemStack.getItem()).getDigit(itemStack, 0)));
			((ItemPortableTeleporter)itemStack.getItem()).setDigit(itemStack, 0, getIncrementedNumber(((ItemPortableTeleporter)itemStack.getItem()).getDigit(itemStack, 0)));
			mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
		}
		else if(xAxis > 62 && xAxis < 76 && yAxis > 44 && yAxis < 58)
		{
			PacketHandler.sendDigitUpdate(1, getIncrementedNumber(((ItemPortableTeleporter)itemStack.getItem()).getDigit(itemStack, 1)));
			((ItemPortableTeleporter)itemStack.getItem()).setDigit(itemStack, 1, getIncrementedNumber(((ItemPortableTeleporter)itemStack.getItem()).getDigit(itemStack, 1)));
			mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
		}
		else if(xAxis > 101 && xAxis < 115 && yAxis > 44 && yAxis < 58)
		{
			PacketHandler.sendDigitUpdate(2, getIncrementedNumber(((ItemPortableTeleporter)itemStack.getItem()).getDigit(itemStack, 2)));
			((ItemPortableTeleporter)itemStack.getItem()).setDigit(itemStack, 2, getIncrementedNumber(((ItemPortableTeleporter)itemStack.getItem()).getDigit(itemStack, 2)));
			mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
		}
		else if(xAxis > 140 && xAxis < 154 && yAxis > 44 && yAxis < 58)
		{
			PacketHandler.sendDigitUpdate(3, getIncrementedNumber(((ItemPortableTeleporter)itemStack.getItem()).getDigit(itemStack, 3)));
			((ItemPortableTeleporter)itemStack.getItem()).setDigit(itemStack, 3, getIncrementedNumber(((ItemPortableTeleporter)itemStack.getItem()).getDigit(itemStack, 3)));
			mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
		}
    }
    
    public int getIncrementedNumber(int i)
    {
    	if(i < 9) i++;
    	else if(i == 9) i=0;
    	
    	return i;
    }
    
    public int getYAxisForNumber(int i)
    {
    	return i*13;
    }
    
    @Override
    public boolean doesGuiPauseGame()
    {
    	return false;
    }
}

package mekanism.client;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import universalelectricity.core.item.IItemElectric;

import mekanism.api.EnumColor;
import mekanism.common.EnumPacketType;
import mekanism.common.IElectricChest;
import mekanism.common.InventoryElectricChest;
import mekanism.common.PacketHandler;
import mekanism.common.TileEntityElectricChest;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.item.ItemStack;

public class GuiPasswordEnter extends GuiScreen
{
    public int xSize = 176;
    public int ySize = 95;
	
	public TileEntityElectricChest tileEntity;
	
	public ItemStack itemStack;
	
	public boolean isBlock;
	
	public GuiTextField passwordField;
	
	public String displayText = EnumColor.BRIGHT_GREEN + "Enter password";
	
	public int ticker = 0;
	
	public GuiPasswordEnter(TileEntityElectricChest tileentity)
	{
		isBlock = true;
		tileEntity = tileentity;
	}
	
	public GuiPasswordEnter(ItemStack itemstack)
	{
		isBlock = false;
		itemStack = itemstack;
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button)
	{
		super.mouseClicked(mouseX, mouseY, button);
		passwordField.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
		
		buttonList.clear();
		buttonList.add(new GuiButton(0, guiWidth + 55, guiHeight + 68, 60, 20, "Open"));
		
		passwordField = new GuiTextField(fontRenderer, guiWidth + 45, guiHeight + 50, 80, 12);
		passwordField.setMaxStringLength(12);
		passwordField.setFocused(true);
	}
	
	@Override
	public void keyTyped(char c, int i)
	{
		super.keyTyped(c, i);
		
		if(i == Keyboard.KEY_RETURN)
		{
			tryOpen();
		}
		
		passwordField.textboxKeyTyped(c, i);
	}
	
	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}
	
	@Override
	public void updateScreen()
	{
		passwordField.updateCursorCounter();
		
		if(ticker > 0)
		{
			ticker--;
		}
		else {
			displayText = EnumColor.BRIGHT_GREEN + "Enter password";
		}
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton)
	{
		if(guibutton.id == 0)
		{
			tryOpen();
		}
	}
	
	public void tryOpen()
	{
		if(passwordField.getText() == null || passwordField.getText() == "")
		{
			displayText = EnumColor.DARK_RED + "Field empty";
			ticker = 30;
		}
		else if(!getPassword().equals(passwordField.getText()))
		{
			displayText = EnumColor.DARK_RED + "Invalid";
			passwordField.setText("");
			ticker = 30;
		}
		else {
			if(isBlock)
			{
				tileEntity.setJoules(tileEntity.getJoules() - 100);
				PacketHandler.sendChestOpen(tileEntity, true, true);
			}
			else {
				((IItemElectric)itemStack.getItem()).setJoules(((IItemElectric)itemStack.getItem()).getJoules(itemStack) - 100, itemStack);
				PacketHandler.sendChestOpen(null, false, true);
			}
		}
	}
	
	public String getPassword()
	{
		if(isBlock)
		{
			return tileEntity.password;
		}
		else {
			return ((IElectricChest)itemStack.getItem()).getPassword(itemStack);
		}
	}
	
	@Override
	public void drawScreen(int i, int j, float f)
	{
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture("/mods/mekanism/gui/GuiPasswordEnter.png");
        
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        passwordField.drawTextBox();
        super.drawScreen(i, j, f);
        fontRenderer.drawString("Password", guiWidth + 64, guiHeight + 5, 0x404040);
        fontRenderer.drawString("Enter:", guiWidth + 45, guiHeight + 40, 0x404040);
        fontRenderer.drawString(displayText, guiWidth + 37, guiHeight + 19, 0x404040);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
	}
}

package mekanism.client;

import mekanism.api.EnumColor;
import mekanism.common.IElectricChest;
import mekanism.common.InventoryElectricChest;
import mekanism.common.PacketHandler;
import mekanism.common.TileEntityElectricChest;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class GuiPasswordModify extends GuiScreen
{
    public int xSize = 176;
    public int ySize = 95;
	
	public TileEntityElectricChest tileEntity;
	
	public ItemStack itemStack;
	
	public boolean isBlock;
	
	public GuiTextField newPasswordField;
	
	public GuiTextField confirmPasswordField;
	
	public String displayText = EnumColor.BRIGHT_GREEN + "Set password";
	
	public int ticker = 0;
	
	public GuiPasswordModify(TileEntityElectricChest tileentity)
	{
		isBlock = true;
		tileEntity = tileentity;
	}
	
	public GuiPasswordModify(ItemStack itemstack)
	{
		isBlock = false;
		itemStack = itemstack;
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
		
		buttonList.clear();
		buttonList.add(new GuiButton(0, guiWidth + 55, guiHeight + 68, 60, 20, "Confirm"));
		
		newPasswordField = new GuiTextField(fontRenderer, guiWidth + 60, guiHeight + 34, 80, 12);
		newPasswordField.setMaxStringLength(12);
		newPasswordField.setFocused(true);
		
		confirmPasswordField = new GuiTextField(fontRenderer, guiWidth + 60, guiHeight + 51, 80, 12);
		confirmPasswordField.setMaxStringLength(12);
		confirmPasswordField.setFocused(false);
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button)
	{
		super.mouseClicked(mouseX, mouseY, button);
		newPasswordField.mouseClicked(mouseX, mouseY, button);
		confirmPasswordField.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public void keyTyped(char c, int i)
	{
		super.keyTyped(c, i);
		
		if(i == Keyboard.KEY_TAB)
		{
			if(!newPasswordField.isFocused() && !confirmPasswordField.isFocused())
			{
				newPasswordField.setFocused(true);
				return;
			}
			else if(newPasswordField.isFocused() && confirmPasswordField.isFocused())
			{
				newPasswordField.setFocused(true);
				confirmPasswordField.setFocused(false);
				return;
			}
			else {
				newPasswordField.setFocused(!newPasswordField.isFocused());
				confirmPasswordField.setFocused(!confirmPasswordField.isFocused());
				return;
			}
		}
		else if(i == Keyboard.KEY_RETURN)
		{
			tryModify();
		}
		
		newPasswordField.textboxKeyTyped(c, i);
		confirmPasswordField.textboxKeyTyped(c, i);
	}
	
	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}
	
	@Override
	public void updateScreen()
	{
		newPasswordField.updateCursorCounter();
		
		if(ticker > 0)
		{
			ticker--;
		}
		else {
			displayText = EnumColor.BRIGHT_GREEN + "Set password";
		}
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton)
	{
		if(guibutton.id == 0)
		{
			tryModify();
		}
	}
	
	public void tryModify()
	{
		if(newPasswordField.getText() == null || newPasswordField.getText().equals("") || confirmPasswordField.getText() == null || confirmPasswordField.getText().equals(""))
		{
			displayText = EnumColor.DARK_RED + "Field(s) empty";
			ticker = 30;
		}
		else if(!newPasswordField.getText().equals(confirmPasswordField.getText()))
		{
			displayText = EnumColor.DARK_RED + "Not matching";
			ticker = 30;
		}
		else if(confirmPasswordField.getText().equals(getPassword()))
		{
			displayText = EnumColor.DARK_RED + "Identical";
			ticker = 30;
		}
		else {
			if(isBlock)
			{
				PacketHandler.sendPasswordChange(tileEntity, confirmPasswordField.getText(), true);
				PacketHandler.sendChestOpen(tileEntity, true, false);
			}
			else {
				((IElectricChest)itemStack.getItem()).setPassword(itemStack, confirmPasswordField.getText());
				PacketHandler.sendPasswordChange(null, confirmPasswordField.getText(), false);
				PacketHandler.sendChestOpen(null, false, false);
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
        super.drawScreen(i, j, f);
        fontRenderer.drawString("Password", guiWidth + 64, guiHeight + 5, 0x404040);
        fontRenderer.drawString(displayText, guiWidth + 37, guiHeight + 19, 0x404040);
        fontRenderer.drawString("Enter:", guiWidth + 27, guiHeight + 37, 0x404040);
        fontRenderer.drawString("Repeat:", guiWidth + 21, guiHeight + 54, 0x404040);
        newPasswordField.drawTextBox();
        confirmPasswordField.drawTextBox();
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
	}
}

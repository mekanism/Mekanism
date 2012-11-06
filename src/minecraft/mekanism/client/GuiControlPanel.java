package mekanism.client;

import org.lwjgl.opengl.GL11;

import mekanism.api.IAccessibleGui;
import mekanism.common.EnumColor;
import mekanism.common.MekanismUtils;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiTextField;
import net.minecraft.src.World;

public class GuiControlPanel extends GuiScreen
{
	public EntityPlayer usingPlayer;
	public World worldObj;
	public String displayText = "";
	public int ticker = 0;
	
	private GuiTextField xField;
	private GuiTextField yField;
	private GuiTextField zField;
	
	public GuiControlPanel(EntityPlayer player, World world)
	{
		usingPlayer = player;
		worldObj = world;
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		controlList.clear();
		controlList.add(new GuiButton(0, width / 2 - 80, height / 4 + 72 + 12, 60, 20, "Access"));
		
		xField = new GuiTextField(fontRenderer, width / 2 - 80, 53, 35, 12);
		xField.setMaxStringLength(4);
		xField.setText("" + 0);
		xField.setFocused(true);
		
		yField = new GuiTextField(fontRenderer, width / 2 - 80, 70, 35, 12);
		yField.setMaxStringLength(4);
		yField.setText("" + 0);
		
		zField = new GuiTextField(fontRenderer, width / 2 - 80, 87, 35, 12);
		zField.setMaxStringLength(4);
		zField.setText("" + 0);
	}
	
	@Override
	public void drawScreen(int i, int j, float f)
	{
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int k = mc.renderEngine.getTexture("/gui/GuiControlPanel.png");
        mc.renderEngine.bindTexture(k);
        drawTexturedModalRect(width / 2 - 100, height / 2 - 100, 0, 0, 176, 166);
        xField.drawTextBox();
        yField.drawTextBox();
        zField.drawTextBox();
        super.drawScreen(i, j, f);
        fontRenderer.drawString("Control Panel", 165, 40, 0x404040);
        fontRenderer.drawString(displayText, 133, 120, 0x404040);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
	}
	
	@Override
	public void keyTyped(char c, int i)
	{
		super.keyTyped(c, i);
		
		xField.textboxKeyTyped(c, i);
		yField.textboxKeyTyped(c, i);
		zField.textboxKeyTyped(c, i);
	}
	
	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}
	
	@Override
	public void updateScreen()
	{
		xField.updateCursorCounter();
		yField.updateCursorCounter();
		zField.updateCursorCounter();
		
		if(ticker > 0)
		{
			ticker--;
		}
		else {
			displayText = "";
		}
	}
	
	@Override
	public void mouseClicked(int x, int y, int z)
	{
		super.mouseClicked(x, y, z);
		xField.mouseClicked(x, y, z);
		yField.mouseClicked(x, y, z);
		zField.mouseClicked(x, y, z);
	}
	
	@Override
	public void actionPerformed(GuiButton guibutton)
	{
		if(!guibutton.enabled)
		{
			return;
		}
		
		if(guibutton.id == 0)
		{
			try {
				if(worldObj.getBlockTileEntity(Integer.parseInt(xField.getText()), Integer.parseInt(yField.getText()), Integer.parseInt(zField.getText())) != null)
				{
					if(worldObj.getBlockTileEntity(Integer.parseInt(xField.getText()), Integer.parseInt(yField.getText()), Integer.parseInt(zField.getText())) instanceof IAccessibleGui)
					{
						IAccessibleGui gui = (IAccessibleGui)worldObj.getBlockTileEntity(Integer.parseInt(xField.getText()), Integer.parseInt(yField.getText()), Integer.parseInt(zField.getText()));
						usingPlayer.openGui(gui.getModInstance(), gui.getGuiID(), worldObj, Integer.parseInt(xField.getText()), Integer.parseInt(yField.getText()), Integer.parseInt(zField.getText()));
					}
					else {
						displayText = EnumColor.DARK_RED + "Tile entity isn't available.";
						ticker = 40;
					}
				}
				else {
					displayText = EnumColor.DARK_RED + "Tile entity doesn't exist.";
					ticker = 40;
				}
			} catch(NumberFormatException e) {
				displayText = EnumColor.DARK_RED + "Invalid characters.";
				ticker = 40;
			}
		}
	}
}

package net.uberkat.obsidian.client;

import org.lwjgl.opengl.GL11;

import obsidian.api.IAccessibleGui;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiTextField;
import net.minecraft.src.World;
import net.uberkat.obsidian.common.EnumColor;
import net.uberkat.obsidian.common.ObsidianUtils;

public class GuiControlPanel extends GuiScreen
{
	public EntityPlayer usingPlayer;
	public World worldObj;
	public String displayText = "Control Panel";
	public int ticker = 0;
	public boolean useTicker = false;
	
	private GuiTextField xField;
	private GuiTextField yField;
	private GuiTextField zField;
	
	public GuiControlPanel(EntityPlayer player, World world)
	{
		usingPlayer = player;
		worldObj = world;
	}
	
	public void initGui()
	{
		super.initGui();
		controlList.clear();
		controlList.add(new GuiButton(0, width / 2 - 80, height / 4 + 72 + 12, 60, 20, "Access"));
		
		xField = new GuiTextField(fontRenderer, 50, 30, 45, 12);
		xField.setMaxStringLength(4);
		xField.setText("" + 0);
		xField.setFocused(true);
		xField.setVisible(true);
		
		yField = new GuiTextField(fontRenderer, 50, 50, 45, 12);
		yField.setMaxStringLength(4);
		yField.setText("" + 0);
		yField.setVisible(true);
		
		zField = new GuiTextField(fontRenderer, 50, 80, 45, 12);
		zField.setMaxStringLength(4);
		zField.setText("" + 0);
		zField.setVisible(true);
	}
	
	public void drawScreen(int i, int j, float f)
	{
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int k = mc.renderEngine.getTexture("/gui/GuiControlPanel.png");
        mc.renderEngine.bindTexture(k);
        drawTexturedModalRect(width / 2 - 100, height / 2 - 100, 0, 0, 176, 166);
        super.drawScreen(i, j, f);
        fontRenderer.drawString(displayText, 50, 96, 0x404040);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
	}
	
	public void keyTyped(char c, int i)
	{
		super.keyTyped(c, i);
		
		if(Character.isDigit(c) || c == '-')
		{
			xField.textboxKeyTyped(c, i);
			yField.textboxKeyTyped(c, i);
			zField.textboxKeyTyped(c, i);
		}
		else {
			useTicker = true;
		}
	}
	
	public void updateScreen()
	{
		xField.updateCursorCounter();
		yField.updateCursorCounter();
		zField.updateCursorCounter();
		
		if(useTicker)
		{
			if(ticker < 40)
			{
				ticker++;
			}
			else {
				useTicker = false;
				ticker = 0;
				displayText = "Control Panel";
			}
		}
	}
	
	public void mouseClicked(int x, int y, int z)
	{
		super.mouseClicked(x, y, z);
		xField.mouseClicked(x, y, z);
		yField.mouseClicked(x, y, z);
		zField.mouseClicked(x, y, z);
	}
	
	public void actionPerformed(GuiButton guibutton)
	{
		if(!guibutton.enabled)
		{
			return;
		}
		
		if(guibutton.id == 0)
		{
			if(worldObj.getBlockTileEntity(Integer.parseInt(xField.getText()), Integer.parseInt(yField.getText()), Integer.parseInt(zField.getText())) != null)
			{
				if(worldObj.getBlockTileEntity(Integer.parseInt(xField.getText()), Integer.parseInt(yField.getText()), Integer.parseInt(zField.getText())) instanceof IAccessibleGui)
				{
					IAccessibleGui gui = (IAccessibleGui)worldObj.getBlockTileEntity(Integer.parseInt(xField.getText()), Integer.parseInt(yField.getText()), Integer.parseInt(zField.getText()));
					usingPlayer.openGui(gui.getModInstance(), gui.getGuiID(), worldObj, Integer.parseInt(xField.getText()), Integer.parseInt(yField.getText()), Integer.parseInt(zField.getText()));
				}
				else {
					displayText = EnumColor.DARK_RED + "Tile entity isn't available.";
					useTicker = true;
				}
			}
			else {
				displayText = EnumColor.DARK_RED + "Tile entity doesn't exist.";
				useTicker = true;
			}
		}
	}
}

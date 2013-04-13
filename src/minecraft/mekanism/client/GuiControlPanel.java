package mekanism.client;

import mekanism.api.EnumColor;
import mekanism.api.IAccessibleGui;
import mekanism.common.PacketHandler;
import mekanism.common.TileEntityControlPanel;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiControlPanel extends GuiScreen
{
	public TileEntityControlPanel tileEntity;
	public EntityPlayer usingPlayer;
	public World worldObj;
	public String displayText = "";
	public int ticker = 0;
	
	private GuiTextField xField;
	private GuiTextField yField;
	private GuiTextField zField;
	
	public GuiControlPanel(TileEntityControlPanel tentity, EntityPlayer player, World world)
	{
		tileEntity = tentity;
		usingPlayer = player;
		worldObj = world;
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		buttonList.clear();
		buttonList.add(new GuiButton(0, width / 2 - 80, height / 4 + 72 + 12, 60, 20, "Access"));
		
		xField = new GuiTextField(fontRenderer, width / 2 - 80, 53, 35, 12);
		xField.setMaxStringLength(4);
		xField.setText(Integer.toString(tileEntity.xCached));
		xField.setFocused(true);
		
		yField = new GuiTextField(fontRenderer, width / 2 - 80, 70, 35, 12);
		yField.setMaxStringLength(4);
		yField.setText(Integer.toString(tileEntity.yCached));
		
		zField = new GuiTextField(fontRenderer, width / 2 - 80, 87, 35, 12);
		zField.setMaxStringLength(4);
		zField.setText(Integer.toString(tileEntity.zCached));
	}
	
	@Override
	public void drawScreen(int i, int j, float f)
	{
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture("/mods/mekanism/gui/GuiControlPanel.png");
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
		
		try {
			tileEntity.xCached = Integer.parseInt(xField.getText());
		} catch(NumberFormatException e) { tileEntity.xCached = 0; }
		try {
			tileEntity.yCached = Integer.parseInt(yField.getText());
		} catch(NumberFormatException e) { tileEntity.yCached = 0; }
		try {
			tileEntity.zCached = Integer.parseInt(zField.getText());
		} catch(NumberFormatException e) { tileEntity.zCached = 0; }
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
						
						try {
				    		Class mod = Class.forName(gui.getClassPath());
				    		
				    		if(mod == null)
				    		{
				    			System.err.println("[Mekanism] Incorrectly implemented IAccessibleGui -- ignoring handler packet.");
				    			System.err.println(" ~ Unable to locate class '" + gui.getClassPath() + ".'");
				    			return;
				    		}
				    		
				    		Object instance = mod.getField(gui.getInstanceName()).get(null);
				    		
				    		if(instance == null)
				    		{
				    			System.err.println("[Mekanism] Incorrectly implemented IAccessibleGui -- ignoring handler packet.");
				    			System.err.println(" ~ Unable to locate instance object '" + gui.getInstanceName() + ".'");
				    			return;
				    		}
							
							PacketHandler.sendGuiRequest(gui.getClassPath(), gui.getInstanceName(), Integer.parseInt(xField.getText()), Integer.parseInt(yField.getText()), Integer.parseInt(zField.getText()), gui.getGuiID());
							usingPlayer.openGui(instance, gui.getGuiID(), worldObj, Integer.parseInt(xField.getText()), Integer.parseInt(yField.getText()), Integer.parseInt(zField.getText()));
						} catch(Exception e) {
							System.err.println("[Mekanism] Error while handling Control Panel GUI request.");
							e.printStackTrace();
						}
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

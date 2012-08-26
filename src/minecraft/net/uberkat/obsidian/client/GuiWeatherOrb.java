package net.uberkat.obsidian.client;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.*;
import net.uberkat.obsidian.common.ObsidianUtils;

public class GuiWeatherOrb extends GuiScreen {
	
	private static EntityPlayer player;
	private int xSize = 176;
	private int ySize = 166;

	public GuiWeatherOrb(EntityPlayer entityplayer)
	{
		player = entityplayer;
	}
	
	public void initGui()
	{
		controlList.clear();
        controlList.add(new GuiButton(1, width / 2 - 80, height / 2 - 65, 50, 20, "Clear"));
        controlList.add(new GuiButton(2, width / 2 - 80, height / 2 - 35, 50, 20, "Storm"));
        controlList.add(new GuiButton(3, width / 2 + 5, height / 2 - 65, 50, 20, "Haze"));
        controlList.add(new GuiButton(4, width / 2 + 5, height / 2 - 35, 50, 20, "Rain"));
        controlList.add(new GuiButton(5, width / 2 - 94, height / 2 + 30, 80, 20, "Credits"));
        controlList.add(new GuiButton(6, width / 2 - 10, height / 2 + 30, 80, 20, "Close"));
	}
	
	public void drawScreen(int i, int j, float f)
	{
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int k = mc.renderEngine.getTexture("/gui/GuiWeatherOrb.png");
        mc.renderEngine.bindTexture(k);
        drawTexturedModalRect(width / 2 - 100, height / 2 - 100, 0, 0, 176, 166);
        drawString(fontRenderer, "Weather Orb", width / 2 - 45, height / 2 - 95, 0xffffff);
        super.drawScreen(i, j, f);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
	}
	
    public void keyTyped(char c, int i)
    {
        if (i == 1)
        {
            mc.displayGuiScreen(null);
        }
    }
	
	public boolean doesGuiPauseGame()
	{
		return false;
	}
	
	public void actionPerformed(GuiButton guibutton)
	{
		if(guibutton.id == 1)
		{
			player.inventory.getCurrentItem().damageItem(4999, player);
			ObsidianUtils.doExplosion(player);
			ObsidianUtils.sendPacketDataInt(5);
			mc.displayGuiScreen(null);
		}
		if(guibutton.id == 2)
		{
			player.inventory.getCurrentItem().damageItem(4999, player);
			ObsidianUtils.doExplosion(player);
			ObsidianUtils.sendPacketDataInt(6);
			mc.displayGuiScreen(null);
		}
		if(guibutton.id == 3)
		{
			player.inventory.getCurrentItem().damageItem(4999, player);
			ObsidianUtils.doExplosion(player);
			ObsidianUtils.sendPacketDataInt(7);
			mc.displayGuiScreen(null);
		}
		if(guibutton.id == 4)
		{
			player.inventory.getCurrentItem().damageItem(4999, player);
			ObsidianUtils.doExplosion(player);
			ObsidianUtils.sendPacketDataInt(8);
			mc.displayGuiScreen(null);
		}
		if(guibutton.id == 5)
		{
			mc.displayGuiScreen(new GuiCredits());
		}
		if(guibutton.id == 6)
		{
			mc.displayGuiScreen(null);
		}
	}
	
	protected void mouseClicked(int i, int j, int k)
	{
		super.mouseClicked(i, j, k);
		int x = i - (width - xSize) / 2;
		int y = j - (height - ySize) / 2;
		
		if(x > 4 && x < 14 && y > 4 && y < 14)
		{
			mc.displayGuiScreen(null);
		}
	}
}

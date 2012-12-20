package mekanism.client;

import mekanism.common.EnumPacketType;
import mekanism.common.MekanismUtils;
import mekanism.common.PacketHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;

import org.lwjgl.opengl.GL11;

public class GuiStopwatch extends GuiScreen {
	
	private static EntityPlayer player;
	private int xSize = 176;
	private int ySize = 166;

	public GuiStopwatch(EntityPlayer entityplayer)
	{
		player = entityplayer;
	}
	
	@Override
	public void initGui()
	{
		controlList.clear();
        controlList.add(new GuiButton(0, width / 2 - 80, height / 2 - 65, 50, 20, "Sunrise"));
        controlList.add(new GuiButton(1, width / 2 - 80, height / 2 - 35, 50, 20, "Noon"));
        controlList.add(new GuiButton(2, width / 2 + 5, height / 2 - 65, 50, 20, "Sunset"));
        controlList.add(new GuiButton(3, width / 2 + 5, height / 2 - 35, 50, 20, "Midnight"));
        controlList.add(new GuiButton(4, width / 2 - 94, height / 2 + 30, 80, 20, "Credits"));
        controlList.add(new GuiButton(5, width / 2 - 10, height / 2 + 30, 80, 20, "Close"));
	}
	
	@Override
	public void drawScreen(int i, int j, float f)
	{
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int k = mc.renderEngine.getTexture("/resources/mekanism/gui/GuiStopwatch.png");
        mc.renderEngine.bindTexture(k);
        drawTexturedModalRect(width / 2 - 100, height / 2 - 100, 0, 0, 176, 166);
        drawString(fontRenderer, "Steve's Stopwatch", width / 2 - 60, height / 2 - 95, 0xffffff);
        super.drawScreen(i, j, f);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
	}
	
	@Override
    public void keyTyped(char c, int i)
    {
        if (i == 1)
        {
            mc.displayGuiScreen(null);
        }
    }
	
	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}
	
	@Override
	public void actionPerformed(GuiButton guibutton)
	{
		if(guibutton.id == 0)
		{
			player.inventory.getCurrentItem().damageItem(4999, player);
			MekanismUtils.doFakeEntityExplosion(player);
			PacketHandler.sendPacketDataInt(EnumPacketType.TIME, 0);
			mc.displayGuiScreen(null);
		}
		if(guibutton.id == 1)
		{
			player.inventory.getCurrentItem().damageItem(4999, player);
			MekanismUtils.doFakeEntityExplosion(player);
			PacketHandler.sendPacketDataInt(EnumPacketType.TIME, 6);
			mc.displayGuiScreen(null);
		}
		if(guibutton.id == 2)
		{
			player.inventory.getCurrentItem().damageItem(4999, player);
			MekanismUtils.doFakeEntityExplosion(player);
			PacketHandler.sendPacketDataInt(EnumPacketType.TIME, 12);
			mc.displayGuiScreen(null);
		}
		if(guibutton.id == 3)
		{
			player.inventory.getCurrentItem().damageItem(4999, player);
			MekanismUtils.doFakeEntityExplosion(player);
			PacketHandler.sendPacketDataInt(EnumPacketType.TIME, 18);
			mc.displayGuiScreen(null);
		}
		if(guibutton.id == 4)
		{
			mc.displayGuiScreen(new GuiCredits());
		}
		if(guibutton.id == 5)
		{
			mc.displayGuiScreen(null);
		}
	}
}

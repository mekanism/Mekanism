package mekanism.client;

import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.MekanismUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiCredits extends GuiScreen 
{
	private static String updateProgress = "";
	
	@Override
	public void initGui()
	{
		buttonList.clear();
		buttonList.add(new GuiButton(0, width / 2 - 100, height / 4 + 72 + 12, "Update"));
		buttonList.add(new GuiButton(1, width / 2 - 100, height / 4 + 96 + 12, "Cancel"));
        ((GuiButton)buttonList.get(0)).enabled = !MekanismUtils.isNotOutdated();
	}
	
	@Override
	public void onGuiClosed()
	{
        updateProgress = "";
	}
	
	public static void onFinishedDownloading()
	{
		updateProgress = "Successfully updated. Restart Minecraft to load.";
	}
	
	public static void onErrorDownloading()
	{
		updateProgress = EnumColor.DARK_RED + "Error updating.";
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton)
	{
		if(!guibutton.enabled)
		{
			return;
		}
		if(guibutton.id == 0)
		{
			if(!MekanismUtils.isNotOutdated())
			{
				updateProgress = "Downloading latest version...";
				guibutton.enabled = false;
				new ThreadClientUpdate("http://dl.dropbox.com/u/90411166/Mekanism-v" + Mekanism.latestVersionNumber + ".jar", 0);
				new ThreadClientUpdate("http://dl.dropbox.com/u/90411166/MekanismGenerators-v" + Mekanism.latestVersionNumber + ".jar", 1);
				new ThreadClientUpdate("http://dl.dropbox.com/u/90411166/MekanismTools-v" + Mekanism.latestVersionNumber + ".jar", 2);
			}
			else {
				updateProgress = "You already have the latest version.";
			}
		}
		if(guibutton.id == 1)
		{
			mc.displayGuiScreen(null);
		}
	}
	
	public void writeText(String text, int yAxis)
	{
		drawString(fontRenderer, text, width / 2 - 140, (height / 4 - 60) + 20 + yAxis, 0xa0a0a0);
	}
	
	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}
	
	@Override
	public void drawScreen(int i, int j, float f)
	{
		drawDefaultBackground();
        drawCenteredString(fontRenderer, EnumColor.DARK_BLUE + "Mekanism" + EnumColor.GREY + " by aidancbrady", width / 2, (height / 4 - 60) + 20, 0xffffff);
        writeText(EnumColor.GREY + "Your version: " + (MekanismUtils.isNotOutdated() ? Mekanism.versionNumber : EnumColor.DARK_RED + Mekanism.versionNumber.toString() + EnumColor.GREY + " -- OUTDATED"), 36);
  		writeText(EnumColor.GREY + "Newest version: " + Mekanism.latestVersionNumber, 45);
  		writeText(EnumColor.GREY + "*Developed on Mac OS X 10.8 Mountain Lion", 63);
  		writeText(EnumColor.GREY + "*Code, textures, and ideas by aidancbrady", 72);
  		writeText(EnumColor.GREY + "Recent news: " + EnumColor.DARK_BLUE + (!Mekanism.recentNews.contains("null") ? Mekanism.recentNews : "couldn't access."), 81);
  		writeText(EnumColor.GREY + updateProgress, 99);
  		super.drawScreen(i, j, f);
	}
}

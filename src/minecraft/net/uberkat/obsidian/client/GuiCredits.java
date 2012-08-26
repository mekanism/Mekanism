package net.uberkat.obsidian.client;

import java.io.IOException;
import java.net.URL;

import org.lwjgl.Sys;

import net.minecraft.src.*;
import net.uberkat.obsidian.common.ObsidianIngots;
import net.uberkat.obsidian.common.ObsidianUtils;

public class GuiCredits extends GuiScreen {
	
	private static String updateProgress = "";
	
	public void initGui()
	{
		controlList.clear();
		controlList.add(new GuiButton(2, width / 2 - 100, height / 4 + 72 + 12, "Update"));
        controlList.add(new GuiButton(1, width / 2 - 100, height / 4 + 96 + 12, "Cancel"));
	}
	
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
		updateProgress = "¤4Error updating.";
	}
	
	protected void actionPerformed(GuiButton guibutton)
	{
		if(!guibutton.enabled)
		{
			return;
		}
		if(guibutton.id == 2)
		{
			if(!ObsidianUtils.isClientLatestVersion())
			{
				updateProgress = "Downloading latest version...";
				guibutton.enabled = false;
				new ThreadUpdate("http://dl.dropbox.com/u/90411166/ObsidianIngots.jar");
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
	
	public void drawScreen(int i, int j, float f)
	{
		drawDefaultBackground();
        drawCenteredString(fontRenderer, "¤1Obsidian Ingots ¤7by aidancbrady", width / 2, (height / 4 - 60) + 20, 0xffffff);
        writeText("¤7Your version: " + (ObsidianUtils.isClientLatestVersion() ? ObsidianIngots.versionNumber.toString() : "¤4" + ObsidianIngots.versionNumber.toString()) + "¤7 -- OUTDATED", 36);
  		writeText("¤7Newest version: " + ObsidianIngots.latestVersionNumber, 45);
  		writeText("¤7*Developed on Mac OS X 10.8 Mountain Lion", 63);
  		writeText("¤7*Code, textures, and ideas by aidancbrady", 72);
  		writeText("¤7Recent news: ¤1" + ObsidianIngots.recentNews, 81);
  		writeText("¤7" + updateProgress, 99);
  		super.drawScreen(i, j, f);
	}
}

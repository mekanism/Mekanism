package mekanism.client;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mekanism.api.EnumColor;
import mekanism.common.IModule;
import mekanism.common.Mekanism;
import mekanism.common.MekanismUtils;
import mekanism.common.Version;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

@SideOnly(Side.CLIENT)
public class GuiCredits extends GuiScreen 
{
	private static String updateProgress = "";
	private boolean updatedRecently;
	private boolean notified = false;
	
	@Override
	public void initGui()
	{
		buttonList.clear();
		buttonList.add(new GuiButton(0, width / 2 - 100, height / 4 + 72 + 12, "Update"));
		buttonList.add(new GuiButton(1, width / 2 - 100, height / 4 + 96 + 12, "Cancel"));
        ((GuiButton)buttonList.get(0)).enabled = !MekanismUtils.noUpdates() && !ThreadClientUpdate.hasUpdated;
	}
	
	public static void onFinishedDownloading()
	{
		updateProgress = "Successfully updated. Restart Minecraft to load.";
		System.out.println("[Mekanism] Successfully updated to latest version (" + Mekanism.latestVersionNumber + ").");
		ThreadClientUpdate.hasUpdated = true;
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
			if(!MekanismUtils.noUpdates())
			{
				updatedRecently = true;
				updateProgress = "Downloading latest version...";
				guibutton.enabled = false;
				
				if(Mekanism.versionNumber.comparedState(Version.get(Mekanism.latestVersionNumber)) == -1)
				{
					new ThreadClientUpdate("http://dl.dropbox.com/u/90411166/Mekanism-v" + Mekanism.latestVersionNumber + ".jar", "");
				}
				
				for(IModule module : Mekanism.modulesLoaded)
				{
					if(module.getVersion().comparedState(Version.get(Mekanism.latestVersionNumber)) == -1)
					{
						new ThreadClientUpdate("http://dl.dropbox.com/u/90411166/Mekanism" + module.getName() + "-v" + Mekanism.latestVersionNumber + ".jar", module.getName());
					}
				}
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
		if(updatedRecently && ThreadClientUpdate.modulesBeingDownloaded == 0 && !updateProgress.contains("Error"))
		{
			if(!notified)
			{
				onFinishedDownloading();
				notified = true;
			}
		}
		else if(ThreadClientUpdate.hasUpdated && !notified)
		{
			updateProgress = "You have already downloaded the update. Restart MC!";
		}
		
		drawDefaultBackground();
        drawCenteredString(fontRenderer, EnumColor.DARK_BLUE + "Mekanism" + EnumColor.GREY + " by aidancbrady", width / 2, (height / 4 - 60) + 20, 0xffffff);
        writeText(EnumColor.INDIGO + "Mekanism " + (Mekanism.versionNumber.comparedState(Version.get(Mekanism.latestVersionNumber)) == -1 ? EnumColor.DARK_RED : EnumColor.GREY) + Mekanism.versionNumber, 36);
        
        int size = 36;
        
        for(IModule module : Mekanism.modulesLoaded)
        {
    		size += 9;
    		writeText(EnumColor.INDIGO + "Mekanism" + module.getName() + (module.getVersion().comparedState(Version.get(Mekanism.latestVersionNumber)) == -1 ? EnumColor.DARK_RED : EnumColor.GREY) + " " + module.getVersion(), size);
        }
        
  		writeText(EnumColor.GREY + "Newest version: " + Mekanism.latestVersionNumber, size+9);
  		writeText(EnumColor.GREY + "*Developed on Mac OS X 10.8 Mountain Lion", size+18);
  		writeText(EnumColor.GREY + "*Code, textures, and ideas by aidancbrady", size+27);
  		writeText(EnumColor.GREY + "Recent news: " + EnumColor.DARK_BLUE + (!Mekanism.recentNews.contains("null") ? Mekanism.recentNews : "couldn't access."), size+36);
  		writeText(EnumColor.GREY + updateProgress, size+45);
  		super.drawScreen(i, j, f);
	}
}

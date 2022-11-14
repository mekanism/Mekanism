package mekanism.client.gui;

import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.Version;
import mekanism.common.base.IModule;
import net.minecraft.client.gui.GuiScreen;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiCredits extends GuiScreen
{
	public void writeText(String text, int yAxis)
	{
		drawString(fontRendererObj, text, width / 2 - 140, (height / 4 - 60) + 20 + yAxis, 0xa0a0a0);
	}

	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTick)
	{
		drawDefaultBackground();
		drawCenteredString(fontRendererObj, EnumColor.DARK_BLUE + "Mekanism" + EnumColor.GREY + " by aidancbrady", width / 2, (height / 4 - 60) + 20, 0xffffff);

		if(Mekanism.latestVersionNumber != null && !Mekanism.latestVersionNumber.equals("null"))
		{
			writeText(EnumColor.INDIGO + "Mekanism " + (Mekanism.versionNumber.comparedState(Version.get(Mekanism.latestVersionNumber)) == -1 ? EnumColor.DARK_RED : EnumColor.GREY) + Mekanism.versionNumber, 36);
		}
		else {
			writeText(EnumColor.INDIGO + "Mekanism " + EnumColor.GREY + Mekanism.versionNumber, 36);
		}

		int size = 36;

		for(IModule module : Mekanism.modulesLoaded)
		{
			size += 9;

			if(Mekanism.latestVersionNumber != null && !Mekanism.latestVersionNumber.equals("null"))
			{
				writeText(EnumColor.INDIGO + "Mekanism" + module.getName() + (module.getVersion().comparedState(Version.get(Mekanism.latestVersionNumber)) == -1 ? EnumColor.DARK_RED : EnumColor.GREY) + " " + module.getVersion(), size);
			}
			else {
				writeText(EnumColor.INDIGO + "Mekanism" + module.getName() + EnumColor.GREY + " " + module.getVersion(), size);
			}
		}

		writeText(EnumColor.GREY + "Newest version: " + Mekanism.latestVersionNumber, size+9);
		writeText(EnumColor.GREY + "Recent news: " + EnumColor.DARK_BLUE + (!Mekanism.recentNews.contains("null") ? Mekanism.recentNews : "couldn't access."), size+18);

		super.drawScreen(mouseX, mouseY, partialTick);
	}
}

package mekanism.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.Version;
import mekanism.common.base.IModule;
import net.minecraft.client.gui.GuiScreen;

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

		writeText(EnumColor.INDIGO + "Mekanism " + EnumColor.GREY + Mekanism.versionNumber, 36);

		int size = 36;

		for(IModule module : Mekanism.modulesLoaded)
		{
			size += 9;
			writeText(EnumColor.INDIGO + "Mekanism" + module.getName() + EnumColor.GREY + " " + module.getVersion(), size);

		}

		super.drawScreen(mouseX, mouseY, partialTick);
	}
}

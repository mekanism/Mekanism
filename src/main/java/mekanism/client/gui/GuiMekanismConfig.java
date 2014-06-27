package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;

import mekanism.common.Mekanism;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.client.config.DummyConfigElement.DummyCategoryElement;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.GuiConfigEntries;
import cpw.mods.fml.client.config.GuiConfigEntries.CategoryEntry;
import cpw.mods.fml.client.config.IConfigElement;

public class GuiMekanismConfig extends GuiConfig
{
	public GuiMekanismConfig(GuiScreen parent)
	{
		super(parent, getConfigElements(),
				"Mekanism", false, false, GuiConfig.getAbridgedConfigPath(Mekanism.configuration.toString()));
	}

	private static List<IConfigElement> getConfigElements()
	{
		List<IConfigElement> list = new ArrayList<IConfigElement>();
		list.add(new DummyCategoryElement("Mekanism General Settings", "mekanism.configgui.ctgy.general", GeneralEntry.class));
		list.add(new DummyCategoryElement("Usage Settings", "mekanism.configgui.ctgy.usage", UsageEntry.class));
		return list;
	}

	public static class GeneralEntry extends CategoryEntry
	{
		public GeneralEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop)
		{
			super(owningScreen, owningEntryList, prop);
		}

		@Override
		protected GuiScreen buildChildScreen()
		{
			return new GuiConfig(this.owningScreen,
					new ConfigElement(Mekanism.configuration.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(),
					this.owningScreen.modID, Configuration.CATEGORY_GENERAL, false, false,
					GuiConfig.getAbridgedConfigPath(Mekanism.configuration.toString()));
		}
	}

	public static class UsageEntry extends CategoryEntry
	{
		public UsageEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop)
		{
			super(owningScreen, owningEntryList, prop);
		}

		@Override
		protected GuiScreen buildChildScreen()
		{
			return new GuiConfig(this.owningScreen,
					new ConfigElement(Mekanism.configuration.getCategory("usage")).getChildElements(),
					this.owningScreen.modID, Configuration.CATEGORY_GENERAL, false, false,
					GuiConfig.getAbridgedConfigPath(Mekanism.configuration.toString()));
		}
	}
}

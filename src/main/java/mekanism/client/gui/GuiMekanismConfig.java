package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;

import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.DummyConfigElement.DummyCategoryElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.GuiConfigEntries.CategoryEntry;
import net.minecraftforge.fml.client.config.IConfigElement;

public class GuiMekanismConfig extends GuiConfig
{
	public GuiMekanismConfig(GuiScreen parent)
	{
		super(parent, getConfigElements(),
				"Mekanism", false, false, "Mekanism");
	}

	private static List<IConfigElement> getConfigElements()
	{
		List<IConfigElement> list = new ArrayList<IConfigElement>();
		list.add(new DummyCategoryElement(MekanismUtils.localize("mekanism.configgui.ctgy.general"), "mekanism.configgui.ctgy.general", GeneralEntry.class));
		list.add(new DummyCategoryElement(MekanismUtils.localize("mekanism.configgui.ctgy.usage"), "mekanism.configgui.ctgy.usage", UsageEntry.class));
		list.add(new DummyCategoryElement(MekanismUtils.localize("mekanism.configgui.ctgy.client"), "mekanism.configgui.ctgy.client", ClientEntry.class));
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
			return new GuiConfig(owningScreen,
					new ConfigElement(Mekanism.configuration.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(),
					owningScreen.modID, Configuration.CATEGORY_GENERAL, false, false,
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
			return new GuiConfig(owningScreen,
					new ConfigElement(Mekanism.configuration.getCategory("usage")).getChildElements(),
					owningScreen.modID, Configuration.CATEGORY_GENERAL, false, false,
					GuiConfig.getAbridgedConfigPath(Mekanism.configuration.toString()));
		}
	}

	public static class ClientEntry extends CategoryEntry
	{
		public ClientEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop)
		{
			super(owningScreen, owningEntryList, prop);
		}

		@Override
		protected GuiScreen buildChildScreen()
		{
			return new GuiConfig(owningScreen,
					new ConfigElement(Mekanism.configuration.getCategory("client")).getChildElements(),
					owningScreen.modID, "client", false, false,
					GuiConfig.getAbridgedConfigPath(Mekanism.configuration.toString()));
		}
	}
}

package mekanism.tools.client.gui;

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

/**
 * Created by ben on 27/06/14.
 */
public class GuiToolsConfig extends GuiConfig
{
	public GuiToolsConfig(GuiScreen parent)
	{
		super(parent, getConfigElements(),
				"Mekanism", false, false, GuiConfig.getAbridgedConfigPath(Mekanism.configuration.toString()));
	}

	private static List<IConfigElement> getConfigElements()
	{
		List<IConfigElement> list = new ArrayList<IConfigElement>();
		list.add(new DummyCategoryElement("Armor Balance", "mekanism.configgui.ctgy.armor", ArmorEntry.class));
		list.add(new DummyCategoryElement("Tools Balance", "mekanism.configgui.ctgy.tools", ToolsEntry.class));
		return list;
	}

	public static class ArmorEntry extends CategoryEntry
	{
		public ArmorEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop)
		{
			super(owningScreen, owningEntryList, prop);
		}

		@Override
		protected GuiScreen buildChildScreen()
		{
			return new GuiConfig(this.owningScreen,
					new ConfigElement(Mekanism.configuration.getCategory("armor-balance")).getChildElements(),
					this.owningScreen.modID, Configuration.CATEGORY_GENERAL, this.configElement.requiresWorldRestart() || this.owningScreen.allRequireWorldRestart,
					this.configElement.requiresMcRestart() || this.owningScreen.allRequireMcRestart,
					GuiConfig.getAbridgedConfigPath(Mekanism.configuration.toString()));
		}
	}

	public static class ToolsEntry extends CategoryEntry
	{
		public ToolsEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop)
		{
			super(owningScreen, owningEntryList, prop);
		}

		@Override
		protected GuiScreen buildChildScreen()
		{
			return new GuiConfig(this.owningScreen,
					new ConfigElement(Mekanism.configuration.getCategory("tool-balance")).getChildElements(),
					this.owningScreen.modID, Configuration.CATEGORY_GENERAL, this.configElement.requiresWorldRestart() || this.owningScreen.allRequireWorldRestart,
					this.configElement.requiresMcRestart() || this.owningScreen.allRequireMcRestart,
					GuiConfig.getAbridgedConfigPath(Mekanism.configuration.toString()));
		}
	}
}

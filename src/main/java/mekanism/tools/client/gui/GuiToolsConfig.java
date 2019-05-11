package mekanism.tools.client.gui;

import java.util.ArrayList;
import java.util.List;
import mekanism.common.Mekanism;
import mekanism.common.util.LangUtils;
import mekanism.tools.common.MekanismTools;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.DummyConfigElement.DummyCategoryElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.GuiConfigEntries.CategoryEntry;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by ben on 27/06/14.
 */
@SideOnly(Side.CLIENT)
public class GuiToolsConfig extends GuiConfig {

    public GuiToolsConfig(GuiScreen parent) {
        super(parent, getConfigElements(), MekanismTools.MODID, false, false, "MekanismTools");
    }

    private static List<IConfigElement> getConfigElements() {
        List<IConfigElement> list = new ArrayList<>();
        list.add(new DummyCategoryElement(LangUtils.localize("mekanism.configgui.ctgy.tools.general"), "mekanism.configgui.ctgy.tools.general", GeneralEntry.class));
        list.add(new DummyCategoryElement(LangUtils.localize("mekanism.configgui.ctgy.tools.armor"), "mekanism.configgui.ctgy.tools.armor", ArmorEntry.class));
        list.add(new DummyCategoryElement(LangUtils.localize("mekanism.configgui.ctgy.tools.tools"), "mekanism.configgui.ctgy.tools.tools", ToolsEntry.class));
        return list;
    }

    public static class GeneralEntry extends CategoryEntry {

        public GeneralEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop) {
            super(owningScreen, owningEntryList, prop);
        }

        @Override
        protected GuiScreen buildChildScreen() {
            return new GuiConfig(owningScreen, new ConfigElement(Mekanism.configuration.getCategory("tools.general")).getChildElements(), owningScreen.modID,
                  Configuration.CATEGORY_GENERAL, configElement.requiresWorldRestart() || owningScreen.allRequireWorldRestart,
                  configElement.requiresMcRestart() || owningScreen.allRequireMcRestart, GuiConfig.getAbridgedConfigPath(Mekanism.configuration.toString()));
        }
    }

    public static class ArmorEntry extends CategoryEntry {

        public ArmorEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop) {
            super(owningScreen, owningEntryList, prop);
        }

        @Override
        protected GuiScreen buildChildScreen() {
            return new GuiConfig(owningScreen, new ConfigElement(Mekanism.configuration.getCategory("tools.armor-balance")).getChildElements(), owningScreen.modID,
                  Configuration.CATEGORY_GENERAL, configElement.requiresWorldRestart() || owningScreen.allRequireWorldRestart,
                  configElement.requiresMcRestart() || owningScreen.allRequireMcRestart, GuiConfig.getAbridgedConfigPath(Mekanism.configuration.toString()));
        }
    }

    public static class ToolsEntry extends CategoryEntry {

        public ToolsEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop) {
            super(owningScreen, owningEntryList, prop);
        }

        @Override
        protected GuiScreen buildChildScreen() {
            return new GuiConfig(owningScreen, new ConfigElement(Mekanism.configuration.getCategory("tools.tool-balance")).getChildElements(), owningScreen.modID,
                  Configuration.CATEGORY_GENERAL, configElement.requiresWorldRestart() || owningScreen.allRequireWorldRestart,
                  configElement.requiresMcRestart() || owningScreen.allRequireMcRestart, GuiConfig.getAbridgedConfigPath(Mekanism.configuration.toString()));
        }
    }
}
package mekanism.tools.client.gui;

import java.util.ArrayList;
import java.util.List;
import mekanism.common.Mekanism;
import mekanism.tools.common.MekanismTools;
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.DummyConfigElement.DummyCategoryElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.client.config.IConfigEntry;

/**
 * Created by ben on 27/06/14.
 */
@OnlyIn(Dist.CLIENT)
public class GuiToolsConfig extends GuiConfig {

    public GuiToolsConfig(Screen parent) {
        super(parent, getConfigElements(), MekanismTools.MODID, false, false, "MekanismTools");
    }

    private static List<IConfigElement> getConfigElements() {
        List<IConfigElement> list = new ArrayList<>();
        list.add(new DummyCategoryElement("General Settings", "mekanism.configgui.ctgy.tools.general", GeneralEntry.class));
        list.add(new DummyCategoryElement("Armor Balance", "mekanism.configgui.ctgy.tools.armor", ArmorEntry.class));
        list.add(new DummyCategoryElement("Tools Balance", "mekanism.configgui.ctgy.tools.tools", ToolsEntry.class));
        return list;
    }

    public static class GeneralEntry implements IConfigEntry {

        public GeneralEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop) {
            super(owningScreen, owningEntryList, prop);
        }

        @Override
        protected Screen buildChildScreen() {
            return new GuiConfig(owningScreen, new ConfigElement(Mekanism.configuration.getCategory("tools.general")).getChildElements(), owningScreen.modID,
                  Configuration.CATEGORY_GENERAL, configElement.requiresWorldRestart() || owningScreen.allRequireWorldRestart,
                  configElement.requiresMcRestart() || owningScreen.allRequireMcRestart, GuiConfig.getAbridgedConfigPath(Mekanism.configuration.toString()));
        }
    }

    public static class ArmorEntry implements IConfigEntry {

        public ArmorEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop) {
            super(owningScreen, owningEntryList, prop);
        }

        @Override
        protected Screen buildChildScreen() {
            return new GuiConfig(owningScreen, new ConfigElement(Mekanism.configuration.getCategory("tools.armor-balance")).getChildElements(), owningScreen.modID,
                  Configuration.CATEGORY_GENERAL, configElement.requiresWorldRestart() || owningScreen.allRequireWorldRestart,
                  configElement.requiresMcRestart() || owningScreen.allRequireMcRestart, GuiConfig.getAbridgedConfigPath(Mekanism.configuration.toString()));
        }
    }

    public static class ToolsEntry implements IConfigEntry {

        public ToolsEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop) {
            super(owningScreen, owningEntryList, prop);
        }

        @Override
        protected Screen buildChildScreen() {
            return new GuiConfig(owningScreen, new ConfigElement(Mekanism.configuration.getCategory("tools.tool-balance")).getChildElements(), owningScreen.modID,
                  Configuration.CATEGORY_GENERAL, configElement.requiresWorldRestart() || owningScreen.allRequireWorldRestart,
                  configElement.requiresMcRestart() || owningScreen.allRequireMcRestart, GuiConfig.getAbridgedConfigPath(Mekanism.configuration.toString()));
        }
    }
}
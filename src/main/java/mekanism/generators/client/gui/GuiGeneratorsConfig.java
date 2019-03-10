package mekanism.generators.client.gui;

import java.util.ArrayList;
import java.util.List;
import mekanism.common.Mekanism;
import mekanism.common.util.LangUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.DummyConfigElement.DummyCategoryElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.GuiConfigEntries.CategoryEntry;
import net.minecraftforge.fml.client.config.IConfigElement;

/**
 * Created by ben on 27/06/14.
 */
public class GuiGeneratorsConfig extends GuiConfig {

    public GuiGeneratorsConfig(GuiScreen parent) {
        super(parent, getConfigElements(), "MekanismGenerators", false, false, "MekanismGenerators");
    }

    private static List<IConfigElement> getConfigElements() {
        List<IConfigElement> list = new ArrayList<>();
        list.add(new DummyCategoryElement(LangUtils.localize("mekanism.configgui.ctgy.generators.general"),
              "mekanism.configgui.ctgy.generators.general", GeneralEntry.class));
        list.add(new DummyCategoryElement(LangUtils.localize("mekanism.configgui.ctgy.generators.generators"),
              "mekanism.configgui.ctgy.generators.generators", GeneratorsEntry.class));
        list.add(new DummyCategoryElement(LangUtils.localize("mekanism.configgui.ctgy.generators.generation"),
              "mekanism.configgui.ctgy.generators.generation", GenerationEntry.class));
        return list;
    }

    public static class GeneralEntry extends CategoryEntry {

        public GeneralEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop) {
            super(owningScreen, owningEntryList, prop);
        }

        @Override
        protected GuiScreen buildChildScreen() {
            return new GuiConfig(owningScreen,
                  new ConfigElement(Mekanism.configuration.getCategory(Configuration.CATEGORY_GENERAL))
                        .getChildElements(),
                  owningScreen.modID, Configuration.CATEGORY_GENERAL, false, false,
                  GuiConfig.getAbridgedConfigPath(Mekanism.configuration.toString()));
        }
    }

    public static class GeneratorsEntry extends CategoryEntry {

        public GeneratorsEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop) {
            super(owningScreen, owningEntryList, prop);
        }

        @Override
        protected GuiScreen buildChildScreen() {
            return new GuiConfig(owningScreen,
                  new ConfigElement(Mekanism.configuration.getCategory("generators")).getChildElements(),
                  owningScreen.modID, Configuration.CATEGORY_GENERAL, false, false,
                  GuiConfig.getAbridgedConfigPath(Mekanism.configuration.toString()));
        }
    }

    public static class GenerationEntry extends CategoryEntry {

        public GenerationEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop) {
            super(owningScreen, owningEntryList, prop);
        }

        @Override
        protected GuiScreen buildChildScreen() {
            return new GuiConfig(owningScreen,
                  new ConfigElement(Mekanism.configuration.getCategory("generation")).getChildElements(),
                  owningScreen.modID, Configuration.CATEGORY_GENERAL, false, false,
                  GuiConfig.getAbridgedConfigPath(Mekanism.configuration.toString()));
        }
    }
}

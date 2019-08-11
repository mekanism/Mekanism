package mekanism.generators.client.gui;

import java.util.ArrayList;
import java.util.List;
import mekanism.common.Mekanism;
import mekanism.generators.common.MekanismGenerators;
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
public class GuiGeneratorsConfig extends GuiConfig {

    public GuiGeneratorsConfig(Screen parent) {
        super(parent, getConfigElements(), MekanismGenerators.MODID, false, false, "MekanismGenerators");
    }

    private static List<IConfigElement> getConfigElements() {
        List<IConfigElement> list = new ArrayList<>();
        list.add(new DummyCategoryElement("General Settings", "mekanism.configgui.ctgy.generators.general", GeneralEntry.class));
        list.add(new DummyCategoryElement("Generator Settings", "mekanism.configgui.ctgy.generators.generators", GeneratorsEntry.class));
        list.add(new DummyCategoryElement("Generation Settings", "mekanism.configgui.ctgy.generators.generation", GenerationEntry.class));
        return list;
    }

    public static class GeneralEntry implements IConfigEntry {

        public GeneralEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop) {
            super(owningScreen, owningEntryList, prop);
        }

        @Override
        protected Screen buildChildScreen() {
            return new GuiConfig(owningScreen, new ConfigElement(Mekanism.configuration.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(), owningScreen.modID,
                  Configuration.CATEGORY_GENERAL, false, false, GuiConfig.getAbridgedConfigPath(Mekanism.configuration.toString()));
        }
    }

    public static class GeneratorsEntry implements IConfigEntry {

        public GeneratorsEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop) {
            super(owningScreen, owningEntryList, prop);
        }

        @Override
        protected Screen buildChildScreen() {
            return new GuiConfig(owningScreen, new ConfigElement(Mekanism.configuration.getCategory("generators")).getChildElements(), owningScreen.modID,
                  Configuration.CATEGORY_GENERAL, false, false, GuiConfig.getAbridgedConfigPath(Mekanism.configuration.toString()));
        }
    }

    public static class GenerationEntry implements IConfigEntry {

        public GenerationEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop) {
            super(owningScreen, owningEntryList, prop);
        }

        @Override
        protected Screen buildChildScreen() {
            return new GuiConfig(owningScreen, new ConfigElement(Mekanism.configuration.getCategory("generation")).getChildElements(), owningScreen.modID,
                  Configuration.CATEGORY_GENERAL, false, false, GuiConfig.getAbridgedConfigPath(Mekanism.configuration.toString()));
        }
    }
}
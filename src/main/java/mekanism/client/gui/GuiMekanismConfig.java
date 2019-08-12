package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;
import mekanism.common.Mekanism;
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

@OnlyIn(Dist.CLIENT)
public class GuiMekanismConfig extends GuiConfig {

    public GuiMekanismConfig(Screen parent) {
        super(parent, getConfigElements(), Mekanism.MODID, false, false, "Mekanism");
    }

    private static List<IConfigElement> getConfigElements() {
        List<IConfigElement> list = new ArrayList<>();
        list.add(new DummyCategoryElement("General Settings", "mekanism.configgui.ctgy.general", GeneralEntry.class));
        list.add(new DummyCategoryElement("Machine Settings", "mekanism.configgui.ctgy.machines", MachinesEntry.class));
        list.add(new DummyCategoryElement("Tier Settings", "mekanism.configgui.ctgy.tier", TierEntry.class));
        list.add(new DummyCategoryElement("Usage Settings", "mekanism.configgui.ctgy.usage", UsageEntry.class));
        list.add(new DummyCategoryElement("Client Settings", "mekanism.configgui.ctgy.storage", StorageEntry.class));
        list.add(new DummyCategoryElement("Energy Storage Settings", "mekanism.configgui.ctgy.client", ClientEntry.class));
        return list;
    }

    public static class GeneralEntry extends IConfigEntry {

        public GeneralEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop) {
            super(owningScreen, owningEntryList, prop);
        }

        @Override
        protected Screen buildChildScreen() {
            return new GuiConfig(owningScreen, new ConfigElement(Mekanism.configuration.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(), owningScreen.modID,
                  Configuration.CATEGORY_GENERAL, false, false, GuiConfig.getAbridgedConfigPath(Mekanism.configuration.toString()));
        }
    }

    public static class MachinesEntry implements IConfigEntry {

        public MachinesEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop) {
            super(owningScreen, owningEntryList, prop);
        }

        @Override
        protected Screen buildChildScreen() {
            return new GuiConfig(owningScreen, new ConfigElement(Mekanism.configuration.getCategory("machines")).getChildElements(), owningScreen.modID,
                  Configuration.CATEGORY_GENERAL, false, false, GuiConfig.getAbridgedConfigPath(Mekanism.configuration.toString()));
        }
    }

    public static class TierEntry implements IConfigEntry {

        public TierEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop) {
            super(owningScreen, owningEntryList, prop);
        }

        @Override
        protected Screen buildChildScreen() {
            return new GuiConfig(owningScreen, new ConfigElement(Mekanism.configuration.getCategory("tier")).getChildElements(), owningScreen.modID,
                  Configuration.CATEGORY_GENERAL, false, false, GuiConfig.getAbridgedConfigPath(Mekanism.configuration.toString()));
        }
    }

    public static class UsageEntry implements IConfigEntry {

        public UsageEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop) {
            super(owningScreen, owningEntryList, prop);
        }

        @Override
        protected Screen buildChildScreen() {
            return new GuiConfig(owningScreen, new ConfigElement(Mekanism.configuration.getCategory("usage")).getChildElements(), owningScreen.modID,
                  Configuration.CATEGORY_GENERAL, false, false, GuiConfig.getAbridgedConfigPath(Mekanism.configuration.toString()));
        }
    }

    public static class StorageEntry implements IConfigEntry {

        public StorageEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop) {
            super(owningScreen, owningEntryList, prop);
        }

        @Override
        protected Screen buildChildScreen() {
            return new GuiConfig(owningScreen, new ConfigElement(Mekanism.configuration.getCategory("storage")).getChildElements(), owningScreen.modID,
                  Configuration.CATEGORY_GENERAL, false, false, GuiConfig.getAbridgedConfigPath(Mekanism.configuration.toString()));
        }
    }

    public static class ClientEntry implements IConfigEntry {

        public ClientEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop) {
            super(owningScreen, owningEntryList, prop);
        }

        @Override
        protected Screen buildChildScreen() {
            return new GuiConfig(owningScreen, new ConfigElement(Mekanism.configuration.getCategory("client")).getChildElements(), owningScreen.modID,
                  Configuration.CATEGORY_CLIENT, false, false, GuiConfig.getAbridgedConfigPath(Mekanism.configuration.toString()));
        }
    }
}
package mekanism.generators.client;

import mekanism.client.MekanismModDisplayInfo;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.MekanismGenerators;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.modlist.ModDisplayInfo;

@Mod(value = MekanismGenerators.MODID, dist = Dist.CLIENT)
public class GeneratorsClient {

    public GeneratorsClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        container.<ModDisplayInfo>registerExtensionPoint(ModDisplayInfo.class, () -> new MekanismModDisplayInfo(container, GeneratorsLang.MEKANISM_GENERATORS));
    }
}
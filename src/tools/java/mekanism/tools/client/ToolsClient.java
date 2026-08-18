package mekanism.tools.client;

import mekanism.client.MekanismModDisplayInfo;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.ToolsLang;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.modlist.ModDisplayInfo;

@Mod(value = MekanismTools.MODID, dist = Dist.CLIENT)
public class ToolsClient {

    public ToolsClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        container.<ModDisplayInfo>registerExtensionPoint(ModDisplayInfo.class, () -> new MekanismModDisplayInfo(container, ToolsLang.MEKANISM_TOOLS));
    }
}
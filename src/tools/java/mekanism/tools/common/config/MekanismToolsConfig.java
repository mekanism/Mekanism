package mekanism.tools.common.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import mekanism.common.config.MekanismConfigHelper;
import net.minecraftforge.fml.ModLoadingContext;

public class MekanismToolsConfig {

    public static final ToolsConfig tools = new ToolsConfig();

    public static void loadFromFiles() {
        //TODO: Let forge handle loading and then do some sort of lazy init/way to let this stuff sync from server to client
        // So that the server can change the values the client knows about
        CommentedFileConfig configData = CommentedFileConfig.builder(MekanismConfigHelper.CONFIG_DIR.resolve(tools.getFileName() + ".toml")).sync().autosave().writingMode(WritingMode.REPLACE).build();
        configData.load();
        tools.getConfigSpec().setConfig(configData);
    }

    public static void registerConfigs(ModLoadingContext modLoadingContext) {
        MekanismConfigHelper.registerConfig(modLoadingContext.getActiveContainer(), tools);
    }
}
package mekanism.common.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import java.nio.file.Path;
import mekanism.common.Mekanism;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

public class MekanismConfigHelper {

    public static Path CONFIG_DIR;

    public static void registerConfig(ModLoadingContext modLoadingContext, IMekanismConfig config) {
        modLoadingContext.registerConfig(config.getConfigType(), config.getConfigSpec(), Mekanism.MODID + "/" + config.getFileName());
    }

    public static void load(IMekanismConfig config) {
        if (CONFIG_DIR == null) {
            CONFIG_DIR = FMLPaths.getOrCreateGameRelativePath(FMLPaths.CONFIGDIR.get().resolve(Mekanism.MODID), Mekanism.MODID);
        }
        CommentedFileConfig configData = CommentedFileConfig.builder(CONFIG_DIR.resolve(config.getFileName())).sync().autosave().writingMode(WritingMode.REPLACE).build();
        configData.load();
        config.getConfigSpec().setConfig(configData);
    }
}
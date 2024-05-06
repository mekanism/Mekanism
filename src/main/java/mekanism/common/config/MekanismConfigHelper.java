package mekanism.common.config;

import java.lang.reflect.Method;
import java.nio.file.Path;
import mekanism.common.Mekanism;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLPaths;

public class MekanismConfigHelper {

    private MekanismConfigHelper() {
    }

    public static final Path CONFIG_DIR = FMLPaths.getOrCreateGameRelativePath(FMLPaths.CONFIGDIR.get().resolve(Mekanism.MOD_NAME));

    /**
     * Creates a mod config so that {@link ConfigTracker} will track it and sync server configs from server to client.
     */
    public static void registerConfig(ModContainer modContainer, IMekanismConfig config) {
        MekanismModConfig modConfig = new MekanismModConfig(modContainer, config);
        if (config.addToContainer()) {
            modContainer.addConfig(modConfig);
        }
        if (config.loadImmediately()) {
            Mekanism.logger.debug("Manually loading config {} early", config.getFileName());
            try {
                Method method = ConfigTracker.class.getDeclaredMethod("openConfig", ModConfig.class, Path.class, Path.class);
                method.setAccessible(true);
                method.invoke(ConfigTracker.INSTANCE, modConfig, FMLPaths.CONFIGDIR.get(), null);
                method.setAccessible(false);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
package mekanism.common.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import java.nio.file.Path;
import java.util.function.Function;
import mekanism.common.Mekanism;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ConfigFileTypeHandler;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLPaths;

/**
 * Custom {@link ModConfig} implementation that allows for rerouting the server config from being in the world's folder to being in the normal config folder. This allows
 * for us to use the builtin sync support, without the extra hassle of having to explain to people where the config file is, or require people in single player to edit
 * the config each time they make a new world.
 */
public class MekanismModConfig extends ModConfig {

    private static final MekanismConfigFileTypeHandler MEK_TOML = new MekanismConfigFileTypeHandler();

    private final IMekanismConfig mekanismConfig;

    public MekanismModConfig(ModContainer container, IMekanismConfig config) {
        super(config.getConfigType(), config.getConfigSpec(), container, Mekanism.MOD_NAME + "/" + config.getFileName() + ".toml");
        this.mekanismConfig = config;
    }

    //@Override//TODO - 1.20.3: Uncomment annotation when https://github.com/neoforged/FancyModLoader/pull/46 is merged
    public ConfigFileTypeHandler getHandler() {
        return MEK_TOML;
    }

    public void clearCache(ModConfigEvent event) {
        mekanismConfig.clearCache(event instanceof ModConfigEvent.Unloading);
    }

    private static class MekanismConfigFileTypeHandler extends ConfigFileTypeHandler {

        private static Path getPath(Path configBasePath) {
            //Intercept server config path reading for Mekanism configs and reroute it to the normal config directory
            if (configBasePath.endsWith("serverconfig")) {
                return FMLPaths.CONFIGDIR.get();
            }
            return configBasePath;
        }

        @Override
        public Function<ModConfig, CommentedFileConfig> reader(Path configBasePath) {
            return super.reader(getPath(configBasePath));
        }

        @Override
        public void unload(Path configBasePath, ModConfig config) {
            super.unload(getPath(configBasePath), config);
        }
    }
}
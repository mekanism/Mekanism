package mekanism.common.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import java.nio.file.Path;
import mekanism.api.block.IBlockDisableable;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.Mekanism;
import net.minecraft.block.Block;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

//TODO: Figure out if things sync automatically or what is needed to make them sync
//TODO: Refactor the config into multiple files and more subsets then it is in the 1.12 version
public class MekanismConfig {

    public static Path CONFIG_DIR;
    //TODO: Debate putting ranges on things so that the configs can store DoubleValue and IntValue objects directly instead of ConfigValue<Type>
    public static final ClientConfig client = new ClientConfig();
    private static final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
    public static final GeneralConfig general = new GeneralConfig(builder);
    public static final StorageConfig storage = new StorageConfig(builder);
    public static final TierConfig tiers = new TierConfig(builder);
    public static final UsageConfig usage = new UsageConfig(builder);

    public static void loadFromFiles() {
        load(client);
        load(general);
        //load(storage);
        //load(tiers);
        //load(usage);
    }

    public static void registerConfigs(ModLoadingContext modLoadingContext) {
        registerConfig(modLoadingContext, client);
        general.setConfigSpec(builder.build());
        registerConfig(modLoadingContext, general);
        //TODO: Figure out why it won't let it have multiple "common" configs
        //registerConfig(modLoadingContext, storage);
        //registerConfig(modLoadingContext, tiers);
        //registerConfig(modLoadingContext, usage);
    }

    public static void registerConfig(ModLoadingContext modLoadingContext, IMekanismConfig config) {
        modLoadingContext.registerConfig(config.getConfigType(), config.getConfigSpec());
    }

    public static void load(IMekanismConfig config) {
        if (CONFIG_DIR == null) {
            CONFIG_DIR = FMLPaths.getOrCreateGameRelativePath(FMLPaths.CONFIGDIR.get().resolve(Mekanism.MODID), Mekanism.MODID);
        }
        //TODO: Don't load as early as it is loading
        CommentedFileConfig configData = CommentedFileConfig.builder(CONFIG_DIR.resolve(config.getFileName()))
              .sync().autosave().writingMode(WritingMode.REPLACE).build();
        configData.load();
        config.getConfigSpec().setConfig(configData);
    }

    public static void addEnabledBlocksCategory(ForgeConfigSpec.Builder builder, IBlockProvider[] blockProviders) {
        for (IBlockProvider blockProvider : blockProviders) {
            Block block = blockProvider.getBlock();
            //Instead of using helper methods in IBlockProvider, just inline everything so that we don't have to keep checking the block's type
            if (block instanceof IBlockDisableable) {
                IBlockDisableable disableable = (IBlockDisableable) block;
                String name = blockProvider.getName();
                BooleanValue enabledReference = builder.comment("Allow " + name + " to be used/crafted. Requires game restart to fully take effect.")
                      .worldRestart().define(name, disableable.isEnabled());
                disableable.setEnabledConfigReference(enabledReference);
            }
        }
    }
}
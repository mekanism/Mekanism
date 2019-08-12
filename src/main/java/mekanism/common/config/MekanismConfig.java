package mekanism.common.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import mekanism.common.base.IBlockProvider;
import mekanism.common.block.interfaces.IBlockDisableable;
import net.minecraft.block.Block;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

//TODO: Figure out if things sync automatically or what is needed to make them sync
//TODO: Refactor the config into multiple files and more subsets then it is in the 1.12 version
public class MekanismConfig {

    //TODO: Debate putting ranges on things so that the configs can store DoubleValue and IntValue objects directly instead of ConfigValue<Type>
    public static final ClientConfig client = new ClientConfig();
    public static final GeneralConfig general = new GeneralConfig();
    public static final StorageConfig storage = new StorageConfig();
    public static final TierConfig tiers = new TierConfig();
    public static final UsageConfig usage = new UsageConfig();

    public static void loadFromFiles() {
        //TODO: Make these all go in the mekanism directory
        load(client);
        load(general);
        load(storage);
        load(tiers);
        load(usage);
    }

    public static void registerConfigs(ModLoadingContext modLoadingContext) {
        registerConfig(modLoadingContext, client);
        registerConfig(modLoadingContext, general);
        registerConfig(modLoadingContext, storage);
        registerConfig(modLoadingContext, tiers);
        registerConfig(modLoadingContext, usage);
    }

    public static void registerConfig(ModLoadingContext modLoadingContext, IMekanismConfig config) {
        modLoadingContext.registerConfig(config.getConfigType(), config.getConfigSpec());
    }

    public static void load(IMekanismConfig config) {
        CommentedFileConfig configData = CommentedFileConfig.builder(FMLPaths.CONFIGDIR.get().resolve(config.getFileName()))
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
package mekanism.common.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;

//TODO: Figure out if things sync automatically or what is needed to make them sync
//TODO: Refactor the config into multiple files and more subsets then it is in the 1.12 version
public class MekanismConfig {

    //TODO: Debate putting ranges on things so that the configs can store DoubleValue and IntValue objects directly instead of ConfigValue<Type>
    public static final ClientConfig client = new ClientConfig();
    private static final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
    public static final GeneralConfig general = new GeneralConfig(builder);
    public static final StorageConfig storage = new StorageConfig(builder);
    public static final TierConfig tiers = new TierConfig(builder);
    public static final UsageConfig usage = new UsageConfig(builder);

    public static void loadFromFiles() {
        MekanismConfigHelper.load(client);
        MekanismConfigHelper.load(general);
        //MekanismConfigHelper.load(storage);
        //MekanismConfigHelper.load(tiers);
        //MekanismConfigHelper.load(usage);
    }

    public static void registerConfigs(ModLoadingContext modLoadingContext) {
        MekanismConfigHelper.registerConfig(modLoadingContext, client);
        general.setConfigSpec(builder.build());
        MekanismConfigHelper.registerConfig(modLoadingContext, general);
        //TODO: Figure out why it won't let it have multiple "common" configs
        //MekanismConfigHelper.registerConfig(modLoadingContext, storage);
        //MekanismConfigHelper.registerConfig(modLoadingContext, tiers);
        //MekanismConfigHelper.registerConfig(modLoadingContext, usage);
    }
}
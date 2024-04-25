package mekanism.tools.common;

import mekanism.common.Mekanism;
import mekanism.common.base.IModModule;
import mekanism.common.config.MekanismModConfig;
import mekanism.common.lib.Version;
import mekanism.tools.common.config.MekanismToolsConfig;
import mekanism.tools.common.material.BaseMekanismMaterial;
import mekanism.tools.common.registries.ToolsCreativeTabs;
import mekanism.tools.common.registries.ToolsItems;
import mekanism.tools.common.registries.ToolsRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(MekanismTools.MODID)
public class MekanismTools implements IModModule {

    public static final String MODID = "mekanismtools";

    public static MekanismTools instance;

    /**
     * MekanismTools version number
     */
    public final Version versionNumber;

    public MekanismTools(ModContainer modContainer, IEventBus modEventBus) {
        Mekanism.addModule(instance = this);
        MekanismToolsConfig.registerConfigs(modContainer);
        //Register the listener for special mob spawning (mobs with Mekanism armor/tools)
        NeoForge.EVENT_BUS.addListener(MobEquipmentHelper::onLivingSpecialSpawn);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onConfigLoad);
        ToolsItems.ITEMS.register(modEventBus);
        ToolsCreativeTabs.CREATIVE_TABS.register(modEventBus);
        ToolsRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
        //Set our version number to match the neoforge.mods.toml file, which matches the one in our build.gradle
        versionNumber = new Version(modContainer);
    }

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MekanismTools.MODID, path);
    }

    private void onConfigLoad(ModConfigEvent configEvent) {
        //Note: We listen to both the initial load and the reload, to make sure that we fix any accidentally
        // cached values from calls before the initial loading
        ModConfig config = configEvent.getConfig();
        //Make sure it is for the same modid as us
        if (config.getModId().equals(MODID) && config instanceof MekanismModConfig mekConfig) {
            mekConfig.clearCache(configEvent);
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        registerTiers(MekanismToolsConfig.tools.bronze, MekanismToolsConfig.tools.lapisLazuli, MekanismToolsConfig.tools.osmium, MekanismToolsConfig.tools.steel,
              MekanismToolsConfig.tools.refinedGlowstone, MekanismToolsConfig.tools.refinedObsidian);
        Mekanism.logger.info("Loaded 'Mekanism: Tools' module.");
    }

    @SuppressWarnings("deprecation")
    private void registerTiers(BaseMekanismMaterial... tiers) {
        //TODO - 1.20.5: Figure this out
        /*Multimap<Integer, Tier> vanillaTiers = HashMultimap.create();
        for (Tiers vanillaTier : Tiers.values()) {
            vanillaTiers.put(vanillaTier.getLevel(), vanillaTier);
        }
        for (BaseMekanismMaterial tier : tiers) {
            int level = tier.getLevel();
            Collection<Tier> equivalent = vanillaTiers.get(level);
            Collection<Tier> vanillaNext = vanillaTiers.get(level + 1);
            //If the tier is equivalent to another tier then the equivalent one should be placed in the after list
            // and if it is equivalent to a vanilla tier (like all ours are when equivalent), the next tier
            // should also specify the next tier in the before list
            TierSortingRegistry.registerTier(tier, rl(tier.getRegistryPrefix()), new ArrayList<>(equivalent), new ArrayList<>(vanillaNext));
        }*/
    }

    @Override
    public Version getVersion() {
        return versionNumber;
    }

    @Override
    public String getName() {
        return "Tools";
    }

    @Override
    public void resetClient() {
    }
}
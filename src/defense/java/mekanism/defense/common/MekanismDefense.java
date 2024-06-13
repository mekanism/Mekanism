package mekanism.defense.common;

import mekanism.common.Mekanism;
import mekanism.common.base.IModModule;
import mekanism.common.config.MekanismModConfig;
import mekanism.common.lib.Version;
import mekanism.defense.common.config.MekanismDefenseConfig;
import mekanism.defense.common.registries.DefenseBlocks;
import mekanism.defense.common.registries.DefenseContainerTypes;
import mekanism.defense.common.registries.DefenseCreativeTabs;
import mekanism.defense.common.registries.DefenseItems;
import mekanism.defense.common.registries.DefenseTileEntityTypes;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

@Mod(MekanismDefense.MODID)
public class MekanismDefense implements IModModule {

    public static final String MODID = "mekanismdefense";

    public static MekanismDefense instance;

    /**
     * MekanismDefense version number
     */
    public final Version versionNumber;

    public MekanismDefense(ModContainer modContainer, IEventBus modEventBus) {
        Mekanism.addModule(instance = this);
        //Set our version number to match the neoforge.mods.toml file, which matches the one in our build.gradle
        versionNumber = new Version(modContainer);
        MekanismDefenseConfig.registerConfigs(modContainer);
        NeoForge.EVENT_BUS.addListener(this::serverStopped);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onConfigLoad);
        DefenseItems.ITEMS.register(modEventBus);
        DefenseBlocks.BLOCKS.register(modEventBus);
        DefenseCreativeTabs.CREATIVE_TABS.register(modEventBus);
        DefenseContainerTypes.CONTAINER_TYPES.register(modEventBus);
        DefenseTileEntityTypes.TILE_ENTITY_TYPES.register(modEventBus);
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MekanismDefense.MODID, path);
    }

    public void commonSetup(FMLCommonSetupEvent event) {
        //Finalization
        Mekanism.logger.info("Loaded 'Mekanism: Defense' module.");
    }

    private void serverStopped(ServerStoppedEvent event) {
    }

    @Override
    public Version getVersion() {
        return versionNumber;
    }

    @Override
    public String getName() {
        return "Defense";
    }

    @Override
    public void resetClient() {
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
}
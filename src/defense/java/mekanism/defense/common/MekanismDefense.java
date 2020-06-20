package mekanism.defense.common;

import mekanism.common.Mekanism;
import mekanism.common.base.IModule;
import mekanism.common.config.MekanismModConfig;
import mekanism.common.lib.Version;
import mekanism.defense.common.config.MekanismDefenseConfig;
import mekanism.defense.common.network.DefensePacketHandler;
import mekanism.defense.common.registries.DefenseBlocks;
import mekanism.defense.common.registries.DefenseContainerTypes;
import mekanism.defense.common.registries.DefenseItems;
import mekanism.defense.common.registries.DefenseTileEntityTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MekanismDefense.MODID)
public class MekanismDefense implements IModule {

    public static final String MODID = "mekanismdefense";

    public static MekanismDefense instance;

    /**
     * MekanismGenerators version number
     */
    public final Version versionNumber;
    /**
     * Mekanism Generators Packet Pipeline
     */
    public static final DefensePacketHandler packetHandler = new DefensePacketHandler();

    public MekanismDefense() {
        Mekanism.modulesLoaded.add(instance = this);
        MekanismDefenseConfig.registerConfigs(ModLoadingContext.get());
        MinecraftForge.EVENT_BUS.addListener(this::serverStopped);

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onConfigLoad);
        DefenseItems.ITEMS.register(modEventBus);
        DefenseBlocks.BLOCKS.register(modEventBus);
        DefenseContainerTypes.CONTAINER_TYPES.register(modEventBus);
        DefenseTileEntityTypes.TILE_ENTITY_TYPES.register(modEventBus);

        //Set our version number to match the mods.toml file, which matches the one in our build.gradle
        versionNumber = new Version(ModLoadingContext.get().getActiveContainer().getModInfo().getVersion());
    }

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MekanismDefense.MODID, path);
    }

    public void commonSetup(FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(this);

        packetHandler.initialize();

        //Finalization
        Mekanism.logger.info("Loaded 'Mekanism Defense' module.");
    }

    private void serverStopped(FMLServerStoppedEvent event) {
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

    private void onConfigLoad(ModConfig.ModConfigEvent configEvent) {
        //Note: We listen to both the initial load and the reload, so as to make sure that we fix any accidentally
        // cached values from calls before the initial loading
        ModConfig config = configEvent.getConfig();
        //Make sure it is for the same modid as us
        if (config.getModId().equals(MODID) && config instanceof MekanismModConfig) {
            ((MekanismModConfig) config).clearCache();
        }
    }
}
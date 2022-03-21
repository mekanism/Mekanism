package mekanism.chemistry.common;

import mekanism.chemistry.common.config.MekanismChemistryConfig;
import mekanism.chemistry.common.network.ChemistryPacketHandler;
import mekanism.chemistry.common.registries.ChemistryBlocks;
import mekanism.chemistry.common.registries.ChemistryContainerTypes;
import mekanism.chemistry.common.registries.ChemistryFluids;
import mekanism.chemistry.common.registries.ChemistryGases;
import mekanism.chemistry.common.registries.ChemistryItems;
import mekanism.chemistry.common.registries.ChemistrySounds;
import mekanism.chemistry.common.registries.ChemistryTileEntityTypes;
import mekanism.common.Mekanism;
import mekanism.common.base.IModModule;
import mekanism.common.config.MekanismModConfig;
import mekanism.common.lib.Version;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MekanismChemistry.MODID)
public class MekanismChemistry implements IModModule {

    public static final String MODID = "mekanismchemistry";

    public static MekanismChemistry instance;

    /**
     * MekanismTools version number
     */
    public final Version versionNumber;

    /**
     * Mekanism Chemistry Packet Pipeline
     */
    private final ChemistryPacketHandler packetHandler;

    // TODO: multiblocks go here

    public MekanismChemistry() {
        Mekanism.modulesLoaded.add(instance = this);
        MekanismChemistryConfig.registerConfigs(ModLoadingContext.get());

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onConfigLoad);

        ChemistryItems.ITEMS.register(modEventBus);
        ChemistryBlocks.BLOCKS.register(modEventBus);
        ChemistryFluids.FLUIDS.register(modEventBus);
        ChemistrySounds.SOUND_EVENTS.register(modEventBus);
        ChemistryContainerTypes.CONTAINER_TYPES.register(modEventBus);
        ChemistryTileEntityTypes.TILE_ENTITY_TYPES.register(modEventBus);
        ChemistryGases.GASES.register(modEventBus);

        //Set our version number to match the mods.toml file, which matches the one in our build.gradle
        versionNumber = new Version(ModLoadingContext.get().getActiveContainer());
        packetHandler = new ChemistryPacketHandler();
    }

    public static ChemistryPacketHandler packetHandler() {
        return instance.packetHandler;
    }

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MekanismChemistry.MODID, path);
    }

    @Override
    public Version getVersion() {
        return versionNumber;
    }

    @Override
    public String getName() {
        return "Chemistry";
    }

    @Override
    public void resetClient() {
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            //Ensure our tags are all initialized
            ChemistryTags.init();
        });

        // TODO: multiblocks go here

        packetHandler.initialize();

        Mekanism.logger.info("Loaded 'Mekanism: Chemistry' module.");
    }

    private void onConfigLoad(ModConfigEvent configEvent) {
        //Note: We listen to both the initial load and the reload, to make sure that we fix any accidentally
        // cached values from calls before the initial loading
        ModConfig config = configEvent.getConfig();
        //Make sure it is for the same modid as us
        if (config.getModId().equals(MODID) && config instanceof MekanismModConfig mekConfig) {
            mekConfig.clearCache();
        }
    }
}

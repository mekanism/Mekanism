package mekanism.additions.common;

import mekanism.additions.client.AdditionsClient;
import mekanism.additions.common.config.MekanismAdditionsConfig;
import mekanism.additions.common.entity.AdditionsEntityType;
import mekanism.additions.common.voice.VoiceServerManager;
import mekanism.common.Mekanism;
import mekanism.common.Version;
import mekanism.common.base.IModule;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MekanismAdditions.MODID)
public class MekanismAdditions implements IModule {

    public static final String MODID = "mekanismadditions";

    public static MekanismAdditions instance;

    /**
     * MekanismTools version number
     */
    public final Version versionNumber;

    /**
     * The VoiceServer manager for walkie talkies
     */
    public static VoiceServerManager voiceManager;

    public MekanismAdditions() {
        Mekanism.modulesLoaded.add(instance = this);
        MekanismAdditionsConfig.registerConfigs(ModLoadingContext.get());

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::serverStarting);
        modEventBus.addListener(this::serverStopping);
        modEventBus.addListener(this::onConfigReload);

        AdditionsItem.ITEMS.register(modEventBus);
        AdditionsBlock.BLOCKS.register(modEventBus);
        AdditionsEntityType.ENTITY_TYPES.register(modEventBus);

        //Set our version number to match the mods.toml file, which matches the one in our build.gradle
        versionNumber = new Version(ModLoadingContext.get().getActiveContainer().getModInfo().getVersion());
    }

    @Override
    public Version getVersion() {
        return versionNumber;
    }

    @Override
    public String getName() {
        return "Additions";
    }

    @Override
    public void resetClient() {
        AdditionsClient.reset();
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        //Set up VoiceServerManager
        if (MekanismAdditionsConfig.additions.voiceServerEnabled.get()) {
            voiceManager = new VoiceServerManager();
        }
        //Add baby skeleton spawner
        //TODO: Spawn baby skeletons
        /*if (MekanismAdditionsConfig.additions.spawnBabySkeletons.get()) {
            for (Biome biome : BiomeProvider.BIOMES_TO_SPAWN_IN) {
                if (biome.getSpawns(EntityClassification.MONSTER).size() > 0) {
                    EntityRegistry.addSpawn(EntityBabySkeleton.class, 40, 1, 3, EntityClassification.MONSTER, biome);
                }
            }
        }*/

        Mekanism.logger.info("Loaded 'Mekanism: Additions' module.");
    }

    private void serverStarting(FMLServerStartingEvent event) {
        if (MekanismAdditionsConfig.additions.voiceServerEnabled.get()) {
            voiceManager.start();
        }
    }

    private void serverStopping(FMLServerStoppingEvent event) {
        if (MekanismAdditionsConfig.additions.voiceServerEnabled.get()) {
            voiceManager.stop();
        }
    }

    private void onConfigReload(ModConfig.ConfigReloading configEvent) {
        //TODO: Handle reloading
    }
}
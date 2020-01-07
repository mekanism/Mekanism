package mekanism.additions.common;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.additions.client.AdditionsClient;
import mekanism.additions.common.config.MekanismAdditionsConfig;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.additions.common.registries.AdditionsEntityTypes;
import mekanism.additions.common.registries.AdditionsItems;
import mekanism.additions.common.registries.AdditionsSounds;
import mekanism.additions.common.voice.VoiceServerManager;
import mekanism.common.Mekanism;
import mekanism.common.Version;
import mekanism.common.base.IModule;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.biome.provider.BiomeProvider;
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

        AdditionsItems.ITEMS.register(modEventBus);
        AdditionsBlocks.BLOCKS.register(modEventBus);
        AdditionsEntityTypes.ENTITY_TYPES.register(modEventBus);
        AdditionsSounds.SOUND_EVENTS.register(modEventBus);

        //Set our version number to match the mods.toml file, which matches the one in our build.gradle
        versionNumber = new Version(ModLoadingContext.get().getActiveContainer().getModInfo().getVersion());
    }

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MekanismAdditions.MODID, path);
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
        //Add baby skeleton to spawn table
        if (MekanismAdditionsConfig.common.spawnBabySkeletons.get()) {
            SpawnListEntry spawnListEntry = new SpawnListEntry(AdditionsEntityTypes.BABY_SKELETON.get(), 40, 1, 3);
            //TODO: Do we want this to look at all biomes?
            for (Biome biome : BiomeProvider.BIOMES_TO_SPAWN_IN) {
                List<SpawnListEntry> monsterSpawns = biome.getSpawns(EntityClassification.MONSTER);
                if (!monsterSpawns.isEmpty()) {
                    monsterSpawns.add(spawnListEntry);
                }
            }
        }

        //TODO: Remove this when we can, for now just lazy add the dispense behavior
        DispenserBlock.registerDispenseBehavior(AdditionsItems.BABY_SKELETON_SPAWN_EGG, new DefaultDispenseItemBehavior() {
            @Nonnull
            @Override
            public ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
                Direction direction = source.getBlockState().get(DispenserBlock.FACING);
                EntityType<?> entityType = ((SpawnEggItem) stack.getItem()).getType(stack.getTag());
                entityType.spawn(source.getWorld(), stack, null, source.getBlockPos().offset(direction), SpawnReason.DISPENSER, direction != Direction.UP, false);
                stack.shrink(1);
                return stack;
            }
        });

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
package mekanism.additions.common;

import java.util.UUID;
import javax.annotation.Nonnull;
import mekanism.additions.client.AdditionsClient;
import mekanism.additions.common.config.MekanismAdditionsConfig;
import mekanism.additions.common.entity.SpawnHelper;
import mekanism.additions.common.item.AdditionsSpawnEggItem;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.additions.common.registries.AdditionsEntityTypes;
import mekanism.additions.common.registries.AdditionsItems;
import mekanism.additions.common.registries.AdditionsSounds;
import mekanism.additions.common.voice.VoiceServerManager;
import mekanism.common.Mekanism;
import mekanism.common.base.IModule;
import mekanism.common.config.MekanismModConfig;
import mekanism.common.lib.Version;
import mekanism.common.registration.impl.ItemRegistryObject;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DeferredWorkQueue;
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

    //COPY of ZombieEntity BABY_SPEED_BOOST_ID and BABY_SPEED_BOOST
    public static final UUID babySpeedBoostUUID = UUID.fromString("B9766B59-9566-4402-BC1F-2EE2A276D836");
    public static final AttributeModifier babySpeedBoostModifier = new AttributeModifier(babySpeedBoostUUID, "Baby speed boost", 0.5D, Operation.MULTIPLY_BASE);

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
        MinecraftForge.EVENT_BUS.addListener(this::serverStarting);
        MinecraftForge.EVENT_BUS.addListener(this::serverStopping);
        modEventBus.addListener(this::onConfigLoad);

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

    @Override
    public void launchClient() {
        AdditionsClient.launch();
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        //Note: This deprecation can be safely ignored - Forge hasn't added the proposed replacement for it yet.
        //noinspection deprecation
        DeferredWorkQueue.runLater(() -> {
            SpawnHelper.addSpawns();
            registerSpawnEggDelayed(AdditionsItems.BABY_CREEPER_SPAWN_EGG, AdditionsItems.BABY_ENDERMAN_SPAWN_EGG, AdditionsItems.BABY_SKELETON_SPAWN_EGG,
                  AdditionsItems.BABY_STRAY_SPAWN_EGG, AdditionsItems.BABY_WITHER_SKELETON_SPAWN_EGG);
            //Add parrot sound imitations for baby mobs
            //Note: There is no imitation sound for endermen
            ParrotEntity.IMITATION_SOUND_EVENTS.put(AdditionsEntityTypes.BABY_CREEPER.getEntityType(), SoundEvents.ENTITY_PARROT_IMITATE_CREEPER);
            ParrotEntity.IMITATION_SOUND_EVENTS.put(AdditionsEntityTypes.BABY_SKELETON.getEntityType(), SoundEvents.ENTITY_PARROT_IMITATE_SKELETON);
            ParrotEntity.IMITATION_SOUND_EVENTS.put(AdditionsEntityTypes.BABY_STRAY.getEntityType(), SoundEvents.ENTITY_PARROT_IMITATE_STRAY);
            ParrotEntity.IMITATION_SOUND_EVENTS.put(AdditionsEntityTypes.BABY_WITHER_SKELETON.getEntityType(), SoundEvents.ENTITY_PARROT_IMITATE_WITHER_SKELETON);
            Mekanism.logger.info("Loaded 'Mekanism: Additions' module.");
        });
    }

    @SafeVarargs
    private static void registerSpawnEggDelayed(ItemRegistryObject<AdditionsSpawnEggItem>... spawnEggs) {
        DefaultDispenseItemBehavior dispenseBehavior = new DefaultDispenseItemBehavior() {
            @Nonnull
            @Override
            public ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
                Direction direction = source.getBlockState().get(DispenserBlock.FACING);
                EntityType<?> entityType = ((SpawnEggItem) stack.getItem()).getType(stack.getTag());
                entityType.spawn(source.getWorld(), stack, null, source.getBlockPos().offset(direction), SpawnReason.DISPENSER, direction != Direction.UP, false);
                stack.shrink(1);
                return stack;
            }
        };
        //TODO: Remove this when we can, for now just lazy add the dispense behavior, and middle clicking
        for (ItemRegistryObject<AdditionsSpawnEggItem> spawnEgg : spawnEggs) {
            spawnEgg.getItem().addToEggLookup();
            DispenserBlock.registerDispenseBehavior(spawnEgg, dispenseBehavior);
        }
    }

    private void serverStarting(FMLServerStartingEvent event) {
        if (MekanismAdditionsConfig.additions.voiceServerEnabled.get()) {
            if (voiceManager == null) {
                voiceManager = new VoiceServerManager();
            }
            voiceManager.start();
        }
    }

    private void serverStopping(FMLServerStoppingEvent event) {
        if (MekanismAdditionsConfig.additions.voiceServerEnabled.get()) {
            voiceManager.stop();
        }
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
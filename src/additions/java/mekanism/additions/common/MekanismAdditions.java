package mekanism.additions.common;

import mekanism.additions.client.AdditionsClient;
import mekanism.additions.common.block.BlockObsidianTNT;
import mekanism.additions.common.config.MekanismAdditionsConfig;
import mekanism.additions.common.entity.baby.EntityBabyStray;
import mekanism.additions.common.registries.AdditionsBiomeModifierSerializers;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.additions.common.registries.AdditionsEntityTypes;
import mekanism.additions.common.registries.AdditionsItems;
import mekanism.additions.common.registries.AdditionsSounds;
import mekanism.additions.common.registries.AdditionsStructureModifierSerializers;
import mekanism.additions.common.voice.VoiceServerManager;
import mekanism.common.Mekanism;
import mekanism.common.base.IModModule;
import mekanism.common.config.MekanismModConfig;
import mekanism.common.lib.Version;
import mekanism.common.registration.impl.EntityTypeRegistryObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.NotNull;

@Mod(MekanismAdditions.MODID)
public class MekanismAdditions implements IModModule {

    public static final String MODID = "mekanismadditions";

    public static MekanismAdditions instance;

    /**
     * MekanismTools version number
     */
    public final Version versionNumber;

    /**
     * The VoiceServer manager for walkie-talkies
     */
    public static VoiceServerManager voiceManager;

    public MekanismAdditions() {
        Mekanism.addModule(instance = this);
        MekanismAdditionsConfig.registerConfigs(ModLoadingContext.get());
        MinecraftForge.EVENT_BUS.addListener(this::serverStarting);
        MinecraftForge.EVENT_BUS.addListener(this::serverStopping);

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onConfigLoad);
        AdditionsItems.ITEMS.register(modEventBus);
        AdditionsBlocks.BLOCKS.register(modEventBus);
        AdditionsEntityTypes.ENTITY_TYPES.register(modEventBus);
        AdditionsSounds.SOUND_EVENTS.register(modEventBus);
        AdditionsBiomeModifierSerializers.BIOME_MODIFIER_SERIALIZERS.register(modEventBus);
        AdditionsStructureModifierSerializers.STRUCTURE_MODIFIER_SERIALIZERS.register(modEventBus);

        //Set our version number to match the mods.toml file, which matches the one in our build.gradle
        versionNumber = new Version(ModLoadingContext.get().getActiveContainer());
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
        event.enqueueWork(() -> {
            //Ensure our tags are all initialized
            AdditionsTags.init();
            //Setup some stuff related to entities
            //Register spawn controls for the baby entities based on the vanilla spawn controls
            registerSpawnControls(AdditionsEntityTypes.BABY_CREEPER, AdditionsEntityTypes.BABY_ENDERMAN, AdditionsEntityTypes.BABY_SKELETON,
                  AdditionsEntityTypes.BABY_WITHER_SKELETON);
            //Slightly different restrictions for the baby stray, as strays have a slightly different spawn restriction
            SpawnPlacements.register(AdditionsEntityTypes.BABY_STRAY.get(), SpawnPlacements.Type.ON_GROUND,
                  Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EntityBabyStray::spawnRestrictions);
            //Add parrot sound imitations for baby mobs
            //Note: There is no imitation sound for endermen
            Parrot.MOB_SOUND_MAP.put(AdditionsEntityTypes.BABY_CREEPER.get(), SoundEvents.PARROT_IMITATE_CREEPER);
            Parrot.MOB_SOUND_MAP.put(AdditionsEntityTypes.BABY_SKELETON.get(), SoundEvents.PARROT_IMITATE_SKELETON);
            Parrot.MOB_SOUND_MAP.put(AdditionsEntityTypes.BABY_STRAY.get(), SoundEvents.PARROT_IMITATE_STRAY);
            Parrot.MOB_SOUND_MAP.put(AdditionsEntityTypes.BABY_WITHER_SKELETON.get(), SoundEvents.PARROT_IMITATE_WITHER_SKELETON);
            //Dispenser behavior
            DispenserBlock.registerBehavior(AdditionsBlocks.OBSIDIAN_TNT, new DefaultDispenseItemBehavior() {
                @NotNull
                @Override
                protected ItemStack execute(@NotNull BlockSource source, @NotNull ItemStack stack) {
                    BlockPos blockpos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
                    if (BlockObsidianTNT.createAndAddEntity(source.getLevel(), blockpos, null)) {
                        source.getLevel().gameEvent(null, GameEvent.ENTITY_PLACE, blockpos);
                        stack.shrink(1);
                        return stack;
                    }
                    //Otherwise, if something went very wrong, eject it as a normal item
                    return super.execute(source, stack);
                }
            });
        });
        Mekanism.logger.info("Loaded 'Mekanism: Additions' module.");
    }

    @SafeVarargs
    private static void registerSpawnControls(EntityTypeRegistryObject<? extends Monster>... entityTypeROs) {
        for (EntityTypeRegistryObject<? extends Monster> entityTypeRO : entityTypeROs) {
            SpawnPlacements.register(entityTypeRO.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                  Monster::checkMonsterSpawnRules);
        }
    }

    private void serverStarting(ServerStartingEvent event) {
        if (MekanismAdditionsConfig.additions.voiceServerEnabled.get()) {
            if (voiceManager == null) {
                voiceManager = new VoiceServerManager();
            }
            voiceManager.start();
        }
    }

    private void serverStopping(ServerStoppingEvent event) {
        if (voiceManager != null) {
            voiceManager.stop();
            voiceManager = null;
        }
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
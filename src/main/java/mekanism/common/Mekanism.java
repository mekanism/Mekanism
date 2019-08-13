package mekanism.common;

import com.mojang.authlib.GameProfile;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import mekanism.api.Coord4D;
import mekanism.api.MekanismAPI;
import mekanism.api.MekanismAPI.BoxBlacklistEvent;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.OreGas;
import mekanism.api.infuse.InfuseObject;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.infuse.InfuseType;
import mekanism.api.transmitters.DynamicNetwork.ClientTickUpdate;
import mekanism.api.transmitters.DynamicNetwork.NetworkClientRequest;
import mekanism.api.transmitters.DynamicNetwork.TransmittersAddedEvent;
import mekanism.api.transmitters.TransmitterNetworkRegistry;
import mekanism.client.ClientProxy;
import mekanism.client.ClientTickHandler;
import mekanism.common.base.IBlockProvider;
import mekanism.common.base.IModule;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.chunkloading.ChunkManager;
import mekanism.common.command.CommandMek;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.boiler.SynchronizedBoilerData;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.content.matrix.SynchronizedMatrixData;
import mekanism.common.content.tank.SynchronizedTankData;
import mekanism.common.content.transporter.PathfinderCache;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.entity.EntityBabySkeleton;
import mekanism.common.entity.EntityBalloon;
import mekanism.common.entity.EntityFlame;
import mekanism.common.entity.EntityObsidianTNT;
import mekanism.common.entity.EntityRobit;
import mekanism.common.fixers.MekanismDataFixers;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.integration.IMCHandler;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.multipart.MultipartMekanism;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.network.PacketDataRequest;
import mekanism.common.network.PacketSimpleGui;
import mekanism.common.network.PacketTransmitterUpdate;
import mekanism.common.network.PacketTransmitterUpdate.PacketType;
import mekanism.common.recipe.BinRecipe;
import mekanism.common.recipe.GasConversionHandler;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.machines.SmeltingRecipe;
import mekanism.common.recipe.outputs.ItemStackOutput;
import mekanism.common.security.SecurityFrequency;
import mekanism.common.tags.MekanismBlockTagsProvider;
import mekanism.common.tags.MekanismItemTagsProvider;
import mekanism.common.tile.TileEntityAdvancedBoundingBlock;
import mekanism.common.transmitters.grid.EnergyNetwork.EnergyTransferEvent;
import mekanism.common.transmitters.grid.FluidNetwork.FluidTransferEvent;
import mekanism.common.transmitters.grid.GasNetwork.GasTransferEvent;
import mekanism.common.voice.VoiceServerManager;
import mekanism.common.world.GenHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.entity.EntityClassification;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Mekanism.MODID)
@Mod.EventBusSubscriber()
public class Mekanism {

    public static final String MODID = "mekanism";
    public static final String MOD_NAME = "Mekanism";
    public static final String LOG_TAG = '[' + MOD_NAME + ']';
    public static final PlayerState playerState = new PlayerState();
    public static final Set<UUID> freeRunnerOn = new HashSet<>();
    /**
     * Mekanism Packet Pipeline
     */
    public static PacketHandler packetHandler = new PacketHandler();
    /**
     * Mekanism logger instance
     */
    public static Logger logger = LogManager.getLogger(MOD_NAME);
    /**
     * Mekanism proxy instance
     */
    public static CommonProxy proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> CommonProxy::new);
    /**
     * Mekanism mod instance
     */
    public static Mekanism instance;
    /**
     * Mekanism hooks instance
     */
    public static MekanismHooks hooks = new MekanismHooks();
    /**
     * Mekanism configuration instance
     */
    public static Configuration configuration;
    /**
     * Mekanism version number
     */
    public static Version versionNumber = new Version(999, 999, 999);
    /**
     * MultiblockManagers for various structrures
     */
    public static MultiblockManager<SynchronizedTankData> tankManager = new MultiblockManager<>("dynamicTank");
    public static MultiblockManager<SynchronizedMatrixData> matrixManager = new MultiblockManager<>("inductionMatrix");
    public static MultiblockManager<SynchronizedBoilerData> boilerManager = new MultiblockManager<>(
          "thermoelectricBoiler");
    /**
     * FrequencyManagers for various networks
     */
    public static FrequencyManager publicTeleporters = new FrequencyManager(Frequency.class, Frequency.TELEPORTER);
    public static Map<UUID, FrequencyManager> privateTeleporters = new HashMap<>();
    public static FrequencyManager publicEntangloporters = new FrequencyManager(InventoryFrequency.class, InventoryFrequency.ENTANGLOPORTER);
    public static Map<UUID, FrequencyManager> privateEntangloporters = new HashMap<>();
    public static FrequencyManager securityFrequencies = new FrequencyManager(SecurityFrequency.class, SecurityFrequency.SECURITY);
    /**
     * Mekanism creative tab
     */
    public static CreativeTabMekanism tabMekanism = new CreativeTabMekanism();
    /**
     * List of Mekanism modules loaded
     */
    public static List<IModule> modulesLoaded = new ArrayList<>();
    /**
     * The server's world tick handler.
     */
    public static CommonWorldTickHandler worldTickHandler = new CommonWorldTickHandler();
    /**
     * The Mekanism world generation handler.
     */
    public static GenHandler genHandler = new GenHandler();
    /**
     * The version of ore generation in this version of Mekanism. Increment this every time the default ore generation changes.
     */
    public static int baseWorldGenVersion = 0;
    /**
     * The VoiceServer manager for walkie talkies
     */
    public static VoiceServerManager voiceManager;
    /**
     * The GameProfile used by the dummy Mekanism player
     */
    public static GameProfile gameProfile = new GameProfile(UUID.nameUUIDFromBytes("mekanism.common".getBytes()), Mekanism.LOG_TAG);
    public static KeySync keyMap = new KeySync();
    public static Set<Coord4D> activeVibrators = new HashSet<>();

    static {
        MekanismFluids.register();
    }

    public Mekanism() {
        instance = this;
        MekanismConfig.registerConfigs(ModLoadingContext.get());

        //TODO: Register other listeners and various stuff that is needed

        MekanismConfig.loadFromFiles();
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        // Register blocks and tile entities
        MekanismBlock.registerBlocks(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        // Register items and itemBlocks
        MekanismItem.registerItems(event.getRegistry());
        MekanismBlock.registerItemBlocks(event.getRegistry());
        //Ore dict entries that are for items not added by mekanism
        OreDictionary.registerOre("alloyBasic", new ItemStack(Items.REDSTONE));
    }

    public void gatherData(GatherDataEvent event) {
        //TODO: Register listener for this event
        DataGenerator gen = event.getGenerator();
        if (event.includeServer()) {
            gen.addProvider(new MekanismBlockTagsProvider(gen));
            gen.addProvider(new MekanismItemTagsProvider(gen));
            //gen.addProvider(new ForgeRecipeProvider(gen));
        }
    }

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityEntry> event) {
        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "ObsidianTNT"), EntityObsidianTNT.class, "ObsidianTNT", 0, Mekanism.instance, 64, 5, true);
        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "Robit"), EntityRobit.class, "Robit", 1, Mekanism.instance, 64, 2, true);
        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "Balloon"), EntityBalloon.class, "Balloon", 2, Mekanism.instance, 64, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "BabySkeleton"), EntityBabySkeleton.class, "BabySkeleton", 3, Mekanism.instance, 64, 5, true, 0xFFFFFF, 0x800080);
        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "Flame"), EntityFlame.class, "Flame", 4, Mekanism.instance, 64, 5, true);
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        // Register models
        proxy.registerBlockRenders();
        proxy.registerItemRenders();
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        MekanismSounds.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        event.getRegistry().register(new BinRecipe());
        addRecipes();
        GasConversionHandler.addDefaultGasMappings();
    }

    /**
     * Adds all in-game crafting, smelting and machine recipes.
     */
    public static void addRecipes() {
        //Furnace Recipes
        GameRegistry.addSmelting(MekanismBlock.OSMIUM_ORE.getItemStack(), MekanismItem.OSMIUM_INGOT.getItemStack(), 1.0F);
        GameRegistry.addSmelting(MekanismBlock.COPPER_ORE.getItemStack(), MekanismItem.COPPER_INGOT.getItemStack(), 1.0F);
        GameRegistry.addSmelting(MekanismBlock.TIN_ORE.getItemStack(), MekanismItem.TIN_INGOT.getItemStack(), 1.0F);
        GameRegistry.addSmelting(MekanismItem.OSMIUM_DUST.getItemStack(), MekanismItem.OSMIUM_INGOT.getItemStack(), 0.0F);
        GameRegistry.addSmelting(MekanismItem.IRON_DUST.getItemStack(), new ItemStack(Items.IRON_INGOT), 0.0F);
        GameRegistry.addSmelting(MekanismItem.GOLD_DUST.getItemStack(), new ItemStack(Items.GOLD_INGOT), 0.0F);
        GameRegistry.addSmelting(MekanismItem.STEEL_DUST.getItemStack(), MekanismItem.STEEL_INGOT.getItemStack(), 0.0F);
        GameRegistry.addSmelting(MekanismItem.COPPER_DUST.getItemStack(), MekanismItem.COPPER_INGOT.getItemStack(), 0.0F);
        GameRegistry.addSmelting(MekanismItem.TIN_DUST.getItemStack(), MekanismItem.TIN_INGOT.getItemStack(), 0.0F);

        //Enrichment Chamber Recipes
        if (MekanismBlock.ENRICHMENT_CHAMBER.isEnabled()) {
            RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.OBSIDIAN), MekanismItem.OBSIDIAN_DUST.getItemStack(4));
            RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Items.COAL), MekanismItem.COMPRESSED_CARBON.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Items.CHARCOAL), MekanismItem.COMPRESSED_CARBON.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Items.REDSTONE), MekanismItem.COMPRESSED_REDSTONE.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.MOSSY_COBBLESTONE), new ItemStack(Blocks.COBBLESTONE));
            RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.STONE), new ItemStack(Blocks.CRACKED_STONE_BRICKS));
            RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.SAND), new ItemStack(Blocks.GRAVEL));
            RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.GRAVEL), new ItemStack(Blocks.COBBLESTONE));
            RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Items.GUNPOWDER), new ItemStack(Items.FLINT));
            RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.CRACKED_STONE_BRICKS), new ItemStack(Blocks.STONE_BRICKS));
            RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.STONE_BRICKS), new ItemStack(Blocks.CHISELED_STONE_BRICKS));
            RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.MOSSY_STONE_BRICKS), new ItemStack(Blocks.STONE_BRICKS));
            RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.GLOWSTONE), new ItemStack(Items.GLOWSTONE_DUST, 4));
            RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Blocks.CLAY), new ItemStack(Items.CLAY_BALL, 4));
            RecipeHandler.addEnrichmentChamberRecipe(MekanismBlock.SALT_BLOCK.getItemStack(), MekanismItem.SALT.getItemStack(4));
            RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Items.DIAMOND), MekanismItem.COMPRESSED_DIAMOND.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(MekanismItem.HDPE_PELLET.getItemStack(3), MekanismItem.HDPE_SHEET.getItemStack());

            //Plastic to Slick Plastic
            RecipeHandler.addEnrichmentChamberRecipe(MekanismBlock.BLACK_PLASTIC_BLOCK.getItemStack(), MekanismBlock.BLACK_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(MekanismBlock.RED_PLASTIC_BLOCK.getItemStack(), MekanismBlock.RED_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(MekanismBlock.GREEN_PLASTIC_BLOCK.getItemStack(), MekanismBlock.GREEN_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(MekanismBlock.BROWN_PLASTIC_BLOCK.getItemStack(), MekanismBlock.BROWN_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(MekanismBlock.BLUE_PLASTIC_BLOCK.getItemStack(), MekanismBlock.BLUE_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(MekanismBlock.PURPLE_PLASTIC_BLOCK.getItemStack(), MekanismBlock.PURPLE_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(MekanismBlock.CYAN_PLASTIC_BLOCK.getItemStack(), MekanismBlock.CYAN_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(MekanismBlock.LIGHT_GRAY_PLASTIC_BLOCK.getItemStack(), MekanismBlock.LIGHT_GRAY_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(MekanismBlock.GRAY_PLASTIC_BLOCK.getItemStack(), MekanismBlock.GRAY_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(MekanismBlock.PINK_PLASTIC_BLOCK.getItemStack(), MekanismBlock.PINK_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(MekanismBlock.LIME_PLASTIC_BLOCK.getItemStack(), MekanismBlock.LIME_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(MekanismBlock.YELLOW_PLASTIC_BLOCK.getItemStack(), MekanismBlock.YELLOW_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(MekanismBlock.LIGHT_BLUE_PLASTIC_BLOCK.getItemStack(), MekanismBlock.LIGHT_BLUE_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(MekanismBlock.MAGENTA_PLASTIC_BLOCK.getItemStack(), MekanismBlock.MAGENTA_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(MekanismBlock.ORANGE_PLASTIC_BLOCK.getItemStack(), MekanismBlock.ORANGE_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(MekanismBlock.WHITE_PLASTIC_BLOCK.getItemStack(), MekanismBlock.WHITE_SLICK_PLASTIC_BLOCK.getItemStack());
        }

        //Combiner recipes
        if (MekanismBlock.COMBINER.isEnabled()) {
            RecipeHandler.addCombinerRecipe(new ItemStack(Items.FLINT), new ItemStack(Blocks.COBBLESTONE), new ItemStack(Blocks.GRAVEL));
            RecipeHandler.addCombinerRecipe(new ItemStack(Items.COAL, 3), new ItemStack(Blocks.COBBLESTONE), new ItemStack(Blocks.COAL_ORE));
        }

        //Osmium Compressor Recipes
        if (MekanismBlock.OSMIUM_COMPRESSOR.isEnabled()) {
            RecipeHandler.addOsmiumCompressorRecipe(new ItemStack(Items.GLOWSTONE_DUST), MekanismItem.REFINED_GLOWSTONE_INGOT.getItemStack());
        }

        //Crusher Recipes
        if (MekanismBlock.CRUSHER.isEnabled()) {
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.IRON_INGOT), MekanismItem.IRON_DUST.getItemStack());
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.GOLD_INGOT), MekanismItem.GOLD_DUST.getItemStack());
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.GRAVEL), new ItemStack(Blocks.SAND));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.STONE), new ItemStack(Blocks.COBBLESTONE));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.COBBLESTONE), new ItemStack(Blocks.GRAVEL));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.CRACKED_STONE_BRICKS), new ItemStack(Blocks.STONE));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.STONE_BRICKS), new ItemStack(Blocks.CRACKED_STONE_BRICKS));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.CHISELED_STONE_BRICKS), new ItemStack(Blocks.STONE_BRICKS));
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.FLINT), new ItemStack(Items.GUNPOWDER));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.SANDSTONE), new ItemStack(Blocks.SAND, 2));

            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.WHITE_WOOL), new ItemStack(Items.STRING, 4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.ORANGE_WOOL), new ItemStack(Items.STRING, 4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.MAGENTA_WOOL), new ItemStack(Items.STRING, 4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.LIGHT_BLUE_WOOL), new ItemStack(Items.STRING, 4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.YELLOW_WOOL), new ItemStack(Items.STRING, 4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.LIME_WOOL), new ItemStack(Items.STRING, 4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.PINK_WOOL), new ItemStack(Items.STRING, 4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.GRAY_WOOL), new ItemStack(Items.STRING, 4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.LIGHT_GRAY_WOOL), new ItemStack(Items.STRING, 4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.CYAN_WOOL), new ItemStack(Items.STRING, 4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.PURPLE_WOOL), new ItemStack(Items.STRING, 4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.BLUE_WOOL), new ItemStack(Items.STRING, 4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.BROWN_WOOL), new ItemStack(Items.STRING, 4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.GREEN_WOOL), new ItemStack(Items.STRING, 4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.RED_WOOL), new ItemStack(Items.STRING, 4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.BLACK_WOOL), new ItemStack(Items.STRING, 4));

            //BioFuel Crusher Recipes
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.GRASS), MekanismItem.BIO_FUEL.getItemStack(4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.SUGAR_CANE), MekanismItem.BIO_FUEL.getItemStack(2));
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.WHEAT_SEEDS), MekanismItem.BIO_FUEL.getItemStack(2));
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.WHEAT), MekanismItem.BIO_FUEL.getItemStack(4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.PUMPKIN_SEEDS), MekanismItem.BIO_FUEL.getItemStack(2));
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.MELON_SEEDS), MekanismItem.BIO_FUEL.getItemStack(2));
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.APPLE), MekanismItem.BIO_FUEL.getItemStack(4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.BREAD), MekanismItem.BIO_FUEL.getItemStack(4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.POTATO), MekanismItem.BIO_FUEL.getItemStack(4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.CARROT), MekanismItem.BIO_FUEL.getItemStack(4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.ROTTEN_FLESH), MekanismItem.BIO_FUEL.getItemStack(2));
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.MELON), MekanismItem.BIO_FUEL.getItemStack(4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.PUMPKIN), MekanismItem.BIO_FUEL.getItemStack(6));
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.BAKED_POTATO), MekanismItem.BIO_FUEL.getItemStack(4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.POISONOUS_POTATO), MekanismItem.BIO_FUEL.getItemStack(4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.BEETROOT), MekanismItem.BIO_FUEL.getItemStack(4));
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.BEETROOT_SEEDS), MekanismItem.BIO_FUEL.getItemStack(2));
            RecipeHandler.addCrusherRecipe(new ItemStack(Blocks.CACTUS), MekanismItem.BIO_FUEL.getItemStack(2));
        }

        //Purification Chamber Recipes
        if (MekanismBlock.PURIFICATION_CHAMBER.isEnabled()) {
            RecipeHandler.addPurificationChamberRecipe(new ItemStack(Blocks.GRAVEL), new ItemStack(Items.FLINT));
        }

        //Chemical Injection Chamber Recipes
        if (MekanismBlock.CHEMICAL_INJECTION_CHAMBER.isEnabled()) {
            RecipeHandler.addChemicalInjectionChamberRecipe(new ItemStack(Blocks.DIRT), MekanismFluids.Water, new ItemStack(Blocks.CLAY));
            RecipeHandler.addChemicalInjectionChamberRecipe(new ItemStack(Blocks.TERRACOTTA), MekanismFluids.Water, new ItemStack(Blocks.CLAY));
            RecipeHandler.addChemicalInjectionChamberRecipe(new ItemStack(Items.BRICK), MekanismFluids.Water, new ItemStack(Items.CLAY_BALL));
            RecipeHandler.addChemicalInjectionChamberRecipe(new ItemStack(Items.GUNPOWDER), MekanismFluids.HydrogenChloride, MekanismItem.SULFUR_DUST.getItemStack());
        }

        //Precision Sawmill Recipes
        if (MekanismBlock.PRECISION_SAWMILL.isEnabled()) {
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.LADDER, 3), new ItemStack(Items.STICK, 7));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.TORCH, 4), new ItemStack(Items.STICK), new ItemStack(Items.COAL), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.CHEST), new ItemStack(Blocks.OAK_PLANKS, 8));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.NOTE_BLOCK), new ItemStack(Blocks.OAK_PLANKS, 8), new ItemStack(Items.REDSTONE), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.REDSTONE_TORCH), new ItemStack(Items.STICK), new ItemStack(Items.REDSTONE), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.CRAFTING_TABLE), new ItemStack(Blocks.OAK_PLANKS, 4));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.JUKEBOX), new ItemStack(Blocks.OAK_PLANKS, 8), new ItemStack(Items.DIAMOND), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.BOOKSHELF), new ItemStack(Blocks.OAK_PLANKS, 6), new ItemStack(Items.BOOK, 3), 1);
            //Trapdoos
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.ACACIA_TRAPDOOR), new ItemStack(Blocks.ACACIA_PLANKS, 3));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.BIRCH_TRAPDOOR), new ItemStack(Blocks.BIRCH_PLANKS, 3));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.DARK_OAK_TRAPDOOR), new ItemStack(Blocks.DARK_OAK_PLANKS, 3));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.JUNGLE_TRAPDOOR), new ItemStack(Blocks.JUNGLE_PLANKS, 3));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.OAK_TRAPDOOR), new ItemStack(Blocks.OAK_PLANKS, 3));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.SPRUCE_TRAPDOOR), new ItemStack(Blocks.SPRUCE_PLANKS, 3));
            //Boats
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.ACACIA_BOAT), new ItemStack(Blocks.ACACIA_PLANKS, 5));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.BIRCH_BOAT), new ItemStack(Blocks.BIRCH_PLANKS, 5));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.DARK_OAK_BOAT), new ItemStack(Blocks.DARK_OAK_PLANKS, 5));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.JUNGLE_BOAT), new ItemStack(Blocks.JUNGLE_PLANKS, 5));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.OAK_BOAT), new ItemStack(Blocks.OAK_PLANKS, 5));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.SPRUCE_BOAT), new ItemStack(Blocks.SPRUCE_PLANKS, 5));
            //Beds
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.WHITE_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.WHITE_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.ORANGE_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.ORANGE_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.MAGENTA_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.MAGENTA_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.LIGHT_BLUE_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.LIGHT_BLUE_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.YELLOW_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.YELLOW_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.LIME_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.LIME_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.PINK_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.PINK_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.GRAY_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.GRAY_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.LIGHT_GRAY_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.LIGHT_GRAY_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.CYAN_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.CYAN_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.PURPLE_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.PURPLE_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.BLUE_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.BLUE_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.BROWN_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.BROWN_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.GREEN_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.GREEN_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.RED_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.RED_WOOL, 3), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.BLACK_BED), new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Blocks.BLACK_WOOL, 3), 1);
            //Doors
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.ACACIA_DOOR), new ItemStack(Blocks.ACACIA_PLANKS, 2));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.BIRCH_DOOR), new ItemStack(Blocks.BIRCH_PLANKS, 2));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.DARK_OAK_DOOR), new ItemStack(Blocks.DARK_OAK_PLANKS, 2));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.JUNGLE_DOOR), new ItemStack(Blocks.JUNGLE_PLANKS, 2));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.OAK_DOOR), new ItemStack(Blocks.OAK_PLANKS, 2));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Items.SPRUCE_DOOR), new ItemStack(Blocks.SPRUCE_PLANKS, 2));
            //Pressure Plates
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.ACACIA_PRESSURE_PLATE), new ItemStack(Blocks.ACACIA_PLANKS, 2));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.BIRCH_PRESSURE_PLATE), new ItemStack(Blocks.BIRCH_PLANKS, 2));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.DARK_OAK_PRESSURE_PLATE), new ItemStack(Blocks.DARK_OAK_PLANKS, 2));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.JUNGLE_PRESSURE_PLATE), new ItemStack(Blocks.JUNGLE_PLANKS, 2));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.OAK_PRESSURE_PLATE), new ItemStack(Blocks.OAK_PLANKS, 2));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.SPRUCE_PRESSURE_PLATE), new ItemStack(Blocks.SPRUCE_PLANKS, 2));
            //Fences
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.ACACIA_FENCE), new ItemStack(Items.STICK, 3));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.BIRCH_FENCE), new ItemStack(Items.STICK, 3));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.DARK_OAK_FENCE), new ItemStack(Items.STICK, 3));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.JUNGLE_FENCE), new ItemStack(Items.STICK, 3));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.OAK_FENCE), new ItemStack(Items.STICK, 3));
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.SPRUCE_FENCE), new ItemStack(Items.STICK, 3));
            //Fence Gates
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.ACACIA_FENCE_GATE), new ItemStack(Blocks.ACACIA_PLANKS, 2), new ItemStack(Items.STICK, 4), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.BIRCH_FENCE_GATE), new ItemStack(Blocks.BIRCH_PLANKS, 2), new ItemStack(Items.STICK, 4), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.DARK_OAK_FENCE_GATE), new ItemStack(Blocks.DARK_OAK_PLANKS, 2), new ItemStack(Items.STICK, 4), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.JUNGLE_FENCE_GATE), new ItemStack(Blocks.JUNGLE_PLANKS, 2), new ItemStack(Items.STICK, 4), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.OAK_FENCE_GATE), new ItemStack(Blocks.OAK_PLANKS, 2), new ItemStack(Items.STICK, 4), 1);
            RecipeHandler.addPrecisionSawmillRecipe(new ItemStack(Blocks.SPRUCE_FENCE_GATE), new ItemStack(Blocks.SPRUCE_PLANKS, 2), new ItemStack(Items.STICK, 4), 1);
        }

        if (MekanismBlock.METALLURGIC_INFUSER.isEnabled()) {
            InfuseType carbon = InfuseRegistry.get("CARBON");
            InfuseType bio = InfuseRegistry.get("BIO");
            InfuseType redstone = InfuseRegistry.get("REDSTONE");
            InfuseType fungi = InfuseRegistry.get("FUNGI");
            InfuseType diamond = InfuseRegistry.get("DIAMOND");
            InfuseType obsidian = InfuseRegistry.get("OBSIDIAN");

            //Infuse objects
            InfuseRegistry.registerInfuseObject(MekanismItem.BIO_FUEL.getItemStack(), new InfuseObject(bio, 5));
            InfuseRegistry.registerInfuseObject(new ItemStack(Items.CHARCOAL), new InfuseObject(carbon, 10));
            InfuseRegistry.registerInfuseObject(new ItemStack(Items.CHARCOAL), new InfuseObject(carbon, 20));
            InfuseRegistry.registerInfuseObject(new ItemStack(Blocks.COAL_BLOCK), new InfuseObject(carbon, 90));
            InfuseRegistry.registerInfuseObject(MekanismBlock.CHARCOAL_BLOCK.getItemStack(), new InfuseObject(carbon, 180));
            InfuseRegistry.registerInfuseObject(MekanismItem.COMPRESSED_CARBON.getItemStack(), new InfuseObject(carbon, 80));
            InfuseRegistry.registerInfuseObject(new ItemStack(Items.REDSTONE), new InfuseObject(redstone, 10));
            InfuseRegistry.registerInfuseObject(new ItemStack(Blocks.REDSTONE_BLOCK), new InfuseObject(redstone, 90));
            InfuseRegistry.registerInfuseObject(MekanismItem.COMPRESSED_REDSTONE.getItemStack(), new InfuseObject(redstone, 80));
            InfuseRegistry.registerInfuseObject(new ItemStack(Blocks.RED_MUSHROOM), new InfuseObject(fungi, 10));
            InfuseRegistry.registerInfuseObject(new ItemStack(Blocks.BROWN_MUSHROOM), new InfuseObject(fungi, 10));
            InfuseRegistry.registerInfuseObject(MekanismItem.COMPRESSED_DIAMOND.getItemStack(), new InfuseObject(diamond, 80));
            InfuseRegistry.registerInfuseObject(MekanismItem.COMPRESSED_OBSIDIAN.getItemStack(), new InfuseObject(obsidian, 80));

            //Metallurgic Infuser Recipes
            RecipeHandler.addMetallurgicInfuserRecipe(carbon, 10, new ItemStack(Items.IRON_INGOT), MekanismItem.ENRICHED_IRON.getItemStack());
            RecipeHandler.addMetallurgicInfuserRecipe(carbon, 10, MekanismItem.ENRICHED_IRON.getItemStack(), MekanismItem.STEEL_DUST.getItemStack());
            RecipeHandler.addMetallurgicInfuserRecipe(redstone, 10, new ItemStack(Items.IRON_INGOT), MekanismItem.ENRICHED_ALLOY.getItemStack());
            RecipeHandler.addMetallurgicInfuserRecipe(fungi, 10, new ItemStack(Blocks.DIRT), new ItemStack(Blocks.MYCELIUM));
            RecipeHandler.addMetallurgicInfuserRecipe(bio, 10, new ItemStack(Blocks.COBBLESTONE), new ItemStack(Blocks.MOSSY_COBBLESTONE));
            RecipeHandler.addMetallurgicInfuserRecipe(bio, 10, new ItemStack(Blocks.STONE_BRICKS), new ItemStack(Blocks.MOSSY_STONE_BRICKS));
            RecipeHandler.addMetallurgicInfuserRecipe(bio, 10, new ItemStack(Blocks.SAND), new ItemStack(Blocks.DIRT));
            RecipeHandler.addMetallurgicInfuserRecipe(bio, 10, new ItemStack(Blocks.DIRT), new ItemStack(Blocks.PODZOL));
            RecipeHandler.addMetallurgicInfuserRecipe(diamond, 10, MekanismItem.ENRICHED_ALLOY.getItemStack(), MekanismItem.REINFORCED_ALLOY.getItemStack());
            RecipeHandler.addMetallurgicInfuserRecipe(obsidian, 10, MekanismItem.REINFORCED_ALLOY.getItemStack(), MekanismItem.ATOMIC_ALLOY.getItemStack());
        }

        //Chemical Infuser Recipes
        if (MekanismBlock.CHEMICAL_INFUSER.isEnabled()) {
            RecipeHandler.addChemicalInfuserRecipe(new GasStack(MekanismFluids.Oxygen, 1), new GasStack(MekanismFluids.SulfurDioxide, 2), new GasStack(MekanismFluids.SulfurTrioxide, 2));
            RecipeHandler.addChemicalInfuserRecipe(new GasStack(MekanismFluids.SulfurTrioxide, 1), new GasStack(MekanismFluids.Water, 1), new GasStack(MekanismFluids.SulfuricAcid, 1));
            RecipeHandler.addChemicalInfuserRecipe(new GasStack(MekanismFluids.Hydrogen, 1), new GasStack(MekanismFluids.Chlorine, 1), new GasStack(MekanismFluids.HydrogenChloride, 1));
            RecipeHandler.addChemicalInfuserRecipe(new GasStack(MekanismFluids.Deuterium, 1), new GasStack(MekanismFluids.Tritium, 1), new GasStack(MekanismFluids.FusionFuel, 2));
        }

        //Electrolytic Separator Recipes
        if (MekanismBlock.ELECTROLYTIC_SEPARATOR.isEnabled()) {
            RecipeHandler.addElectrolyticSeparatorRecipe(FluidRegistry.getFluidStack("water", 2), 2 * MekanismConfig.general.FROM_H2.get(),
                  new GasStack(MekanismFluids.Hydrogen, 2), new GasStack(MekanismFluids.Oxygen, 1));
            RecipeHandler.addElectrolyticSeparatorRecipe(FluidRegistry.getFluidStack("brine", 10), 2 * MekanismConfig.general.FROM_H2.get(),
                  new GasStack(MekanismFluids.Sodium, 1), new GasStack(MekanismFluids.Chlorine, 1));
            RecipeHandler.addElectrolyticSeparatorRecipe(FluidRegistry.getFluidStack("heavywater", 2), MekanismConfig.usage.heavyWaterElectrolysis.get(),
                  new GasStack(MekanismFluids.Deuterium, 2), new GasStack(MekanismFluids.Oxygen, 1));
        }

        //Thermal Evaporation Plant Recipes
        RecipeHandler.addThermalEvaporationRecipe(FluidRegistry.getFluidStack("water", 10), FluidRegistry.getFluidStack("brine", 1));
        RecipeHandler.addThermalEvaporationRecipe(FluidRegistry.getFluidStack("brine", 10), FluidRegistry.getFluidStack("liquidlithium", 1));

        //Chemical Crystallizer Recipes
        if (MekanismBlock.CHEMICAL_CRYSTALLIZER.isEnabled()) {
            RecipeHandler.addChemicalCrystallizerRecipe(new GasStack(MekanismFluids.Lithium, 100), MekanismItem.SULFUR_DUST.getItemStack());
            RecipeHandler.addChemicalCrystallizerRecipe(new GasStack(MekanismFluids.Brine, 15), MekanismItem.SALT.getItemStack());
        }

        //T4 Processing Recipes
        for (Gas gas : GasRegistry.getRegisteredGasses()) {
            if (gas instanceof OreGas && !((OreGas) gas).isClean()) {
                OreGas oreGas = (OreGas) gas;
                if (MekanismBlock.CHEMICAL_WASHER.isEnabled()) {
                    RecipeHandler.addChemicalWasherRecipe(new GasStack(oreGas, 1), new GasStack(oreGas.getCleanGas(), 1));
                }

                if (MekanismBlock.CHEMICAL_CRYSTALLIZER.isEnabled()) {
                    //do the crystallizer only if it's one of our gases!
                    Resource gasResource = Resource.getFromName(oreGas.getName());
                    if (gasResource != null) {
                        //TODO: Better way to do this
                        MekanismItem crystal = null;
                        switch (gasResource) {
                            case IRON:
                                crystal = MekanismItem.IRON_CRYSTAL;
                                break;
                            case GOLD:
                                crystal = MekanismItem.GOLD_CRYSTAL;
                                break;
                            case OSMIUM:
                                crystal = MekanismItem.OSMIUM_CRYSTAL;
                                break;
                            case COPPER:
                                crystal = MekanismItem.COPPER_CRYSTAL;
                                break;
                            case TIN:
                                crystal = MekanismItem.TIN_CRYSTAL;
                                break;
                            case SILVER:
                                crystal = MekanismItem.SILVER_CRYSTAL;
                                break;
                            case LEAD:
                                crystal = MekanismItem.LEAD_CRYSTAL;
                                break;
                        }
                        RecipeHandler.addChemicalCrystallizerRecipe(new GasStack(oreGas.getCleanGas(), 200), crystal.getItemStack());
                    }
                }
            }
        }

        //Pressurized Reaction Chamber Recipes
        if (MekanismBlock.PRESSURIZED_REACTION_CHAMBER.isEnabled()) {
            RecipeHandler.addPRCRecipe(MekanismItem.SUBSTRATE.getItemStack(2), new FluidStack(FluidRegistry.WATER, 10), new GasStack(MekanismFluids.Hydrogen, 100),
                  MekanismItem.SUBSTRATE.getItemStack(), new GasStack(MekanismFluids.Ethene, 100), 0, 100);
            RecipeHandler.addPRCRecipe(MekanismItem.SUBSTRATE.getItemStack(), new FluidStack(MekanismFluids.Ethene.getFluid(), 50),
                  new GasStack(MekanismFluids.Oxygen, 10), MekanismItem.HDPE_PELLET.getItemStack(), new GasStack(MekanismFluids.Oxygen, 5), 1000, 60);
            RecipeHandler.addPRCRecipe(MekanismItem.SUBSTRATE.getItemStack(), new FluidStack(FluidRegistry.WATER, 200), new GasStack(MekanismFluids.Ethene, 100),
                  MekanismItem.SUBSTRATE.getItemStack(8), new GasStack(MekanismFluids.Oxygen, 10), 200, 400);
            RecipeHandler.addPRCRecipe(new ItemStack(Items.COAL), new FluidStack(FluidRegistry.WATER, 100), new GasStack(MekanismFluids.Oxygen, 100),
                  MekanismItem.SULFUR_DUST.getItemStack(), new GasStack(MekanismFluids.Hydrogen, 100), 0, 100);
            RecipeHandler.addPRCRecipe(new ItemStack(Items.CHARCOAL), new FluidStack(FluidRegistry.WATER, 100), new GasStack(MekanismFluids.Oxygen, 100),
                  MekanismItem.SULFUR_DUST.getItemStack(), new GasStack(MekanismFluids.Hydrogen, 100), 0, 100);
        }

        //Solar Neutron Activator Recipes
        if (MekanismBlock.SOLAR_NEUTRON_ACTIVATOR.isEnabled()) {
            RecipeHandler.addSolarNeutronRecipe(new GasStack(MekanismFluids.Lithium, 1), new GasStack(MekanismFluids.Tritium, 1));
        }

        //Fuel Gases
        FuelHandler.addGas(MekanismFluids.Hydrogen, 1, MekanismConfig.general.FROM_H2.get());
    }

    public static void registerTileEntities(IBlockProvider[] blockProviders) {
        Set<Class<? extends TileEntity>> alreadyAdded = new HashSet<>();
        for (IBlockProvider blockProvider : blockProviders) {
            Block block = blockProvider.getBlock();
            if (block instanceof IHasTileEntity<?>) {
                IHasTileEntity<?> tileHolder = (IHasTileEntity<?>) block;
                Class<? extends TileEntity> tileClass = tileHolder.getTileClass();
                if (tileClass != null) {
                    if (tileHolder.hasMultipleBlocks()) {
                        if (!alreadyAdded.contains(tileClass)) {
                            alreadyAdded.add(tileClass);
                            GameRegistry.registerTileEntity(tileClass, new ResourceLocation(MODID, tileHolder.getTileName()));
                        }
                    } else {
                        GameRegistry.registerTileEntity(tileClass, new ResourceLocation(MODID, blockProvider.getName()));
                    }
                }
            }
        }
    }

    /**
     * Adds and registers all tile entities.
     */
    private void registerTileEntities() {
        registerTileEntities(MekanismBlock.values());
        //TODO: Make block for advanced bounding?
        GameRegistry.registerTileEntity(TileEntityAdvancedBoundingBlock.class, new ResourceLocation(MODID, "advanced_bounding_block"));

        //Register the TESRs
        proxy.registerTESRs();

        MekanismDataFixers.register();
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        if (MekanismConfig.general.voiceServerEnabled.get()) {
            voiceManager.start();
        }
        CommandMek.register(event);
    }

    @EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
        if (MekanismConfig.general.voiceServerEnabled.get()) {
            voiceManager.stop();
        }

        //Clear all cache data
        playerState.clear();
        activeVibrators.clear();
        worldTickHandler.resetRegenChunks();
        privateTeleporters.clear();
        privateEntangloporters.clear();
        freeRunnerOn.clear();

        //Reset consistent managers
        MultiblockManager.reset();
        FrequencyManager.reset();
        TransporterManager.reset();
        PathfinderCache.reset();
        TransmitterNetworkRegistry.reset();
    }

    @EventHandler
    public void loadComplete(FMLInterModComms.IMCEvent event) {
        new IMCHandler().onIMCEvent(event.getMessages());
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        //sanity check the api location if not deobf
        if (!((Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment"))) {
            String apiLocation = MekanismAPI.class.getProtectionDomain().getCodeSource().getLocation().toString();
            if (apiLocation.toLowerCase(Locale.ROOT).contains("-api.jar")) {
                proxy.throwApiPresentException();
            }
        }

        File config = event.getSuggestedConfigurationFile();

        //Set the mod's configuration
        configuration = new Configuration(config);

        //Load configuration
        proxy.loadConfiguration();
        proxy.onConfigSync(false);

        MinecraftForge.EVENT_BUS.register(MekanismItem.GAS_MASK.getItem());
        MinecraftForge.EVENT_BUS.register(MekanismItem.FREE_RUNNERS.getItem());

        if (ModList.get().isLoaded("mcmultipart")) {
            //Set up multiparts
            new MultipartMekanism();
        } else {
            logger.info("Didn't detect MCMP, ignoring compatibility package");
        }

        Mekanism.proxy.preInit();

        //Register infuses
        InfuseRegistry.registerInfuseType(new InfuseType("CARBON", new ResourceLocation(Mekanism.MODID, "blocks/infuse/Carbon")).setTranslationKey("carbon"));
        InfuseRegistry.registerInfuseType(new InfuseType("TIN", new ResourceLocation(Mekanism.MODID, "blocks/infuse/Tin")).setTranslationKey("tin"));
        InfuseRegistry.registerInfuseType(new InfuseType("DIAMOND", new ResourceLocation(Mekanism.MODID, "blocks/infuse/Diamond")).setTranslationKey("diamond"));
        InfuseRegistry.registerInfuseType(new InfuseType("REDSTONE", new ResourceLocation(Mekanism.MODID, "blocks/infuse/Redstone")).setTranslationKey("redstone"));
        InfuseRegistry.registerInfuseType(new InfuseType("FUNGI", new ResourceLocation(Mekanism.MODID, "blocks/infuse/Fungi")).setTranslationKey("fungi"));
        InfuseRegistry.registerInfuseType(new InfuseType("BIO", new ResourceLocation(Mekanism.MODID, "blocks/infuse/Bio")).setTranslationKey("bio"));
        InfuseRegistry.registerInfuseType(new InfuseType("OBSIDIAN", new ResourceLocation(Mekanism.MODID, "blocks/infuse/Obsidian")).setTranslationKey("obsidian"));

        Capabilities.registerCapabilities();

        hooks.hookPreInit();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        //Register the mod's world generators
        GameRegistry.registerWorldGenerator(genHandler, 1);

        //Register the mod's GUI handler
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new CoreGuiHandler());

        //Register player tracker
        MinecraftForge.EVENT_BUS.register(new CommonPlayerTracker());
        MinecraftForge.EVENT_BUS.register(new CommonPlayerTickHandler());

        //Initialization notification
        logger.info("Version " + versionNumber + " initializing...");

        //Register with ForgeChunkManager
        ForgeChunkManager.setForcedChunkLoadingCallback(this, new ChunkManager());

        //Register to receive subscribed events
        MinecraftForge.EVENT_BUS.register(this);

        //Register this module's GUI handler in the simple packet protocol
        PacketSimpleGui.handlers.add(0, proxy);

        //Set up VoiceServerManager
        if (MekanismConfig.general.voiceServerEnabled.get()) {
            voiceManager = new VoiceServerManager();
        }

        //Register with TransmitterNetworkRegistry
        TransmitterNetworkRegistry.initiate();

        //Add baby skeleton spawner
        if (MekanismConfig.general.spawnBabySkeletons.get()) {
            for (Biome biome : BiomeProvider.BIOMES_TO_SPAWN_IN) {
                if (biome.getSpawns(EntityClassification.MONSTER).size() > 0) {
                    EntityRegistry.addSpawn(EntityBabySkeleton.class, 40, 1, 3, EntityClassification.MONSTER, biome);
                }
            }
        }

        //Load this module
        registerTileEntities();

        hooks.hookInit();

        //Packet registrations
        packetHandler.initialize();

        //Load proxy
        proxy.init();

        //Completion notification
        logger.info("Loading complete.");

        //Success message
        logger.info("Mod loaded.");
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        logger.info("Fake player readout: UUID = " + gameProfile.getId().toString() + ", name = " + gameProfile.getName());

        // Add all furnace recipes to the energized smelter
        // Must happen after CraftTweaker for vanilla stuff has run.
        for (Entry<ItemStack, ItemStack> entry : FurnaceRecipes.instance().getSmeltingList().entrySet()) {
            SmeltingRecipe recipe = new SmeltingRecipe(new ItemStackInput(entry.getKey()), new ItemStackOutput(entry.getValue()));
            Recipe.ENERGIZED_SMELTER.put(recipe);
        }

        hooks.hookPostInit();

        MinecraftForge.EVENT_BUS.post(new BoxBlacklistEvent());

        logger.info("Hooking complete.");
    }

    @SubscribeEvent
    public void onEnergyTransferred(EnergyTransferEvent event) {
        try {
            packetHandler.sendToReceivers(new PacketTransmitterUpdate(PacketType.ENERGY, event.energyNetwork.firstTransmitter().coord(), event.power),
                  event.energyNetwork.getPacketRange());
        } catch (Exception ignored) {
        }
    }

    @SubscribeEvent
    public void onGasTransferred(GasTransferEvent event) {
        try {
            packetHandler.sendToReceivers(new PacketTransmitterUpdate(PacketType.GAS, event.gasNetwork.firstTransmitter().coord(), event.transferType, event.didTransfer),
                  event.gasNetwork.getPacketRange());
        } catch (Exception ignored) {
        }
    }

    @SubscribeEvent
    public void onLiquidTransferred(FluidTransferEvent event) {
        try {
            packetHandler.sendToReceivers(new PacketTransmitterUpdate(PacketType.FLUID, event.fluidNetwork.firstTransmitter().coord(), event.fluidType, event.didTransfer),
                  event.fluidNetwork.getPacketRange());
        } catch (Exception ignored) {
        }
    }

    @SubscribeEvent
    public void onTransmittersAddedEvent(TransmittersAddedEvent event) {
        try {
            packetHandler.sendToReceivers(new PacketTransmitterUpdate(PacketType.UPDATE, event.network.firstTransmitter().coord(), event.newNetwork, event.newTransmitters),
                  event.network.getPacketRange());
        } catch (Exception ignored) {
        }
    }

    @SubscribeEvent
    public void onNetworkClientRequest(NetworkClientRequest event) {
        try {
            packetHandler.sendToServer(new PacketDataRequest(Coord4D.get(event.tileEntity)));
        } catch (Exception ignored) {
        }
    }

    @SubscribeEvent
    public void onClientTickUpdate(ClientTickUpdate event) {
        try {
            if (event.operation == 0) {
                ClientTickHandler.tickingSet.remove(event.network);
            } else {
                ClientTickHandler.tickingSet.add(event.network);
            }
        } catch (Exception ignored) {
        }
    }

    @SubscribeEvent
    public void onBlacklistUpdate(BoxBlacklistEvent event) {
        event.blacklist(MekanismBlock.CARDBOARD_BOX.getBlock());

        // Mekanism multiblock structures
        event.blacklist(MekanismBlock.BOUNDING_BLOCK.getBlock());
        event.blacklist(MekanismBlock.SECURITY_DESK.getBlock());
        event.blacklist(MekanismBlock.DIGITAL_MINER.getBlock());
        event.blacklist(MekanismBlock.SEISMIC_VIBRATOR.getBlock());
        event.blacklist(MekanismBlock.SOLAR_NEUTRON_ACTIVATOR.getBlock());

        // Minecraft unobtainable
        event.blacklist(Blocks.BEDROCK);
        event.blacklist(Blocks.NETHER_PORTAL);
        event.blacklist(Blocks.END_PORTAL);
        event.blacklist(Blocks.END_PORTAL_FRAME);

        // Minecraft multiblock structures
        event.blacklist(Blocks.WHITE_BED);
        event.blacklist(Blocks.ORANGE_BED);
        event.blacklist(Blocks.MAGENTA_BED);
        event.blacklist(Blocks.LIGHT_BLUE_BED);
        event.blacklist(Blocks.YELLOW_BED);
        event.blacklist(Blocks.LIME_BED);
        event.blacklist(Blocks.PINK_BED);
        event.blacklist(Blocks.GRAY_BED);
        event.blacklist(Blocks.LIGHT_GRAY_BED);
        event.blacklist(Blocks.CYAN_BED);
        event.blacklist(Blocks.PURPLE_BED);
        event.blacklist(Blocks.BLUE_BED);
        event.blacklist(Blocks.BROWN_BED);
        event.blacklist(Blocks.GREEN_BED);
        event.blacklist(Blocks.RED_BED);
        event.blacklist(Blocks.BLACK_BED);
        event.blacklist(Blocks.OAK_DOOR);
        event.blacklist(Blocks.SPRUCE_DOOR);
        event.blacklist(Blocks.BIRCH_DOOR);
        event.blacklist(Blocks.JUNGLE_DOOR);
        event.blacklist(Blocks.ACACIA_DOOR);
        event.blacklist(Blocks.DARK_OAK_DOOR);
        event.blacklist(Blocks.IRON_DOOR);

        //Extra Utils 2
        event.blacklist(new ResourceLocation("extrautils2", "machine"));

        //ImmEng multiblocks
        event.blacklist(new ResourceLocation("immersiveengineering", "metal_device0"));
        event.blacklist(new ResourceLocation("immersiveengineering", "metal_device1"));
        event.blacklist(new ResourceLocation("immersiveengineering", "wooden_device0"));
        event.blacklist(new ResourceLocation("immersiveengineering", "wooden_device1"));
        event.blacklist(new ResourceLocation("immersiveengineering", "connector"));
        event.blacklist(new ResourceLocation("immersiveengineering", "metal_multiblock"));

        //IC2
        event.blacklist(new ResourceLocation("ic2", "te"));

        event.blacklistMod("storagedrawers");//without packing tape, you're gonna have a bad time
        event.blacklistMod("colossalchests");

        BoxBlacklistParser.load();
    }

    @SubscribeEvent
    public void chunkSave(ChunkDataEvent.Save event) {
        if (!event.getWorld().isRemote()) {
            CompoundNBT nbtTags = event.getData();

            nbtTags.putInt("MekanismWorldGen", baseWorldGenVersion);
            nbtTags.putInt("MekanismUserWorldGen", MekanismConfig.general.userWorldGenVersion.get());
        }
    }

    @SubscribeEvent
    public synchronized void onChunkDataLoad(ChunkDataEvent.Load event) {
        if (!event.getWorld().isRemote()) {
            if (MekanismConfig.general.enableWorldRegeneration.get()) {
                CompoundNBT loadData = event.getData();
                if (loadData.getInt("MekanismWorldGen") == baseWorldGenVersion &&
                    loadData.getInt("MekanismUserWorldGen") == MekanismConfig.general.userWorldGenVersion.get()) {
                    return;
                }
                ChunkPos coordPair = event.getChunk().getPos();
                //TODO: Is this correct
                worldTickHandler.addRegenChunk(event.getWorld().getDimension().getType().getId(), coordPair);
            }
        }
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(Mekanism.MODID)) {
            proxy.loadConfiguration();
            proxy.onConfigSync(false);
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        playerState.init(event.getWorld());
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        // Make sure the global fake player drops its reference to the World
        // when the server shuts down
        if (event.getWorld() instanceof ServerWorld) {
            MekFakePlayer.releaseInstance(event.getWorld());
        }
    }
}
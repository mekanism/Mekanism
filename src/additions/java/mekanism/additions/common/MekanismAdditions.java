package mekanism.additions.common;

import mekanism.additions.client.AdditionsClient;
import mekanism.additions.common.config.MekanismAdditionsConfig;
import mekanism.additions.common.voice.VoiceServerManager;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlock;
import mekanism.common.Version;
import mekanism.common.base.IModule;
import mekanism.common.recipe.RecipeHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
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
    public static Version versionNumber = new Version(999, 999, 999);

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

        MekanismAdditionsConfig.loadFromFiles();
        Mekanism.logger.info("Loaded 'Mekanism: Additions' module.");
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
        if (MekanismBlock.ENRICHMENT_CHAMBER.isEnabled()) {
            //Plastic to Slick Plastic
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(AdditionsBlock.BLACK_PLASTIC_BLOCK), AdditionsBlock.BLACK_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(AdditionsBlock.RED_PLASTIC_BLOCK), AdditionsBlock.RED_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(AdditionsBlock.GREEN_PLASTIC_BLOCK), AdditionsBlock.GREEN_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(AdditionsBlock.BROWN_PLASTIC_BLOCK), AdditionsBlock.BROWN_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(AdditionsBlock.BLUE_PLASTIC_BLOCK), AdditionsBlock.BLUE_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(AdditionsBlock.PURPLE_PLASTIC_BLOCK), AdditionsBlock.PURPLE_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(AdditionsBlock.CYAN_PLASTIC_BLOCK), AdditionsBlock.CYAN_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(AdditionsBlock.LIGHT_GRAY_PLASTIC_BLOCK), AdditionsBlock.LIGHT_GRAY_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(AdditionsBlock.GRAY_PLASTIC_BLOCK), AdditionsBlock.GRAY_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(AdditionsBlock.PINK_PLASTIC_BLOCK), AdditionsBlock.PINK_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(AdditionsBlock.LIME_PLASTIC_BLOCK), AdditionsBlock.LIME_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(AdditionsBlock.YELLOW_PLASTIC_BLOCK), AdditionsBlock.YELLOW_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(AdditionsBlock.LIGHT_BLUE_PLASTIC_BLOCK), AdditionsBlock.LIGHT_BLUE_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(AdditionsBlock.MAGENTA_PLASTIC_BLOCK), AdditionsBlock.MAGENTA_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(AdditionsBlock.ORANGE_PLASTIC_BLOCK), AdditionsBlock.ORANGE_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(AdditionsBlock.WHITE_PLASTIC_BLOCK), AdditionsBlock.WHITE_SLICK_PLASTIC_BLOCK.getItemStack());
        }

        //Set up VoiceServerManager
        if (MekanismAdditionsConfig.additions.voiceServerEnabled.get()) {
            voiceManager = new VoiceServerManager();
        }
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
}
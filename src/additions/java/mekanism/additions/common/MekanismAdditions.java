package mekanism.additions.common;

import mekanism.additions.common.config.MekanismAdditionsConfig;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlock;
import mekanism.common.Version;
import mekanism.common.base.IModule;
import mekanism.common.recipe.RecipeHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MekanismAdditions.MODID)
public class MekanismAdditions implements IModule {

    public static final String MODID = "mekanismadditions";

    public static MekanismAdditions instance;

    /**
     * MekanismTools version number
     */
    public static Version versionNumber = new Version(999, 999, 999);

    public MekanismAdditions() {
        Mekanism.modulesLoaded.add(instance = this);
        MekanismAdditionsConfig.registerConfigs(ModLoadingContext.get());

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        //TODO: Move voice server to MekanismAdditions
        modEventBus.addListener(this::commonSetup);
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
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        if (MekanismBlock.ENRICHMENT_CHAMBER.isEnabled()) {
            //Plastic to Slick Plastic
            RecipeHandler.addEnrichmentChamberRecipe(AdditionsBlock.BLACK_PLASTIC_BLOCK.getItemStack(), AdditionsBlock.BLACK_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(AdditionsBlock.RED_PLASTIC_BLOCK.getItemStack(), AdditionsBlock.RED_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(AdditionsBlock.GREEN_PLASTIC_BLOCK.getItemStack(), AdditionsBlock.GREEN_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(AdditionsBlock.BROWN_PLASTIC_BLOCK.getItemStack(), AdditionsBlock.BROWN_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(AdditionsBlock.BLUE_PLASTIC_BLOCK.getItemStack(), AdditionsBlock.BLUE_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(AdditionsBlock.PURPLE_PLASTIC_BLOCK.getItemStack(), AdditionsBlock.PURPLE_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(AdditionsBlock.CYAN_PLASTIC_BLOCK.getItemStack(), AdditionsBlock.CYAN_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(AdditionsBlock.LIGHT_GRAY_PLASTIC_BLOCK.getItemStack(), AdditionsBlock.LIGHT_GRAY_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(AdditionsBlock.GRAY_PLASTIC_BLOCK.getItemStack(), AdditionsBlock.GRAY_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(AdditionsBlock.PINK_PLASTIC_BLOCK.getItemStack(), AdditionsBlock.PINK_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(AdditionsBlock.LIME_PLASTIC_BLOCK.getItemStack(), AdditionsBlock.LIME_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(AdditionsBlock.YELLOW_PLASTIC_BLOCK.getItemStack(), AdditionsBlock.YELLOW_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(AdditionsBlock.LIGHT_BLUE_PLASTIC_BLOCK.getItemStack(), AdditionsBlock.LIGHT_BLUE_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(AdditionsBlock.MAGENTA_PLASTIC_BLOCK.getItemStack(), AdditionsBlock.MAGENTA_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(AdditionsBlock.ORANGE_PLASTIC_BLOCK.getItemStack(), AdditionsBlock.ORANGE_SLICK_PLASTIC_BLOCK.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(AdditionsBlock.WHITE_PLASTIC_BLOCK.getItemStack(), AdditionsBlock.WHITE_SLICK_PLASTIC_BLOCK.getItemStack());
        }
    }
}
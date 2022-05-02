package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.bracket.CommandStringDisplayable;
import java.util.Locale;
import java.util.function.Function;
import javax.annotation.Nonnull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.math.FloatingLong;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTInfusionStack;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTPigmentStack;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTSlurryStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTInfusionStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTPigmentStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTSlurryStack;
import mekanism.common.integration.crafttweaker.example.BaseCrTExampleProvider;
import mekanism.common.integration.crafttweaker.example.component.CrTImportsComponent;
import mekanism.common.integration.crafttweaker.example.component.ICrTExampleComponent;
import mekanism.common.integration.crafttweaker.recipe.manager.ChemicalCrystallizerRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.ChemicalDissolutionRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.ChemicalInfuserRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.CombinerRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.ElectrolysisRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.FluidSlurryToSlurryRecipeManager.ChemicalWasherRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.FluidToFluidRecipeManager.EvaporatingRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.GasToGasRecipeManager.IsotopicCentrifugeRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.GasToGasRecipeManager.SolarNeutronActivatorRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.ItemStackChemicalToItemStackRecipeManager.ChemicalInjectionRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.ItemStackChemicalToItemStackRecipeManager.MetallurgicInfuserRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.ItemStackChemicalToItemStackRecipeManager.OsmiumCompressorRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.ItemStackChemicalToItemStackRecipeManager.PaintingRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.ItemStackChemicalToItemStackRecipeManager.PurificationRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.ItemStackToEnergyRecipeManager.EnergyConversionRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.ItemStackToGasRecipeManager.ChemicalOxidizerRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.ItemStackToGasRecipeManager.GasConversionRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.ItemStackToInfuseTypeRecipeManager.InfusionConversionRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.ItemStackToItemStackRecipeManager.CrusherRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.ItemStackToItemStackRecipeManager.EnergizedSmelterRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.ItemStackToItemStackRecipeManager.EnrichmentChamberRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.ItemStackToPigmentRecipeManager.PigmentExtractingRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.NucleosynthesizingRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.PigmentMixingRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.PressurizedReactionRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.RotaryRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.SawmillRecipeManager;
import mekanism.common.registration.impl.SlurryRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismGases;
import mekanism.common.registries.MekanismInfuseTypes;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismPigments;
import mekanism.common.registries.MekanismSlurries;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.ChemicalUtil;
import net.minecraft.data.DataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fluids.FluidAttributes;

public class MekanismCrTExampleProvider extends BaseCrTExampleProvider {

    private static final String EXPANSION_TARGET_JEITWEAKER = "mods.jei.JEI";

    public MekanismCrTExampleProvider(DataGenerator gen, ExistingFileHelper existingFileHelper) {
        super(gen, existingFileHelper, Mekanism.MODID);
    }

    @Override
    protected void addExamples() {
        //Recipes
        addRecipeExamples();
        //Content
        exampleBuilder("mekanism_custom_chemicals")
              .addComponent(() -> "#loader " + CrTConstants.CONTENT_LOADER)
              .blankLine()
              .imports()
              .comment("Adds five very simple chemicals to show a very basic usage of the content creation capabilities provided. Custom content needs to be created "
                       + "in the mekanismcontent loader and requires a full game restart to take effect as well as have names defined in a lang file. One thing to note "
                       + "is that these examples are extremely basic and there is quite a bit more that is possible with this system including using custom textures and "
                       + "adding various attributes.",
                    "1) Creates an example Gas that is colored magenta.",
                    "2) Creates an example Infuse Type that is colored green.",
                    "3) Creates an example Pigment that is colored yellowish green.",
                    "4) Creates an example Dirty Slurry that is for a yellow ore.",
                    "5) Creates an example Clean Slurry that is for the same yellow ore."
              ).blankLine()
              .addComponent(imports -> new SimpleCustomChemicalComponent(imports.addImport(CrTConstants.CLASS_BUILDER_GAS), "example_gas", 0xDF03FC))
              .addComponent(imports -> new SimpleCustomChemicalComponent(imports.addImport(CrTConstants.CLASS_BUILDER_INFUSE_TYPE), "example_infuse_type", 0x03FC0B))
              .addComponent(imports -> new SimpleCustomChemicalComponent(imports.addImport(CrTConstants.CLASS_BUILDER_PIGMENT), "example_pigment", 0xCAFC03))
              .addComponent(imports -> new SimpleCustomChemicalComponent(imports.addImport(CrTConstants.CLASS_BUILDER_SLURRY), "dirty", "example_dirty_slurry", 0xF0FC03))
              .addComponent(imports -> new SimpleCustomChemicalComponent(imports.addImport(CrTConstants.CLASS_BUILDER_SLURRY), "clean", "example_clean_slurry", 0xF0FC03))
        ;
        //JEITweaker integration
        exampleBuilder("mekanism_jeitweaker_integration")
              .addComponent(() -> "#modloaded " + MekanismHooks.JEITWEAKER_MOD_ID)
              .blankLine()
              .imports()
              .comment("If JEITweaker is installed, Mekanism will add integration with it that allows for hiding our chemicals, and adding descriptions to them.")
              .blankLine()
              .comment("Hides four chemicals (one of each type: Gas, Infuse Type, Pigment, Slurry) from JEI:",
                    "1) Hides gaseous brine",
                    "2) Hides the bio infuse type",
                    "3) Hides dark red pigment",
                    "4) Hides clean copper slurry"
              ).blankLine()
              .comment(imports -> hideSignature(imports, ICrTGasStack.class))
              .comment(imports -> hideSignature(imports, ICrTInfusionStack.class))
              .comment(imports -> hideSignature(imports, ICrTPigmentStack.class))
              .comment(imports -> hideSignature(imports, ICrTSlurryStack.class))
              .blankLine()
              .addComponent(imports -> new JEIHidingComponent<>(imports, MekanismGases.BRINE, CrTGasStack::new))
              .addComponent(imports -> new JEIHidingComponent<>(imports, MekanismInfuseTypes.BIO, CrTInfusionStack::new))
              .addComponent(imports -> new JEIHidingComponent<>(imports, MekanismPigments.PIGMENT_COLOR_LOOKUP.get(EnumColor.DARK_RED), CrTPigmentStack::new))
              .addComponent(imports -> new JEIHidingComponent<>(imports, MekanismSlurries.PROCESSED_RESOURCES.get(PrimaryResource.GOLD).getCleanSlurry(), CrTSlurryStack::new))
              .blankLine()
              .comment("Adds a description to the passed in chemical. This example adds some basic text to JEI's information tab when looking at Hydrogen.")
              .blankLine()
              .comment(imports -> descriptionSignature(imports, ICrTGasStack.class))
              .comment(imports -> descriptionSignature(imports, ICrTInfusionStack.class))
              .comment(imports -> descriptionSignature(imports, ICrTPigmentStack.class))
              .comment(imports -> descriptionSignature(imports, ICrTSlurryStack.class))
              .blankLine()
              .addComponent(imports -> () -> imports.addImport(EXPANSION_TARGET_JEITWEAKER) + ".addDescription(" +
                                             new CrTGasStack(MekanismGases.HYDROGEN.getStack(FluidAttributes.BUCKET_VOLUME)).getCommandString() +
                                             ", \"Hydrogen is a basic gas that is produced in an electrolytic separator\");")
        ;
    }

    private void addRecipeExamples() {
        exampleBuilder("mekanism_crystallizer")
              .comment("Adds two Crystallizing Recipes that do the following:",
                    "1) Adds a recipe that produces one Osmium Ingot out of 200 mB of Osmium.",
                    "2) Adds a recipe that produces one Gold Nugget out of 9 mB of the Gold Infuse Type."
              ).blankLine()
              .recipe(ChemicalCrystallizerRecipeManager.INSTANCE)
              .addExample("osmium_ingotification", IngredientCreatorAccess.gas().from(MekanismGases.OSMIUM, 200),
                    new ItemStack(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM)))
              .addExample("gold_infusion_to_gold", IngredientCreatorAccess.infusion().from(MekanismTags.InfuseTypes.GOLD, 9), new ItemStack(Items.GOLD_NUGGET))
              .end()
              .comment("Removes two Crystallizing Recipes:",
                    "1) The recipe for producing Lithium Dust.",
                    "2) The recipe for producing Antimatter Pellets."
              ).blankLine()
              .removeRecipes(ChemicalCrystallizerRecipeManager.INSTANCE,
                    Mekanism.rl("crystallizing/lithium"),
                    Mekanism.rl("processing/lategame/antimatter_pellet/from_gas")
              )
        ;
        exampleBuilder("mekanism_dissolution")
              .comment("Adds a Dissolution Recipe that uses 100 mB of Sulfuric Acid (1 mB per tick) to convert Salt into 10 mB of Hydrogen Chloride.")
              .blankLine()
              .recipe(ChemicalDissolutionRecipeManager.INSTANCE)
              .addExample("salt_to_hydrogen_chloride", IngredientCreatorAccess.item().from(MekanismItems.SALT), IngredientCreatorAccess.gas().from(MekanismGases.SULFURIC_ACID, 1),
                    MekanismGases.HYDROGEN_CHLORIDE.getStack(10))
              .end()
              .comment("Removes two Dissolution Recipes:",
                    "1) The recipe for producing Hydrofluoric Acid from Fluorite.",
                    "2) The recipe for producing Dirty Lead Slurry from Lead Ore."
              ).blankLine()
              .removeRecipes(ChemicalDissolutionRecipeManager.INSTANCE,
                    Mekanism.rl("processing/uranium/hydrofluoric_acid"),
                    Mekanism.rl("processing/lead/slurry/dirty/from_ore")
              )
        ;
        exampleBuilder("mekanism_chemical_infusing")
              .comment("Adds a Chemical Infusing Recipe that uses 1 mB of Hydrogen Chloride and 1 mB of Water Vapor to produce 2 mB of Gaseous Brine.")
              .blankLine()
              .recipe(ChemicalInfuserRecipeManager.INSTANCE)
              .addExample("gaseous_brine", IngredientCreatorAccess.gas().from(MekanismGases.HYDROGEN_CHLORIDE, 1), IngredientCreatorAccess.gas().from(MekanismGases.WATER_VAPOR, 1),
                    MekanismGases.BRINE.getStack(2))
              .end()
              .comment("Removes the Chemical Infusing Recipe for producing Sulfur Trioxide from Oxygen and Sulfur Dioxide.")
              .blankLine()
              .removeRecipes(ChemicalInfuserRecipeManager.INSTANCE, Mekanism.rl("chemical_infusing/sulfur_trioxide"))
        ;
        exampleBuilder("mekanism_combining")
              .comment("Adds two Combining Recipes that do the following:",
                    "1) Adds a recipe that combines three Books and six Planks into a Bookshelf.",
                    "2) Adds a recipe that combines eight Prismarine Shards and one Black Dye into a block of Dark Prismarine."
              ).blankLine()
              .recipe(CombinerRecipeManager.INSTANCE)
              .addExample("combining/bookshelf", IngredientCreatorAccess.item().from(Items.BOOK, 3), IngredientCreatorAccess.item().from(ItemTags.PLANKS, 6),
                    new ItemStack(Blocks.BOOKSHELF))
              .addExample("combining/dark_prismarine", IngredientCreatorAccess.item().from(Items.PRISMARINE_SHARD, 8), IngredientCreatorAccess.item().from(Tags.Items.DYES_BLACK),
                    new ItemStack(Blocks.DARK_PRISMARINE))
              .end()
              .comment("Removes two Combining Recipes:",
                    "1) The recipe for producing Fluorite Ore.",
                    "2) The recipe for producing Light Blue Dye from Blue Dye and White Dye."
              ).blankLine()
              .removeRecipes(CombinerRecipeManager.INSTANCE,
                    Mekanism.rl("processing/fluorite/to_ore"),
                    Mekanism.rl("combining/dye/light_blue")
              )
        ;
        exampleBuilder("mekanism_separating")
              .comment("Adds two Separating Recipes that do the following:",
                    "1) Adds a recipe that separates 2 mB of Liquid Sulfur Trioxide into 1 mB of Oxygen and 2 mB of Sulfur Dioxide.",
                    "2) Adds a recipe that separates 1 mB of Liquid Sulfur Acid into 1 mB of Water Vapor and 1 mB of Sulfur Trioxide, "
                    + "using one and a half times as much energy as it takes to separate Oxygen and Hydrogen from Water."
              ).blankLine()
              .recipe(ElectrolysisRecipeManager.INSTANCE)
              .addExample("separator/sulfur_trioxide", IngredientCreatorAccess.fluid().from(MekanismTags.Fluids.SULFUR_TRIOXIDE, 2), MekanismGases.OXYGEN.getStack(1),
                    MekanismGases.SULFUR_DIOXIDE.getStack(2))
              .addExample("separator/sulfuric_acid", IngredientCreatorAccess.fluid().from(MekanismTags.Fluids.SULFURIC_ACID, 1), MekanismGases.WATER_VAPOR.getStack(1),
                    MekanismGases.SULFUR_TRIOXIDE.getStack(1), FloatingLong.createConst(1.5))
              .end()
              .comment("Removes the Separating Recipe for separating Brine into Sodium and Chlorine.")
              .blankLine()
              .removeRecipes(ElectrolysisRecipeManager.INSTANCE, Mekanism.rl("separator/brine"))
        ;
        SlurryRegistryObject<Slurry, Slurry> uraniumSlurryRO = MekanismSlurries.PROCESSED_RESOURCES.get(PrimaryResource.URANIUM);
        exampleBuilder("mekanism_washing")
              .comment("Removes the Washing Recipe for cleaning Dirty Uranium Slurry.")
              .blankLine()
              .removeRecipes(ChemicalWasherRecipeManager.INSTANCE, Mekanism.rl("processing/uranium/slurry/clean"))
              .comment("Add back the Washing Recipe that was removed above, this time having it require 10 mB of water to clean 1 mB of Dirty Uranium Slurry instead of 5 mB:")
              .blankLine()
              .recipe(ChemicalWasherRecipeManager.INSTANCE)
              .addExample("cleaning_uranium_slurry", IngredientCreatorAccess.fluid().from(FluidTags.WATER, 10),
                    IngredientCreatorAccess.slurry().from(uraniumSlurryRO.getDirtySlurry(), 1), uraniumSlurryRO.getCleanSlurry().getStack(1))
              .end()
        ;
        exampleBuilder("mekanism_evaporating")
              .comment("Adds an Evaporating Recipe that evaporates 10 mB of Lithium and produces 1 mB of Chlorine.")
              .blankLine()
              .recipe(EvaporatingRecipeManager.INSTANCE)
              .addExample("evaporate_lithium", IngredientCreatorAccess.fluid().from(MekanismTags.Fluids.LITHIUM, 10), MekanismFluids.CHLORINE.getFluidStack(1))
              .end()
              .comment("Removes the Evaporating Recipe for producing Lithium from Brine.")
              .blankLine()
              .removeRecipes(EvaporatingRecipeManager.INSTANCE, Mekanism.rl("evaporating/lithium"))
        ;
        exampleBuilder("mekanism_activating")
              .comment("Adds an Activating Recipe that converts 1 mB of Water Vapor to 1 mB of Gaseous Brine.")
              .blankLine()
              .recipe(SolarNeutronActivatorRecipeManager.INSTANCE)
              .addExample("activate_water_vapor", IngredientCreatorAccess.gas().from(MekanismGases.WATER_VAPOR, 1), MekanismGases.BRINE.getStack(1))
              .end()
              .comment("Removes the Activating Recipe for producing Polonium from Nuclear Waste.")
              .blankLine()
              .removeRecipes(SolarNeutronActivatorRecipeManager.INSTANCE, Mekanism.rl("processing/lategame/polonium"))
        ;
        exampleBuilder("mekanism_centrifuging")
              .comment("Adds a Centrifuging Recipe that converts 1 mB of Gaseous Brine into 1 mB of Hydrogen Chloride.")
              .blankLine()
              .recipe(IsotopicCentrifugeRecipeManager.INSTANCE)
              .addExample("centrifuge_brine", IngredientCreatorAccess.gas().from(MekanismGases.BRINE, 1), MekanismGases.HYDROGEN_CHLORIDE.getStack(1))
              .end()
              .comment("Removes the Centrifuging Recipe for producing Plutonium from Nuclear Waste.")
              .blankLine()
              .removeRecipes(IsotopicCentrifugeRecipeManager.INSTANCE, Mekanism.rl("processing/lategame/plutonium"))
        ;
        exampleBuilder("mekanism_compressing")
              .comment("Adds a Compressing Recipe that compresses Emerald Dust into an Emerald.")
              .blankLine()
              .recipe(OsmiumCompressorRecipeManager.INSTANCE)
              .addExample("compress_emerald", IngredientCreatorAccess.item().from(MekanismTags.Items.DUSTS_EMERALD), IngredientCreatorAccess.gas().from(MekanismGases.OSMIUM, 1),
                    new ItemStack(Items.EMERALD))
              .end()
              .comment("Removes the Compressing Recipe that creates Refined Obsidian Ingots.")
              .blankLine()
              .removeRecipes(OsmiumCompressorRecipeManager.INSTANCE, Mekanism.rl("processing/refined_obsidian/ingot/from_dust"))
        ;
        exampleBuilder("mekanism_injecting")
              .comment("Adds an Injecting Recipe that injects 1,000 mB of Water Vapor (5 mB per tick) into a Dry Sponge to make it Wet.")
              .blankLine()
              .recipe(ChemicalInjectionRecipeManager.INSTANCE)
              .addExample("inject_water_to_sponge", IngredientCreatorAccess.item().from(Blocks.SPONGE), IngredientCreatorAccess.gas().from(MekanismTags.Gases.WATER_VAPOR, 5),
                    new ItemStack(Blocks.WET_SPONGE))
              .end()
              .comment("Removes the Injecting Recipe that creates Gold Shards from Gold Ore.")
              .blankLine()
              .removeRecipes(ChemicalInjectionRecipeManager.INSTANCE, Mekanism.rl("processing/gold/shard/from_ore"))
        ;
        exampleBuilder("mekanism_purifying")
              .comment("Adds a Purifying Recipe that uses 200 mB of Oxygen (1 mB per tick) Basalt into Polished Basalt.")
              .blankLine()
              .recipe(PurificationRecipeManager.INSTANCE)
              .addExample("purify_basalt", IngredientCreatorAccess.item().from(Blocks.BASALT), IngredientCreatorAccess.gas().from(MekanismGases.OXYGEN, 1),
                    new ItemStack(Blocks.POLISHED_BASALT))
              .end()
              .comment("Removes the Purifying Recipe that creates Gold Clumps from Gold Ore.")
              .blankLine()
              .removeRecipes(PurificationRecipeManager.INSTANCE, Mekanism.rl("processing/gold/clump/from_ore"))
        ;
        exampleBuilder("mekanism_metallurgic_infusing")
              .comment("Adds a Metallurgic Infusing Recipe that uses 10 mB of Fungi Infuse Type to convert any Oak Planks into Crimson Planks.")
              .blankLine()
              .recipe(MetallurgicInfuserRecipeManager.INSTANCE)
              .addExample("infuse_planks", IngredientCreatorAccess.item().from(Blocks.OAK_PLANKS), IngredientCreatorAccess.infusion().from(MekanismInfuseTypes.FUNGI, 10),
                    new ItemStack(Blocks.CRIMSON_PLANKS))
              .end()
              .comment("Removes the Metallurgic Infusing Recipe that allows creating Dirt from Sand.")
              .blankLine()
              .removeRecipes(MetallurgicInfuserRecipeManager.INSTANCE, Mekanism.rl("metallurgic_infusing/sand_to_dirt"))
        ;
        exampleBuilder("mekanism_painting")
              .comment("Adds a Painting Recipe that uses 256 mB Red Pigment to convert Clear Sand into Red Sand.")
              .blankLine()
              .recipe(PaintingRecipeManager.INSTANCE)
              .addExample("paint_sand", IngredientCreatorAccess.item().from(Tags.Items.SAND_COLORLESS),
                    IngredientCreatorAccess.pigment().from(MekanismPigments.PIGMENT_COLOR_LOOKUP.get(EnumColor.RED), 256),
                    new ItemStack(Blocks.RED_SAND))
              .end()
              .comment("Removes the Painting Recipe that allows creating White Dye.")
              .blankLine()
              .removeRecipes(PaintingRecipeManager.INSTANCE, Mekanism.rl("painting/dye/white"))
        ;
        exampleBuilder("mekanism_energy_conversion")
              .comment("Adds an Energy Conversion Recipe that allows converting Redstone Ore into 45 kJ of power.")
              .blankLine()
              .recipe(EnergyConversionRecipeManager.INSTANCE)
              .addExample("redstone_ore_to_power", IngredientCreatorAccess.item().from(Tags.Items.ORES_REDSTONE), FloatingLong.createConst(45_000))
              .end()
              .comment("Removes the Energy Conversion Recipe that allows converting Redstone Blocks into Power.")
              .blankLine()
              .removeRecipes(EnergyConversionRecipeManager.INSTANCE, Mekanism.rl("energy_conversion/redstone_block"))
        ;
        exampleBuilder("mekanism_gas_conversion")
              .comment("Adds a Gas Conversion Recipe that allows converting Osmium Nuggets into 22 mB of Osmium.")
              .blankLine()
              .recipe(GasConversionRecipeManager.INSTANCE)
              .addExample("gas_conversion/osmium_from_nugget", IngredientCreatorAccess.item().from(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.NUGGET, PrimaryResource.OSMIUM)),
                    MekanismGases.OSMIUM.getStack(22))
              .end()
              .comment("Removes the Gas Conversion Recipe that allows converting Osmium Blocks into Osmium.")
              .blankLine()
              .removeRecipes(GasConversionRecipeManager.INSTANCE, Mekanism.rl("gas_conversion/osmium_from_block"))
        ;
        exampleBuilder("mekanism_oxidizing")
              .comment("Adds an Oxidizing Recipe that allows converting Salt Blocks into 60 mB of Gaseous Brine.")
              .blankLine()
              .recipe(ChemicalOxidizerRecipeManager.INSTANCE)
              .addExample("oxidize_salt_block", IngredientCreatorAccess.item().from(MekanismBlocks.SALT_BLOCK), MekanismGases.BRINE.getStack(60))
              .end()
              .comment("Removes the Oxidizing Recipe that allows Sulfur Dioxide from Sulfur Dust.")
              .blankLine()
              .removeRecipes(ChemicalOxidizerRecipeManager.INSTANCE, Mekanism.rl("oxidizing/sulfur_dioxide"))
        ;
        exampleBuilder("mekanism_infusion_conversion")
              .comment("Adds an Infusion Conversion Recipe that allows converting Gold Ingots into 10 mB Gold Infuse Type.")
              .blankLine()
              .recipe(InfusionConversionRecipeManager.INSTANCE)
              .addExample("infusion_conversion/gold/from_ingot", IngredientCreatorAccess.item().from(Tags.Items.INGOTS_GOLD), MekanismInfuseTypes.GOLD.getStack(10))
              .end()
              .comment("Removes the Infusion Conversion Recipe that allows converting Bio Fuel into the Bio Infuse Type.")
              .blankLine()
              .removeRecipes(InfusionConversionRecipeManager.INSTANCE, Mekanism.rl("infusion_conversion/bio/from_bio_fuel"))
        ;
        exampleBuilder("mekanism_crushing")
              .comment("Adds a Crushing Recipe to crush Brick Blocks into four Bricks.")
              .blankLine()
              .recipe(CrusherRecipeManager.INSTANCE)
              .addExample("crush_bricks", IngredientCreatorAccess.item().from(Blocks.BRICKS), new ItemStack(Items.BRICK, 4))
              .end()
              .comment("Removes the Crushing Recipe that produces String from Wool.")
              .blankLine()
              .removeRecipes(CrusherRecipeManager.INSTANCE, Mekanism.rl("crushing/wool_to_string"))
        ;
        exampleBuilder("mekanism_enriching")
              .comment("Adds an Enriching Recipe to convert 20 Oak Leaves into an Oak Sapling.")
              .blankLine()
              .recipe(EnrichmentChamberRecipeManager.INSTANCE)
              .addExample("oak_leaves_to_saplings", IngredientCreatorAccess.item().from(Blocks.OAK_LEAVES, 20), new ItemStack(Blocks.OAK_SAPLING))
              .end()
              .comment("Removes the Enriching Recipe that creates Gold Dust from Gold Ore.")
              .blankLine()
              .removeRecipes(EnrichmentChamberRecipeManager.INSTANCE, Mekanism.rl("processing/gold/dust/from_ore"))
        ;
        exampleBuilder("mekanism_smelting")
              .comment("Adds a Smelting Recipe that works in Mekanism machines but won't work in a regular furnace to smelt Stone Slabs into Smooth Stone Slabs.")
              .blankLine()
              .recipe(EnergizedSmelterRecipeManager.INSTANCE)
              .addExample("smelt_stone_slab", IngredientCreatorAccess.item().from(Blocks.STONE_SLAB), new ItemStack(Blocks.SMOOTH_STONE_SLAB))
              .end()
        ;
        exampleBuilder("mekanism_pigment_extracting")
              .comment("Adds a Pigment Extracting Recipe that extracts 6,912 mB of Blue Pigment from a Lapis Lazuli Block.")
              .blankLine()
              .recipe(PigmentExtractingRecipeManager.INSTANCE)
              .addExample("extract_lapis_block_pigment", IngredientCreatorAccess.item().from(Tags.Items.STORAGE_BLOCKS_LAPIS),
                    MekanismPigments.PIGMENT_COLOR_LOOKUP.get(EnumColor.DARK_BLUE).getStack(6_912))
              .end()
              .comment("Removes the Pigment Extracting Recipe that extracts Brown Pigment from Brown Dye.")
              .blankLine()
              .removeRecipes(PigmentExtractingRecipeManager.INSTANCE, Mekanism.rl("pigment_extracting/dye/brown"))
        ;
        exampleBuilder("mekanism_nucleosynthesizing")
              .comment("Adds a Nucleosynthesizing Recipe that converts a Block of Coal to a Block of Diamond in 9,000 ticks (7 minutes 30 seconds).")
              .blankLine()
              .recipe(NucleosynthesizingRecipeManager.INSTANCE)
              .addExample("coal_block_to_diamond_block", IngredientCreatorAccess.item().from(Tags.Items.STORAGE_BLOCKS_COAL),
                    IngredientCreatorAccess.gas().from(MekanismGases.ANTIMATTER, 36), new ItemStack(Blocks.DIAMOND_BLOCK), 9_000)
              .end()
              .comment("Removes the Nucleosynthesizing Recipe that converts Tin Ingots into Iron Ingots.")
              .blankLine()
              .removeRecipes(NucleosynthesizingRecipeManager.INSTANCE, Mekanism.rl("nucleosynthesizing/iron"))
        ;
        exampleBuilder("mekanism_pigment_mixing")
              .comment("Adds a Pigment Mixing Recipe that mixes 1 mB of White Pigment with 4 mB of Dark Red Pigment to produce 5 mB of Red Pigment.")
              .blankLine()
              .recipe(PigmentMixingRecipeManager.INSTANCE)
              .addExample("pigment_mixing/white_dark_red_to_red", IngredientCreatorAccess.pigment().from(MekanismPigments.PIGMENT_COLOR_LOOKUP.get(EnumColor.WHITE), 1),
                    IngredientCreatorAccess.pigment().from(MekanismPigments.PIGMENT_COLOR_LOOKUP.get(EnumColor.DARK_RED), 4),
                    MekanismPigments.PIGMENT_COLOR_LOOKUP.get(EnumColor.RED).getStack(5))
              .end()
              .comment("Removes the Pigment Mixing Recipe that produces Dark Red Pigment from Black and Red Pigment.")
              .blankLine()
              .removeRecipes(PigmentMixingRecipeManager.INSTANCE, Mekanism.rl("pigment_mixing/black_red_to_dark_red"))
        ;
        exampleBuilder("mekanism_reaction")
              .comment("Adds six Reaction Recipes that do the following:",
                    "1) Adds a recipe that uses 350 mB of Water, 50 mB of Hydrogen Chloride, and a piece of Sawdust to create two pieces of Paper in 45 ticks, using an extra 25 Joules.",
                    "2) Adds a recipe that uses 100 mB of Liquid Chlorine, 100 mB of Hydrogen, and a Block of Sand to create a Salt Block in 300 ticks.",
                    "3) Adds a recipe that uses 50 mB of Water, 50 mB of Oxygen, and eight Wooden Pressure Plates to create 50 mB of Hydrogen in 74 ticks, using an extra 100 Joules.",
                    "4) Adds a recipe that uses 25 mB of Water, 25 mB of Oxygen, and eight Wooden Buttons to create 25 mB of Hydrogen in 37 ticks.",
                    "5) Adds a recipe that uses 400 mB of Water, 400 mB of Oxygen, and twenty Wooden Fence to create a Charcoal Dust and 400 mB of Hydrogen in 600 ticks, using an extra 300 Joules.",
                    "6) Adds a recipe that uses 400 mB of Water, 400 mB of Oxygen, and four Boats to create a Charcoal Dust and 400 mB of Hydrogen in 600 ticks."
              ).blankLine()
              .recipe(PressurizedReactionRecipeManager.INSTANCE)
              .addExample("reaction/sawdust", IngredientCreatorAccess.item().from(MekanismTags.Items.SAWDUST), IngredientCreatorAccess.fluid().from(FluidTags.WATER, 350),
                    IngredientCreatorAccess.gas().from(MekanismGases.HYDROGEN_CHLORIDE, 50), 45, new ItemStack(Items.PAPER, 2), FloatingLong.createConst(25))
              .addExample("reaction/sand", IngredientCreatorAccess.item().from(Tags.Items.SAND), IngredientCreatorAccess.fluid().from(MekanismTags.Fluids.CHLORINE, 100),
                    IngredientCreatorAccess.gas().from(MekanismGases.HYDROGEN, 100), 300, MekanismBlocks.SALT_BLOCK.getItemStack())
              .addExample("reaction/wooden_buttons", IngredientCreatorAccess.item().from(ItemTags.WOODEN_BUTTONS, 8), IngredientCreatorAccess.fluid().from(FluidTags.WATER, 25),
                    IngredientCreatorAccess.gas().from(MekanismGases.OXYGEN, 25), 37, MekanismGases.HYDROGEN.getStack(25))
              .addExample("reaction/wooden_pressure_plates", IngredientCreatorAccess.item().from(ItemTags.WOODEN_PRESSURE_PLATES, 8), IngredientCreatorAccess.fluid().from(FluidTags.WATER, 50),
                    IngredientCreatorAccess.gas().from(MekanismGases.OXYGEN, 50), 74, MekanismGases.HYDROGEN.getStack(50), FloatingLong.createConst(100))
              .addExample("reaction/wooden_fences", IngredientCreatorAccess.item().from(ItemTags.WOODEN_FENCES, 20), IngredientCreatorAccess.fluid().from(FluidTags.WATER, 400),
                    IngredientCreatorAccess.gas().from(MekanismGases.OXYGEN, 400), 600, MekanismItems.CHARCOAL_DUST.getItemStack(), MekanismGases.HYDROGEN.getStack(400),
                    FloatingLong.createConst(300))
              .addExample("reaction/boat", IngredientCreatorAccess.item().from(ItemTags.BOATS, 4), IngredientCreatorAccess.fluid().from(FluidTags.WATER, 400),
                    IngredientCreatorAccess.gas().from(MekanismGases.OXYGEN, 400), 600, MekanismItems.CHARCOAL_DUST.getItemStack(), MekanismGases.HYDROGEN.getStack(400))
              .end()
              .comment("Removes the Reaction Recipe for producing Substrate from Bio Fuel.")
              .blankLine()
              .removeRecipes(PressurizedReactionRecipeManager.INSTANCE, Mekanism.rl("reaction/substrate/water_hydrogen"))
        ;
        exampleBuilder("mekanism_rotary")
              .comment("Removes three Rotary Recipes:",
                    "1) The recipe for converting between Liquid Lithium and Lithium.",
                    "2) The recipe for converting between Liquid Sulfur Dioxide and Sulfur Dioxide.",
                    "3) The recipe for converting between Liquid Sulfur Trioxide and Sulfur Trioxide."
              ).blankLine()
              .removeRecipes(RotaryRecipeManager.INSTANCE,
                    Mekanism.rl("rotary/lithium"),
                    Mekanism.rl("rotary/sulfur_dioxide"),
                    Mekanism.rl("rotary/sulfur_trioxide")
              )
              .comment("Adds back three Rotary Recipes that correspond to the ones removed above:",
                    "1) Adds a recipe to condensentrate Lithium to Liquid Lithium.",
                    "2) Adds a recipe to decondensentrate Liquid Sulfur Dioxide to Sulfur Dioxide.",
                    "3) Adds a recipe to convert between Liquid Sulfur Trioxide and Sulfur Trioxide."
              )
              .blankLine()
              .recipe(RotaryRecipeManager.INSTANCE)
              .addExample("condensentrate_lithium", IngredientCreatorAccess.gas().from(MekanismGases.LITHIUM, 1), MekanismFluids.LITHIUM.getFluidStack(1))
              .addExample("decondensentrate_sulfur_dioxide", IngredientCreatorAccess.fluid().from(MekanismTags.Fluids.SULFUR_DIOXIDE, 1),
                    MekanismGases.SULFUR_DIOXIDE.getStack(1))
              .addExample("rotary_sulfur_trioxide", IngredientCreatorAccess.fluid().from(MekanismTags.Fluids.SULFUR_TRIOXIDE, 1),
                    IngredientCreatorAccess.gas().from(MekanismGases.SULFUR_TRIOXIDE, 1), MekanismGases.SULFUR_TRIOXIDE.getStack(1),
                    MekanismFluids.SULFUR_TRIOXIDE.getFluidStack(1))
              .end()
        ;
        exampleBuilder("mekanism_sawing")
              .comment("Adds five Sawing Recipes that do the following:",
                    "1) Adds a recipe for sawing Melon Slices into Melon Seeds.",
                    "2) Adds a recipe for sawing fifteen Leaves into a 5% chance of Sawdust.",
                    "3) Adds a recipe for sawing five Saplings into a 75% chance of Sawdust.",
                    "4) Adds a recipe for sawing a Shield into four Planks and a 50% chance of four additional Planks.",
                    "5) Adds a recipe for sawing a Crafting Table into five Oak Planks and a 25% chance of Sawdust.",
                    "6) Adds a recipe for sawing Books into Paper and Leather."
              ).blankLine()
              .recipe(SawmillRecipeManager.INSTANCE)
              .addExample("sawing/melon_to_seeds", IngredientCreatorAccess.item().from(Items.MELON_SLICE), new WeightedItemStack(Items.MELON_SEEDS))
              .addExample("sawing/leaves", IngredientCreatorAccess.item().from(ItemTags.LEAVES, 15), new WeightedItemStack(MekanismItems.SAWDUST, 0.5))
              .addExample("sawing/saplings", IngredientCreatorAccess.item().from(ItemTags.SAPLINGS, 5), MekanismItems.SAWDUST.getItemStack(), 0.75)
              .addExample("sawing/shield", IngredientCreatorAccess.item().from(Items.SHIELD), new WeightedItemStack(new ItemStack(Items.OAK_PLANKS, 4), 1.5))
              .addExample("sawing/workbench", IngredientCreatorAccess.item().from(Blocks.CRAFTING_TABLE), new ItemStack(Items.OAK_PLANKS, 5),
                    new WeightedItemStack(MekanismItems.SAWDUST, 0.25))
              .addExample("sawing/book", IngredientCreatorAccess.item().from(Items.BOOK), new ItemStack(Items.PAPER, 3), new ItemStack(Items.LEATHER, 6), 1.0)
              .end()
              .comment("Removes the Sawing Recipe for producing Oak Planks from Oak Logs.")
              .blankLine()
              .removeRecipes(SawmillRecipeManager.INSTANCE, Mekanism.rl("sawing/log/oak"))
        ;
    }

    private String hideSignature(CrTImportsComponent imports, Class<?> clazz) {
        return imports.addImport(EXPANSION_TARGET_JEITWEAKER) + ".hideIngredient(stack as " + getCrTClassName(clazz) + ")";
    }

    private String descriptionSignature(CrTImportsComponent imports, Class<?> clazz) {
        return imports.addImport(EXPANSION_TARGET_JEITWEAKER) + ".addDescription(stack as " + getCrTClassName(clazz) + ", " + getCrTClassName(Component.class) + "...)";
    }

    private record JEIHidingComponent<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>>(
          CrTImportsComponent imports, IChemicalProvider<CHEMICAL> chemicalProvider,
          Function<STACK, CommandStringDisplayable> describer) implements ICrTExampleComponent {

        @Nonnull
        @Override
        public String asString() {
            return imports.addImport(EXPANSION_TARGET_JEITWEAKER) + ".hideIngredient(" +
                   describer.apply(ChemicalUtil.withAmount(chemicalProvider, FluidAttributes.BUCKET_VOLUME)).getCommandString() + ");";
        }
    }

    private static class SimpleCustomChemicalComponent implements ICrTExampleComponent {

        private final String type;
        private final String name;
        private final String constructor;
        private final int color;

        public SimpleCustomChemicalComponent(String type, String name, int color) {
            this(type, "builder", name, color);
        }

        public SimpleCustomChemicalComponent(String type, String constructor, String name, int color) {
            this.type = type;
            this.constructor = constructor;
            this.name = name;
            this.color = color;
        }

        @Nonnull
        @Override
        public String asString() {
            return type + '.' + constructor + "().color(0x" + Integer.toHexString(color).toUpperCase(Locale.ROOT) + ").build(\"" + name + "\");";
        }
    }
}
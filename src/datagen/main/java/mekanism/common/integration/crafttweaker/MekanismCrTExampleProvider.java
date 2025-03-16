package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.bracket.CommandStringDisplayable;
import java.util.Locale;
import java.util.function.Function;
import mekanism.api.MekanismAPITags;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.integration.crafttweaker.example.BaseCrTExampleProvider;
import mekanism.common.integration.crafttweaker.example.component.CrTImportsComponent;
import mekanism.common.integration.crafttweaker.example.component.ICrTExampleComponent;
import mekanism.common.integration.crafttweaker.recipe.manager.ChemicalChemicalToChemicalRecipeManager.ChemicalInfuserRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.ChemicalChemicalToChemicalRecipeManager.PigmentMixingRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.ChemicalCrystallizerRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.ChemicalDissolutionRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.ChemicalToChemicalRecipeManager.IsotopicCentrifugeRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.ChemicalToChemicalRecipeManager.SolarNeutronActivatorRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.CombinerRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.ElectrolysisRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.FluidChemicalToChemicalRecipeManager.ChemicalWasherRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.FluidToFluidRecipeManager.EvaporatingRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.ItemStackChemicalToItemStackRecipeManager.ChemicalInjectionRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.ItemStackChemicalToItemStackRecipeManager.MetallurgicInfuserRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.ItemStackChemicalToItemStackRecipeManager.OsmiumCompressorRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.ItemStackChemicalToItemStackRecipeManager.PaintingRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.ItemStackChemicalToItemStackRecipeManager.PurificationRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.ItemStackToChemicalRecipeManager.ChemicalConversionRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.ItemStackToChemicalRecipeManager.ChemicalOxidizerRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.ItemStackToChemicalRecipeManager.PigmentExtractingRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.ItemStackToEnergyRecipeManager.EnergyConversionRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.ItemStackToItemStackRecipeManager.CrusherRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.ItemStackToItemStackRecipeManager.EnergizedSmelterRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.ItemStackToItemStackRecipeManager.EnrichmentChamberRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.NucleosynthesizingRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.PressurizedReactionRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.RotaryRecipeManager;
import mekanism.common.integration.crafttweaker.recipe.manager.SawmillRecipeManager;
import mekanism.common.registration.impl.SlurryRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

public class MekanismCrTExampleProvider extends BaseCrTExampleProvider {

    private static final String EXPANSION_TARGET_JEITWEAKER = "mods.jeitweaker.Jei";

    public MekanismCrTExampleProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, existingFileHelper, Mekanism.MODID);
    }

    @Override
    protected void addExamples() {
        //Recipes
        addRecipeExamples();
        //Content
        exampleBuilder("mekanism/custom_chemicals")
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
              .addComponent(imports -> new SimpleCustomChemicalComponent(imports.addImport(CrTConstants.CLASS_BUILDER_CHEMICAL), "builder", "example_gas", 0xDF03FC))
              .addComponent(imports -> new SimpleCustomChemicalComponent(imports.addImport(CrTConstants.CLASS_BUILDER_CHEMICAL), "infuseType", "example_infuse_type", 0x03FC0B))
              .addComponent(imports -> new SimpleCustomChemicalComponent(imports.addImport(CrTConstants.CLASS_BUILDER_CHEMICAL), "pigment", "example_pigment", 0xCAFC03))
              .addComponent(imports -> new SimpleCustomChemicalComponent(imports.addImport(CrTConstants.CLASS_BUILDER_CHEMICAL), "dirty", "example_dirty_slurry", 0xF0FC03))
              .addComponent(imports -> new SimpleCustomChemicalComponent(imports.addImport(CrTConstants.CLASS_BUILDER_CHEMICAL), "clean", "example_clean_slurry", 0xF0FC03))
        ;
        //JEITweaker integration
        exampleBuilder("mekanism/jeitweaker_integration")
              .addComponent(() -> "#modloaded " + Mekanism.hooks.jeiTweaker.modid())
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
              .comment(imports -> hideSignature(imports, ICrTChemicalStack.class))
              .blankLine()
              .addComponent(imports -> new JEIHidingComponent(imports, MekanismChemicals.BRINE, CrTChemicalStack::new))
              .addComponent(imports -> new JEIHidingComponent(imports, MekanismChemicals.BIO, CrTChemicalStack::new))
              .addComponent(imports -> new JEIHidingComponent(imports, MekanismChemicals.PIGMENT_COLOR_LOOKUP.get(EnumColor.DARK_RED), CrTChemicalStack::new))
              .addComponent(imports -> new JEIHidingComponent(imports, MekanismChemicals.PROCESSED_RESOURCES.get(PrimaryResource.GOLD).getCleanSlurry(), CrTChemicalStack::new))
              .blankLine()
              .comment("Adds a description to the passed in chemical. This example adds some basic text to JEI's information tab when looking at Hydrogen.")
              .blankLine()
              .comment(imports -> descriptionSignature(imports, ICrTChemicalStack.class))
              .blankLine()
              .addComponent(imports -> () -> imports.addImport(EXPANSION_TARGET_JEITWEAKER) + ".addIngredientInformation(" +
                                             new CrTChemicalStack(MekanismChemicals.HYDROGEN.asStack(FluidType.BUCKET_VOLUME)).getCommandString() +
                                             ", \"Hydrogen is a basic gas that is produced in an electrolytic separator\");")
        ;
    }

    private void addRecipeExamples() {
        exampleBuilder("mekanism/crystallizer")
              .comment("Adds two Crystallizing Recipes that do the following:",
                    "1) Adds a recipe that produces one Osmium Ingot out of 200 mB of Osmium.",
                    "2) Adds a recipe that produces one Gold Nugget out of 9 mB of the Gold Infuse Type."
              ).blankLine()
              .recipe(ChemicalCrystallizerRecipeManager.INSTANCE)
              .addExample("osmium_ingotification", IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.OSMIUM, 200),
                    MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM).asStack())
              .addExample("gold_infusion_to_gold", IngredientCreatorAccess.chemicalStack().from(MekanismAPITags.Chemicals.GOLD, 9), new ItemStack(Items.GOLD_NUGGET))
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
        exampleBuilder("mekanism/dissolution")
              .comment("Adds a Dissolution Recipe that uses 100 mB of Sulfuric Acid (1 mB per tick) to convert Salt into 10 mB of Hydrogen Chloride.")
              .blankLine()
              .recipe(ChemicalDissolutionRecipeManager.INSTANCE)
              .addExample("salt_to_hydrogen_chloride", IngredientCreatorAccess.item().from(MekanismItems.SALT), IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.SULFURIC_ACID, 1),
                    MekanismChemicals.HYDROGEN_CHLORIDE.asStack(10), true)
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
        exampleBuilder("mekanism/chemical_infusing")
              .comment("Adds a Chemical Infusing Recipe that uses 1 mB of Hydrogen Chloride and 1 mB of Water Vapor to produce 2 mB of Gaseous Brine.")
              .blankLine()
              .recipe(ChemicalInfuserRecipeManager.INSTANCE)
              .addExample("gaseous_brine", IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.HYDROGEN_CHLORIDE, 1), IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.WATER_VAPOR, 1),
                    MekanismChemicals.BRINE.asStack(2))
              .end()
              .comment("Removes the Chemical Infusing Recipe for producing Sulfur Trioxide from Oxygen and Sulfur Dioxide.")
              .blankLine()
              .removeRecipes(ChemicalInfuserRecipeManager.INSTANCE, Mekanism.rl("chemical_infusing/sulfur_trioxide"))
        ;
        exampleBuilder("mekanism/combining")
              .comment("Adds two Combining Recipes that do the following:",
                    "1) Adds a recipe that combines three Books and six Planks into a Bookshelf.",
                    "2) Adds a recipe that combines eight Prismarine Shards and one Black Dye into a block of Dark Prismarine."
              ).blankLine()
              .recipe(CombinerRecipeManager.INSTANCE)
              .addExample("combining/bookshelf", IngredientCreatorAccess.item().from(Items.BOOK, 3), IngredientCreatorAccess.item().from(ItemTags.PLANKS, 6),
                    new ItemStack(Items.BOOKSHELF))
              .addExample("combining/dark_prismarine", IngredientCreatorAccess.item().from(Items.PRISMARINE_SHARD, 8), IngredientCreatorAccess.item().from(Tags.Items.DYES_BLACK),
                    new ItemStack(Items.DARK_PRISMARINE))
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
        exampleBuilder("mekanism/separating")
              .comment("Adds two Separating Recipes that do the following:",
                    "1) Adds a recipe that separates 2 mB of Liquid Sulfur Trioxide into 1 mB of Oxygen and 2 mB of Sulfur Dioxide.",
                    "2) Adds a recipe that separates 1 mB of Liquid Sulfur Acid into 1 mB of Water Vapor and 1 mB of Sulfur Trioxide, "
                    + "using twice as much energy as it takes to separate Oxygen and Hydrogen from Water."
              ).blankLine()
              .recipe(ElectrolysisRecipeManager.INSTANCE)
              .addExample("separator/sulfur_trioxide", IngredientCreatorAccess.fluid().from(MekanismTags.Fluids.SULFUR_TRIOXIDE, 2), MekanismChemicals.OXYGEN.asStack(1),
                    MekanismChemicals.SULFUR_DIOXIDE.asStack(2))
              .addExample("separator/sulfuric_acid", IngredientCreatorAccess.fluid().from(MekanismTags.Fluids.SULFURIC_ACID, 1), MekanismChemicals.WATER_VAPOR.asStack(1),
                    MekanismChemicals.SULFUR_TRIOXIDE.asStack(1), 2L)
              .end()
              .comment("Removes the Separating Recipe for separating Brine into Sodium and Chlorine.")
              .blankLine()
              .removeRecipes(ElectrolysisRecipeManager.INSTANCE, Mekanism.rl("separator/brine"))
        ;
        SlurryRegistryObject<Chemical, Chemical> uraniumSlurryRO = MekanismChemicals.PROCESSED_RESOURCES.get(PrimaryResource.URANIUM);
        exampleBuilder("mekanism/washing")
              .comment("Removes the Washing Recipe for cleaning Dirty Uranium Slurry.")
              .blankLine()
              .removeRecipes(ChemicalWasherRecipeManager.INSTANCE, Mekanism.rl("processing/uranium/slurry/clean"))
              .comment("Add back the Washing Recipe that was removed above, this time having it require 10 mB of water to clean 1 mB of Dirty Uranium Slurry instead of 5 mB:")
              .blankLine()
              .recipe(ChemicalWasherRecipeManager.INSTANCE)
              .addExample("cleaning_uranium_slurry", IngredientCreatorAccess.fluid().from(FluidTags.WATER, 10),
                    IngredientCreatorAccess.chemicalStack().fromHolder(uraniumSlurryRO, 1), new ChemicalStack(uraniumSlurryRO.getCleanSlurry(), 1))
              .end()
        ;
        exampleBuilder("mekanism/evaporating")
              .comment("Adds an Evaporating Recipe that evaporates 10 mB of Lithium and produces 1 mB of Chlorine.")
              .blankLine()
              .recipe(EvaporatingRecipeManager.INSTANCE)
              .addExample("evaporate_lithium", IngredientCreatorAccess.fluid().from(MekanismTags.Fluids.LITHIUM, 10), MekanismFluids.CHLORINE.asStack(1))
              .end()
              .comment("Removes the Evaporating Recipe for producing Lithium from Brine.")
              .blankLine()
              .removeRecipes(EvaporatingRecipeManager.INSTANCE, Mekanism.rl("evaporating/lithium"))
        ;
        exampleBuilder("mekanism/activating")
              .comment("Adds an Activating Recipe that converts 1 mB of Water Vapor to 1 mB of Gaseous Brine.")
              .blankLine()
              .recipe(SolarNeutronActivatorRecipeManager.INSTANCE)
              .addExample("activate_water_vapor", IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.WATER_VAPOR, 1), MekanismChemicals.BRINE.asStack(1))
              .end()
              .comment("Removes the Activating Recipe for producing Polonium from Nuclear Waste.")
              .blankLine()
              .removeRecipes(SolarNeutronActivatorRecipeManager.INSTANCE, Mekanism.rl("processing/lategame/polonium"))
        ;
        exampleBuilder("mekanism/centrifuging")
              .comment("Adds a Centrifuging Recipe that converts 1 mB of Gaseous Brine into 1 mB of Hydrogen Chloride.")
              .blankLine()
              .recipe(IsotopicCentrifugeRecipeManager.INSTANCE)
              .addExample("centrifuge_brine", IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.BRINE, 1), MekanismChemicals.HYDROGEN_CHLORIDE.asStack(1))
              .end()
              .comment("Removes the Centrifuging Recipe for producing Plutonium from Nuclear Waste.")
              .blankLine()
              .removeRecipes(IsotopicCentrifugeRecipeManager.INSTANCE, Mekanism.rl("processing/lategame/plutonium"))
        ;
        exampleBuilder("mekanism/compressing")
              .comment("Adds a Compressing Recipe that compresses Emerald Dust into an Emerald.")
              .blankLine()
              .recipe(OsmiumCompressorRecipeManager.INSTANCE)
              .addExample("compress_emerald", IngredientCreatorAccess.item().from(MekanismTags.Items.DUSTS_EMERALD), IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.OSMIUM, 1),
                    new ItemStack(Items.EMERALD), true)
              .end()
              .comment("Removes the Compressing Recipe that creates Refined Obsidian Ingots.")
              .blankLine()
              .removeRecipes(OsmiumCompressorRecipeManager.INSTANCE, Mekanism.rl("processing/refined_obsidian/ingot/from_dust"))
        ;
        exampleBuilder("mekanism/injecting")
              .comment("Adds an Injecting Recipe that injects 1,000 mB of Water Vapor (5 mB per tick) into a Dry Sponge to make it Wet.")
              .blankLine()
              .recipe(ChemicalInjectionRecipeManager.INSTANCE)
              .addExample("inject_water_to_sponge", IngredientCreatorAccess.item().from(Items.SPONGE), IngredientCreatorAccess.chemicalStack().from(MekanismTags.Chemicals.WATER_VAPOR, 5),
                    new ItemStack(Items.WET_SPONGE), true)
              .end()
              .comment("Removes the Injecting Recipe that creates Gold Shards from Gold Ore.")
              .blankLine()
              .removeRecipes(ChemicalInjectionRecipeManager.INSTANCE, Mekanism.rl("processing/gold/shard/from_ore"))
        ;
        exampleBuilder("mekanism/purifying")
              .comment("Adds a Purifying Recipe that uses 200 mB of Oxygen (1 mB per tick) Basalt into Polished Basalt.")
              .blankLine()
              .recipe(PurificationRecipeManager.INSTANCE)
              .addExample("purify_basalt", IngredientCreatorAccess.item().from(Items.BASALT), IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.OXYGEN, 1),
                    new ItemStack(Items.POLISHED_BASALT), true)
              .end()
              .comment("Removes the Purifying Recipe that creates Gold Clumps from Gold Ore.")
              .blankLine()
              .removeRecipes(PurificationRecipeManager.INSTANCE, Mekanism.rl("processing/gold/clump/from_ore"))
        ;
        exampleBuilder("mekanism/metallurgic_infusing")
              .comment("Adds a Metallurgic Infusing Recipe that uses 10 mB of Fungi Infuse Type to convert any Oak Planks into Crimson Planks.")
              .blankLine()
              .recipe(MetallurgicInfuserRecipeManager.INSTANCE)
              .addExample("infuse_planks", IngredientCreatorAccess.item().from(Items.OAK_PLANKS), IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.FUNGI, 10),
                    new ItemStack(Items.CRIMSON_PLANKS), false)
              .end()
              .comment("Removes the Metallurgic Infusing Recipe that allows creating Dirt from Sand.")
              .blankLine()
              .removeRecipes(MetallurgicInfuserRecipeManager.INSTANCE, Mekanism.rl("metallurgic_infusing/sand_to_dirt"))
        ;
        exampleBuilder("mekanism/painting")
              .comment("Adds a Painting Recipe that uses 256 mB Red Pigment to convert Clear Sand into Red Sand.")
              .blankLine()
              .recipe(PaintingRecipeManager.INSTANCE)
              .addExample("paint_sand", IngredientCreatorAccess.item().from(Tags.Items.SANDS_COLORLESS),
                    IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.PIGMENT_COLOR_LOOKUP.get(EnumColor.RED), 256),
                    new ItemStack(Items.RED_SAND), false)
              .end()
              .comment("Removes the Painting Recipe that allows creating White Dye.")
              .blankLine()
              .removeRecipes(PaintingRecipeManager.INSTANCE, Mekanism.rl("painting/dye/white"))
        ;
        exampleBuilder("mekanism/energy_conversion")
              .comment("Adds an Energy Conversion Recipe that allows converting Redstone Ore into 45 kJ of power.")
              .blankLine()
              .recipe(EnergyConversionRecipeManager.INSTANCE)
              .addExample("redstone_ore_to_power", IngredientCreatorAccess.item().from(Tags.Items.ORES_REDSTONE), 45_000L)
              .end()
              .comment("Removes the Energy Conversion Recipe that allows converting Redstone Blocks into Power.")
              .blankLine()
              .removeRecipes(EnergyConversionRecipeManager.INSTANCE, Mekanism.rl("energy_conversion/redstone_block"))
        ;
        exampleBuilder("mekanism/gas_conversion")
              .comment("Adds a Gas Conversion Recipe that allows converting Osmium Nuggets into 22 mB of Osmium.")
              .blankLine()
              .recipe(ChemicalConversionRecipeManager.INSTANCE)
              .addExample("gas_conversion/osmium_from_nugget", IngredientCreatorAccess.item().from(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.NUGGET, PrimaryResource.OSMIUM)),
                    MekanismChemicals.OSMIUM.asStack(22))
              .end()
              .comment("Removes the Gas Conversion Recipe that allows converting Osmium Blocks into Osmium.")
              .blankLine()
              .removeRecipes(ChemicalConversionRecipeManager.INSTANCE, Mekanism.rl("chemical_conversion/osmium_from_block"))
        ;
        exampleBuilder("mekanism/oxidizing")
              .comment("Adds an Oxidizing Recipe that allows converting Salt Blocks into 60 mB of Gaseous Brine.")
              .blankLine()
              .recipe(ChemicalOxidizerRecipeManager.INSTANCE)
              .addExample("oxidize_salt_block", IngredientCreatorAccess.item().from(MekanismBlocks.SALT_BLOCK), MekanismChemicals.BRINE.asStack(60))
              .end()
              .comment("Removes the Oxidizing Recipe that allows Sulfur Dioxide from Sulfur Dust.")
              .blankLine()
              .removeRecipes(ChemicalOxidizerRecipeManager.INSTANCE, Mekanism.rl("oxidizing/sulfur_dioxide"))
        ;
        exampleBuilder("mekanism/infusion_conversion")
              .comment("Adds an Infusion Conversion Recipe that allows converting Gold Ingots into 10 mB Gold Infuse Type.")
              .blankLine()
              .recipe(ChemicalConversionRecipeManager.INSTANCE)
              .addExample("chemical_conversion/gold/from_ingot", IngredientCreatorAccess.item().from(Tags.Items.INGOTS_GOLD), MekanismChemicals.GOLD.asStack(10))
              .end()
              .comment("Removes the Infusion Conversion Recipe that allows converting Bio Fuel into the Bio Infuse Type.")
              .blankLine()
              .removeRecipes(ChemicalConversionRecipeManager.INSTANCE, Mekanism.rl("chemical_conversion/bio/from_bio_fuel"))
        ;
        exampleBuilder("mekanism/crushing")
              .comment("Adds a Crushing Recipe to crush Brick Blocks into four Bricks.")
              .blankLine()
              .recipe(CrusherRecipeManager.INSTANCE)
              .addExample("crush_bricks", IngredientCreatorAccess.item().from(Items.BRICKS), new ItemStack(Items.BRICK, 4))
              .end()
              .comment("Removes the Crushing Recipe that produces String from Wool.")
              .blankLine()
              .removeRecipes(CrusherRecipeManager.INSTANCE, Mekanism.rl("crushing/wool_to_string"))
        ;
        exampleBuilder("mekanism/enriching")
              .comment("Adds an Enriching Recipe to convert 20 Oak Leaves into an Oak Sapling.")
              .blankLine()
              .recipe(EnrichmentChamberRecipeManager.INSTANCE)
              .addExample("oak_leaves_to_saplings", IngredientCreatorAccess.item().from(Items.OAK_LEAVES, 20), new ItemStack(Items.OAK_SAPLING))
              .end()
              .comment("Removes the Enriching Recipe that creates Gold Dust from Gold Ore.")
              .blankLine()
              .removeRecipes(EnrichmentChamberRecipeManager.INSTANCE, Mekanism.rl("processing/gold/dust/from_ore"))
        ;
        exampleBuilder("mekanism/smelting")
              .comment("Adds a Smelting Recipe that works in Mekanism machines but won't work in a regular furnace to smelt Stone Slabs into Smooth Stone Slabs.")
              .blankLine()
              .recipe(EnergizedSmelterRecipeManager.INSTANCE)
              .addExample("smelt_stone_slab", IngredientCreatorAccess.item().from(Items.STONE_SLAB), new ItemStack(Items.SMOOTH_STONE_SLAB))
              .end()
        ;
        exampleBuilder("mekanism/pigment_extracting")
              .comment("Adds a Pigment Extracting Recipe that extracts 6,912 mB of Blue Pigment from a Lapis Lazuli Block.")
              .blankLine()
              .recipe(PigmentExtractingRecipeManager.INSTANCE)
              .addExample("extract_lapis_block_pigment", IngredientCreatorAccess.item().from(Tags.Items.STORAGE_BLOCKS_LAPIS),
                    MekanismChemicals.PIGMENT_COLOR_LOOKUP.get(EnumColor.DARK_BLUE).asStack(6_912))
              .end()
              .comment("Removes the Pigment Extracting Recipe that extracts Brown Pigment from Brown Dye.")
              .blankLine()
              .removeRecipes(PigmentExtractingRecipeManager.INSTANCE, Mekanism.rl("pigment_extracting/dye/brown"))
        ;
        exampleBuilder("mekanism/nucleosynthesizing")
              .comment("Adds a Nucleosynthesizing Recipe that converts a Block of Coal to a Block of Diamond in 9,000 ticks (7 minutes 30 seconds).")
              .blankLine()
              .recipe(NucleosynthesizingRecipeManager.INSTANCE)
              .addExample("coal_block_to_diamond_block", IngredientCreatorAccess.item().from(Tags.Items.STORAGE_BLOCKS_COAL),
                    IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 36), new ItemStack(Items.DIAMOND_BLOCK), 9_000, false)
              .end()
              .comment("Removes the Nucleosynthesizing Recipe that converts Tin Ingots into Iron Ingots.")
              .blankLine()
              .removeRecipes(NucleosynthesizingRecipeManager.INSTANCE, Mekanism.rl("nucleosynthesizing/iron"))
        ;
        exampleBuilder("mekanism/pigment_mixing")
              .comment("Adds a Pigment Mixing Recipe that mixes 1 mB of White Pigment with 4 mB of Dark Red Pigment to produce 5 mB of Red Pigment.")
              .blankLine()
              .recipe(PigmentMixingRecipeManager.INSTANCE)
              .addExample("pigment_mixing/white_dark_red_to_red", IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.PIGMENT_COLOR_LOOKUP.get(EnumColor.WHITE), 1),
                    IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.PIGMENT_COLOR_LOOKUP.get(EnumColor.DARK_RED), 4),
                    MekanismChemicals.PIGMENT_COLOR_LOOKUP.get(EnumColor.RED).asStack(5))
              .end()
              .comment("Removes the Pigment Mixing Recipe that produces Dark Red Pigment from Black and Red Pigment.")
              .blankLine()
              .removeRecipes(PigmentMixingRecipeManager.INSTANCE, Mekanism.rl("pigment_mixing/black_red_to_dark_red"))
        ;
        exampleBuilder("mekanism/reaction")
              .comment("Adds six Reaction Recipes that do the following:",
                    "1) Adds a recipe that uses 350 mB of Water, 50 mB of Hydrogen Chloride, and a piece of Sawdust to create two pieces of Paper in 45 ticks, using an extra 25 Joules.",
                    "2) Adds a recipe that uses 100 mB of Liquid Chlorine, 100 mB of Hydrogen, and a Block of Sand to create a Salt Block in 300 ticks.",
                    "3) Adds a recipe that uses 50 mB of Water, 50 mB of Oxygen, and eight Wooden Pressure Plates to create 50 mB of Hydrogen in 74 ticks, using an extra 100 Joules.",
                    "4) Adds a recipe that uses 25 mB of Water, 25 mB of Oxygen, and eight Wooden Buttons to create 25 mB of Hydrogen in 37 ticks.",
                    "5) Adds a recipe that uses 400 mB of Water, 400 mB of Oxygen, and twenty Wooden Fence to create a Charcoal Dust and 400 mB of Hydrogen in 600 ticks, using an extra 300 Joules.",
                    "6) Adds a recipe that uses 400 mB of Water, 400 mB of Oxygen, and four Boats to create a Charcoal Dust and 400 mB of Hydrogen in 600 ticks."
              ).blankLine()
              .recipe(PressurizedReactionRecipeManager.INSTANCE)
              .addExample("reaction/sawdust", IngredientCreatorAccess.item().from(MekanismTags.Items.DUSTS_WOOD), IngredientCreatorAccess.fluid().from(FluidTags.WATER, 350),
                    IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.HYDROGEN_CHLORIDE, 50), 45, new ItemStack(Items.PAPER, 2), 25L)
              .addExample("reaction/sand", IngredientCreatorAccess.item().from(Tags.Items.SANDS), IngredientCreatorAccess.fluid().from(MekanismTags.Fluids.CHLORINE, 100),
                    IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.HYDROGEN, 100), 300, new ItemStack(MekanismBlocks.SALT_BLOCK))
              .addExample("reaction/wooden_buttons", IngredientCreatorAccess.item().from(ItemTags.WOODEN_BUTTONS, 8), IngredientCreatorAccess.fluid().from(FluidTags.WATER, 25),
                    IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.OXYGEN, 25), 37, MekanismChemicals.HYDROGEN.asStack(25))
              .addExample("reaction/wooden_pressure_plates", IngredientCreatorAccess.item().from(ItemTags.WOODEN_PRESSURE_PLATES, 8), IngredientCreatorAccess.fluid().from(FluidTags.WATER, 50),
                    IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.OXYGEN, 50), 74, MekanismChemicals.HYDROGEN.asStack(50), 100L)
              .addExample("reaction/wooden_fences", IngredientCreatorAccess.item().from(ItemTags.WOODEN_FENCES, 20), IngredientCreatorAccess.fluid().from(FluidTags.WATER, 400),
                    IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.OXYGEN, 400), 600, MekanismItems.CHARCOAL_DUST.asStack(), MekanismChemicals.HYDROGEN.asStack(400),
                    300L)
              .addExample("reaction/boat", IngredientCreatorAccess.item().from(ItemTags.BOATS, 4), IngredientCreatorAccess.fluid().from(FluidTags.WATER, 400),
                    IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.OXYGEN, 400), 600, MekanismItems.CHARCOAL_DUST.asStack(), MekanismChemicals.HYDROGEN.asStack(400))
              .end()
              .comment("Removes the Reaction Recipe for producing Substrate from Bio Fuel.")
              .blankLine()
              .removeRecipes(PressurizedReactionRecipeManager.INSTANCE, Mekanism.rl("reaction/substrate/water_hydrogen"))
        ;
        exampleBuilder("mekanism/rotary")
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
              .addExample("condensentrate_lithium", IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.LITHIUM, 1), MekanismFluids.LITHIUM.asStack(1))
              .addExample("decondensentrate_sulfur_dioxide", IngredientCreatorAccess.fluid().from(MekanismTags.Fluids.SULFUR_DIOXIDE, 1),
                    MekanismChemicals.SULFUR_DIOXIDE.asStack(1))
              .addExample("rotary_sulfur_trioxide", IngredientCreatorAccess.fluid().from(MekanismTags.Fluids.SULFUR_TRIOXIDE, 1),
                    IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.SULFUR_TRIOXIDE, 1), MekanismChemicals.SULFUR_TRIOXIDE.asStack(1),
                    MekanismFluids.SULFUR_TRIOXIDE.asStack(1))
              .end()
        ;
        exampleBuilder("mekanism/sawing")
              .comment("Adds five Sawing Recipes that do the following:",
                    "1) Adds a recipe for sawing Melon Slices into Melon Seeds.",
                    "2) Adds a recipe for sawing fifteen Leaves into a 5% chance of Sawdust.",
                    "3) Adds a recipe for sawing five Saplings into a 75% chance of Sawdust.",
                    "4) Adds a recipe for sawing a Shield into four Planks and a 50% chance of four additional Planks.",
                    "5) Adds a recipe for sawing a Crafting Table into five Oak Planks and a 25% chance of Sawdust.",
                    "6) Adds a recipe for sawing Books into Paper and Leather."
              ).blankLine()
              .recipe(SawmillRecipeManager.INSTANCE)
              .addExample("sawing/melon_to_seeds", IngredientCreatorAccess.item().from(Items.MELON_SLICE), new WeightedItemStack(Items.MELON_SEEDS.builtInRegistryHolder()))
              .addExample("sawing/leaves", IngredientCreatorAccess.item().from(ItemTags.LEAVES, 15), new WeightedItemStack(MekanismItems.SAWDUST, 0.5))
              .addExample("sawing/saplings", IngredientCreatorAccess.item().from(ItemTags.SAPLINGS, 5), MekanismItems.SAWDUST.asStack(), 0.75)
              .addExample("sawing/shield", IngredientCreatorAccess.item().from(Items.SHIELD), new WeightedItemStack(new ItemStack(Items.OAK_PLANKS, 4), 1.5))
              .addExample("sawing/workbench", IngredientCreatorAccess.item().from(Items.CRAFTING_TABLE), new ItemStack(Items.OAK_PLANKS, 5),
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
        return imports.addImport(EXPANSION_TARGET_JEITWEAKER) + ".addIngredientInformation(stack as " + getCrTClassName(clazz) + ", " + getCrTClassName(Component.class) + "...)";
    }

    private record JEIHidingComponent(CrTImportsComponent imports, Holder<Chemical> chemicalProvider,
                                      Function<ChemicalStack, CommandStringDisplayable> describer) implements ICrTExampleComponent {

        @NotNull
        @Override
        public String asString() {
            return imports.addImport(EXPANSION_TARGET_JEITWEAKER) + ".hideIngredient(" +
                   describer.apply(new ChemicalStack(chemicalProvider, FluidType.BUCKET_VOLUME)).getCommandString() + ");";
        }
    }

    private record SimpleCustomChemicalComponent(String type, String constructor, String name, int color) implements ICrTExampleComponent {

        @NotNull
        @Override
        public String asString() {
            return type + '.' + constructor + "().tint(0x" + Integer.toHexString(color).toUpperCase(Locale.ROOT) + ").build(\"" + name + "\");";
        }
    }
}
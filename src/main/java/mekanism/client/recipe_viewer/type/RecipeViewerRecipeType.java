package mekanism.client.recipe_viewer.type;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.api.recipes.ChemicalToChemicalRecipe;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.FluidChemicalToChemicalRecipe;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.ItemStackToChemicalRecipe;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.basic.BasicItemStackToFluidOptionalItemRecipe;
import mekanism.client.recipe_viewer.recipe.BoilerRecipeViewerRecipe;
import mekanism.client.recipe_viewer.recipe.SPSRecipeViewerRecipe;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;

//Note: Do not use any classes from any recipe viewer mods here as this is to allow us to safely keep them each optional while referencing from our GUIs
@NothingNullByDefault
public class RecipeViewerRecipeType {

    private RecipeViewerRecipeType() {
    }

    //This exists for use in ensuring optional Recipe Viewer support
    public static final VanillaRVRecipeType<CraftingRecipe> VANILLA_CRAFTING = new VanillaRVRecipeType<>(RecipeType.CRAFTING, CraftingRecipe.class, Items.CRAFTING_TABLE, MekanismBlocks.FORMULAIC_ASSEMBLICATOR, MekanismItems.ROBIT);
    public static final VanillaRVRecipeType<SmeltingRecipe> VANILLA_SMELTING = new VanillaRVRecipeType<>(RecipeType.SMELTING, SmeltingRecipe.class, Items.FURNACE, MekanismBlocks.ENERGIZED_SMELTER, MekanismItems.ROBIT);

    public static final RVRecipeTypeWrapper<?, ItemStackToItemStackRecipe, ?> CRUSHING = new RVRecipeTypeWrapper<>(MekanismRecipeType.CRUSHING, ItemStackToItemStackRecipe.class, -28, -16, 144, 54, MekanismBlocks.CRUSHER);
    public static final RVRecipeTypeWrapper<?, ItemStackToItemStackRecipe, ?> ENRICHING = new RVRecipeTypeWrapper<>(MekanismRecipeType.ENRICHING, ItemStackToItemStackRecipe.class, -28, -16, 144, 54, MekanismBlocks.ENRICHMENT_CHAMBER);
    public static final RVRecipeTypeWrapper<?, ItemStackToItemStackRecipe, ?> SMELTING = new RVRecipeTypeWrapper<>(MekanismRecipeType.SMELTING, ItemStackToItemStackRecipe.class, -28, -16, 144, 54, MekanismBlocks.ENERGIZED_SMELTER, MekanismItems.ROBIT);

    public static final RVRecipeTypeWrapper<?, ChemicalChemicalToChemicalRecipe, ?> CHEMICAL_INFUSING = new RVRecipeTypeWrapper<>(MekanismRecipeType.CHEMICAL_INFUSING, ChemicalChemicalToChemicalRecipe.class, -3, -3, 170, 80, MekanismBlocks.CHEMICAL_INFUSER);

    public static final RVRecipeTypeWrapper<?, CombinerRecipe, ?> COMBINING = new RVRecipeTypeWrapper<>(MekanismRecipeType.COMBINING, CombinerRecipe.class, -28, -16, 144, 54, MekanismBlocks.COMBINER);

    public static final RVRecipeTypeWrapper<?, ElectrolysisRecipe, ?> SEPARATING = new RVRecipeTypeWrapper<>(MekanismRecipeType.SEPARATING, ElectrolysisRecipe.class, -4, -9, 167, 62, MekanismBlocks.ELECTROLYTIC_SEPARATOR);

    public static final RVRecipeTypeWrapper<?, FluidChemicalToChemicalRecipe, ?> WASHING = new RVRecipeTypeWrapper<>(MekanismRecipeType.WASHING, FluidChemicalToChemicalRecipe.class, -7, -13, 162, 60, MekanismBlocks.CHEMICAL_WASHER);

    public static final RVRecipeTypeWrapper<?, FluidToFluidRecipe, ?> EVAPORATING = new RVRecipeTypeWrapper<>(MekanismRecipeType.EVAPORATING, FluidToFluidRecipe.class, -3, -12, 176, 62, MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER, MekanismBlocks.THERMAL_EVAPORATION_VALVE, MekanismBlocks.THERMAL_EVAPORATION_BLOCK);

    public static final RVRecipeTypeWrapper<?, ChemicalToChemicalRecipe, ?> ACTIVATING = new RVRecipeTypeWrapper<>(MekanismRecipeType.ACTIVATING, ChemicalToChemicalRecipe.class, -4, -13, 168, 60, MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR);
    public static final RVRecipeTypeWrapper<?, ChemicalToChemicalRecipe, ?> CENTRIFUGING = new RVRecipeTypeWrapper<>(MekanismRecipeType.CENTRIFUGING, ChemicalToChemicalRecipe.class, -4, -13, 168, 60, MekanismBlocks.ISOTOPIC_CENTRIFUGE);

    public static final RVRecipeTypeWrapper<?, ChemicalCrystallizerRecipe, ?> CRYSTALLIZING = new RVRecipeTypeWrapper<>(MekanismRecipeType.CRYSTALLIZING, ChemicalCrystallizerRecipe.class, -5, -3, 147, 79, MekanismBlocks.CHEMICAL_CRYSTALLIZER);

    //Note: This previously had a lang key for a shorter string. Though ideally especially due to translations we will eventually instead just make the text scale
    //TODO - 1.20.4: Re-evaluate ^^
    public static final RVRecipeTypeWrapper<?, ChemicalDissolutionRecipe, ?> DISSOLUTION = new RVRecipeTypeWrapper<>(MekanismRecipeType.DISSOLUTION, ChemicalDissolutionRecipe.class, -3, -3, 170, 79, MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER);

    public static final RVRecipeTypeWrapper<?, ItemStackChemicalToItemStackRecipe, ?> COMPRESSING = new RVRecipeTypeWrapper<>(MekanismRecipeType.COMPRESSING, ItemStackChemicalToItemStackRecipe.class, -28, -16, 144, 54, MekanismBlocks.OSMIUM_COMPRESSOR);
    public static final RVRecipeTypeWrapper<?, ItemStackChemicalToItemStackRecipe, ?> PURIFYING = new RVRecipeTypeWrapper<>(MekanismRecipeType.PURIFYING, ItemStackChemicalToItemStackRecipe.class, -28, -16, 144, 54, MekanismBlocks.PURIFICATION_CHAMBER);
    public static final RVRecipeTypeWrapper<?, ItemStackChemicalToItemStackRecipe, ?> INJECTING = new RVRecipeTypeWrapper<>(MekanismRecipeType.INJECTING, ItemStackChemicalToItemStackRecipe.class, -28, -16, 144, 54, MekanismBlocks.CHEMICAL_INJECTION_CHAMBER);

    public static final RVRecipeTypeWrapper<?, NucleosynthesizingRecipe, ?> NUCLEOSYNTHESIZING = new RVRecipeTypeWrapper<>(MekanismRecipeType.NUCLEOSYNTHESIZING, NucleosynthesizingRecipe.class, -6, -18, 182, 80, MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER);

    //TODO: Decide if we want to make it so all mekanism energy supporting blocks that have gui's are added as catalysts?
    public static final SimpleRVRecipeType<?, ItemStackToEnergyRecipe, ?> ENERGY_CONVERSION = new SimpleRVRecipeType<>(MekanismRecipeType.ENERGY_CONVERSION, ItemStackToEnergyRecipe.class, MekanismLang.CONVERSION_ENERGY, MekanismUtils.getResource(ResourceType.GUI, "energy.png"), -20, -12, 132, 62, MekanismBlocks.BASIC_ENERGY_CUBE, MekanismBlocks.ADVANCED_ENERGY_CUBE, MekanismBlocks.ELITE_ENERGY_CUBE, MekanismBlocks.ULTIMATE_ENERGY_CUBE);

    public static final SimpleRVRecipeType<?, ItemStackToChemicalRecipe, ?> CHEMICAL_CONVERSION = new SimpleRVRecipeType<>(MekanismRecipeType.CHEMICAL_CONVERSION, ItemStackToChemicalRecipe.class, MekanismLang.CONVERSION_CHEMICAL, MekanismUtils.getResource(ResourceType.GUI, "chemicals.png"), -20, -12, 132, 62, MekanismBlocks.PURIFICATION_CHAMBER, MekanismBlocks.METALLURGIC_INFUSER, MekanismBlocks.OSMIUM_COMPRESSOR, MekanismBlocks.CHEMICAL_INJECTION_CHAMBER, MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER, MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER);

    public static final RVRecipeTypeWrapper<?, ItemStackToChemicalRecipe, ?> OXIDIZING = new RVRecipeTypeWrapper<>(MekanismRecipeType.OXIDIZING, ItemStackToChemicalRecipe.class, -20, -12, 132, 62, MekanismBlocks.CHEMICAL_OXIDIZER);

    public static final RVRecipeTypeWrapper<?, ItemStackToChemicalRecipe, ?> PIGMENT_EXTRACTING = new RVRecipeTypeWrapper<>(MekanismRecipeType.PIGMENT_EXTRACTING, ItemStackToChemicalRecipe.class, -20, -12, 132, 62, MekanismBlocks.PIGMENT_EXTRACTOR);

    public static final RVRecipeTypeWrapper<?, ChemicalChemicalToChemicalRecipe, ?> PIGMENT_MIXING = new RVRecipeTypeWrapper<>(MekanismRecipeType.PIGMENT_MIXING, ChemicalChemicalToChemicalRecipe.class, -3, -3, 170, 80, MekanismBlocks.PIGMENT_MIXER);

    public static final RVRecipeTypeWrapper<?, ItemStackChemicalToItemStackRecipe, ?> METALLURGIC_INFUSING = new RVRecipeTypeWrapper<>(MekanismRecipeType.METALLURGIC_INFUSING, ItemStackChemicalToItemStackRecipe.class, -5, -16, 166, 54, MekanismBlocks.METALLURGIC_INFUSER);

    public static final RVRecipeTypeWrapper<?, ItemStackChemicalToItemStackRecipe, ?> PAINTING = new RVRecipeTypeWrapper<>(MekanismRecipeType.PAINTING, ItemStackChemicalToItemStackRecipe.class, -25, -13, 146, 60, MekanismBlocks.PAINTING_MACHINE);

    //Note: This previously had a lang key for a shorter string. Though ideally especially due to translations we will eventually instead just make the text scale
    //TODO - 1.20.4: Re-evaluate ^^
    public static final RVRecipeTypeWrapper<?, PressurizedReactionRecipe, ?> REACTION = new RVRecipeTypeWrapper<>(MekanismRecipeType.REACTION, PressurizedReactionRecipe.class, -3, -15, 170, 60, MekanismBlocks.PRESSURIZED_REACTION_CHAMBER);

    public static final RotaryRVRecipeType CONDENSENTRATING = new RotaryRVRecipeType(Mekanism.rl("condensentrating"), MekanismLang.CONDENSENTRATING);
    public static final RotaryRVRecipeType DECONDENSENTRATING = new RotaryRVRecipeType(Mekanism.rl("decondensentrating"), MekanismLang.DECONDENSENTRATING);

    public static final RVRecipeTypeWrapper<?, SawmillRecipe, ?> SAWING = new RVRecipeTypeWrapper<>(MekanismRecipeType.SAWING, SawmillRecipe.class, -28, -16, 144, 54, MekanismBlocks.PRECISION_SAWMILL);

    public static final FakeRVRecipeType<BoilerRecipeViewerRecipe> BOILER = new FakeRVRecipeType<>(MekanismBlocks.BOILER_CASING.getId(), MekanismUtils.getResource(ResourceType.GUI, "heat.png"), MekanismLang.BOILER, BoilerRecipeViewerRecipe.class, -6, -13, 180, 60, MekanismBlocks.BOILER_CASING, MekanismBlocks.BOILER_VALVE, MekanismBlocks.PRESSURE_DISPERSER, MekanismBlocks.SUPERHEATING_ELEMENT);
    public static final FakeRVRecipeType<SPSRecipeViewerRecipe> SPS = new FakeRVRecipeType<>(MekanismBlocks.SPS_CASING.getId(), MekanismItems.ANTIMATTER_PELLET, MekanismLang.SPS, SPSRecipeViewerRecipe.class, -3, -12, 168, 74, false, MekanismBlocks.SPS_CASING, MekanismBlocks.SPS_PORT, MekanismBlocks.SUPERCHARGED_COIL);

    public static final FakeRVRecipeType<BasicItemStackToFluidOptionalItemRecipe> NUTRITIONAL_LIQUIFICATION = new FakeRVRecipeType<>(MekanismBlocks.NUTRITIONAL_LIQUIFIER, BasicItemStackToFluidOptionalItemRecipe.class, -20, -12, 132, 62);
}
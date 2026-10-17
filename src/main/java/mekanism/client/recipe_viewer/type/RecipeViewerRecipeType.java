package mekanism.client.recipe_viewer.type;

import java.util.stream.Stream;
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
import mekanism.api.recipes.ItemStackToFluidOptionalItemRecipe;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.book.ActivatingBookCategory;
import mekanism.api.recipes.book.ActivatingBookCategory.ActivatingBookInfo;
import mekanism.api.recipes.book.CentrifugingBookCategory;
import mekanism.api.recipes.book.CentrifugingBookCategory.CentrifugingBookInfo;
import mekanism.api.recipes.book.ChemicalConversionBookCategory;
import mekanism.api.recipes.book.ChemicalConversionBookCategory.ChemicalConversionBookInfo;
import mekanism.api.recipes.book.ChemicalInfusingBookCategory;
import mekanism.api.recipes.book.ChemicalInfusingBookCategory.ChemicalInfusingBookInfo;
import mekanism.api.recipes.book.CompressingBookCategory;
import mekanism.api.recipes.book.CompressingBookCategory.CompressingBookInfo;
import mekanism.api.recipes.book.CrushingBookCategory;
import mekanism.api.recipes.book.CrushingBookCategory.CrushingBookInfo;
import mekanism.api.recipes.book.EnrichingBookCategory;
import mekanism.api.recipes.book.EnrichingBookCategory.EnrichingBookInfo;
import mekanism.api.recipes.book.EvaporatingBookCategory;
import mekanism.api.recipes.book.EvaporatingBookCategory.EvaporatingBookInfo;
import mekanism.api.recipes.book.InjectingBookCategory;
import mekanism.api.recipes.book.InjectingBookCategory.InjectingBookInfo;
import mekanism.api.recipes.book.LiquificationBookCategory;
import mekanism.api.recipes.book.LiquificationBookCategory.LiquificationBookInfo;
import mekanism.api.recipes.book.MekanismBookCategory;
import mekanism.api.recipes.book.MetallurgicInfusingBookCategory;
import mekanism.api.recipes.book.MetallurgicInfusingBookCategory.MetallurgicInfusingBookInfo;
import mekanism.api.recipes.book.OxidizingBookCategory;
import mekanism.api.recipes.book.OxidizingBookCategory.OxidizingBookInfo;
import mekanism.api.recipes.book.PaintingBookCategory;
import mekanism.api.recipes.book.PaintingBookCategory.PaintingBookInfo;
import mekanism.api.recipes.book.PigmentExtractingBookCategory;
import mekanism.api.recipes.book.PigmentExtractingBookCategory.PigmentExtractingBookInfo;
import mekanism.api.recipes.book.PigmentMixingBookCategory;
import mekanism.api.recipes.book.PigmentMixingBookCategory.PigmentMixingBookInfo;
import mekanism.api.recipes.book.PurifyingBookCategory;
import mekanism.api.recipes.book.PurifyingBookCategory.PurifyingBookInfo;
import mekanism.api.recipes.book.WashingBookCategory;
import mekanism.api.recipes.book.WashingBookCategory.WashingBookInfo;
import mekanism.client.recipe_viewer.recipe.BoilerRecipeViewerRecipe;
import mekanism.client.recipe_viewer.recipe.SPSRecipeViewerRecipe;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleItem;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe.CookingBookInfo;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay.ItemSlotDisplay;

//Note: Do not use any classes from any recipe viewer mods here as this is to allow us to safely keep them each optional while referencing from our GUIs
public class RecipeViewerRecipeType {//TODO - 26.3: Switch some of these displays to using tags instead of hardcoded blocks here

    private RecipeViewerRecipeType() {
    }

    //This exists for use in ensuring optional Recipe Viewer support
    public static final VanillaRVRecipeType<CraftingRecipe> VANILLA_CRAFTING = new VanillaRVRecipeType<>(RecipeType.CRAFTING, CraftingRecipe.class, Items.CRAFTING_TABLE, MekanismBlocks.FORMULAIC_ASSEMBLICATOR, MekanismItems.ROBIT);
    public static final VanillaRVRecipeType<SmeltingRecipe> VANILLA_SMELTING = new VanillaRVRecipeType<>(RecipeType.SMELTING, SmeltingRecipe.class, Items.FURNACE, MekanismBlocks.ENERGIZED_SMELTER, MekanismItems.ROBIT);

    public static final RVRecipeTypeWrapper<SingleRecipeInput, CrushingBookCategory, CrushingBookInfo, ItemStackToItemStackRecipe<CrushingBookCategory, CrushingBookInfo>, SingleItem<ItemStackToItemStackRecipe<CrushingBookCategory, CrushingBookInfo>>> CRUSHING = new RVRecipeTypeWrapper<>(MekanismRecipeType.CRUSHING, itemToItem(), -28, -16, 144, 54, MekanismBlocks.CRUSHER);
    public static final RVRecipeTypeWrapper<SingleRecipeInput, EnrichingBookCategory, EnrichingBookInfo, ItemStackToItemStackRecipe<EnrichingBookCategory, EnrichingBookInfo>, SingleItem<ItemStackToItemStackRecipe<EnrichingBookCategory, EnrichingBookInfo>>> ENRICHING = new RVRecipeTypeWrapper<>(MekanismRecipeType.ENRICHING, itemToItem(), -28, -16, 144, 54, MekanismBlocks.ENRICHMENT_CHAMBER);
    public static final RVRecipeTypeWrapper<SingleRecipeInput, CookingBookCategory, CookingBookInfo, ItemStackToItemStackRecipe<CookingBookCategory, CookingBookInfo>, SingleItem<ItemStackToItemStackRecipe<CookingBookCategory, CookingBookInfo>>> SMELTING = new RVRecipeTypeWrapper<>(MekanismRecipeType.SMELTING, itemToItem(), -28, -16, 144, 54, MekanismBlocks.ENERGIZED_SMELTER, MekanismItems.ROBIT);

    public static final RVRecipeTypeWrapper<?, ?, ?, ChemicalChemicalToChemicalRecipe<ChemicalInfusingBookCategory, ChemicalInfusingBookInfo>, ?> CHEMICAL_INFUSING = new RVRecipeTypeWrapper<>(MekanismRecipeType.CHEMICAL_INFUSING, chemicalChemicalToChemical(), -3, -3, 170, 80, MekanismBlocks.CHEMICAL_INFUSER);

    public static final RVRecipeTypeWrapper<?, ?, ?, CombinerRecipe, ?> COMBINING = new RVRecipeTypeWrapper<>(MekanismRecipeType.COMBINING, CombinerRecipe.class, -28, -16, 144, 54, MekanismBlocks.COMBINER);

    public static final RVRecipeTypeWrapper<?, ?, ?, ElectrolysisRecipe, ?> SEPARATING = new RVRecipeTypeWrapper<>(MekanismRecipeType.SEPARATING, ElectrolysisRecipe.class, -4, -9, 167, 62, MekanismBlocks.ELECTROLYTIC_SEPARATOR);

    public static final RVRecipeTypeWrapper<?, ?, ?, FluidChemicalToChemicalRecipe<WashingBookCategory, WashingBookInfo>, ?> WASHING = new RVRecipeTypeWrapper<>(MekanismRecipeType.WASHING, fluidChemicalToChemical(), -7, -13, 162, 60, MekanismBlocks.CHEMICAL_WASHER);

    public static final RVRecipeTypeWrapper<?, ?, ?, FluidToFluidRecipe<EvaporatingBookCategory, EvaporatingBookInfo>, ?> EVAPORATING = new RVRecipeTypeWrapper<>(MekanismRecipeType.EVAPORATING, fluidToFluid(), -3, -12, 176, 62, MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER, MekanismBlocks.THERMAL_EVAPORATION_VALVE, MekanismBlocks.THERMAL_EVAPORATION_BLOCK);

    public static final RVRecipeTypeWrapper<?, ?, ?, ChemicalToChemicalRecipe<ActivatingBookCategory, ActivatingBookInfo>, ?> ACTIVATING = new RVRecipeTypeWrapper<>(MekanismRecipeType.ACTIVATING, chemicalToChemical(), -4, -13, 168, 60, MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR);
    public static final RVRecipeTypeWrapper<?, ?, ?, ChemicalToChemicalRecipe<CentrifugingBookCategory, CentrifugingBookInfo>, ?> CENTRIFUGING = new RVRecipeTypeWrapper<>(MekanismRecipeType.CENTRIFUGING, chemicalToChemical(), -4, -13, 168, 60, MekanismBlocks.ISOTOPIC_CENTRIFUGE);

    public static final RVRecipeTypeWrapper<?, ?, ?, ChemicalCrystallizerRecipe, ?> CRYSTALLIZING = new RVRecipeTypeWrapper<>(MekanismRecipeType.CRYSTALLIZING, ChemicalCrystallizerRecipe.class, -5, -3, 147, 79, MekanismBlocks.CHEMICAL_CRYSTALLIZER);

    //Note: This previously had a lang key for a shorter string. Though ideally especially due to translations we will eventually instead just make the text scale
    //TODO - 1.20.4: Re-evaluate ^^
    public static final RVRecipeTypeWrapper<?, ?, ?, ChemicalDissolutionRecipe, ?> DISSOLUTION = new RVRecipeTypeWrapper<>(MekanismRecipeType.DISSOLUTION, ChemicalDissolutionRecipe.class, -3, -3, 170, 79, MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER);

    public static final RVRecipeTypeWrapper<?, ?, ?, ItemStackChemicalToItemStackRecipe<CompressingBookCategory, CompressingBookInfo>, ?> COMPRESSING = new RVRecipeTypeWrapper<>(MekanismRecipeType.COMPRESSING, itemChemicalToItem(), -28, -16, 144, 54, MekanismBlocks.OSMIUM_COMPRESSOR);
    public static final RVRecipeTypeWrapper<?, ?, ?, ItemStackChemicalToItemStackRecipe<PurifyingBookCategory, PurifyingBookInfo>, ?> PURIFYING = new RVRecipeTypeWrapper<>(MekanismRecipeType.PURIFYING, itemChemicalToItem(), -28, -16, 144, 54, MekanismBlocks.PURIFICATION_CHAMBER);
    public static final RVRecipeTypeWrapper<?, ?, ?, ItemStackChemicalToItemStackRecipe<InjectingBookCategory, InjectingBookInfo>, ?> INJECTING = new RVRecipeTypeWrapper<>(MekanismRecipeType.INJECTING, itemChemicalToItem(), -28, -16, 144, 54, MekanismBlocks.CHEMICAL_INJECTION_CHAMBER);

    public static final RVRecipeTypeWrapper<?, ?, ?, NucleosynthesizingRecipe, ?> NUCLEOSYNTHESIZING = new RVRecipeTypeWrapper<>(MekanismRecipeType.NUCLEOSYNTHESIZING, NucleosynthesizingRecipe.class, -6, -18, 182, 80, MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER);

    //TODO: Decide if we want to make it so all mekanism energy supporting blocks that have gui's are added as catalysts?
    public static final SimpleRVRecipeType<?, ?, ?, ItemStackToEnergyRecipe, ?> ENERGY_CONVERSION = new SimpleRVRecipeType<>(MekanismRecipeType.ENERGY_CONVERSION, ItemStackToEnergyRecipe.class, MekanismLang.CONVERSION_ENERGY, TransmissionType.ENERGY.guiTexture(), -20, -12, 132, 62, new SlotDisplay.Composite(
          Stream.of(MekanismBlocks.BASIC_ENERGY_CUBE, MekanismBlocks.ADVANCED_ENERGY_CUBE, MekanismBlocks.ELITE_ENERGY_CUBE, MekanismBlocks.ULTIMATE_ENERGY_CUBE)
                .<SlotDisplay>map(block -> new ItemSlotDisplay(block.getItemHolder()))
                .toList()
    ));

    public static final SimpleRVRecipeType<?, ?, ?, ItemStackToChemicalRecipe<ChemicalConversionBookCategory, ChemicalConversionBookInfo>, ?> CHEMICAL_CONVERSION = new SimpleRVRecipeType<>(MekanismRecipeType.CHEMICAL_CONVERSION, itemToChemical(), MekanismLang.CONVERSION_CHEMICAL, TransmissionType.CHEMICAL.guiTexture(), -20, -12, 132, 62, MekanismBlocks.PURIFICATION_CHAMBER, MekanismBlocks.METALLURGIC_INFUSER, MekanismBlocks.OSMIUM_COMPRESSOR, MekanismBlocks.CHEMICAL_INJECTION_CHAMBER, MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER, MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER);

    public static final RVRecipeTypeWrapper<?, ?, ?, ItemStackToChemicalRecipe<OxidizingBookCategory, OxidizingBookInfo>, ?> OXIDIZING = new RVRecipeTypeWrapper<>(MekanismRecipeType.OXIDIZING, itemToChemical(), -20, -12, 132, 62, MekanismBlocks.CHEMICAL_OXIDIZER);

    public static final RVRecipeTypeWrapper<?, ?, ?, ItemStackToChemicalRecipe<PigmentExtractingBookCategory, PigmentExtractingBookInfo>, ?> PIGMENT_EXTRACTING = new RVRecipeTypeWrapper<>(MekanismRecipeType.PIGMENT_EXTRACTING, itemToChemical(), -20, -12, 132, 62, MekanismBlocks.PIGMENT_EXTRACTOR);

    public static final RVRecipeTypeWrapper<?, ?, ?, ChemicalChemicalToChemicalRecipe<PigmentMixingBookCategory, PigmentMixingBookInfo>, ?> PIGMENT_MIXING = new RVRecipeTypeWrapper<>(MekanismRecipeType.PIGMENT_MIXING, chemicalChemicalToChemical(), -3, -3, 170, 80, MekanismBlocks.PIGMENT_MIXER);

    public static final RVRecipeTypeWrapper<?, ?, ?, ItemStackChemicalToItemStackRecipe<MetallurgicInfusingBookCategory, MetallurgicInfusingBookInfo>, ?> METALLURGIC_INFUSING = new RVRecipeTypeWrapper<>(MekanismRecipeType.METALLURGIC_INFUSING, itemChemicalToItem(), -5, -16, 166, 54, MekanismBlocks.METALLURGIC_INFUSER);

    public static final RVRecipeTypeWrapper<?, ?, ?, ItemStackChemicalToItemStackRecipe<PaintingBookCategory, PaintingBookInfo>, ?> PAINTING = new RVRecipeTypeWrapper<>(MekanismRecipeType.PAINTING, itemChemicalToItem(), -25, -13, 146, 60, MekanismBlocks.PAINTING_MACHINE);

    //Note: This previously had a lang key for a shorter string. Though ideally especially due to translations we will eventually instead just make the text scale
    //TODO - 1.20.4: Re-evaluate ^^
    public static final RVRecipeTypeWrapper<?, ?, ?, PressurizedReactionRecipe, ?> REACTION = new RVRecipeTypeWrapper<>(MekanismRecipeType.REACTION, PressurizedReactionRecipe.class, -3, -15, 170, 60, MekanismBlocks.PRESSURIZED_REACTION_CHAMBER);

    public static final RotaryRVRecipeType CONDENSENTRATING = new RotaryRVRecipeType(Mekanism.rl("condensentrating"), MekanismLang.CONDENSENTRATING);
    public static final RotaryRVRecipeType DECONDENSENTRATING = new RotaryRVRecipeType(Mekanism.rl("decondensentrating"), MekanismLang.DECONDENSENTRATING);

    public static final RVRecipeTypeWrapper<?, ?, ?, SawmillRecipe, ?> SAWING = new RVRecipeTypeWrapper<>(MekanismRecipeType.SAWING, SawmillRecipe.class, -28, -16, 144, 54, MekanismBlocks.PRECISION_SAWMILL);
    public static final RVRecipeTypeWrapper<?, ?, ?, ItemStackToFluidOptionalItemRecipe<LiquificationBookCategory, LiquificationBookInfo>, ?> NUTRITIONAL_LIQUIFICATION = new RVRecipeTypeWrapper<>(MekanismRecipeType.LIQUIFICATION, itemToFluidOptionalItem(), -20, -12, 132, 62, MekanismBlocks.NUTRITIONAL_LIQUIFIER);

    public static final FakeRVRecipeType<BoilerRecipeViewerRecipe> BOILER = new FakeRVRecipeType<>(MekanismBlocks.BOILER_CASING.getId(), TransmissionType.HEAT.guiTexture(), MekanismLang.BOILER, BoilerRecipeViewerRecipe.class, -6, -13, 180, 60, MekanismBlocks.BOILER_CASING, MekanismBlocks.BOILER_VALVE, MekanismBlocks.PRESSURE_DISPERSER, MekanismBlocks.SUPERHEATING_ELEMENT);
    public static final FakeRVRecipeType<SPSRecipeViewerRecipe> SPS = new FakeRVRecipeType<>(MekanismBlocks.SPS_CASING.getId(), MekanismItems.ANTIMATTER_PELLET, MekanismLang.SPS, SPSRecipeViewerRecipe.class, -3, -12, 168, 74, false, MekanismBlocks.SPS_CASING, MekanismBlocks.SPS_PORT, MekanismBlocks.SUPERCHARGED_COIL);

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <BOOK_CATEGORY, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>> Class<ItemStackToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO>> itemToItem() {
        return (Class<ItemStackToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO>>) (Class) ItemStackToItemStackRecipe.class;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>> Class<ItemStackToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO>> itemToChemical() {
        return (Class<ItemStackToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO>>) (Class) ItemStackToChemicalRecipe.class;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>> Class<ItemStackChemicalToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO>> itemChemicalToItem() {
        return (Class<ItemStackChemicalToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO>>) (Class) ItemStackChemicalToItemStackRecipe.class;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>> Class<ChemicalToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO>> chemicalToChemical() {
        return (Class<ChemicalToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO>>) (Class) ChemicalToChemicalRecipe.class;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>> Class<ChemicalChemicalToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO>> chemicalChemicalToChemical() {
        return (Class<ChemicalChemicalToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO>>) (Class) ChemicalChemicalToChemicalRecipe.class;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>> Class<ItemStackToFluidOptionalItemRecipe<BOOK_CATEGORY, BOOK_INFO>> itemToFluidOptionalItem() {
        return (Class<ItemStackToFluidOptionalItemRecipe<BOOK_CATEGORY, BOOK_INFO>>) (Class) ItemStackToFluidOptionalItemRecipe.class;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>> Class<FluidToFluidRecipe<BOOK_CATEGORY, BOOK_INFO>> fluidToFluid() {
        return (Class<FluidToFluidRecipe<BOOK_CATEGORY, BOOK_INFO>>) (Class) FluidToFluidRecipe.class;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>> Class<FluidChemicalToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO>> fluidChemicalToChemical() {
        return (Class<FluidChemicalToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO>>) (Class) FluidChemicalToChemicalRecipe.class;
    }
}
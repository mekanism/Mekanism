package mekanism.common.registries;

import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.book.ActivatingBookCategory;
import mekanism.api.recipes.book.CentrifugingBookCategory;
import mekanism.api.recipes.book.ChemicalConversionBookCategory;
import mekanism.api.recipes.book.ChemicalInfusingBookCategory;
import mekanism.api.recipes.book.CombiningBookCategory;
import mekanism.api.recipes.book.CompressingBookCategory;
import mekanism.api.recipes.book.CrushingBookCategory;
import mekanism.api.recipes.book.CrystallizingBookCategory;
import mekanism.api.recipes.book.DissolutionBookCategory;
import mekanism.api.recipes.book.ElectrolysisBookCategory;
import mekanism.api.recipes.book.EnergyConversionBookCategory;
import mekanism.api.recipes.book.EnrichingBookCategory;
import mekanism.api.recipes.book.EvaporatingBookCategory;
import mekanism.api.recipes.book.InjectingBookCategory;
import mekanism.api.recipes.book.LiquificationBookCategory;
import mekanism.api.recipes.book.MetallurgicInfusingBookCategory;
import mekanism.api.recipes.book.NucleosynthesizingBookCategory;
import mekanism.api.recipes.book.OxidizingBookCategory;
import mekanism.api.recipes.book.PaintingBookCategory;
import mekanism.api.recipes.book.PigmentExtractingBookCategory;
import mekanism.api.recipes.book.PigmentMixingBookCategory;
import mekanism.api.recipes.book.PurifyingBookCategory;
import mekanism.api.recipes.book.ReactingBookCategory;
import mekanism.api.recipes.book.RotaryBookCategory;
import mekanism.api.recipes.book.SawingBookCategory;
import mekanism.api.recipes.book.SmeltingBookCategory;
import mekanism.api.recipes.book.WashingBookCategory;
import mekanism.api.recipes.display.recipe.ActivatingRecipeDisplay;
import mekanism.api.recipes.display.recipe.CentrifugingRecipeDisplay;
import mekanism.api.recipes.display.recipe.ChemicalConversionRecipeDisplay;
import mekanism.api.recipes.display.recipe.ChemicalInfusingRecipeDisplay;
import mekanism.api.recipes.display.recipe.CombiningRecipeDisplay;
import mekanism.api.recipes.display.recipe.CompressingRecipeDisplay;
import mekanism.api.recipes.display.recipe.CrushingRecipeDisplay;
import mekanism.api.recipes.display.recipe.CrystallizerRecipeDisplay;
import mekanism.api.recipes.display.recipe.DissolutionRecipeDisplay;
import mekanism.api.recipes.display.recipe.ElectrolysisRecipeDisplay;
import mekanism.api.recipes.display.recipe.EnergyConversionRecipeDisplay;
import mekanism.api.recipes.display.recipe.EnrichingRecipeDisplay;
import mekanism.api.recipes.display.recipe.EvaporatingRecipeDisplay;
import mekanism.api.recipes.display.recipe.InjectingRecipeDisplay;
import mekanism.api.recipes.display.recipe.LiquificationRecipeDisplay;
import mekanism.api.recipes.display.recipe.MetallurgicInfusingRecipeDisplay;
import mekanism.api.recipes.display.recipe.NucleosynthesizingRecipeDisplay;
import mekanism.api.recipes.display.recipe.OxidizingRecipeDisplay;
import mekanism.api.recipes.display.recipe.PaintingRecipeDisplay;
import mekanism.api.recipes.display.recipe.PigmentExtractingRecipeDisplay;
import mekanism.api.recipes.display.recipe.PigmentMixingRecipeDisplay;
import mekanism.api.recipes.display.recipe.PurifyingRecipeDisplay;
import mekanism.api.recipes.display.recipe.ReactionRecipeDisplay;
import mekanism.api.recipes.display.recipe.RotaryRecipeDisplay;
import mekanism.api.recipes.display.recipe.SawingRecipeDisplay;
import mekanism.api.recipes.display.recipe.SmeltingRecipeDisplay;
import mekanism.api.recipes.display.recipe.WashingRecipeDisplay;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.registration.impl.RecipeBookCategoryDeferredRegister;
import mekanism.common.registration.impl.RecipeBookCategoryDeferredRegister.MekanismRecipeBookTypes;
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;

public class MekanismRecipeBookCategories {

    private MekanismRecipeBookCategories() {
    }

    public static final RecipeBookCategoryDeferredRegister RECIPE_BOOK_CATEGORIES = new RecipeBookCategoryDeferredRegister(Mekanism.MODID);

    public static final MekanismRecipeBookTypes ACTIVATING = RECIPE_BOOK_CATEGORIES.registerAll(MekanismRecipeTypes.NAME_ACTIVATING, MekanismLang.RECIPE_BOOK_TOGGLE_ACTIVATING, ActivatingBookCategory.values(),
          display -> display instanceof ActivatingRecipeDisplay);

    public static final MekanismRecipeBookTypes CENTRIFUGING = RECIPE_BOOK_CATEGORIES.registerAll(MekanismRecipeTypes.NAME_CENTRIFUGING, MekanismLang.RECIPE_BOOK_TOGGLE_CENTRIFUGING, CentrifugingBookCategory.values(),
          display -> display instanceof CentrifugingRecipeDisplay);

    public static final MekanismRecipeBookTypes CHEMICAL_CONVERSION = RECIPE_BOOK_CATEGORIES.registerAll(MekanismRecipeTypes.NAME_CHEMICAL_CONVERSION, MekanismLang.RECIPE_BOOK_TOGGLE_CHEMICAL_CONVERSION, ChemicalConversionBookCategory.values(),
          display -> display instanceof ChemicalConversionRecipeDisplay);

    public static final MekanismRecipeBookTypes CHEMICAL_INFUSING = RECIPE_BOOK_CATEGORIES.registerAll(MekanismRecipeTypes.NAME_CHEMICAL_INFUSING, MekanismLang.RECIPE_BOOK_TOGGLE_CHEMICAL_INFUSING, ChemicalInfusingBookCategory.values(),
          display -> display instanceof ChemicalInfusingRecipeDisplay);

    public static final MekanismRecipeBookTypes COMBINING = RECIPE_BOOK_CATEGORIES.registerAll(MekanismRecipeTypes.NAME_COMBINING, MekanismLang.RECIPE_BOOK_TOGGLE_COMBINING, CombiningBookCategory.values(),
          display -> display instanceof CombiningRecipeDisplay);

    public static final MekanismRecipeBookTypes COMPRESSING = RECIPE_BOOK_CATEGORIES.registerAll(MekanismRecipeTypes.NAME_COMPRESSING, MekanismLang.RECIPE_BOOK_TOGGLE_COMPRESSING, CompressingBookCategory.values(),
          display -> display instanceof CompressingRecipeDisplay);

    public static final MekanismRecipeBookTypes CRUSHING = RECIPE_BOOK_CATEGORIES.registerAll(MekanismRecipeTypes.NAME_CRUSHING, MekanismLang.RECIPE_BOOK_TOGGLE_CRUSHING, CrushingBookCategory.values(),
          display -> display instanceof CrushingRecipeDisplay);

    public static final MekanismRecipeBookTypes CRYSTALLIZING = RECIPE_BOOK_CATEGORIES.registerAll(MekanismRecipeTypes.NAME_CRYSTALLIZING, MekanismLang.RECIPE_BOOK_TOGGLE_CRYSTALLIZING, CrystallizingBookCategory.values(),
          display -> display instanceof CrystallizerRecipeDisplay);

    public static final MekanismRecipeBookTypes DISSOLUTION = RECIPE_BOOK_CATEGORIES.registerAll(MekanismRecipeTypes.NAME_DISSOLUTION, MekanismLang.RECIPE_BOOK_TOGGLE_DISSOLUTION, DissolutionBookCategory.values(),
          display -> display instanceof DissolutionRecipeDisplay);

    public static final MekanismRecipeBookTypes SEPARATING = RECIPE_BOOK_CATEGORIES.registerAll(MekanismRecipeTypes.NAME_SEPARATING, MekanismLang.RECIPE_BOOK_TOGGLE_SEPARATING, ElectrolysisBookCategory.values(),
          display -> display instanceof ElectrolysisRecipeDisplay);

    public static final MekanismRecipeBookTypes ENERGY_CONVERSION = RECIPE_BOOK_CATEGORIES.registerAll(MekanismRecipeTypes.NAME_ENERGY_CONVERSION, MekanismLang.RECIPE_BOOK_TOGGLE_ENERGY_CONVERSION, EnergyConversionBookCategory.values(),
          display -> display instanceof EnergyConversionRecipeDisplay);

    public static final MekanismRecipeBookTypes ENRICHING = RECIPE_BOOK_CATEGORIES.registerAll(MekanismRecipeTypes.NAME_ENRICHING, MekanismLang.RECIPE_BOOK_TOGGLE_ENRICHING, EnrichingBookCategory.values(),
          display -> display instanceof EnrichingRecipeDisplay);

    public static final MekanismRecipeBookTypes EVAPORATING = RECIPE_BOOK_CATEGORIES.registerAll(MekanismRecipeTypes.NAME_EVAPORATING, MekanismLang.RECIPE_BOOK_TOGGLE_EVAPORATING, EvaporatingBookCategory.values(),
          display -> display instanceof EvaporatingRecipeDisplay);

    public static final MekanismRecipeBookTypes INJECTING = RECIPE_BOOK_CATEGORIES.registerAll(MekanismRecipeTypes.NAME_INJECTING, MekanismLang.RECIPE_BOOK_TOGGLE_INJECTING, InjectingBookCategory.values(),
          display -> display instanceof InjectingRecipeDisplay);

    public static final MekanismRecipeBookTypes LIQUIFICATION = RECIPE_BOOK_CATEGORIES.registerAll(MekanismRecipeTypes.NAME_LIQUIFICATION, MekanismLang.RECIPE_BOOK_TOGGLE_LIQUIFICATION, LiquificationBookCategory.values(),
          display -> display instanceof LiquificationRecipeDisplay);

    public static final MekanismRecipeBookTypes METALLURGIC_INFUSING = RECIPE_BOOK_CATEGORIES.registerAll(MekanismRecipeTypes.NAME_METALLURGIC_INFUSING, MekanismLang.RECIPE_BOOK_TOGGLE_METALLURGIC_INFUSING, MetallurgicInfusingBookCategory.values(),
          display -> display instanceof MetallurgicInfusingRecipeDisplay);

    public static final MekanismRecipeBookTypes NUCLEOSYNTHESIZING = RECIPE_BOOK_CATEGORIES.registerAll(MekanismRecipeTypes.NAME_NUCLEOSYNTHESIZING, MekanismLang.RECIPE_BOOK_TOGGLE_NUCLEOSYNTHESIZING, NucleosynthesizingBookCategory.values(),
          display -> display instanceof NucleosynthesizingRecipeDisplay);

    public static final MekanismRecipeBookTypes OXIDIZING = RECIPE_BOOK_CATEGORIES.registerAll(MekanismRecipeTypes.NAME_OXIDIZING, MekanismLang.RECIPE_BOOK_TOGGLE_OXIDIZING, OxidizingBookCategory.values(),
          display -> display instanceof OxidizingRecipeDisplay);

    public static final MekanismRecipeBookTypes PAINTING = RECIPE_BOOK_CATEGORIES.registerAll(MekanismRecipeTypes.NAME_PAINTING, MekanismLang.RECIPE_BOOK_TOGGLE_PAINTING, PaintingBookCategory.values(),
          display -> display instanceof PaintingRecipeDisplay);

    public static final MekanismRecipeBookTypes PIGMENT_EXTRACTING = RECIPE_BOOK_CATEGORIES.registerAll(MekanismRecipeTypes.NAME_PIGMENT_EXTRACTING, MekanismLang.RECIPE_BOOK_TOGGLE_PIGMENT_EXTRACTING, PigmentExtractingBookCategory.values(),
          display -> display instanceof PigmentExtractingRecipeDisplay);

    public static final MekanismRecipeBookTypes PIGMENT_MIXING = RECIPE_BOOK_CATEGORIES.registerAll(MekanismRecipeTypes.NAME_PIGMENT_MIXING, MekanismLang.RECIPE_BOOK_TOGGLE_PIGMENT_MIXING, PigmentMixingBookCategory.values(),
          display -> display instanceof PigmentMixingRecipeDisplay);

    public static final MekanismRecipeBookTypes PURIFYING = RECIPE_BOOK_CATEGORIES.registerAll(MekanismRecipeTypes.NAME_PURIFYING, MekanismLang.RECIPE_BOOK_TOGGLE_PURIFYING, PurifyingBookCategory.values(),
          display -> display instanceof PurifyingRecipeDisplay);

    public static final MekanismRecipeBookTypes REACTION = RECIPE_BOOK_CATEGORIES.registerAll(MekanismRecipeTypes.NAME_REACTION, MekanismLang.RECIPE_BOOK_TOGGLE_REACTION, ReactingBookCategory.values(),
          display -> display instanceof ReactionRecipeDisplay);

    public static final MekanismRecipeBookTypes ROTARY = RECIPE_BOOK_CATEGORIES.registerAll(MekanismRecipeTypes.NAME_ROTARY, MekanismLang.RECIPE_BOOK_TOGGLE_ROTARY, RotaryBookCategory.values(),
          display -> display instanceof RotaryRecipeDisplay);

    public static final MekanismRecipeBookTypes SAWING = RECIPE_BOOK_CATEGORIES.registerAll(MekanismRecipeTypes.NAME_SAWING, MekanismLang.RECIPE_BOOK_TOGGLE_SAWING, SawingBookCategory.values(),
          display -> display instanceof SawingRecipeDisplay);

    public static final MekanismRecipeBookTypes SMELTING = RECIPE_BOOK_CATEGORIES.constructTypes(() -> "gui.recipebook.toggleRecipes.smeltable", SmeltingBookCategory.values(),
          display -> display instanceof SmeltingRecipeDisplay || display instanceof FurnaceRecipeDisplay);

    public static final MekanismRecipeBookTypes WASHING = RECIPE_BOOK_CATEGORIES.registerAll(MekanismRecipeTypes.NAME_WASHING, MekanismLang.RECIPE_BOOK_TOGGLE_WASHING, WashingBookCategory.values(),
          display -> display instanceof WashingRecipeDisplay);
}
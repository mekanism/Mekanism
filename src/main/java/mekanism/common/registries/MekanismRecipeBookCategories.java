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
import mekanism.api.recipes.book.WashingBookCategory;
import mekanism.common.Mekanism;
import mekanism.common.registration.impl.RecipeBookCategoryDeferredRegister;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.neoforged.neoforge.registries.DeferredHolder;

//TODO - 26.3: Make use of this and RegisterRecipeBookSearchCategoriesEvent, RecipeBookComponent, AbstractRecipeBookScreen
public class MekanismRecipeBookCategories {

    private MekanismRecipeBookCategories() {
    }

    public static final RecipeBookCategoryDeferredRegister RECIPE_BOOK_CATEGORIES = new RecipeBookCategoryDeferredRegister(Mekanism.MODID);

    public static final DeferredHolder<RecipeBookCategory, RecipeBookCategory> ACTIVATING_MISC = RECIPE_BOOK_CATEGORIES.register(MekanismRecipeTypes.NAME_ACTIVATING, ActivatingBookCategory.MISC);

    public static final DeferredHolder<RecipeBookCategory, RecipeBookCategory> CENTRIFUGING_MISC = RECIPE_BOOK_CATEGORIES.register(MekanismRecipeTypes.NAME_CENTRIFUGING, CentrifugingBookCategory.MISC);

    public static final DeferredHolder<RecipeBookCategory, RecipeBookCategory> CHEMICAL_CONVERSION_MISC = RECIPE_BOOK_CATEGORIES.register(MekanismRecipeTypes.NAME_CHEMICAL_CONVERSION, ChemicalConversionBookCategory.MISC);

    public static final DeferredHolder<RecipeBookCategory, RecipeBookCategory> CHEMICAL_INFUSING_MISC = RECIPE_BOOK_CATEGORIES.register(MekanismRecipeTypes.NAME_CHEMICAL_INFUSING, ChemicalInfusingBookCategory.MISC);

    public static final DeferredHolder<RecipeBookCategory, RecipeBookCategory> COMBINING_MISC = RECIPE_BOOK_CATEGORIES.register(MekanismRecipeTypes.NAME_COMBINING, CombiningBookCategory.MISC);

    public static final DeferredHolder<RecipeBookCategory, RecipeBookCategory> COMPRESSING_MISC = RECIPE_BOOK_CATEGORIES.register(MekanismRecipeTypes.NAME_COMPRESSING, CompressingBookCategory.MISC);

    public static final DeferredHolder<RecipeBookCategory, RecipeBookCategory> CRUSHING_MISC = RECIPE_BOOK_CATEGORIES.register(MekanismRecipeTypes.NAME_CRUSHING, CrushingBookCategory.MISC);

    public static final DeferredHolder<RecipeBookCategory, RecipeBookCategory> CRYSTALLIZING_MISC = RECIPE_BOOK_CATEGORIES.register(MekanismRecipeTypes.NAME_CRYSTALLIZING, CrystallizingBookCategory.MISC);

    public static final DeferredHolder<RecipeBookCategory, RecipeBookCategory> DISSOLUTION_MISC = RECIPE_BOOK_CATEGORIES.register(MekanismRecipeTypes.NAME_DISSOLUTION, DissolutionBookCategory.MISC);

    public static final DeferredHolder<RecipeBookCategory, RecipeBookCategory> SEPARATING_MISC = RECIPE_BOOK_CATEGORIES.register(MekanismRecipeTypes.NAME_SEPARATING, ElectrolysisBookCategory.MISC);

    public static final DeferredHolder<RecipeBookCategory, RecipeBookCategory> ENERGY_CONVERSION_MISC = RECIPE_BOOK_CATEGORIES.register(MekanismRecipeTypes.NAME_ENERGY_CONVERSION, EnergyConversionBookCategory.MISC);

    public static final DeferredHolder<RecipeBookCategory, RecipeBookCategory> ENRICHING_MISC = RECIPE_BOOK_CATEGORIES.register(MekanismRecipeTypes.NAME_ENRICHING, EnrichingBookCategory.MISC);

    public static final DeferredHolder<RecipeBookCategory, RecipeBookCategory> EVAPORATING_MISC = RECIPE_BOOK_CATEGORIES.register(MekanismRecipeTypes.NAME_EVAPORATING, EvaporatingBookCategory.MISC);

    public static final DeferredHolder<RecipeBookCategory, RecipeBookCategory> INJECTING_MISC = RECIPE_BOOK_CATEGORIES.register(MekanismRecipeTypes.NAME_INJECTING, InjectingBookCategory.MISC);

    public static final DeferredHolder<RecipeBookCategory, RecipeBookCategory> LIQUIFICATION_MISC = RECIPE_BOOK_CATEGORIES.register(MekanismRecipeTypes.NAME_LIQUIFICATION, LiquificationBookCategory.MISC);

    public static final DeferredHolder<RecipeBookCategory, RecipeBookCategory> METALLURGIC_INFUSING_MISC = RECIPE_BOOK_CATEGORIES.register(MekanismRecipeTypes.NAME_METALLURGIC_INFUSING, MetallurgicInfusingBookCategory.MISC);

    public static final DeferredHolder<RecipeBookCategory, RecipeBookCategory> NUCLEOSYNTHESIZING_MISC = RECIPE_BOOK_CATEGORIES.register(MekanismRecipeTypes.NAME_NUCLEOSYNTHESIZING, NucleosynthesizingBookCategory.MISC);

    public static final DeferredHolder<RecipeBookCategory, RecipeBookCategory> OXIDIZING_MISC = RECIPE_BOOK_CATEGORIES.register(MekanismRecipeTypes.NAME_OXIDIZING, OxidizingBookCategory.MISC);

    public static final DeferredHolder<RecipeBookCategory, RecipeBookCategory> PAINTING_MISC = RECIPE_BOOK_CATEGORIES.register(MekanismRecipeTypes.NAME_PAINTING, PaintingBookCategory.MISC);

    public static final DeferredHolder<RecipeBookCategory, RecipeBookCategory> PIGMENT_EXTRACTING_MISC = RECIPE_BOOK_CATEGORIES.register(MekanismRecipeTypes.NAME_PIGMENT_EXTRACTING, PigmentExtractingBookCategory.MISC);

    public static final DeferredHolder<RecipeBookCategory, RecipeBookCategory> PIGMENT_MIXING_MISC = RECIPE_BOOK_CATEGORIES.register(MekanismRecipeTypes.NAME_PIGMENT_MIXING, PigmentMixingBookCategory.MISC);

    public static final DeferredHolder<RecipeBookCategory, RecipeBookCategory> PURIFYING_MISC = RECIPE_BOOK_CATEGORIES.register(MekanismRecipeTypes.NAME_PURIFYING, PurifyingBookCategory.MISC);

    public static final DeferredHolder<RecipeBookCategory, RecipeBookCategory> REACTION_MISC = RECIPE_BOOK_CATEGORIES.register(MekanismRecipeTypes.NAME_REACTION, ReactingBookCategory.MISC);

    public static final DeferredHolder<RecipeBookCategory, RecipeBookCategory> ROTARY_MISC = RECIPE_BOOK_CATEGORIES.register(MekanismRecipeTypes.NAME_ROTARY, RotaryBookCategory.MISC);

    public static final DeferredHolder<RecipeBookCategory, RecipeBookCategory> SAWING_MISC = RECIPE_BOOK_CATEGORIES.register(MekanismRecipeTypes.NAME_SAWING, SawingBookCategory.MISC);

    public static final DeferredHolder<RecipeBookCategory, RecipeBookCategory> WASHING_MISC = RECIPE_BOOK_CATEGORIES.register(MekanismRecipeTypes.NAME_WASHING, WashingBookCategory.MISC);
}
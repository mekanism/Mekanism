package mekanism.api.datagen.recipe.builder;

import java.util.function.BiFunction;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.ItemStackChemicalToItemStackRecipe.Factory;
import mekanism.api.recipes.basic.BasicCompressingRecipe;
import mekanism.api.recipes.basic.BasicInjectingRecipe;
import mekanism.api.recipes.basic.BasicMetallurgicInfuserRecipe;
import mekanism.api.recipes.basic.BasicPaintingRecipe;
import mekanism.api.recipes.basic.BasicPurifyingRecipe;
import mekanism.api.recipes.book.CompressingBookCategory;
import mekanism.api.recipes.book.CompressingBookCategory.CompressingBookInfo;
import mekanism.api.recipes.book.InjectingBookCategory;
import mekanism.api.recipes.book.InjectingBookCategory.InjectingBookInfo;
import mekanism.api.recipes.book.MekanismBookCategory;
import mekanism.api.recipes.book.MetallurgicInfusingBookCategory;
import mekanism.api.recipes.book.MetallurgicInfusingBookCategory.MetallurgicInfusingBookInfo;
import mekanism.api.recipes.book.PaintingBookCategory;
import mekanism.api.recipes.book.PaintingBookCategory.PaintingBookInfo;
import mekanism.api.recipes.book.PurifyingBookCategory;
import mekanism.api.recipes.book.PurifyingBookCategory.PurifyingBookInfo;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;

public class ItemStackChemicalToItemStackRecipeBuilder<BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>,
      RECIPE extends ItemStackChemicalToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO>>
      extends MekanismRecipeBuilder<BOOK_CATEGORY, BOOK_INFO, ItemStackChemicalToItemStackRecipeBuilder<BOOK_CATEGORY, BOOK_INFO, RECIPE>> {

    private final ItemStackChemicalToItemStackRecipe.Factory<BOOK_CATEGORY, BOOK_INFO, RECIPE> factory;
    private final ItemStackIngredient itemInput;
    private final ChemicalStackIngredient chemicalInput;
    private final ItemStackTemplate output;
    private final boolean perTickUsage;

    protected ItemStackChemicalToItemStackRecipeBuilder(ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput, boolean perTickUsage,
          ItemStackTemplate output, Factory<BOOK_CATEGORY, BOOK_INFO, RECIPE> factory, BiFunction<BOOK_CATEGORY, String, BOOK_INFO> bookInfoCreator, BOOK_CATEGORY defaultCategory) {
        this.itemInput = itemInput;
        this.chemicalInput = chemicalInput;
        this.perTickUsage = perTickUsage;
        this.output = output;
        this.factory = factory;
        super(bookInfoCreator, defaultCategory);
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return RecipeBuilder.getDefaultRecipeId(output);
    }

    /// Creates a Compressing recipe builder.
    ///
    /// @param itemInput     Item Input.
    /// @param chemicalInput Chemical Input, used at a constant rate over the duration of the recipe.
    /// @param perTickUsage  Should the recipe consume the chemical input each tick it is processing.
    /// @param output        Output.
    public static ItemStackChemicalToItemStackRecipeBuilder<CompressingBookCategory, CompressingBookInfo, ?> compressing(ItemStackIngredient itemInput,
          ChemicalStackIngredient chemicalInput, boolean perTickUsage, ItemStackTemplate output) {
        return new ItemStackChemicalToItemStackRecipeBuilder<>(itemInput, chemicalInput, perTickUsage, output, BasicCompressingRecipe::new, CompressingBookInfo::new,
              CompressingBookCategory.MISC);
    }

    /// Creates a Purifying recipe builder.
    ///
    /// @param itemInput     Item Input.
    /// @param chemicalInput Chemical Input, used at a near constant rate over the duration of the recipe.
    /// @param perTickUsage  Should the recipe consume the chemical input each tick it is processing.
    /// @param output        Output.
    public static ItemStackChemicalToItemStackRecipeBuilder<PurifyingBookCategory, PurifyingBookInfo, ?> purifying(ItemStackIngredient itemInput,
          ChemicalStackIngredient chemicalInput, boolean perTickUsage, ItemStackTemplate output) {
        return new ItemStackChemicalToItemStackRecipeBuilder<>(itemInput, chemicalInput, perTickUsage, output, BasicPurifyingRecipe::new, PurifyingBookInfo::new,
              PurifyingBookCategory.MISC);
    }

    /// Creates an Injecting recipe builder.
    ///
    /// @param itemInput     Item Input.
    /// @param chemicalInput Chemical Input, used at a near constant rate over the duration of the recipe.
    /// @param perTickUsage  Should the recipe consume the chemical input each tick it is processing.
    /// @param output        Output.
    public static ItemStackChemicalToItemStackRecipeBuilder<InjectingBookCategory, InjectingBookInfo, ?> injecting(ItemStackIngredient itemInput,
          ChemicalStackIngredient chemicalInput, boolean perTickUsage, ItemStackTemplate output) {
        return new ItemStackChemicalToItemStackRecipeBuilder<>(itemInput, chemicalInput, perTickUsage, output, BasicInjectingRecipe::new, InjectingBookInfo::new,
              InjectingBookCategory.MISC);
    }

    /// Creates a Metallurgic Infusing recipe builder.
    ///
    /// @param itemInput     Item Input.
    /// @param chemicalInput Infusion Input.
    /// @param perTickUsage  Should the recipe consume the chemical input each tick it is processing.
    /// @param output        Output.
    public static ItemStackChemicalToItemStackRecipeBuilder<MetallurgicInfusingBookCategory, MetallurgicInfusingBookInfo, ?> metallurgicInfusing(ItemStackIngredient itemInput,
          ChemicalStackIngredient chemicalInput, boolean perTickUsage, ItemStackTemplate output) {
        return new ItemStackChemicalToItemStackRecipeBuilder<>(itemInput, chemicalInput, perTickUsage, output, BasicMetallurgicInfuserRecipe::new,
              MetallurgicInfusingBookInfo::new, MetallurgicInfusingBookCategory.MISC);
    }

    /// Creates a Painting recipe builder.
    ///
    /// @param itemInput     Item Input.
    /// @param chemicalInput Chemical Input.
    /// @param perTickUsage  Should the recipe consume the chemical input each tick it is processing.
    /// @param output        Output.
    public static ItemStackChemicalToItemStackRecipeBuilder<PaintingBookCategory, PaintingBookInfo, ?> painting(ItemStackIngredient itemInput,
          ChemicalStackIngredient chemicalInput, boolean perTickUsage, ItemStackTemplate output) {
        return new ItemStackChemicalToItemStackRecipeBuilder<>(itemInput, chemicalInput, perTickUsage, output, BasicPaintingRecipe::new, PaintingBookInfo::new,
              PaintingBookCategory.MISC);
    }

    @Override
    protected RECIPE asRecipe() {
        return factory.create(commonInfo(), bookInfo(), itemInput, chemicalInput, perTickUsage, output);
    }
}
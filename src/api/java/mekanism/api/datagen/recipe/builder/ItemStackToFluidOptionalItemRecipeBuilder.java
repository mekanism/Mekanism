package mekanism.api.datagen.recipe.builder;

import java.util.Objects;
import java.util.function.BiFunction;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ItemStackToFluidOptionalItemRecipe;
import mekanism.api.recipes.ItemStackToFluidOptionalItemRecipe.FluidOptionalItemOutput;
import mekanism.api.recipes.basic.BasicLiquificationRecipe;
import mekanism.api.recipes.book.LiquificationBookCategory;
import mekanism.api.recipes.book.LiquificationBookCategory.LiquificationBookInfo;
import mekanism.api.recipes.book.MekanismBookCategory;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.fluids.FluidStackTemplate;

/// @since 10.8.0
public class ItemStackToFluidOptionalItemRecipeBuilder<BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>,
      RECIPE extends ItemStackToFluidOptionalItemRecipe<BOOK_CATEGORY, BOOK_INFO>>
      extends MekanismRecipeBuilder<BOOK_CATEGORY, BOOK_INFO, ItemStackToFluidOptionalItemRecipeBuilder<BOOK_CATEGORY, BOOK_INFO, RECIPE>> {

    private final ItemStackToFluidOptionalItemRecipe.Factory<BOOK_CATEGORY, BOOK_INFO, RECIPE> factory;
    private final ItemStackIngredient input;
    private final FluidOptionalItemOutput output;

    protected ItemStackToFluidOptionalItemRecipeBuilder(ItemStackIngredient input, FluidOptionalItemOutput output,
          ItemStackToFluidOptionalItemRecipe.Factory<BOOK_CATEGORY, BOOK_INFO, RECIPE> factory,
          BiFunction<BOOK_CATEGORY, String, BOOK_INFO> bookInfoCreator, BOOK_CATEGORY defaultCategory) {
        this.input = input;
        this.output = output;
        this.factory = factory;
        super(bookInfoCreator, defaultCategory);
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        Identifier fluidId = instanceId(output.fluid());
        if (output.optionalItem() == null) {
            return ResourceKey.create(Registries.RECIPE, fluidId);
        }
        Identifier itemId = instanceId(output.optionalItem());
        return ResourceKey.create(Registries.RECIPE, fluidId.withSuffix("_" + itemId.getPath()));
    }

    /// Creates a nutritional liquification recipe builder.
    ///
    /// @param input       Input.
    /// @param fluidOutput Fluid Output.
    public static ItemStackToFluidOptionalItemRecipeBuilder<LiquificationBookCategory, LiquificationBookInfo, ?> liquification(ItemStackIngredient input,
          FluidStackTemplate fluidOutput) {
        return liquification(input, new FluidOptionalItemOutput(fluidOutput, null));
    }

    /// Creates a nutritional liquification recipe builder.
    ///
    /// @param input       Input.
    /// @param fluidOutput Fluid Output.
    /// @param itemOutput  Item Output.
    public static ItemStackToFluidOptionalItemRecipeBuilder<LiquificationBookCategory, LiquificationBookInfo, ?> liquification(ItemStackIngredient input,
          FluidStackTemplate fluidOutput, ItemStackTemplate itemOutput) {
        Objects.requireNonNull(itemOutput, "Item output may not be null, use the liquification method without an item parameter to not have a remainder item.");
        return liquification(input, new FluidOptionalItemOutput(fluidOutput, itemOutput));
    }

    /// Creates a nutritional liquification recipe builder.
    ///
    /// @param input  Input.
    /// @param output Output.
    public static ItemStackToFluidOptionalItemRecipeBuilder<LiquificationBookCategory, LiquificationBookInfo, ?> liquification(ItemStackIngredient input, FluidOptionalItemOutput output) {
        return new ItemStackToFluidOptionalItemRecipeBuilder<>(input, output, BasicLiquificationRecipe::new, LiquificationBookInfo::new, LiquificationBookCategory.MISC);
    }

    @Override
    protected RECIPE asRecipe() {
        return factory.create(commonInfo(), bookInfo(), input, output);
    }
}
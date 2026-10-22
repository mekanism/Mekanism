package mekanism.common.recipe.serializer;

import com.mojang.serialization.MapCodec;
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
import mekanism.api.recipes.basic.BasicChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.basic.BasicChemicalCrystallizerRecipe;
import mekanism.api.recipes.basic.BasicChemicalDissolutionRecipe;
import mekanism.api.recipes.basic.BasicChemicalToChemicalRecipe;
import mekanism.api.recipes.basic.BasicCombinerRecipe;
import mekanism.api.recipes.basic.BasicElectrolysisRecipe;
import mekanism.api.recipes.basic.BasicFluidChemicalToChemicalRecipe;
import mekanism.api.recipes.basic.BasicFluidToFluidRecipe;
import mekanism.api.recipes.basic.BasicItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.basic.BasicItemStackToChemicalRecipe;
import mekanism.api.recipes.basic.BasicItemStackToEnergyRecipe;
import mekanism.api.recipes.basic.BasicItemStackToFluidOptionalItemRecipe;
import mekanism.api.recipes.basic.BasicItemStackToItemStackRecipe;
import mekanism.api.recipes.basic.BasicNucleosynthesizingRecipe;
import mekanism.api.recipes.basic.BasicPressurizedReactionRecipe;
import mekanism.api.recipes.basic.BasicSawmillRecipe;
import mekanism.api.recipes.book.MekanismBookCategory;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class MekanismRecipeSerializer {

    public static <RECIPE extends Recipe<?>> RecipeSerializer<RECIPE> singleton(RECIPE instance) {
        return new RecipeSerializer<>(MapCodec.unit(instance), StreamCodec.unit(instance));
    }

    public static <BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>,
          RECIPE extends BasicItemStackToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO>> RecipeSerializer<RECIPE> itemToItem(
          ItemStackToItemStackRecipe.Factory<BOOK_CATEGORY, BOOK_INFO, RECIPE> factory, MapCodec<BOOK_INFO> bookMapCodec,
          StreamCodec<RegistryFriendlyByteBuf, BOOK_INFO> bookStreamCodec
    ) {
        return new RecipeSerializer<>(BasicItemStackToItemStackRecipe.simpleMapCodec(factory, bookMapCodec), BasicItemStackToItemStackRecipe.simpleStreamCodec(factory, bookStreamCodec));
    }

    public static <RECIPE extends BasicChemicalCrystallizerRecipe> RecipeSerializer<RECIPE> crystallizing(ChemicalCrystallizerRecipe.Factory<RECIPE> factory) {
        return new RecipeSerializer<>(BasicChemicalCrystallizerRecipe.simpleMapCodec(factory), BasicChemicalCrystallizerRecipe.simpleStreamCodec(factory));
    }

    public static <RECIPE extends BasicCombinerRecipe> RecipeSerializer<RECIPE> combining(CombinerRecipe.Factory<RECIPE> factory) {
        return new RecipeSerializer<>(BasicCombinerRecipe.simpleMapCodec(factory), BasicCombinerRecipe.simpleStreamCodec(factory));
    }

    public static <RECIPE extends BasicItemStackToEnergyRecipe> RecipeSerializer<RECIPE> itemToEnergy(ItemStackToEnergyRecipe.Factory<RECIPE> factory) {
        return new RecipeSerializer<>(BasicItemStackToEnergyRecipe.simpleMapCodec(factory), BasicItemStackToEnergyRecipe.simpleStreamCodec(factory));
    }

    public static <BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>,
          RECIPE extends BasicFluidToFluidRecipe<BOOK_CATEGORY, BOOK_INFO>> RecipeSerializer<RECIPE> fluidToFluid(
          FluidToFluidRecipe.Factory<BOOK_CATEGORY, BOOK_INFO, RECIPE> factory, MapCodec<BOOK_INFO> bookMapCodec,
          StreamCodec<RegistryFriendlyByteBuf, BOOK_INFO> bookStreamCodec
    ) {
        return new RecipeSerializer<>(BasicFluidToFluidRecipe.simpleMapCodec(factory, bookMapCodec), BasicFluidToFluidRecipe.simpleStreamCodec(factory, bookStreamCodec));
    }

    public static <BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>,
          RECIPE extends BasicChemicalToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO>> RecipeSerializer<RECIPE> chemicalToChemical(
          ChemicalToChemicalRecipe.Factory<BOOK_CATEGORY, BOOK_INFO, RECIPE> factory, MapCodec<BOOK_INFO> bookMapCodec,
          StreamCodec<RegistryFriendlyByteBuf, BOOK_INFO> bookStreamCodec
    ) {
        return new RecipeSerializer<>(BasicChemicalToChemicalRecipe.simpleMapCodec(factory, bookMapCodec), BasicChemicalToChemicalRecipe.simpleStreamCodec(factory, bookStreamCodec));
    }

    public static <BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>,
          RECIPE extends BasicFluidChemicalToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO>> RecipeSerializer<RECIPE> fluidChemicalToChemical(
          FluidChemicalToChemicalRecipe.Factory<BOOK_CATEGORY, BOOK_INFO, RECIPE> factory, MapCodec<BOOK_INFO> bookMapCodec,
          StreamCodec<RegistryFriendlyByteBuf, BOOK_INFO> bookStreamCodec
    ) {
        return new RecipeSerializer<>(BasicFluidChemicalToChemicalRecipe.simpleMapCodec(factory, bookMapCodec), BasicFluidChemicalToChemicalRecipe.simpleStreamCodec(factory, bookStreamCodec));
    }

    public static <RECIPE extends BasicNucleosynthesizingRecipe> RecipeSerializer<RECIPE> nucleosynthesizing(NucleosynthesizingRecipe.Factory<RECIPE> factory) {
        return new RecipeSerializer<>(BasicNucleosynthesizingRecipe.simpleMapCodec(factory), BasicNucleosynthesizingRecipe.simpleStreamCodec(factory));
    }

    public static <BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>,
          RECIPE extends BasicItemStackToFluidOptionalItemRecipe<BOOK_CATEGORY, BOOK_INFO>> RecipeSerializer<RECIPE> itemToFluidOptionalItem(
          ItemStackToFluidOptionalItemRecipe.Factory<BOOK_CATEGORY, BOOK_INFO, RECIPE> factory, MapCodec<BOOK_INFO> bookMapCodec,
          StreamCodec<RegistryFriendlyByteBuf, BOOK_INFO> bookStreamCodec
    ) {
        return new RecipeSerializer<>(BasicItemStackToFluidOptionalItemRecipe.simpleMapCodec(factory, bookMapCodec), BasicItemStackToFluidOptionalItemRecipe.simpleStreamCodec(factory, bookStreamCodec));
    }

    public static <RECIPE extends BasicElectrolysisRecipe> RecipeSerializer<RECIPE> separating(ElectrolysisRecipe.Factory<RECIPE> factory) {
        return new RecipeSerializer<>(BasicElectrolysisRecipe.simpleMapCodec(factory), BasicElectrolysisRecipe.simpleStreamCodec(factory));
    }

    public static <RECIPE extends BasicChemicalDissolutionRecipe> RecipeSerializer<RECIPE> dissolution(ChemicalDissolutionRecipe.Factory<RECIPE> factory) {
        return new RecipeSerializer<>(BasicChemicalDissolutionRecipe.simpleMapCodec(factory), BasicChemicalDissolutionRecipe.simpleStreamCodec(factory));
    }

    public static <RECIPE extends BasicPressurizedReactionRecipe> RecipeSerializer<RECIPE> reaction(PressurizedReactionRecipe.Factory<RECIPE> factory) {
        return new RecipeSerializer<>(BasicPressurizedReactionRecipe.simpleMapCodec(factory), BasicPressurizedReactionRecipe.simpleStreamCodec(factory));
    }

    public static <RECIPE extends BasicSawmillRecipe> RecipeSerializer<RECIPE> sawing(SawmillRecipe.Factory<RECIPE> factory) {
        return new RecipeSerializer<>(BasicSawmillRecipe.simpleMapCodec(factory), BasicSawmillRecipe.simpleStreamCodec(factory));
    }

    public static <BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>,
          RECIPE extends BasicItemStackToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO>> RecipeSerializer<RECIPE> itemToChemical(
          ItemStackToChemicalRecipe.Factory<BOOK_CATEGORY, BOOK_INFO, RECIPE> factory, MapCodec<BOOK_INFO> bookMapCodec,
          StreamCodec<RegistryFriendlyByteBuf, BOOK_INFO> bookStreamCodec
    ) {
        return new RecipeSerializer<>(BasicItemStackToChemicalRecipe.simpleMapCodec(factory, bookMapCodec), BasicItemStackToChemicalRecipe.simpleStreamCodec(factory, bookStreamCodec));
    }

    public static <BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>,
          RECIPE extends BasicItemStackChemicalToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO>> RecipeSerializer<RECIPE> itemChemicalToItem(
          ItemStackChemicalToItemStackRecipe.Factory<BOOK_CATEGORY, BOOK_INFO, RECIPE> factory, MapCodec<BOOK_INFO> bookMapCodec,
          StreamCodec<RegistryFriendlyByteBuf, BOOK_INFO> bookStreamCodec
    ) {
        return new RecipeSerializer<>(BasicItemStackChemicalToItemStackRecipe.simpleMapCodec(factory, bookMapCodec), BasicItemStackChemicalToItemStackRecipe.simpleStreamCodec(factory, bookStreamCodec));
    }

    public static <BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>,
          RECIPE extends BasicChemicalChemicalToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO>> RecipeSerializer<RECIPE> chemicalChemicalToChemical(
          ChemicalChemicalToChemicalRecipe.Factory<BOOK_CATEGORY, BOOK_INFO, RECIPE> factory, MapCodec<BOOK_INFO> bookMapCodec,
          StreamCodec<RegistryFriendlyByteBuf, BOOK_INFO> bookStreamCodec
    ) {
        return new RecipeSerializer<>(BasicChemicalChemicalToChemicalRecipe.simpleMapCodec(factory, bookMapCodec), BasicChemicalChemicalToChemicalRecipe.simpleStreamCodec(factory, bookStreamCodec));
    }
}
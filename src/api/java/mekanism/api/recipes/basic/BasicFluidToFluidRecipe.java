package mekanism.api.recipes.basic;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.SerializationConstants;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.book.MekanismBookCategory;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.core.TypedInstance;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStackTemplate;
import net.neoforged.neoforge.fluids.crafting.display.FluidStackSlotDisplay;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public abstract class BasicFluidToFluidRecipe<BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>>
      extends FluidToFluidRecipe<BOOK_CATEGORY, BOOK_INFO> {

    private final RecipeType<FluidToFluidRecipe<BOOK_CATEGORY, BOOK_INFO>> recipeType;
    protected final FluidStackIngredient input;
    protected final FluidStackTemplate output;

    /// @param commonInfo Common recipe info.
    /// @param bookInfo   Recipe book info.
    /// @param input      Input.
    /// @param output     Output.
    protected BasicFluidToFluidRecipe(RecipeType<FluidToFluidRecipe<BOOK_CATEGORY, BOOK_INFO>> recipeType, CommonInfo commonInfo, BOOK_INFO bookInfo,
          FluidStackIngredient input, FluidStackTemplate output) {
        this.recipeType = Objects.requireNonNull(recipeType, "Recipe type cannot be null");
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        this.output = Objects.requireNonNull(output, "Output cannot be null.");
        super(commonInfo, bookInfo);
    }

    @Override
    public final RecipeType<FluidToFluidRecipe<BOOK_CATEGORY, BOOK_INFO>> getType() {
        return recipeType;
    }

    @Override
    public FluidStackIngredient getInput() {
        return input;
    }

    @Override
    public List<FluidStackTemplate> getOutputDefinition(ContextMap contextMap) {
        return Collections.singletonList(output);
    }

    @Override
    public SlotDisplay getOutputDisplay() {
        return new FluidStackSlotDisplay(output);
    }

    @Override
    @Contract(pure = true)
    public FluidStackTemplate getOutput(TypedInstance<Fluid> input) {
        return output;
    }

    public FluidStackTemplate getOutputRaw() {
        return output;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicFluidToFluidRecipe<?, ?> other = (BasicFluidToFluidRecipe<?, ?>) o;
        return input.equals(other.input) && output.equals(other.output);
    }

    @Override
    public int hashCode() {
        int hash = input.hashCode();
        hash = 31 * hash + output.hashCode();
        return hash;
    }

    /// Helper to create a [MapCodec] for a simple [BasicFluidToFluidRecipe].
    ///
    /// @param factory   Recipe constructor
    /// @param bookCodec Map codec for serializing recipe book data
    ///
    /// @since 10.8.0
    public static <BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>, RECIPE extends BasicFluidToFluidRecipe<BOOK_CATEGORY, BOOK_INFO>>
    MapCodec<RECIPE> simpleMapCodec(FluidToFluidRecipe.Factory<BOOK_CATEGORY, BOOK_INFO, RECIPE> factory, MapCodec<BOOK_INFO> bookCodec) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
              Recipe.CommonInfo.MAP_CODEC.forGetter(MekanismRecipe::commonInfo),
              bookCodec.forGetter(MekanismRecipe::bookInfo),
              FluidStackIngredient.CODEC.fieldOf(SerializationConstants.INPUT).forGetter(FluidToFluidRecipe::getInput),
              FluidStackTemplate.MAP_CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(recipe -> recipe.output)
        ).apply(instance, factory::create));
    }

    /// Helper to create a [StreamCodec] for a simple [BasicFluidToFluidRecipe].
    ///
    /// @param factory   Recipe constructor
    /// @param bookCodec Stream codec for serializing recipe book data
    ///
    /// @since 10.8.0
    public static <BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>, RECIPE extends BasicFluidToFluidRecipe<BOOK_CATEGORY, BOOK_INFO>>
    StreamCodec<RegistryFriendlyByteBuf, RECIPE> simpleStreamCodec(FluidToFluidRecipe.Factory<BOOK_CATEGORY, BOOK_INFO, RECIPE> factory,
          StreamCodec<RegistryFriendlyByteBuf, BOOK_INFO> bookCodec) {
        return StreamCodec.composite(
              CommonInfo.STREAM_CODEC, MekanismRecipe::commonInfo,
              bookCodec, MekanismRecipe::bookInfo,
              FluidStackIngredient.STREAM_CODEC, FluidToFluidRecipe::getInput,
              FluidStackTemplate.STREAM_CODEC, recipe -> recipe.output,
              factory::create
        );
    }
}
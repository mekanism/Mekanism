package mekanism.api.recipes.basic;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.SerializationConstants;
import mekanism.api.recipes.ItemStackToFluidOptionalItemRecipe;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.book.MekanismBookCategory;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.TypedInstance;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.fluids.crafting.display.FluidStackSlotDisplay;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

/// Basic implementation of [ItemStackToFluidOptionalItemRecipe]
///
/// @since 10.6.3
public abstract class BasicItemStackToFluidOptionalItemRecipe<BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>>
      extends ItemStackToFluidOptionalItemRecipe<BOOK_CATEGORY, BOOK_INFO> {

    protected final ItemStackIngredient input;
    protected final FluidOptionalItemOutput output;

    /// @param commonInfo Common recipe info.
    /// @param bookInfo   Recipe book info.
    /// @param input      Input.
    /// @param output     Output.
    public BasicItemStackToFluidOptionalItemRecipe(CommonInfo commonInfo, BOOK_INFO bookInfo, ItemStackIngredient input, FluidOptionalItemOutput output) {
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        this.output = Objects.requireNonNull(output, "Output cannot be null.");
        super(commonInfo, bookInfo);
    }

    @Override
    public ItemStackIngredient getInput() {
        return input;
    }

    @Override
    @Contract(pure = true)
    public FluidOptionalItemOutput getOutput(TypedInstance<Item> input) {
        return output;
    }

    @Override
    public List<FluidOptionalItemOutput> getOutputDefinition(ContextMap contextMap) {
        return Collections.singletonList(output);
    }

    @Override
    public final SlotDisplay getOutputDisplay() {
        SlotDisplay fluidDisplay = new FluidStackSlotDisplay(output.fluid());
        if (output.optionalItem() == null) {
            return fluidDisplay;
        }
        return new SlotDisplay.WithRemainder(fluidDisplay, new SlotDisplay.ItemStackSlotDisplay(output.optionalItem()));
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicItemStackToFluidOptionalItemRecipe<?, ?> other = (BasicItemStackToFluidOptionalItemRecipe<?, ?>) o;
        return input.equals(other.input) && output.equals(other.output);
    }

    @Override
    public int hashCode() {
        int result = input.hashCode();
        result = 31 * result + output.hashCode();
        return result;
    }

    /// Helper to create a [MapCodec] for a simple [BasicItemStackToFluidOptionalItemRecipe].
    ///
    /// @param factory   Recipe constructor
    /// @param bookCodec Map codec for serializing recipe book data
    ///
    /// @since 10.8.0
    public static <BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>,
          RECIPE extends BasicItemStackToFluidOptionalItemRecipe<BOOK_CATEGORY, BOOK_INFO>> MapCodec<RECIPE> simpleMapCodec(
          ItemStackToFluidOptionalItemRecipe.Factory<BOOK_CATEGORY, BOOK_INFO, RECIPE> factory, MapCodec<BOOK_INFO> bookCodec
    ) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
              Recipe.CommonInfo.MAP_CODEC.forGetter(MekanismRecipe::commonInfo),
              bookCodec.forGetter(MekanismRecipe::bookInfo),
              ItemStackIngredient.CODEC.fieldOf(SerializationConstants.INPUT).forGetter(ItemStackToFluidOptionalItemRecipe::getInput),
              FluidOptionalItemOutput.CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(recipe -> recipe.output)
        ).apply(instance, factory::create));
    }

    /// Helper to create a [StreamCodec] for a simple [BasicItemStackToFluidOptionalItemRecipe].
    ///
    /// @param factory   Recipe constructor
    /// @param bookCodec Stream codec for serializing recipe book data
    ///
    /// @since 10.8.0
    public static <BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>,
          RECIPE extends BasicItemStackToFluidOptionalItemRecipe<BOOK_CATEGORY, BOOK_INFO>> StreamCodec<RegistryFriendlyByteBuf, RECIPE> simpleStreamCodec(
          ItemStackToFluidOptionalItemRecipe.Factory<BOOK_CATEGORY, BOOK_INFO, RECIPE> factory, StreamCodec<RegistryFriendlyByteBuf, BOOK_INFO> bookCodec
    ) {
        return StreamCodec.composite(
              CommonInfo.STREAM_CODEC, MekanismRecipe::commonInfo,
              bookCodec, MekanismRecipe::bookInfo,
              ItemStackIngredient.STREAM_CODEC, ItemStackToFluidOptionalItemRecipe::getInput,
              FluidOptionalItemOutput.STREAM_CODEC, recipe -> recipe.output,
              factory::create
        );
    }
}
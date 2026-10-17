package mekanism.api.recipes.basic;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.SerializationConstants;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.TypedInstance;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public abstract class BasicItemStackToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>> extends ItemStackToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO> {

    protected final ItemStackIngredient input;
    protected final ItemStackTemplate output;

    /// @param commonInfo Common recipe info.
    /// @param bookInfo   Recipe book info.
    /// @param input      Input.
    /// @param output     Output.
    public BasicItemStackToItemStackRecipe(RecipeType<ItemStackToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO>> recipeType, CommonInfo commonInfo, BOOK_INFO bookInfo, ItemStackIngredient input,
          ItemStackTemplate output) {
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        this.output = Objects.requireNonNull(output, "Output cannot be null.");
        super(recipeType, commonInfo, bookInfo);
    }

    @Override
    public ItemStackIngredient getInput() {
        return input;
    }

    @Override
    @Contract(pure = true)
    public ItemStackTemplate getOutput(TypedInstance<Item> input) {
        return output;
    }

    @Override
    public List<ItemStackTemplate> getOutputDefinition(ContextMap contextMap) {
        return Collections.singletonList(output);
    }

    @Override
    public SlotDisplay getOutputDisplay() {
        return new SlotDisplay.ItemStackSlotDisplay(output);
    }

    public ItemStackTemplate getOutputRaw() {
        return output;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicItemStackToItemStackRecipe<?, ?> other = (BasicItemStackToItemStackRecipe<?, ?>) o;
        return input.equals(other.input) && output.equals(other.output);
    }

    @Override
    public int hashCode() {
        int hash = input.hashCode();
        hash = 31 * hash + output.hashCode();
        return hash;
    }

    /// Helper to create a [MapCodec] for a simple [BasicItemStackToItemStackRecipe].
    ///
    /// @param factory   Recipe constructor
    /// @param bookCodec Map codec for serializing recipe book data
    ///
    /// @since 10.8.0
    public static <BOOK_CATEGORY, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>,
          RECIPE extends BasicItemStackToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO>> MapCodec<RECIPE> simpleMapCodec(
          ItemStackToItemStackRecipe.Factory<BOOK_CATEGORY, BOOK_INFO, RECIPE> factory, MapCodec<BOOK_INFO> bookCodec
    ) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
              Recipe.CommonInfo.MAP_CODEC.forGetter(MekanismRecipe::commonInfo),
              bookCodec.forGetter(MekanismRecipe::bookInfo),
              ItemStackIngredient.CODEC.fieldOf(SerializationConstants.INPUT).forGetter(ItemStackToItemStackRecipe::getInput),
              ItemStackTemplate.CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(recipe -> recipe.output)
        ).apply(instance, factory::create));
    }

    /// Helper to create a [StreamCodec] for a simple [BasicItemStackToItemStackRecipe].
    ///
    /// @param factory   Recipe constructor
    /// @param bookCodec Stream codec for serializing recipe book data
    ///
    /// @since 10.8.0
    public static <BOOK_CATEGORY, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>,
          RECIPE extends BasicItemStackToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO>> StreamCodec<RegistryFriendlyByteBuf, RECIPE> simpleStreamCodec(
          ItemStackToItemStackRecipe.Factory<BOOK_CATEGORY, BOOK_INFO, RECIPE> factory, StreamCodec<RegistryFriendlyByteBuf, BOOK_INFO> bookCodec
    ) {
        return StreamCodec.composite(
              CommonInfo.STREAM_CODEC, MekanismRecipe::commonInfo,
              bookCodec, MekanismRecipe::bookInfo,
              ItemStackIngredient.STREAM_CODEC, ItemStackToItemStackRecipe::getInput,
              ItemStackTemplate.STREAM_CODEC, recipe -> recipe.output,
              factory::create
        );
    }
}
package mekanism.api.recipes.basic;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.ChemicalToChemicalRecipe;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.book.MekanismBookCategory;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.chemical.display.ChemicalStackSlotDisplay;
import net.minecraft.core.TypedInstance;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public abstract class BasicChemicalToChemicalRecipe<BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>>
      extends ChemicalToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO> {

    private final RecipeType<ChemicalToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO>> recipeType;
    protected final ChemicalStackTemplate output;
    private final ChemicalStackIngredient input;

    /// @param commonInfo Common recipe info.
    /// @param bookInfo   Recipe book info.
    /// @param input      Input.
    /// @param output     Output.
    protected BasicChemicalToChemicalRecipe(RecipeType<ChemicalToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO>> recipeType, CommonInfo commonInfo, BOOK_INFO bookInfo,
          ChemicalStackIngredient input, ChemicalStackTemplate output) {
        this.recipeType = Objects.requireNonNull(recipeType, "Recipe type cannot be null");
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        this.output = Objects.requireNonNull(output, "Output cannot be null.");
        super(commonInfo, bookInfo);
    }

    @Override
    public final RecipeType<ChemicalToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO>> getType() {
        return recipeType;
    }

    @Override
    public ChemicalStackIngredient getInput() {
        return input;
    }

    @Override
    public List<ChemicalStackTemplate> getOutputDefinition(ContextMap contextMap) {
        return Collections.singletonList(output);
    }

    @Override
    public SlotDisplay getOutputDisplay() {
        return new ChemicalStackSlotDisplay(output);
    }

    @Override
    @Contract(pure = true)
    public ChemicalStackTemplate getOutput(TypedInstance<Chemical> input) {
        return output;
    }

    /// For Serializer usage only. Do not modify the returned stack!
    ///
    /// @return the uncopied output definition
    public ChemicalStackTemplate getOutputRaw() {
        return output;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicChemicalToChemicalRecipe<?, ?> other = (BasicChemicalToChemicalRecipe<?, ?>) o;
        //Note: We don't need to compare the recipe type as that gets covered by the explicit class type check above
        return output.equals(other.output) && input.equals(other.input);
    }

    @Override
    public int hashCode() {
        int result = output.hashCode();
        result = 31 * result + input.hashCode();
        return result;
    }

    /// Helper to create a [MapCodec] for a simple [BasicChemicalToChemicalRecipe].
    ///
    /// @param factory   Recipe constructor
    /// @param bookCodec Map codec for serializing recipe book data
    ///
    /// @since 10.8.0
    public static <BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>,
          RECIPE extends BasicChemicalToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO>> MapCodec<RECIPE> simpleMapCodec(
          ChemicalToChemicalRecipe.Factory<BOOK_CATEGORY, BOOK_INFO, RECIPE> factory, MapCodec<BOOK_INFO> bookCodec
    ) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
              Recipe.CommonInfo.MAP_CODEC.forGetter(MekanismRecipe::commonInfo),
              bookCodec.forGetter(MekanismRecipe::bookInfo),
              ChemicalStackIngredient.CODEC.fieldOf(SerializationConstants.INPUT).forGetter(ChemicalToChemicalRecipe::getInput),
              ChemicalStackTemplate.MAP_CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(recipe -> recipe.output)
        ).apply(instance, factory::create));
    }

    /// Helper to create a [StreamCodec] for a simple [BasicChemicalToChemicalRecipe].
    ///
    /// @param factory   Recipe constructor
    /// @param bookCodec Stream codec for serializing recipe book data
    ///
    /// @since 10.8.0
    public static <BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>,
          RECIPE extends BasicChemicalToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO>> StreamCodec<RegistryFriendlyByteBuf, RECIPE> simpleStreamCodec(
          ChemicalToChemicalRecipe.Factory<BOOK_CATEGORY, BOOK_INFO, RECIPE> factory, StreamCodec<RegistryFriendlyByteBuf, BOOK_INFO> bookCodec
    ) {
        return StreamCodec.composite(
              CommonInfo.STREAM_CODEC, MekanismRecipe::commonInfo,
              bookCodec, MekanismRecipe::bookInfo,
              ChemicalStackIngredient.STREAM_CODEC, ChemicalToChemicalRecipe::getInput,
              ChemicalStackTemplate.STREAM_CODEC, recipe -> recipe.output,
              factory::create
        );
    }
}
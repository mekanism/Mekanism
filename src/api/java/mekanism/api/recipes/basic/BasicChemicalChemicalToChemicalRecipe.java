package mekanism.api.recipes.basic;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.ChemicalChemicalToChemicalRecipe;
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

public abstract class BasicChemicalChemicalToChemicalRecipe<BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>>
      extends ChemicalChemicalToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO> implements IBasicChemicalOutput {

    private final RecipeType<ChemicalChemicalToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO>> recipeType;
    protected final ChemicalStackIngredient leftInput;
    protected final ChemicalStackIngredient rightInput;
    protected final ChemicalStackTemplate output;

    /// @param commonInfo Common recipe info.
    /// @param bookInfo   Recipe book info.
    /// @param leftInput  Left input.
    /// @param rightInput Right input.
    /// @param output     Output.
    ///
    /// @apiNote The order of the inputs does not matter.
    public BasicChemicalChemicalToChemicalRecipe(RecipeType<ChemicalChemicalToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO>> recipeType, CommonInfo commonInfo, BOOK_INFO bookInfo,
          ChemicalStackIngredient leftInput, ChemicalStackIngredient rightInput, ChemicalStackTemplate output) {
        this.recipeType = Objects.requireNonNull(recipeType, "Recipe type cannot be null");
        this.leftInput = Objects.requireNonNull(leftInput, "Left input cannot be null.");
        this.rightInput = Objects.requireNonNull(rightInput, "Right input cannot be null.");
        this.output = Objects.requireNonNull(output, "Output cannot be null.");
        super(commonInfo, bookInfo);
    }

    @Override
    public final RecipeType<ChemicalChemicalToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO>> getType() {
        return recipeType;
    }

    @Override
    public boolean test(ChemicalStack input1, ChemicalStack input2) {
        return super.test(input1, input2) || super.test(input2, input1);
    }

    @Override
    @Contract(value = "_, _ -> new", pure = true)
    public ChemicalStackTemplate getOutput(TypedInstance<Chemical> input1, TypedInstance<Chemical> input2) {
        return output;
    }

    @Override
    public ChemicalStackIngredient getLeftInput() {
        return leftInput;
    }

    @Override
    public ChemicalStackIngredient getRightInput() {
        return rightInput;
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
        BasicChemicalChemicalToChemicalRecipe<?, ?> other = (BasicChemicalChemicalToChemicalRecipe<?, ?>) o;
        //Note: We don't need to compare the recipe type as that gets covered by the explicit class type check above
        return leftInput.equals(other.leftInput) && rightInput.equals(other.rightInput) && output.equals(other.output);
    }

    @Override
    public int hashCode() {
        int result = leftInput.hashCode();
        result = 31 * result + rightInput.hashCode();
        result = 31 * result + output.hashCode();
        return result;
    }

    /// Helper to create a [MapCodec] for a simple [BasicChemicalChemicalToChemicalRecipe].
    ///
    /// @param factory   Recipe constructor
    /// @param bookCodec Map codec for serializing recipe book data
    ///
    /// @since 10.8.0
    public static <BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>,
          RECIPE extends BasicChemicalChemicalToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO>> MapCodec<RECIPE> simpleMapCodec(
          ChemicalChemicalToChemicalRecipe.Factory<BOOK_CATEGORY, BOOK_INFO, RECIPE> factory, MapCodec<BOOK_INFO> bookCodec
    ) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
              Recipe.CommonInfo.MAP_CODEC.forGetter(MekanismRecipe::commonInfo),
              bookCodec.forGetter(MekanismRecipe::bookInfo),
              ChemicalStackIngredient.CODEC.fieldOf(SerializationConstants.LEFT_INPUT).forGetter(ChemicalChemicalToChemicalRecipe::getLeftInput),
              ChemicalStackIngredient.CODEC.fieldOf(SerializationConstants.RIGHT_INPUT).forGetter(ChemicalChemicalToChemicalRecipe::getRightInput),
              ChemicalStackTemplate.MAP_CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(recipe -> recipe.output)
        ).apply(instance, factory::create));
    }

    /// Helper to create a [StreamCodec] for a simple [BasicChemicalChemicalToChemicalRecipe].
    ///
    /// @param factory   Recipe constructor
    /// @param bookCodec Stream codec for serializing recipe book data
    ///
    /// @since 10.8.0
    public static <BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>,
          RECIPE extends BasicChemicalChemicalToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO>> StreamCodec<RegistryFriendlyByteBuf, RECIPE> simpleStreamCodec(
          ChemicalChemicalToChemicalRecipe.Factory<BOOK_CATEGORY, BOOK_INFO, RECIPE> factory, StreamCodec<RegistryFriendlyByteBuf, BOOK_INFO> bookCodec
    ) {
        return StreamCodec.composite(
              CommonInfo.STREAM_CODEC, MekanismRecipe::commonInfo,
              bookCodec, MekanismRecipe::bookInfo,
              ChemicalStackIngredient.STREAM_CODEC, ChemicalChemicalToChemicalRecipe::getLeftInput,
              ChemicalStackIngredient.STREAM_CODEC, ChemicalChemicalToChemicalRecipe::getRightInput,
              ChemicalStackTemplate.STREAM_CODEC, recipe -> recipe.output,
              factory::create
        );
    }
}
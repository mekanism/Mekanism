package mekanism.api.recipes.basic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.recipes.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.book.MekanismBookCategory;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.TypedInstance;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public abstract class BasicItemStackChemicalToItemStackRecipe<BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>>
      extends ItemStackChemicalToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO> implements IBasicItemStackOutput {

    private final RecipeType<? extends ItemStackChemicalToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO>> recipeType;
    protected final ItemStackIngredient itemInput;
    protected final ChemicalStackIngredient chemicalInput;
    protected final ItemStackTemplate output;
    private final boolean perTickUsage;

    /// @param commonInfo    Common recipe info.
    /// @param bookInfo      Recipe book info.
    /// @param itemInput     Item input.
    /// @param chemicalInput Chemical input.
    /// @param perTickUsage  Should the recipe consume the chemical input each tick it is processing.
    /// @param output        Output.
    public BasicItemStackChemicalToItemStackRecipe(RecipeType<? extends ItemStackChemicalToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO>> recipeType, CommonInfo commonInfo,
          BOOK_INFO bookInfo, ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput, boolean perTickUsage, ItemStackTemplate output) {
        this.recipeType = Objects.requireNonNull(recipeType, "Recipe type cannot be null");
        this.itemInput = Objects.requireNonNull(itemInput, "Item input cannot be null.");
        this.chemicalInput = Objects.requireNonNull(chemicalInput, "Chemical input cannot be null.");
        this.output = Objects.requireNonNull(output, "Output cannot be null.");
        this.perTickUsage = perTickUsage;
        super(commonInfo, bookInfo);
    }

    @Override
    public final RecipeType<? extends ItemStackChemicalToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO>> getType() {
        return recipeType;
    }

    @Override
    public ItemStackIngredient getItemInput() {
        return itemInput;
    }

    @Override
    public ChemicalStackIngredient getChemicalInput() {
        return chemicalInput;
    }

    @Override
    @Contract(pure = true)
    public ItemStackTemplate getOutput(TypedInstance<Item> inputItem, TypedInstance<Chemical> inputChemical) {
        return output;
    }

    @Override
    public final boolean perTickUsage() {
        return perTickUsage;
    }

    @Override
    public List<ItemStackTemplate> getOutputDefinition(ContextMap contextMap) {
        return Collections.singletonList(output);
    }

    @Override
    public SlotDisplay getOutputDisplay() {
        return new SlotDisplay.ItemStackSlotDisplay(output);
    }

    @Override
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
        BasicItemStackChemicalToItemStackRecipe<?, ?> other = (BasicItemStackChemicalToItemStackRecipe<?, ?>) o;
        //Note: We don't need to compare the recipe type as that gets covered by the explicit class type check above
        return perTickUsage == other.perTickUsage && itemInput.equals(other.itemInput) && chemicalInput.equals(other.chemicalInput) && output.equals(other.output);
    }

    @Override
    public int hashCode() {
        int result = itemInput.hashCode();
        result = 31 * result + chemicalInput.hashCode();
        result = 31 * result + Boolean.hashCode(perTickUsage);
        result = 31 * result + output.hashCode();
        return result;
    }

    /// Helper to create a [MapCodec] for a simple [BasicItemStackChemicalToItemStackRecipe].
    ///
    /// @param factory   Recipe constructor
    /// @param bookCodec Map codec for serializing recipe book data
    ///
    /// @since 10.8.0
    public static <BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>,
          RECIPE extends BasicItemStackChemicalToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO>> MapCodec<RECIPE> simpleMapCodec(
          ItemStackChemicalToItemStackRecipe.Factory<BOOK_CATEGORY, BOOK_INFO, RECIPE> factory, MapCodec<BOOK_INFO> bookCodec
    ) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
              Recipe.CommonInfo.MAP_CODEC.forGetter(MekanismRecipe::commonInfo),
              bookCodec.forGetter(MekanismRecipe::bookInfo),
              ItemStackIngredient.CODEC.fieldOf(SerializationConstants.ITEM_INPUT).forGetter(ItemStackChemicalToItemStackRecipe::getItemInput),
              ChemicalStackIngredient.CODEC.fieldOf(SerializationConstants.CHEMICAL_INPUT).forGetter(ItemStackChemicalToItemStackRecipe::getChemicalInput),
              Codec.BOOL.fieldOf(SerializationConstants.PER_TICK_USAGE).forGetter(ItemStackChemicalToItemStackRecipe::perTickUsage),
              ItemStackTemplate.CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(recipe -> recipe.output)
        ).apply(instance, factory::create));
    }

    /// Helper to create a [StreamCodec] for a simple [BasicItemStackChemicalToItemStackRecipe].
    ///
    /// @param factory   Recipe constructor
    /// @param bookCodec Stream codec for serializing recipe book data
    ///
    /// @since 10.8.0
    public static <BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>,
          RECIPE extends BasicItemStackChemicalToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO>> StreamCodec<RegistryFriendlyByteBuf, RECIPE> simpleStreamCodec(
          ItemStackChemicalToItemStackRecipe.Factory<BOOK_CATEGORY, BOOK_INFO, RECIPE> factory, StreamCodec<RegistryFriendlyByteBuf, BOOK_INFO> bookCodec
    ) {
        return StreamCodec.composite(
              CommonInfo.STREAM_CODEC, MekanismRecipe::commonInfo,
              bookCodec, MekanismRecipe::bookInfo,
              ItemStackIngredient.STREAM_CODEC, ItemStackChemicalToItemStackRecipe::getItemInput,
              ChemicalStackIngredient.STREAM_CODEC, ItemStackChemicalToItemStackRecipe::getChemicalInput,
              ByteBufCodecs.BOOL, ItemStackChemicalToItemStackRecipe::perTickUsage,
              ItemStackTemplate.STREAM_CODEC, recipe -> recipe.output,
              factory::create
        );
    }
}
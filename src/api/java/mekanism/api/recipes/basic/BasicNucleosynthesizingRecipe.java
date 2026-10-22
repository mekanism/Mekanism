package mekanism.api.recipes.basic;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.recipes.ItemStackChemicalToObjectRecipe;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.api.recipes.book.NucleosynthesizingBookCategory.NucleosynthesizingBookInfo;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.TypedInstance;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public class BasicNucleosynthesizingRecipe extends NucleosynthesizingRecipe implements IBasicItemStackOutput {

    protected final ItemStackIngredient itemInput;
    protected final ChemicalStackIngredient chemicalInput;
    protected final ItemStackTemplate output;
    private final int duration;
    private final boolean perTickUsage;

    /// @param commonInfo    Common recipe info.
    /// @param bookInfo      Recipe book info.
    /// @param itemInput     Item input.
    /// @param chemicalInput Chemical input.
    /// @param perTickUsage  Should the recipe consume the chemical input each tick it is processing.
    /// @param output        Output.
    /// @param duration      Duration in ticks that it takes the recipe to complete. Must be greater than zero.
    public BasicNucleosynthesizingRecipe(CommonInfo commonInfo, NucleosynthesizingBookInfo bookInfo, ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput,
          boolean perTickUsage, ItemStackTemplate output, int duration) {
        this.itemInput = Objects.requireNonNull(itemInput, "Item input cannot be null.");
        this.chemicalInput = Objects.requireNonNull(chemicalInput, "Chemical input cannot be null.");
        this.perTickUsage = perTickUsage;
        this.output = Objects.requireNonNull(output, "Output cannot be null.");
        Preconditions.checkArgument(duration > 0, "Duration must be a number greater than zero.");
        this.duration = duration;
        super(commonInfo, bookInfo);
    }

    @Override
    public SlotDisplay getOutputDisplay() {
        return new SlotDisplay.ItemStackSlotDisplay(output);
    }

    @Override
    public int getDuration() {
        return duration;
    }

    @Override
    public boolean perTickUsage() {
        return perTickUsage;
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
    public List<ItemStackTemplate> getOutputDefinition(ContextMap contextMap) {
        return Collections.singletonList(output);
    }

    @Override
    public ItemStackTemplate getOutputRaw() {
        return output;
    }

    @Override
    public RecipeSerializer<BasicNucleosynthesizingRecipe> getSerializer() {
        return MekanismRecipeSerializers.NUCLEOSYNTHESIZING.get();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicNucleosynthesizingRecipe other = (BasicNucleosynthesizingRecipe) o;
        return duration == other.duration && perTickUsage == other.perTickUsage && itemInput.equals(other.itemInput) && chemicalInput.equals(other.chemicalInput) &&
               output.equals(other.output);
    }

    @Override
    public int hashCode() {
        int result = itemInput.hashCode();
        result = 31 * result + chemicalInput.hashCode();
        result = 31 * result + duration;
        result = 31 * result + Boolean.hashCode(perTickUsage);
        result = 31 * result + output.hashCode();
        return result;
    }

    /// Helper to create a [MapCodec] for a simple [BasicNucleosynthesizingRecipe].
    ///
    /// @param factory Recipe constructor
    ///
    /// @since 10.8.0
    public static <RECIPE extends BasicNucleosynthesizingRecipe> MapCodec<RECIPE> simpleMapCodec(NucleosynthesizingRecipe.Factory<RECIPE> factory) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
              Recipe.CommonInfo.MAP_CODEC.forGetter(MekanismRecipe::commonInfo),
              NucleosynthesizingBookInfo.MAP_CODEC.forGetter(MekanismRecipe::bookInfo),
              ItemStackIngredient.CODEC.fieldOf(SerializationConstants.ITEM_INPUT).forGetter(ItemStackChemicalToObjectRecipe::getItemInput),
              ChemicalStackIngredient.CODEC.fieldOf(SerializationConstants.CHEMICAL_INPUT).forGetter(ItemStackChemicalToObjectRecipe::getChemicalInput),
              Codec.BOOL.fieldOf(SerializationConstants.PER_TICK_USAGE).forGetter(ItemStackChemicalToObjectRecipe::perTickUsage),
              ItemStackTemplate.CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(recipe -> recipe.output),
              ExtraCodecs.POSITIVE_INT.fieldOf(SerializationConstants.DURATION).forGetter(NucleosynthesizingRecipe::getDuration)
        ).apply(instance, factory::create));
    }

    /// Helper to create a [StreamCodec] for a simple [BasicNucleosynthesizingRecipe].
    ///
    /// @param factory Recipe constructor
    ///
    /// @since 10.8.0
    public static <RECIPE extends BasicNucleosynthesizingRecipe> StreamCodec<RegistryFriendlyByteBuf, RECIPE> simpleStreamCodec(NucleosynthesizingRecipe.Factory<RECIPE> factory) {
        return StreamCodec.composite(
              CommonInfo.STREAM_CODEC, MekanismRecipe::commonInfo,
              NucleosynthesizingBookInfo.STREAM_CODEC, MekanismRecipe::bookInfo,
              ItemStackIngredient.STREAM_CODEC, ItemStackChemicalToObjectRecipe::getItemInput,
              ChemicalStackIngredient.STREAM_CODEC, ItemStackChemicalToObjectRecipe::getChemicalInput,
              ByteBufCodecs.BOOL, ItemStackChemicalToObjectRecipe::perTickUsage,
              ItemStackTemplate.STREAM_CODEC, recipe -> recipe.output,
              ByteBufCodecs.VAR_INT, NucleosynthesizingRecipe::getDuration,
              factory::create
        );
    }
}
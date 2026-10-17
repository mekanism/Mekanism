package mekanism.api.recipes.basic;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.SerializationConstants;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.book.EnergyConversionBookCategory.EnergyConversionBookInfo;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import org.jspecify.annotations.Nullable;

public class BasicItemStackToEnergyRecipe extends ItemStackToEnergyRecipe {

    protected final ItemStackIngredient input;
    protected final int output;

    /// @param commonInfo Common recipe info.
    /// @param bookInfo   Recipe book info.
    /// @param input      Input.
    /// @param output     Output, must be greater than zero.
    public BasicItemStackToEnergyRecipe(CommonInfo commonInfo, EnergyConversionBookInfo bookInfo, ItemStackIngredient input, int output) {
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        if (output <= 0) {
            throw new IllegalArgumentException("Output must be greater than zero.");
        }
        this.output = output;
        super(commonInfo, bookInfo);
    }

    @Override
    public boolean test(ItemStack itemStack) {
        return input.test(itemStack);
    }

    @Override
    public ItemStackIngredient getInput() {
        return input;
    }

    @Override
    public int getOutput(ItemStack input) {
        return output;
    }

    @Override
    public int[] getOutputDefinition(ContextMap contextMap) {
        return new int[]{output};
    }

    @Override
    public RecipeSerializer<BasicItemStackToEnergyRecipe> getSerializer() {
        return MekanismRecipeSerializers.ENERGY_CONVERSION.get();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicItemStackToEnergyRecipe other = (BasicItemStackToEnergyRecipe) o;
        return output == other.output && input.equals(other.input);
    }

    @Override
    public int hashCode() {
        int result = input.hashCode();
        result = 31 * result + output;
        return result;
    }

    @Override
    public List<RecipeDisplay> display() {
        return Collections.emptyList();
    }

    /// Helper to create a [MapCodec] for a simple [BasicItemStackToEnergyRecipe].
    ///
    /// @param factory Recipe constructor
    ///
    /// @since 10.8.0
    public static <RECIPE extends BasicItemStackToEnergyRecipe> MapCodec<RECIPE> simpleMapCodec(ItemStackToEnergyRecipe.Factory<RECIPE> factory) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
              Recipe.CommonInfo.MAP_CODEC.forGetter(MekanismRecipe::commonInfo),
              EnergyConversionBookInfo.MAP_CODEC.forGetter(MekanismRecipe::bookInfo),
              ItemStackIngredient.CODEC.fieldOf(SerializationConstants.INPUT).forGetter(ItemStackToEnergyRecipe::getInput),
              ExtraCodecs.POSITIVE_INT.fieldOf(SerializationConstants.OUTPUT).forGetter(recipe -> recipe.output)
        ).apply(instance, factory::create));
    }

    /// Helper to create a [StreamCodec] for a simple [BasicItemStackToEnergyRecipe].
    ///
    /// @param factory Recipe constructor
    ///
    /// @since 10.8.0
    public static <RECIPE extends BasicItemStackToEnergyRecipe> StreamCodec<RegistryFriendlyByteBuf, RECIPE> simpleStreamCodec(ItemStackToEnergyRecipe.Factory<RECIPE> factory) {
        return StreamCodec.composite(
              CommonInfo.STREAM_CODEC, MekanismRecipe::commonInfo,
              EnergyConversionBookInfo.STREAM_CODEC, MekanismRecipe::bookInfo,
              ItemStackIngredient.STREAM_CODEC, ItemStackToEnergyRecipe::getInput,
              ByteBufCodecs.VAR_INT, recipe -> recipe.output,
              factory::create
        );
    }
}
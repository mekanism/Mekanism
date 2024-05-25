package mekanism.common.recipe.serializer;

import com.mojang.datafixers.util.Function4;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import mekanism.api.SerializationConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.basic.BasicSawmillRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

@NothingNullByDefault
public class SawmillRecipeSerializer implements RecipeSerializer<BasicSawmillRecipe> {

    private final StreamCodec<RegistryFriendlyByteBuf, BasicSawmillRecipe> streamCodec;
    private final MapCodec<BasicSawmillRecipe> codec;

    public SawmillRecipeSerializer(Function4<ItemStackIngredient, ItemStack, ItemStack, Double, BasicSawmillRecipe> factory) {
        Codec<Double> chanceCodec = Codec.DOUBLE.validate(d -> d > 0 && d <= 1 ? DataResult.success(d) : DataResult.error(() -> "Expected secondaryChance to be greater than zero, and less than or equal to one. Found " + d));
        MapCodec<Optional<Double>> secondaryChanceFieldBase = chanceCodec.optionalFieldOf(SerializationConstants.SECONDARY_CHANCE);
        MapCodec<Optional<ItemStack>> mainOutputFieldBase = ItemStack.CODEC.optionalFieldOf(SerializationConstants.MAIN_OUTPUT);
        RecordCodecBuilder<BasicSawmillRecipe, Optional<ItemStack>> secondaryOutputField = ItemStack.CODEC.optionalFieldOf(SerializationConstants.SECONDARY_OUTPUT).forGetter(BasicSawmillRecipe::getSecondaryOutputRaw);

        this.codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
              ItemStackIngredient.CODEC.fieldOf(SerializationConstants.INPUT).forGetter(SawmillRecipe::getInput),
              SerializerHelper.oneRequired(secondaryOutputField, mainOutputFieldBase, BasicSawmillRecipe::getMainOutputRaw),
              secondaryOutputField,
              SerializerHelper.dependentOptionality(secondaryOutputField, secondaryChanceFieldBase, sawmillRecipe -> {
                  double secondaryChance = sawmillRecipe.getSecondaryChance();
                  return secondaryChance == 0 ? Optional.empty() : Optional.of(secondaryChance);
              })
        ).apply(instance, (input, mainOutput, secondaryOutput, secondChance) ->
              factory.apply(input, mainOutput.orElse(ItemStack.EMPTY), secondaryOutput.orElse(ItemStack.EMPTY), secondChance.orElse(0D))
        ));
        this.streamCodec = StreamCodec.composite(
              ItemStackIngredient.STREAM_CODEC, SawmillRecipe::getInput,
              ItemStack.OPTIONAL_STREAM_CODEC, (BasicSawmillRecipe recipe) -> recipe.getMainOutputRaw().orElse(ItemStack.EMPTY),
              ItemStack.OPTIONAL_STREAM_CODEC, (BasicSawmillRecipe recipe) -> recipe.getSecondaryOutputRaw().orElse(ItemStack.EMPTY),
              ByteBufCodecs.DOUBLE, SawmillRecipe::getSecondaryChance,
              factory
        );
    }

    @Override
    public MapCodec<BasicSawmillRecipe> codec() {
        return codec;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, BasicSawmillRecipe> streamCodec() {
        return streamCodec;
    }
}
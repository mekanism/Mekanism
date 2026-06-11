package mekanism.common.recipe.serializer;

import com.mojang.datafixers.util.Function4;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import mekanism.api.SerializationConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.basic.BasicSawmillRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jspecify.annotations.Nullable;

public class SawmillRecipeSerializer {

    public static RecipeSerializer<BasicSawmillRecipe> create(Function4<ItemStackIngredient, @Nullable ItemStackTemplate, @Nullable ItemStackTemplate, Double, BasicSawmillRecipe> factory) {
        Codec<Double> chanceCodec = Codec.DOUBLE.validate(d -> d > 0 && d <= 1 ? DataResult.success(d) : DataResult.error(() -> "Expected secondaryChance to be greater than zero, and less than or equal to one. Found " + d));
        MapCodec<Optional<Double>> secondaryChanceFieldBase = chanceCodec.optionalFieldOf(SerializationConstants.SECONDARY_CHANCE);
        MapCodec<Optional<ItemStackTemplate>> mainOutputFieldBase = ItemStackTemplate.CODEC.optionalFieldOf(SerializationConstants.MAIN_OUTPUT);
        RecordCodecBuilder<BasicSawmillRecipe, Optional<ItemStackTemplate>> secondaryOutputField = ItemStackTemplate.CODEC.optionalFieldOf(SerializationConstants.SECONDARY_OUTPUT).forGetter(BasicSawmillRecipe::getSecondaryOutputRaw);

        MapCodec<BasicSawmillRecipe> codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
              ItemStackIngredient.CODEC.fieldOf(SerializationConstants.INPUT).forGetter(SawmillRecipe::getInput),
              SerializerHelper.oneRequired(secondaryOutputField, mainOutputFieldBase, BasicSawmillRecipe::getMainOutputRaw),
              secondaryOutputField,
              SerializerHelper.dependentOptionality(secondaryOutputField, secondaryChanceFieldBase, sawmillRecipe -> {
                  double secondaryChance = sawmillRecipe.getSecondaryChance();
                  return secondaryChance == 0 ? Optional.empty() : Optional.of(secondaryChance);
              })
        ).apply(instance, (input, mainOutput, secondaryOutput, secondChance) ->
              factory.apply(input, mainOutput.orElse(null), secondaryOutput.orElse(null), secondChance.orElse(0D))
        ));
        StreamCodec<RegistryFriendlyByteBuf, BasicSawmillRecipe> streamCodec = StreamCodec.composite(
              ItemStackIngredient.STREAM_CODEC, SawmillRecipe::getInput,
              ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC), BasicSawmillRecipe::getMainOutputRaw,
              ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC), BasicSawmillRecipe::getSecondaryOutputRaw,
              ByteBufCodecs.DOUBLE, SawmillRecipe::getSecondaryChance,
              (input, mainOutput, secondaryOutput, secondChance) ->
                    factory.apply(input, mainOutput.orElse(null), secondaryOutput.orElse(null), secondChance)
        );

        return new RecipeSerializer<>(codec, streamCodec);
    }

}
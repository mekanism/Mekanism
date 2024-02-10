package mekanism.common.recipe.serializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.basic.BasicSawmillRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class SawmillRecipeSerializer implements RecipeSerializer<BasicSawmillRecipe> {

    private final IFactory<BasicSawmillRecipe> factory;
    private Codec<BasicSawmillRecipe> codec;

    public SawmillRecipeSerializer(IFactory<BasicSawmillRecipe> factory) {
        this.factory = factory;
    }

    @NotNull
    @Override
    public Codec<BasicSawmillRecipe> codec() {
        if (codec == null) {
            Codec<Double> chanceCodec = ExtraCodecs.validate(Codec.DOUBLE, d -> d > 0 && d <= 1 ? DataResult.success(d) : DataResult.error(() -> "Expected secondaryChance to be greater than zero, and less than or equal to one. Found " + d));
            MapCodec<Optional<Double>> secondaryChanceFieldBase = ExtraCodecs.strictOptionalField(chanceCodec, JsonConstants.SECONDARY_CHANCE);
            MapCodec<Optional<ItemStack>> mainOutputFieldBase = ItemStack.ITEM_WITH_COUNT_CODEC.optionalFieldOf(JsonConstants.MAIN_OUTPUT);
            RecordCodecBuilder<BasicSawmillRecipe, Optional<ItemStack>> secondaryOutputField = ItemStack.ITEM_WITH_COUNT_CODEC.optionalFieldOf(JsonConstants.SECONDARY_OUTPUT).forGetter(BasicSawmillRecipe::getSecondaryOutputRaw);

            codec = RecordCodecBuilder.create(instance -> instance.group(
                  IngredientCreatorAccess.item().codec().fieldOf(JsonConstants.INPUT).forGetter(SawmillRecipe::getInput),
                  SerializerHelper.oneRequired(secondaryOutputField, mainOutputFieldBase, BasicSawmillRecipe::getMainOutputRaw),
                  secondaryOutputField,
                  SerializerHelper.dependentOptionality(secondaryOutputField, secondaryChanceFieldBase, sawmillRecipe -> {
                      double secondaryChance = sawmillRecipe.getSecondaryChance();
                      return secondaryChance == 0 ? Optional.empty() : Optional.of(secondaryChance);
                  })
            ).apply(instance,
                  (input, mainOutput, secondaryOutput, secondChance) -> factory.create(input, mainOutput.orElse(ItemStack.EMPTY), secondaryOutput.orElse(ItemStack.EMPTY), secondChance.orElse(0D))
            ));
        }
        return codec;
    }

    @NotNull
    @Override
    public BasicSawmillRecipe fromNetwork(@NotNull FriendlyByteBuf buffer) {
        ItemStackIngredient inputIngredient = IngredientCreatorAccess.item().read(buffer);
        ItemStack mainOutput = buffer.readItem();
        ItemStack secondaryOutput = buffer.readItem();
        double secondaryChance = buffer.readDouble();
        return this.factory.create(inputIngredient, mainOutput, secondaryOutput, secondaryChance);
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull BasicSawmillRecipe recipe) {
        recipe.getInput().write(buffer);
        buffer.writeItem(recipe.getMainOutputRaw().orElse(ItemStack.EMPTY));
        buffer.writeItem(recipe.getSecondaryOutputRaw().orElse(ItemStack.EMPTY));
        buffer.writeDouble(recipe.getSecondaryChance());
    }

    @FunctionalInterface
    public interface IFactory<RECIPE extends BasicSawmillRecipe> {

        RECIPE create(ItemStackIngredient input, ItemStack mainOutput, ItemStack secondaryOutput, double secondaryChance);
    }
}
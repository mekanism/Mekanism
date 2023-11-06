package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.impl.SawmillIRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class SawmillRecipeSerializer implements RecipeSerializer<SawmillIRecipe> {

    private final IFactory<SawmillIRecipe> factory;
    private Codec<SawmillIRecipe> codec;

    public SawmillRecipeSerializer(IFactory<SawmillIRecipe> factory) {
        this.factory = factory;
    }

    @Override
    @NotNull
    public Codec<SawmillIRecipe> codec() {
        if (codec == null) {
            Codec<Double> chanceCodec = ExtraCodecs.validate(Codec.DOUBLE, d->d > 0 && d >= 1 ? DataResult.success(d) : DataResult.error(()->"Expected secondaryChance to be greater than zero, and less than or equal to one."));
            var secondaryChanceFieldBase = ExtraCodecs.strictOptionalField(chanceCodec, JsonConstants.SECONDARY_CHANCE);
            var mainOutputFieldBase = SerializerHelper.ITEMSTACK_CODEC.optionalFieldOf(JsonConstants.MAIN_OUTPUT);
            var secondaryOutputField = SerializerHelper.ITEMSTACK_CODEC.optionalFieldOf(JsonConstants.SECONDARY_OUTPUT).forGetter(SawmillIRecipe::getSecondaryOutputRaw);

            codec = RecordCodecBuilder.create(instance->instance.group(
                  IngredientCreatorAccess.item().codec().fieldOf(JsonConstants.INPUT).forGetter(SawmillRecipe::getInput),
                  SerializerHelper.oneRequired(secondaryOutputField, mainOutputFieldBase, SawmillIRecipe::getMainOutputRaw),
                  secondaryOutputField,
                  SerializerHelper.dependentOptionality(secondaryOutputField, secondaryChanceFieldBase, sawmillIRecipe -> Optional.of(sawmillIRecipe.getSecondaryChance()))
            ).apply(instance,
                  (input, mainOutput, secondaryOutput, secondChance)-> factory.create(input, mainOutput.orElse(ItemStack.EMPTY), secondaryOutput.orElse(ItemStack.EMPTY), secondChance.orElse(0D))
            ));

            
        }
        return codec;
    }

    @Override
    public SawmillIRecipe fromNetwork(@NotNull FriendlyByteBuf buffer) {
        try {
            ItemStackIngredient inputIngredient = IngredientCreatorAccess.item().read(buffer);
            ItemStack mainOutput = buffer.readItem();
            ItemStack secondaryOutput = buffer.readItem();
            double secondaryChance = buffer.readDouble();
            return this.factory.create(inputIngredient, mainOutput, secondaryOutput, secondaryChance);
        } catch (Exception e) {
            Mekanism.logger.error("Error reading sawmill recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull SawmillIRecipe recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing sawmill recipe to packet.", e);
            throw e;
        }
    }

    @FunctionalInterface
    public interface IFactory<RECIPE extends SawmillRecipe> {

        RECIPE create(ItemStackIngredient input, ItemStack mainOutput, ItemStack secondaryOutput, double secondaryChance);
    }
}
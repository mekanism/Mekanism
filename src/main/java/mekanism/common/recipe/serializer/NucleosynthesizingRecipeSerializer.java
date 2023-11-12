package mekanism.common.recipe.serializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.impl.NucleosynthesizingIRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class NucleosynthesizingRecipeSerializer implements RecipeSerializer<NucleosynthesizingIRecipe> {

    private final IFactory<NucleosynthesizingIRecipe> factory;
    private Codec<NucleosynthesizingIRecipe> codec;

    public NucleosynthesizingRecipeSerializer(IFactory<NucleosynthesizingIRecipe> factory) {
        this.factory = factory;
    }

    @NotNull
    @Override
    public Codec<NucleosynthesizingIRecipe> codec() {
        if (codec == null) {
            codec = RecordCodecBuilder.create(instance->instance.group(
                  IngredientCreatorAccess.item().codec().fieldOf(JsonConstants.ITEM_INPUT).forGetter(NucleosynthesizingRecipe::getItemInput),
                  IngredientCreatorAccess.gas().codec().fieldOf(JsonConstants.GAS_INPUT).forGetter(NucleosynthesizingRecipe::getChemicalInput),
                  SerializerHelper.ITEMSTACK_CODEC.fieldOf(JsonConstants.OUTPUT).forGetter(NucleosynthesizingIRecipe::getOutputRaw),
                  ExtraCodecs.POSITIVE_INT.fieldOf(JsonConstants.DURATION).forGetter(NucleosynthesizingRecipe::getDuration)
            ).apply(instance, factory::create));
        }
        return codec;
    }

    @Override
    public NucleosynthesizingIRecipe fromNetwork(@NotNull FriendlyByteBuf buffer) {
        try {
            ItemStackIngredient inputSolid = IngredientCreatorAccess.item().read(buffer);
            GasStackIngredient inputGas = IngredientCreatorAccess.gas().read(buffer);
            ItemStack outputItem = buffer.readItem();
            int duration = buffer.readVarInt();
            return this.factory.create(inputSolid, inputGas, outputItem, duration);
        } catch (Exception e) {
            Mekanism.logger.error("Error reading nucleosynthesizing recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull NucleosynthesizingIRecipe recipe) {
        try {
            recipe.getItemInput().write(buffer);
            recipe.getChemicalInput().write(buffer);
            buffer.writeItem(recipe.getOutputRaw());
            buffer.writeVarInt(recipe.getDuration());
        } catch (Exception e) {
            Mekanism.logger.error("Error writing nucleosynthesizing recipe to packet.", e);
            throw e;
        }
    }

    @FunctionalInterface
    public interface IFactory<RECIPE extends NucleosynthesizingRecipe> {

        RECIPE create(ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack outputItem, int duration);
    }
}
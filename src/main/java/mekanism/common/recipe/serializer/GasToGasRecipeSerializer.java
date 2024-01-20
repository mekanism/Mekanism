package mekanism.common.recipe.serializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.JsonConstants;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.basic.BasicGasToGasRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class GasToGasRecipeSerializer<RECIPE extends BasicGasToGasRecipe> implements RecipeSerializer<RECIPE> {

    private final IFactory<RECIPE> factory;
    private Codec<RECIPE> codec;

    public GasToGasRecipeSerializer(IFactory<RECIPE> factory) {
        this.factory = factory;
    }

    @NotNull
    @Override
    public Codec<RECIPE> codec() {
        if (codec == null) {
            codec = RecordCodecBuilder.create(instance -> instance.group(
                  IngredientCreatorAccess.gas().codec().fieldOf(JsonConstants.INPUT).forGetter(GasToGasRecipe::getInput),
                  ChemicalUtils.GAS_STACK_CODEC.fieldOf(JsonConstants.OUTPUT).forGetter(BasicGasToGasRecipe::getOutputRaw)
            ).apply(instance, factory::create));
        }
        return codec;
    }

    @NotNull
    @Override
    public RECIPE fromNetwork(@NotNull FriendlyByteBuf buffer) {
        GasStackIngredient inputIngredient = IngredientCreatorAccess.gas().read(buffer);
        GasStack output = GasStack.readFromPacket(buffer);
        return this.factory.create(inputIngredient, output);
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull RECIPE recipe) {
        recipe.getInput().write(buffer);
        recipe.getOutputRaw().writeToPacket(buffer);
    }

    @FunctionalInterface
    public interface IFactory<RECIPE extends GasToGasRecipe> {

        RECIPE create(GasStackIngredient input, GasStack output);
    }
}
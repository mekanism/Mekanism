package mekanism.common.recipe.serializer;

import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.api.recipes.inputs.chemical.ChemicalIngredientDeserializer;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import net.minecraft.network.PacketBuffer;

public class ChemicalInfuserRecipeSerializer<RECIPE extends ChemicalInfuserRecipe> extends
      ChemicalChemicalToChemicalRecipeSerializer<Gas, GasStack, GasStackIngredient, RECIPE> {

    public ChemicalInfuserRecipeSerializer(IFactory<Gas, GasStack, GasStackIngredient, RECIPE> factory) {
        super(factory);
    }

    @Override
    protected ChemicalIngredientDeserializer<Gas, GasStack, GasStackIngredient> getDeserializer() {
        return ChemicalIngredientDeserializer.GAS;
    }

    @Override
    protected GasStack fromJson(@Nonnull JsonObject json, @Nonnull String key) {
        return SerializerHelper.getGasStack(json, key);
    }

    @Override
    protected GasStack fromBuffer(@Nonnull PacketBuffer buffer) {
        return GasStack.readFromPacket(buffer);
    }
}
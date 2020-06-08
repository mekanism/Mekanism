package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.JsonConstants;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import net.minecraft.util.ResourceLocation;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemStackToEnergyRecipeBuilder extends MekanismRecipeBuilder<ItemStackToEnergyRecipeBuilder> {

    private final ItemStackIngredient input;
    private final FloatingLong output;

    protected ItemStackToEnergyRecipeBuilder(ItemStackIngredient input, FloatingLong output, ResourceLocation serializerName) {
        super(serializerName);
        this.input = input;
        this.output = output;
    }

    public static ItemStackToEnergyRecipeBuilder energyConversion(ItemStackIngredient input, FloatingLong output) {
        if (output.isZero()) {
            throw new IllegalArgumentException("This energy conversion recipe requires an energy output greater than zero");
        }
        return new ItemStackToEnergyRecipeBuilder(input, output, mekSerializer("energy_conversion"));
    }

    @Override
    protected ItemStackToEnergyRecipeResult getResult(ResourceLocation id) {
        return new ItemStackToEnergyRecipeResult(id);
    }

    public class ItemStackToEnergyRecipeResult extends RecipeResult {

        protected ItemStackToEnergyRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serialize(@Nonnull JsonObject json) {
            json.add(JsonConstants.INPUT, input.serialize());
            json.addProperty(JsonConstants.OUTPUT, output);
        }
    }
}
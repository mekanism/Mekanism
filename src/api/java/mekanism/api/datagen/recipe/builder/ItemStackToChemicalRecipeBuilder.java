package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.JsonConstants;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.ChemicalIngredientDeserializer;
import net.minecraft.util.ResourceLocation;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemStackToChemicalRecipeBuilder<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> extends
      MekanismRecipeBuilder<ItemStackToChemicalRecipeBuilder<CHEMICAL, STACK>> {

    private final ChemicalIngredientDeserializer<CHEMICAL, STACK, ?> outputSerializer;
    private final ItemStackIngredient input;
    private final STACK output;

    protected ItemStackToChemicalRecipeBuilder(ResourceLocation serializerName, ItemStackIngredient input, STACK output,
          ChemicalIngredientDeserializer<CHEMICAL, STACK, ?> outputSerializer) {
        super(serializerName);
        this.input = input;
        this.output = output;
        this.outputSerializer = outputSerializer;
    }

    public static ItemStackToChemicalRecipeBuilder<Gas, GasStack> gasConversion(ItemStackIngredient input, GasStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This gas conversion recipe requires a non empty gas output.");
        }
        return new ItemStackToChemicalRecipeBuilder<>(mekSerializer("gas_conversion"), input, output, ChemicalIngredientDeserializer.GAS);
    }

    public static ItemStackToChemicalRecipeBuilder<Gas, GasStack> oxidizing(ItemStackIngredient input, GasStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This oxidizing recipe requires a non empty gas output.");
        }
        return new ItemStackToChemicalRecipeBuilder<>(mekSerializer("oxidizing"), input, output, ChemicalIngredientDeserializer.GAS);
    }

    public static ItemStackToChemicalRecipeBuilder<InfuseType, InfusionStack> infusionConversion(ItemStackIngredient input, InfusionStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This infusion conversion recipe requires a non empty infusion output.");
        }
        return new ItemStackToChemicalRecipeBuilder<>(mekSerializer("infusion_conversion"), input, output, ChemicalIngredientDeserializer.INFUSION);
    }

    @Override
    protected ItemStackToChemicalRecipeResult getResult(ResourceLocation id) {
        return new ItemStackToChemicalRecipeResult(id);
    }

    public class ItemStackToChemicalRecipeResult extends RecipeResult {

        protected ItemStackToChemicalRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serialize(@Nonnull JsonObject json) {
            json.add(JsonConstants.INPUT, input.serialize());
            json.add(JsonConstants.OUTPUT, outputSerializer.serializeStack(output));
        }
    }
}
package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.function.Function;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class ItemStackToChemicalRecipeBuilder<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> extends
      MekanismRecipeBuilder<ItemStackToChemicalRecipeBuilder<CHEMICAL, STACK>> {

    private final Function<STACK, JsonElement> outputSerializer;
    private final ItemStackIngredient input;
    private final STACK output;

    protected ItemStackToChemicalRecipeBuilder(ResourceLocation serializerName, ItemStackIngredient input, STACK output, Function<STACK, JsonElement> outputSerializer) {
        super(serializerName);
        this.input = input;
        this.output = output;
        this.outputSerializer = outputSerializer;
    }

    /**
     * Creates a Gas Conversion recipe builder.
     *
     * @param input  Input.
     * @param output Output.
     */
    public static ItemStackToChemicalRecipeBuilder<Gas, GasStack> gasConversion(ItemStackIngredient input, GasStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This gas conversion recipe requires a non empty gas output.");
        }
        return new ItemStackToChemicalRecipeBuilder<>(mekSerializer("gas_conversion"), input, output, SerializerHelper::serializeGasStack);
    }

    /**
     * Creates an Oxidizing recipe builder.
     *
     * @param input  Input.
     * @param output Output.
     */
    public static ItemStackToChemicalRecipeBuilder<Gas, GasStack> oxidizing(ItemStackIngredient input, GasStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This oxidizing recipe requires a non empty gas output.");
        }
        return new ItemStackToChemicalRecipeBuilder<>(mekSerializer("oxidizing"), input, output, SerializerHelper::serializeGasStack);
    }

    /**
     * Creates an Infusion Conversion recipe builder.
     *
     * @param input  Input.
     * @param output Output.
     */
    public static ItemStackToChemicalRecipeBuilder<InfuseType, InfusionStack> infusionConversion(ItemStackIngredient input, InfusionStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This infusion conversion recipe requires a non empty infusion output.");
        }
        return new ItemStackToChemicalRecipeBuilder<>(mekSerializer("infusion_conversion"), input, output, SerializerHelper::serializeInfusionStack);
    }

    /**
     * Creates a Pigment Extracting recipe builder.
     *
     * @param input  Input.
     * @param output Output.
     */
    public static ItemStackToChemicalRecipeBuilder<Pigment, PigmentStack> pigmentExtracting(ItemStackIngredient input, PigmentStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This pigment extracting recipe requires a non empty pigment output.");
        }
        return new ItemStackToChemicalRecipeBuilder<>(mekSerializer("pigment_extracting"), input, output, SerializerHelper::serializePigmentStack);
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
        public void serializeRecipeData(@NotNull JsonObject json) {
            json.add(JsonConstants.INPUT, input.serialize());
            json.add(JsonConstants.OUTPUT, outputSerializer.apply(output));
        }
    }
}
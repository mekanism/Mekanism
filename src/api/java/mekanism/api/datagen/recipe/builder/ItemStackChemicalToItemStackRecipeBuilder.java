package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.api.recipes.inputs.chemical.IChemicalStackIngredient;
import mekanism.api.recipes.inputs.chemical.InfusionStackIngredient;
import mekanism.api.recipes.inputs.chemical.PigmentStackIngredient;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemStackChemicalToItemStackRecipeBuilder<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends IChemicalStackIngredient<CHEMICAL, STACK>> extends MekanismRecipeBuilder<ItemStackChemicalToItemStackRecipeBuilder<CHEMICAL, STACK, INGREDIENT>> {

    private final ItemStackIngredient itemInput;
    private final INGREDIENT chemicalInput;
    private final ItemStack output;
    //TODO - 1.18: Just inline this as JsonConstants.CHEMICAL_INPUT
    private final String chemicalInputKey;

    protected ItemStackChemicalToItemStackRecipeBuilder(ResourceLocation serializerName, ItemStackIngredient itemInput, INGREDIENT chemicalInput, ItemStack output,
          String chemicalInputKey) {
        super(serializerName);
        this.itemInput = itemInput;
        this.chemicalInput = chemicalInput;
        this.output = output;
        this.chemicalInputKey = chemicalInputKey;
    }

    /**
     * Creates a Compressing recipe builder.
     *
     * @param itemInput Item Input.
     * @param gasInput  Gas Input, used at a constant rate over the duration of the recipe.
     * @param output    Output.
     */
    public static ItemStackChemicalToItemStackRecipeBuilder<Gas, GasStack, GasStackIngredient> compressing(ItemStackIngredient itemInput, GasStackIngredient gasInput,
          ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This compressing recipe requires a non empty item output.");
        }
        return new ItemStackChemicalToItemStackRecipeBuilder<>(mekSerializer("compressing"), itemInput, gasInput, output, JsonConstants.GAS_INPUT);
    }

    /**
     * Creates a Purifying recipe builder.
     *
     * @param itemInput Item Input.
     * @param gasInput  Gas Input, used at a near constant rate over the duration of the recipe.
     * @param output    Output.
     */
    public static ItemStackChemicalToItemStackRecipeBuilder<Gas, GasStack, GasStackIngredient> purifying(ItemStackIngredient itemInput, GasStackIngredient gasInput,
          ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This purifying recipe requires a non empty item output.");
        }
        return new ItemStackChemicalToItemStackRecipeBuilder<>(mekSerializer("purifying"), itemInput, gasInput, output, JsonConstants.GAS_INPUT);
    }

    /**
     * Creates an Injecting recipe builder.
     *
     * @param itemInput Item Input.
     * @param gasInput  Gas Input, used at a near constant rate over the duration of the recipe.
     * @param output    Output.
     */
    public static ItemStackChemicalToItemStackRecipeBuilder<Gas, GasStack, GasStackIngredient> injecting(ItemStackIngredient itemInput, GasStackIngredient gasInput,
          ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This injecting recipe requires a non empty item output.");
        }
        return new ItemStackChemicalToItemStackRecipeBuilder<>(mekSerializer("injecting"), itemInput, gasInput, output, JsonConstants.GAS_INPUT);
    }

    /**
     * Creates a Metallurgic Infusing recipe builder.
     *
     * @param itemInput     Item Input.
     * @param infusionInput Infusion Input.
     * @param output        Output.
     */
    public static ItemStackChemicalToItemStackRecipeBuilder<InfuseType, InfusionStack, InfusionStackIngredient> metallurgicInfusing(ItemStackIngredient itemInput,
          InfusionStackIngredient infusionInput, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This metallurgic infusing recipe requires a non empty output.");
        }
        return new ItemStackChemicalToItemStackRecipeBuilder<>(mekSerializer("metallurgic_infusing"), itemInput, infusionInput, output, JsonConstants.INFUSION_INPUT);
    }

    /**
     * Creates a Painting recipe builder.
     *
     * @param itemInput    Item Input.
     * @param pigmentInput Pigment Input.
     * @param output       Output.
     */
    public static ItemStackChemicalToItemStackRecipeBuilder<Pigment, PigmentStack, PigmentStackIngredient> painting(ItemStackIngredient itemInput,
          PigmentStackIngredient pigmentInput, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This painting recipe requires a non empty item output.");
        }
        return new ItemStackChemicalToItemStackRecipeBuilder<>(mekSerializer("painting"), itemInput, pigmentInput, output, JsonConstants.CHEMICAL_INPUT);
    }

    @Override
    protected ItemStackChemicalToItemStackRecipeResult getResult(ResourceLocation id) {
        return new ItemStackChemicalToItemStackRecipeResult(id);
    }

    /**
     * Builds this recipe using the output item's name as the recipe name.
     *
     * @param consumer Finished Recipe Consumer.
     */
    public void build(Consumer<IFinishedRecipe> consumer) {
        build(consumer, output.getItem().getRegistryName());
    }

    public class ItemStackChemicalToItemStackRecipeResult extends RecipeResult {

        protected ItemStackChemicalToItemStackRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serializeRecipeData(@Nonnull JsonObject json) {
            json.add(JsonConstants.ITEM_INPUT, itemInput.serialize());
            json.add(chemicalInputKey, chemicalInput.serialize());
            json.add(JsonConstants.OUTPUT, SerializerHelper.serializeItemStack(output));
        }
    }
}
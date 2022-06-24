package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
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
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.InfusionStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.PigmentStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class ItemStackChemicalToItemStackRecipeBuilder<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>> extends MekanismRecipeBuilder<ItemStackChemicalToItemStackRecipeBuilder<CHEMICAL, STACK, INGREDIENT>> {

    private final ItemStackIngredient itemInput;
    private final INGREDIENT chemicalInput;
    private final ItemStack output;

    protected ItemStackChemicalToItemStackRecipeBuilder(ResourceLocation serializerName, ItemStackIngredient itemInput, INGREDIENT chemicalInput, ItemStack output) {
        super(serializerName);
        this.itemInput = itemInput;
        this.chemicalInput = chemicalInput;
        this.output = output;
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
        return new ItemStackChemicalToItemStackRecipeBuilder<>(mekSerializer("compressing"), itemInput, gasInput, output);
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
        return new ItemStackChemicalToItemStackRecipeBuilder<>(mekSerializer("purifying"), itemInput, gasInput, output);
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
        return new ItemStackChemicalToItemStackRecipeBuilder<>(mekSerializer("injecting"), itemInput, gasInput, output);
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
        return new ItemStackChemicalToItemStackRecipeBuilder<>(mekSerializer("metallurgic_infusing"), itemInput, infusionInput, output);
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
        return new ItemStackChemicalToItemStackRecipeBuilder<>(mekSerializer("painting"), itemInput, pigmentInput, output);
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
    public void build(Consumer<FinishedRecipe> consumer) {
        build(consumer, output.getItem());
    }

    public class ItemStackChemicalToItemStackRecipeResult extends RecipeResult {

        protected ItemStackChemicalToItemStackRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serializeRecipeData(@NotNull JsonObject json) {
            json.add(JsonConstants.ITEM_INPUT, itemInput.serialize());
            json.add(JsonConstants.CHEMICAL_INPUT, chemicalInput.serialize());
            json.add(JsonConstants.OUTPUT, SerializerHelper.serializeItemStack(output));
        }
    }
}
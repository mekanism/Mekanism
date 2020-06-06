package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemStackGasToItemStackRecipeBuilder extends MekanismRecipeBuilder<ItemStackGasToItemStackRecipeBuilder> {

    private final ItemStackIngredient itemInput;
    private final GasStackIngredient gasInput;
    private final ItemStack output;

    protected ItemStackGasToItemStackRecipeBuilder(ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack output, ResourceLocation serializerName) {
        super(serializerName);
        this.itemInput = itemInput;
        this.gasInput = gasInput;
        this.output = output;
    }

    public static ItemStackGasToItemStackRecipeBuilder compressing(ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This compressing recipe requires a non empty item output.");
        }
        return new ItemStackGasToItemStackRecipeBuilder(itemInput, gasInput, output, mekSerializer("compressing"));
    }

    public static ItemStackGasToItemStackRecipeBuilder purifying(ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This purifying recipe requires a non empty item output.");
        }
        return new ItemStackGasToItemStackRecipeBuilder(itemInput, gasInput, output, mekSerializer("purifying"));
    }

    public static ItemStackGasToItemStackRecipeBuilder injecting(ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This injecting recipe requires a non empty item output.");
        }
        return new ItemStackGasToItemStackRecipeBuilder(itemInput, gasInput, output, mekSerializer("injecting"));
    }

    @Override
    protected ItemStackGasToItemStackRecipeResult getResult(ResourceLocation id) {
        return new ItemStackGasToItemStackRecipeResult(id);
    }

    public void build(Consumer<IFinishedRecipe> consumer) {
        build(consumer, output.getItem().getRegistryName());
    }

    public class ItemStackGasToItemStackRecipeResult extends RecipeResult {

        protected ItemStackGasToItemStackRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serialize(@Nonnull JsonObject json) {
            json.add(JsonConstants.ITEM_INPUT, itemInput.serialize());
            json.add(JsonConstants.GAS_INPUT, gasInput.serialize());
            json.add(JsonConstants.OUTPUT, SerializerHelper.serializeItemStack(output));
        }
    }
}
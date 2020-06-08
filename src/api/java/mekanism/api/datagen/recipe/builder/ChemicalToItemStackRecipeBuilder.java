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
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.api.recipes.inputs.chemical.IChemicalStackIngredient;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ChemicalToItemStackRecipeBuilder<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends IChemicalStackIngredient<CHEMICAL, STACK>> extends MekanismRecipeBuilder<ChemicalToItemStackRecipeBuilder<CHEMICAL, STACK, INGREDIENT>> {

    private final INGREDIENT input;
    private final ItemStack output;

    protected ChemicalToItemStackRecipeBuilder(ResourceLocation serializerName, INGREDIENT input, ItemStack output) {
        super(serializerName);
        this.input = input;
        this.output = output;
    }

    public static ChemicalToItemStackRecipeBuilder<Gas, GasStack, GasStackIngredient> crystallizing(GasStackIngredient input, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This crystallizing recipe requires a non empty item output.");
        }
        return new ChemicalToItemStackRecipeBuilder<>(mekSerializer("crystallizing"), input, output);
    }

    @Override
    protected ChemicalToItemStackRecipeResult getResult(ResourceLocation id) {
        return new ChemicalToItemStackRecipeResult(id);
    }

    public void build(Consumer<IFinishedRecipe> consumer) {
        build(consumer, output.getItem().getRegistryName());
    }

    public class ChemicalToItemStackRecipeResult extends RecipeResult {

        protected ChemicalToItemStackRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serialize(@Nonnull JsonObject json) {
            json.add(JsonConstants.INPUT, input.serialize());
            json.add(JsonConstants.OUTPUT, SerializerHelper.serializeItemStack(output));
        }
    }
}
package mekanism.api.recipes;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.api.recipes.outputs.OreDictSupplier;
import mekanism.common.util.FieldsAreNonnullByDefault;
import net.minecraft.item.ItemStack;

/**
 * Created by Thiakil on 14/07/2019.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@FieldsAreNonnullByDefault
public class ChemicalCrystallizerRecipe implements Predicate<@NonNull GasStack>{

    private final GasStackIngredient input;

    private final ItemStack outputRepresentation;

    public ChemicalCrystallizerRecipe(GasStackIngredient input, ItemStack outputRepresentation) {
        this.input = input;
        this.outputRepresentation = outputRepresentation.copy();
    }

    public ItemStack getOutput(@NonNull GasStack input) {
        return outputRepresentation.copy();
    }

    public Collection<ItemStack> getOutputDefinition() {
        return Collections.singleton(outputRepresentation);
    }

    @Override
    public boolean test(@NonNull GasStack gasStack) {
        return input.test(gasStack);
    }

    public GasStackIngredient getInput() {
        return input;
    }

    public static class ChemicalCrystallizerRecipeOre extends ChemicalCrystallizerRecipe {

        private final OreDictSupplier outputSupplier;

        public ChemicalCrystallizerRecipeOre(GasStackIngredient input, String outputOreName) {
            super(input, ItemStack.EMPTY);
            this.outputSupplier = new OreDictSupplier(outputOreName);
        }

        @Override
        public ItemStack getOutput(@NonNull GasStack input) {
            return outputSupplier.get();
        }

        @Override
        public Collection<ItemStack> getOutputDefinition() {
            return outputSupplier.getPossibleOutputs();
        }
    }
}

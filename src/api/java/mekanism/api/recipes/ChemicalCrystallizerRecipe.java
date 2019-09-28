package mekanism.api.recipes;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.api.recipes.outputs.OreDictSupplier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.Tag;

/**
 * Created by Thiakil on 14/07/2019.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@FieldsAreNonnullByDefault
//TODO: Rename this to GasToItemStackIngredient
public class ChemicalCrystallizerRecipe implements IMekanismRecipe, Predicate<@NonNull GasStack> {

    private final GasStackIngredient input;
    private final ItemStack outputRepresentation;

    public ChemicalCrystallizerRecipe(GasStackIngredient input, ItemStack outputRepresentation) {
        this.input = input;
        this.outputRepresentation = outputRepresentation.copy();
    }

    public ItemStack getOutput(GasStack input) {
        return outputRepresentation.copy();
    }

    public List<ItemStack> getOutputDefinition() {
        return outputRepresentation.isEmpty() ? Collections.emptyList() : Collections.singletonList(outputRepresentation);
    }

    @Override
    public boolean test(@NonNull GasStack gasStack) {
        return input.test(gasStack);
    }

    public GasStackIngredient getInput() {
        return input;
    }

    @Override
    public void write(PacketBuffer buffer) {
        input.write(buffer);
        buffer.writeItemStack(outputRepresentation);
    }

    public static class ChemicalCrystallizerRecipeOre extends ChemicalCrystallizerRecipe {

        private final OreDictSupplier outputSupplier;

        public ChemicalCrystallizerRecipeOre(GasStackIngredient input, Tag<Item> outputTag) {
            super(input, ItemStack.EMPTY);
            this.outputSupplier = new OreDictSupplier(outputTag);
        }

        @Override
        public ItemStack getOutput(@NonNull GasStack input) {
            return outputSupplier.get();
        }

        @Override
        public List<ItemStack> getOutputDefinition() {
            return outputSupplier.getPossibleOutputs();
        }
    }
}

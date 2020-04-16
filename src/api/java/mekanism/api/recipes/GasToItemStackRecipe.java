package mekanism.api.recipes;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.inputs.GasStackIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

/**
 * Created by Thiakil on 14/07/2019.
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class GasToItemStackRecipe extends MekanismRecipe implements Predicate<@NonNull GasStack> {

    private final GasStackIngredient input;
    private final ItemStack output;

    public GasToItemStackRecipe(ResourceLocation id, GasStackIngredient input, ItemStack output) {
        super(id);
        this.input = input;
        this.output = output.copy();
    }

    public ItemStack getOutput(GasStack input) {
        return output.copy();
    }

    public List<ItemStack> getOutputDefinition() {
        return output.isEmpty() ? Collections.emptyList() : Collections.singletonList(output);
    }

    @Override
    public boolean test(GasStack gasStack) {
        return input.test(gasStack);
    }

    public GasStackIngredient getInput() {
        return input;
    }

    @Override
    public void write(PacketBuffer buffer) {
        input.write(buffer);
        buffer.writeItemStack(output);
    }
}
package mekanism.api.recipes;

import java.util.function.Predicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

/**
 * Created by Thiakil on 14/07/2019.
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ItemStackToGasRecipe extends MekanismRecipe implements Predicate<@NonNull ItemStack> {

    private final ItemStackIngredient input;
    private final GasStack output;

    public ItemStackToGasRecipe(ResourceLocation id, ItemStackIngredient input, GasStack output) {
        super(id);
        this.input = input;
        this.output = output;
    }

    @Override
    public boolean test(@NonNull ItemStack itemStack) {
        return input.test(itemStack);
    }

    public ItemStackIngredient getInput() {
        return input;
    }

    public GasStack getOutput(ItemStack input) {
        return output.copy();
    }

    public GasStack getOutputDefinition() {
        return output;
    }

    @Override
    public void write(PacketBuffer buffer) {
        input.write(buffer);
        output.writeToPacket(buffer);
    }
}
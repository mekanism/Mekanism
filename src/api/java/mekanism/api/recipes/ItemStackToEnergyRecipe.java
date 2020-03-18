package mekanism.api.recipes;

import java.util.function.Predicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ItemStackToEnergyRecipe extends MekanismRecipe implements Predicate<@NonNull ItemStack> {

    protected final ItemStackIngredient input;
    protected final double output;

    public ItemStackToEnergyRecipe(ResourceLocation id, ItemStackIngredient input, double output) {
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

    public double getOutput(ItemStack input) {
        return output;
    }

    public double getOutputDefinition() {
        return output;
    }

    @Override
    public void write(PacketBuffer buffer) {
        input.write(buffer);
        buffer.writeDouble(output);
    }
}
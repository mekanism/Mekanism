package mekanism.api.recipes;

import java.util.function.Predicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ItemStackToEnergyRecipe extends MekanismRecipe implements Predicate<@NonNull ItemStack> {

    protected final ItemStackIngredient input;
    protected final FloatingLong output;

    public ItemStackToEnergyRecipe(ResourceLocation id, ItemStackIngredient input, FloatingLong output) {
        super(id);
        this.input = input;
        //Ensure that the floating long we are storing is immutable
        this.output = output.copyAsConst();
    }

    @Override
    public boolean test(ItemStack itemStack) {
        return input.test(itemStack);
    }

    public ItemStackIngredient getInput() {
        return input;
    }

    public FloatingLong getOutput(ItemStack input) {
        return output;
    }

    public FloatingLong getOutputDefinition() {
        return output;
    }

    @Override
    public void write(PacketBuffer buffer) {
        input.write(buffer);
        output.writeToBuffer(buffer);
    }
}
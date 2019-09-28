package mekanism.api.recipes;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

/**
 * Inputs: ItemStack (item) Output: ItemStack (transformed)
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@FieldsAreNonnullByDefault
public abstract class ItemStackToItemStackRecipe extends MekanismRecipe implements Predicate<@NonNull ItemStack> {

    private final ItemStackIngredient input;
    private ItemStack output;

    public ItemStackToItemStackRecipe(ResourceLocation id, ItemStackIngredient input, ItemStack output) {
        super(id);
        this.input = input;
        this.output = output.copy();
    }

    @Override
    public boolean test(@NonNull ItemStack input) {
        return this.input.test(input);
    }

    public ItemStackIngredient getInput() {
        return input;
    }

    public ItemStack getOutput(@NonNull ItemStack input) {
        return output.copy();
    }

    /**
     * For JEI, gets a display stack
     *
     * @return Representation of output, MUST NOT be modified
     */
    public List<ItemStack> getOutputDefinition() {
        return output.isEmpty() ? Collections.emptyList() : Collections.singletonList(output);
    }

    @Override
    public void write(PacketBuffer buffer) {
        input.write(buffer);
        buffer.writeItemStack(output);
    }
}
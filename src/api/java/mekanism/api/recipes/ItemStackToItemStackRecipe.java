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

    private final ItemStackIngredient mainInput;
    private ItemStack outputDefinition;

    public ItemStackToItemStackRecipe(ResourceLocation id, ItemStackIngredient input, ItemStack outputDefinition) {
        super(id);
        this.mainInput = input;
        this.outputDefinition = outputDefinition.copy();
    }

    @Override
    public boolean test(@NonNull ItemStack input) {
        return mainInput.test(input);
    }

    public ItemStackIngredient getInput() {
        return mainInput;
    }

    public ItemStack getOutput(@NonNull ItemStack input) {
        return outputDefinition.copy();
    }

    /**
     * For JEI, gets a display stack
     *
     * @return Representation of output, MUST NOT be modified
     */
    public List<ItemStack> getOutputDefinition() {
        return outputDefinition.isEmpty() ? Collections.emptyList() : Collections.singletonList(outputDefinition);
    }

    @Override
    public void write(PacketBuffer buffer) {
        mainInput.write(buffer);
        buffer.writeItemStack(outputDefinition);
    }
}
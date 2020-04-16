package mekanism.api.recipes;

import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

/**
 * Inputs: ItemStack + GasStack Output: ItemStack
 *
 * Ex-AdvancedMachineInput based; InjectionRecipe, OsmiumCompressorRecipe, PurificationRecipe
 *
 * @apiNote The gas input is a base value, and will still be multiplied by a per tick usage
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ItemStackGasToItemStackRecipe extends MekanismRecipe implements BiPredicate<@NonNull ItemStack, @NonNull GasStack> {

    private final ItemStackIngredient itemInput;
    private final GasStackIngredient gasInput;
    private final ItemStack output;

    public ItemStackGasToItemStackRecipe(ResourceLocation id, ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack output) {
        super(id);
        this.itemInput = itemInput;
        this.gasInput = gasInput;
        this.output = output;
    }

    public ItemStackIngredient getItemInput() {
        return itemInput;
    }

    public GasStackIngredient getGasInput() {
        return gasInput;
    }

    public ItemStack getOutput(ItemStack inputItem, GasStack inputGas) {
        return output.copy();
    }

    @Override
    public boolean test(ItemStack itemStack, GasStack gasStack) {
        return itemInput.test(itemStack) && gasInput.test(gasStack);
    }

    public @NonNull List<@NonNull ItemStack> getOutputDefinition() {
        return output.isEmpty() ? Collections.emptyList() : Collections.singletonList(output);
    }

    @Override
    public void write(PacketBuffer buffer) {
        itemInput.write(buffer);
        gasInput.write(buffer);
        buffer.writeItemStack(output);
    }
}
package mekanism.api.recipes;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

/**
 * Extension of {@link ItemStackGasToItemStackRecipe} with a defined amount of ticks needed to process. Input: ItemStack
 * <br>
 * Input: Gas (Base value, will be multiplied by a per tick amount)
 * <br>
 * Output: ItemStack
 *
 * @apiNote Nucleosynthesizers can process this recipe type.
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class NucleosynthesizingRecipe extends ItemStackGasToItemStackRecipe {

    private final int duration;

    /**
     * @param id        Recipe name.
     * @param itemInput Item input.
     * @param gasInput  Gas input.
     * @param output    Output.
     * @param duration  Duration in ticks that it takes the recipe to complete. Must be greater than zero.
     */
    public NucleosynthesizingRecipe(ResourceLocation id, ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack output, int duration) {
        super(id, itemInput, gasInput, output);
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be a number greater than zero.");
        }
        this.duration = duration;
    }

    @Override
    public void write(PacketBuffer buffer) {
        super.write(buffer);
        buffer.writeVarInt(duration);
    }

    /**
     * Gets the duration in ticks this recipe takes to complete.
     */
    public int getDuration() {
        return duration;
    }
}
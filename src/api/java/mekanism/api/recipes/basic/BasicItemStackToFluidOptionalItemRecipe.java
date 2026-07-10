package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.recipes.ItemStackToFluidOptionalItemRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.TypedInstance;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.fluids.crafting.display.FluidStackSlotDisplay;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

/// Basic implementation of [ItemStackToFluidOptionalItemRecipe]
///
/// @since 10.6.3
public abstract class BasicItemStackToFluidOptionalItemRecipe extends ItemStackToFluidOptionalItemRecipe {

    protected final ItemStackIngredient input;
    protected final FluidOptionalItemOutput output;

    /// @param input  Input.
    /// @param output Output.
    public BasicItemStackToFluidOptionalItemRecipe(ItemStackIngredient input, FluidOptionalItemOutput output) {
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        this.output = output;
    }

    @Override
    public ItemStackIngredient getInput() {
        return input;
    }

    @Override
    @Contract(pure = true)
    public FluidOptionalItemOutput getOutput(TypedInstance<Item> input) {
        return output;
    }

    public FluidOptionalItemOutput getOutputRaw() {
        return output;
    }

    @Override
    public List<FluidOptionalItemOutput> getOutputDefinition(ContextMap contextMap) {
        return Collections.singletonList(output);
    }

    @Override
    public final SlotDisplay getOutputDisplay() {
        SlotDisplay fluidDisplay = new FluidStackSlotDisplay(output.fluid().create());
        if (output.optionalItem() == null) {
            return fluidDisplay;
        }
        return new SlotDisplay.WithRemainder(fluidDisplay, new SlotDisplay.ItemStackSlotDisplay(output.optionalItem()));
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicItemStackToFluidOptionalItemRecipe other = (BasicItemStackToFluidOptionalItemRecipe) o;
        return input.equals(other.input) && output.equals(other.output);
    }

    @Override
    public int hashCode() {
        int result = input.hashCode();
        result = 31 * result + output.hashCode();
        return result;
    }
}
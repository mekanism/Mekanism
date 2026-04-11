package mekanism.api.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import mekanism.api.ItemStackTemplateHelper;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base class for defining ItemStack to fluid recipes with an optional item output.
 * <br>
 * Input: ItemStack
 * <br>
 * Output: FluidStack, Optional ItemStack
 *
 * @apiNote There is currently only one type of ItemStack to FluidStack recipe type:
 * <ul>
 *     <li>Nutritional Liquification: These cannot currently be created, but are processed in the Nutritional Liquifier.</li>
 * </ul>
 * @since 10.6.3
 */
@NothingNullByDefault
public abstract class ItemStackToFluidOptionalItemRecipe extends MekanismRecipe<SingleRecipeInput> implements Predicate<@NotNull ItemStack> {

    @Override
    public abstract boolean test(ItemStack itemStack);

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        //Don't match incomplete recipes or ones that don't match
        return !isIncomplete() && test(input.item());
    }

    /**
     * Gets the input ingredient.
     */
    public abstract ItemStackIngredient getInput();

    /**
     * Gets a new output based on the given input.
     *
     * @param input Specific input.
     *
     * @return New output.
     *
     * @apiNote While Mekanism does not currently make use of the input, it is important to support it and pass the proper value in case any addons define input based
     * outputs where things like NBT may be different
     * @implNote The passed in input should <strong>NOT</strong> be modified.
     */
    @Contract(value = "_ -> new", pure = true)
    public abstract FluidOptionalItemOutput getOutput(ItemStack input);

    /**
     * For JEI, gets the output representations to display.
     *
     * @return Representation of the output, <strong>MUST NOT</strong> be modified.
     */
    public abstract List<FluidOptionalItemOutput> getOutputDefinition();

    @Override
    public boolean isIncomplete() {
        return getInput().hasNoMatchingInstances();
    }

    @Override
    public void logMissingTags() {
        getInput().logMissingTags();
    }

    /**
     * @apiNote Fluid must be present, but the item may be empty.
     *///todo 26.1 FluidInstance/template
    public record FluidOptionalItemOutput(FluidStack fluid, @Nullable ItemStackTemplate optionalItem) {

        FluidOptionalItemOutput(FluidStack fluid, Optional<ItemStackTemplate> optionalItem) {
            this(fluid, optionalItem.orElse(null));
        }

        public static final Codec<FluidOptionalItemOutput> CODEC = RecordCodecBuilder.create(instance -> instance.group(
              FluidStack.CODEC.fieldOf(SerializationConstants.FLUID).forGetter(FluidOptionalItemOutput::fluid),
              ItemStackTemplate.CODEC.optionalFieldOf(SerializationConstants.ITEM).forGetter((t) -> Optional.ofNullable(t.optionalItem))
        ).apply(instance, FluidOptionalItemOutput::new));

        public FluidOptionalItemOutput {
            Objects.requireNonNull(fluid, "Fluid output cannot be null.");
            Objects.requireNonNull(optionalItem, "Item output cannot be null.");
            if (fluid.isEmpty()) {
                throw new IllegalArgumentException("Fluid output cannot be empty.");
            }
        }

        /**
         * Copies the backing objects of this output object.
         */
        public FluidOptionalItemOutput copy() {
            return new FluidOptionalItemOutput(fluid.copy(), optionalItem);
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (o == null || getClass() != o.getClass()) {
                return false;
            }
            FluidOptionalItemOutput other = (FluidOptionalItemOutput) o;
            return FluidStack.matches(fluid, other.fluid) && ItemStackTemplateHelper.matches(optionalItem, other.optionalItem);
        }

        @Override
        public int hashCode() {
            int hash = FluidStack.hashFluidAndComponents(fluid);
            hash = 31 * hash + fluid.getAmount();
            if (optionalItem != null) {
                hash = 31 * hash + ItemStackTemplateHelper.hashItemAndComponents(optionalItem);
                hash = 31 * hash + optionalItem.count();
            }
            return hash;
        }
    }
}

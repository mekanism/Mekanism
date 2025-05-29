package mekanism.api.recipes.basic;


import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Contract;

@NothingNullByDefault
public class BasicSawmillRecipe extends SawmillRecipe {

    protected final ItemStackIngredient input;
    protected final ItemStack mainOutput;
    protected final ItemStack secondaryOutput;
    protected final double secondaryChance;

    /**
     * @param input           Input.
     * @param mainOutput      Main Output.
     * @param secondaryOutput Secondary Output (chance based).
     * @param secondaryChance Chance of the secondary output being produced. This must be at least zero and at most one.
     *
     * @apiNote At least one output must not be empty.
     */
    public BasicSawmillRecipe(ItemStackIngredient input, ItemStack mainOutput, ItemStack secondaryOutput, double secondaryChance) {
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        Objects.requireNonNull(mainOutput, "Main output cannot be null.");
        Objects.requireNonNull(secondaryOutput, "Secondary output cannot be null.");
        if (mainOutput.isEmpty() && secondaryOutput.isEmpty()) {
            throw new IllegalArgumentException("At least one output must not be empty.");
        } else if (secondaryChance < 0 || secondaryChance > 1) {
            throw new IllegalArgumentException("Secondary output chance must be at least zero and at most one.");
        } else if (mainOutput.isEmpty()) {
            if (secondaryChance == 0 || secondaryChance == 1) {
                throw new IllegalArgumentException("Secondary output must have a chance greater than zero and less than one.");
            }
        } else if (secondaryOutput.isEmpty() && secondaryChance != 0) {
            throw new IllegalArgumentException("If there is no secondary output, the chance of getting the secondary output should be zero.");
        }
        this.mainOutput = mainOutput.copy();
        this.secondaryOutput = secondaryOutput.copy();
        this.secondaryChance = secondaryChance;
    }

    @Override
    public boolean test(ItemStack stack) {
        return this.input.test(stack);
    }

    @Override
    @Contract("_ -> new")
    public ChanceOutput getOutput(ItemStack input) {
        return new BasicChanceOutput(secondaryChance > 0 ? RANDOM.nextDouble() : 0);
    }

    @Override
    public List<ItemStack> getMainOutputDefinition() {
        return mainOutput.isEmpty() ? Collections.emptyList() : Collections.singletonList(mainOutput);
    }

    @Override
    public List<ItemStack> getSecondaryOutputDefinition() {
        return secondaryOutput.isEmpty() ? Collections.emptyList() : Collections.singletonList(secondaryOutput);
    }

    @Override
    public double getSecondaryChance() {
        return secondaryChance;
    }

    @Override
    public ItemStackIngredient getInput() {
        return input;
    }

    /**
     * For Serializer use. DO NOT MODIFY RETURN VALUE.
     *
     * @return the uncopied basic output, or empty if the value is ItemStack.EMPTY
     */
    public Optional<ItemStack> getMainOutputRaw() {
        return this.mainOutput.isEmpty() ? Optional.empty() : Optional.of(this.mainOutput);
    }

    /**
     * For Serializer use. DO NOT MODIFY RETURN VALUE.
     *
     * @return the uncopied basic output
     */
    public Optional<ItemStack> getSecondaryOutputRaw() {
        return this.secondaryOutput.isEmpty() ? Optional.empty() : Optional.of(this.secondaryOutput);
    }

    @Override
    public RecipeSerializer<BasicSawmillRecipe> getSerializer() {
        return MekanismRecipeSerializers.SAWING.get();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicSawmillRecipe other = (BasicSawmillRecipe) o;
        return secondaryChance == other.secondaryChance && input.equals(other.input) && ItemStack.matches(mainOutput, other.mainOutput) &&
               ItemStack.matches(secondaryOutput, other.secondaryOutput);
    }

    @Override
    public int hashCode() {
        int hash = 31 * input.hashCode() + Double.hashCode(secondaryChance);
        hash = 31 * hash + ItemStack.hashItemAndComponents(mainOutput);
        hash = 31 * hash + mainOutput.getCount();
        if (!secondaryOutput.isEmpty()) {
            hash = 31 * hash + ItemStack.hashItemAndComponents(secondaryOutput);
            hash = 31 * hash + secondaryOutput.getCount();
        }
        return hash;
    }

    public class BasicChanceOutput implements ChanceOutput {

        protected final double rand;

        protected BasicChanceOutput(double rand) {
            this.rand = rand;
        }

        @Override
        public ItemStack getMainOutput() {
            return mainOutput.copy();
        }

        @Override
        public ItemStack getMaxSecondaryOutput() {
            return secondaryChance > 0 ? secondaryOutput.copy() : ItemStack.EMPTY;
        }

        @Override
        public ItemStack getSecondaryOutput() {
            if (rand <= secondaryChance) {
                return secondaryOutput.copy();
            }
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack nextSecondaryOutput() {
            if (secondaryChance > 0) {
                double rand = RANDOM.nextDouble();
                if (rand <= secondaryChance) {
                    return secondaryOutput.copy();
                }
            }
            return ItemStack.EMPTY;
        }
    }
}
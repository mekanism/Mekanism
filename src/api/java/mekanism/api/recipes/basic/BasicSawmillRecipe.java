package mekanism.api.recipes.basic;


import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import mekanism.api.ItemStackTemplateHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class BasicSawmillRecipe extends SawmillRecipe {

    protected final ItemStackIngredient input;
    @Nullable
    protected final ItemStackTemplate mainOutput;
    @Nullable
    protected final ItemStackTemplate secondaryOutput;
    protected final double secondaryChance;

    /**
     * @param input           Input.
     * @param mainOutput      Main Output.
     * @param secondaryOutput Secondary Output (chance based).
     * @param secondaryChance Chance of the secondary output being produced. This must be at least zero and at most one.
     *
     * @apiNote At least one output must not be empty.
     */
    public BasicSawmillRecipe(ItemStackIngredient input, @Nullable ItemStackTemplate mainOutput, @Nullable ItemStackTemplate secondaryOutput, double secondaryChance) {
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        if (mainOutput == null && secondaryOutput == null) {
            throw new IllegalArgumentException("At least one output must not be empty.");
        } else if (secondaryChance < 0 || secondaryChance > 1) {
            throw new IllegalArgumentException("Secondary output chance must be at least zero and at most one.");
        } else if (mainOutput == null) {
            if (secondaryChance == 0 || secondaryChance == 1) {
                throw new IllegalArgumentException("Secondary output must have a chance greater than zero and less than one.");
            }
        } else if (secondaryOutput == null && secondaryChance != 0) {
            throw new IllegalArgumentException("If there is no secondary output, the chance of getting the secondary output should be zero.");
        }
        this.mainOutput = mainOutput;
        this.secondaryOutput = secondaryOutput;
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

    @Override// TODO - 26.1 - see if template can be used
    public List<ItemStack> getMainOutputDefinition() {
        return mainOutput == null ? Collections.emptyList() : Collections.singletonList(mainOutput.create());
    }

    @Override
    public List<ItemStack> getSecondaryOutputDefinition() {
        return secondaryOutput == null ? Collections.emptyList() : Collections.singletonList(secondaryOutput.create());
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
    public Optional<ItemStackTemplate> getMainOutputRaw() {
        return Optional.ofNullable(this.mainOutput);
    }

    /**
     * For Serializer use. DO NOT MODIFY RETURN VALUE.
     *
     * @return the uncopied basic output
     */
    public Optional<ItemStackTemplate> getSecondaryOutputRaw() {
        return Optional.ofNullable(this.secondaryOutput);
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
        return secondaryChance == other.secondaryChance && input.equals(other.input) && ItemStackTemplateHelper.matches(mainOutput, other.mainOutput) &&
               ItemStackTemplateHelper.matches(secondaryOutput, other.secondaryOutput);
    }

    @Override
    public int hashCode() {
        int hash = 31 * input.hashCode() + Double.hashCode(secondaryChance);
        hash = 31 * hash + ItemStackTemplateHelper.hashItemAndComponents(mainOutput);
        hash = 31 * hash + (mainOutput != null ? mainOutput.count() : 0);
        if (secondaryOutput != null) {
            hash = 31 * hash + ItemStackTemplateHelper.hashItemAndComponents(secondaryOutput);
            hash = 31 * hash + secondaryOutput.count();
        }
        return hash;
    }

    public class BasicChanceOutput implements ChanceOutput {

        protected final double rand;

        protected BasicChanceOutput(double rand) {
            this.rand = rand;
        }

        @Override
        @Nullable
        public ItemStackTemplate getMainOutput() {
            return mainOutput;
        }

        @Override
        @Nullable
        public ItemStackTemplate getMaxSecondaryOutput() {
            return secondaryChance > 0 ? secondaryOutput : null;
        }

        @Override
        @Nullable
        public ItemStackTemplate getSecondaryOutput() {
            if (secondaryOutput != null && rand <= secondaryChance) {
                return secondaryOutput;
            }
            return null;
        }

        @Override
        @Nullable
        public ItemStackTemplate nextSecondaryOutput() {
            if (secondaryOutput != null && secondaryChance > 0) {
                double rand = RANDOM.nextDouble();
                if (rand <= secondaryChance) {
                    return secondaryOutput;
                }
            }
            return null;
        }
    }
}
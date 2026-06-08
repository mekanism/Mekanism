package mekanism.api.recipes.basic;


import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.TypedInstance;
import net.minecraft.world.item.Item;
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
    @Contract("_ -> new")
    public ChanceOutput getOutput(TypedInstance<Item> input) {
        return new BasicChanceOutput(secondaryChance > 0 ? RANDOM.nextDouble() : 0);
    }

    @Override
    public List<ItemStackTemplate> getMainOutputDefinition() {
        return mainOutput == null ? Collections.emptyList() : Collections.singletonList(mainOutput);
    }

    @Override
    public List<ItemStackTemplate> getSecondaryOutputDefinition() {
        return secondaryOutput == null ? Collections.emptyList() : Collections.singletonList(secondaryOutput);
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
        return secondaryChance == other.secondaryChance && input.equals(other.input) && Objects.equals(mainOutput, other.mainOutput) &&
               Objects.equals(secondaryOutput, other.secondaryOutput);
    }

    @Override
    public int hashCode() {
        int hash = 31 * input.hashCode() + Double.hashCode(secondaryChance);
        hash = 31 * hash + (mainOutput == null ? 0 : mainOutput.hashCode());
        if (secondaryOutput != null) {
            hash = 31 * hash + secondaryOutput.hashCode();
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
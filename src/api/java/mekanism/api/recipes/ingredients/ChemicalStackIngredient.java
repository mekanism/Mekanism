package mekanism.api.recipes.ingredients;

import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IEmptyStackProvider;
import mekanism.api.recipes.ingredients.chemical.IChemicalIngredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base implementation for a ChemicalIngredient with an amount.
 *
 * <p>{@link IChemicalIngredient}, like its item counterpart, explicitly does not perform count checks,
 * so this class is used to (a) wrap a standard ChemicalIngredient with an amount and (b) provide a standard serialization format for mods to use.
 *
 * @see net.neoforged.neoforge.common.crafting.SizedIngredient
 * @see GasStackIngredient
 * @see InfusionStackIngredient
 * @see PigmentStackIngredient
 * @see SlurryStackIngredient
 */
@NothingNullByDefault
public abstract sealed class ChemicalStackIngredient<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends IChemicalIngredient<CHEMICAL, INGREDIENT>> implements InputIngredient<@NotNull STACK>, IEmptyStackProvider<CHEMICAL, STACK>
      permits GasStackIngredient, InfusionStackIngredient, PigmentStackIngredient, SlurryStackIngredient {

    private final INGREDIENT ingredient;
    private final long amount;

    protected ChemicalStackIngredient(INGREDIENT ingredient, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }
        this.ingredient = ingredient;
        this.amount = amount;
    }

    @Nullable
    private List<STACK> representations;

    @Override
    public boolean test(STACK stack) {
        return testType(stack) && stack.getAmount() >= amount;
    }

    @Override
    public boolean testType(STACK stack) {
        Objects.requireNonNull(stack);
        return testType(stack.getChemical());
    }

    /**
     * Evaluates this predicate on the given argument, ignoring any size data.
     *
     * @param chemical Input argument.
     *
     * @return {@code true} if the input argument matches the predicate, otherwise {@code false}
     */
    public boolean testType(CHEMICAL chemical) {
        Objects.requireNonNull(chemical);
        return ingredient.test(chemical);
    }

    @Override
    public STACK getMatchingInstance(STACK stack) {
        return test(stack) ? (STACK) stack.copyWithAmount(amount) : getEmptyStack();
    }

    @Override
    public long getNeededAmount(STACK stack) {
        return testType(stack) ? amount : 0;
    }

    @Override
    public boolean hasNoMatchingInstances() {
        return ingredient.hasNoChemicals();
    }

    @Override
    public List<STACK> getRepresentations() {
        if (this.representations == null) {
            this.representations = ingredient.getChemicals().stream()
                  .map(s -> (STACK) s.getStack(amount))
                  .toList();
        }
        return representations;
    }

    /**
     * For use in recipe input caching. Gets the internal Chemical Ingredient.
     *
     * @since 10.6.0
     */
    public INGREDIENT ingredient() {
        return ingredient;
    }

    /**
     * For use in recipe input caching. Gets the internal amount this ingredient represents.
     *
     * @since 10.6.0
     */
    public long amount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChemicalStackIngredient<?, ?, ?> other = (ChemicalStackIngredient<?, ?, ?>) o;
        return amount == other.amount && ingredient.equals(other.ingredient);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ingredient, amount);
    }

    @Override
    public String toString() {
        return amount + "x " + ingredient;
    }
}
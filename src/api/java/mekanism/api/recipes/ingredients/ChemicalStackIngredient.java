package mekanism.api.recipes.ingredients;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import mekanism.api.SerializationConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.chemical.ChemicalIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

/**
 * Base implementation for a ChemicalIngredient with an amount.
 *
 * <p>{@link ChemicalIngredient}, like its item counterpart, explicitly does not perform count checks,
 * so this class is used to (a) wrap a standard ChemicalIngredient with an amount and (b) provide a standard serialization format for mods to use.
 *
 * @see net.neoforged.neoforge.common.crafting.SizedIngredient
 */
@NothingNullByDefault
public final class ChemicalStackIngredient implements InputIngredient<ChemicalStack> {

    /**
     * The "flat" codec for {@link ChemicalStackIngredient}.
     *
     * <p>The amount is serialized inline with the rest of the ingredient, for example:
     *
     * <pre>{@code
     * {
     *     "chemical": "mekanism:hydrogen",
     *     "amount": 250
     * }
     * }</pre>
     * <p>
     * Compound chemical ingredients are always serialized using the map codec, i.e.
     *
     * <pre>{@code
     * {
     *     "type": "mekanism:compound",
     *     "ingredients": [
     *         { "chemical": "mekanism:hydrogen" },
     *         { "chemical": "mekanism:oxygen" }
     *     ],
     *     "amount": 500
     * }
     * }</pre>
     *
     * @since 10.6.0
     */
    public static final Codec<ChemicalStackIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          IngredientCreatorAccess.chemical().mapCodecNonEmpty().forGetter(ChemicalStackIngredient::ingredient),
          SerializerHelper.POSITIVE_LONG_CODEC.fieldOf(SerializationConstants.AMOUNT).forGetter(ChemicalStackIngredient::amount)
    ).apply(instance, ChemicalStackIngredient::new));

    /**
     * A stream codec for sending {@link ChemicalStackIngredient}s over the network.
     *
     * @since 10.6.0
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, ChemicalStackIngredient> STREAM_CODEC = StreamCodec.composite(
          IngredientCreatorAccess.chemical().streamCodec(), ChemicalStackIngredient::ingredient,
          ByteBufCodecs.VAR_LONG, ChemicalStackIngredient::amount,
          ChemicalStackIngredient::new
    );

    /**
     * Creates a Chemical Stack Ingredient that matches a given ingredient and amount. Prefer calling via
     * {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#chemical()} and
     * {@link mekanism.api.recipes.ingredients.creator.IChemicalStackIngredientCreator#from(ChemicalIngredient, long)}.
     *
     * @param ingredient Ingredient to match.
     * @param amount     Amount to match.
     *
     * @throws NullPointerException     if the given instance is null.
     * @throws IllegalArgumentException if the given instance is empty.
     * @since 10.6.0
     */
    public static ChemicalStackIngredient of(ChemicalIngredient ingredient, long amount) {
        Objects.requireNonNull(ingredient, "ChemicalStackIngredients cannot be created from a null ingredient.");
        if (ingredient.isEmpty()) {
            throw new IllegalArgumentException("ChemicalStackIngredients cannot be created using the empty ingredient.");
        }
        return new ChemicalStackIngredient(ingredient, amount);
    }

    private final ChemicalIngredient ingredient;
    private final long amount;

    public ChemicalStackIngredient(ChemicalIngredient ingredient, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }
        this.ingredient = ingredient;
        this.amount = amount;
    }

    @Nullable
    private List<ChemicalStack> representations;

    @Override
    public boolean test(ChemicalStack stack) {
        return testType(stack) && stack.getAmount() >= amount;
    }

    @Override
    public boolean testType(ChemicalStack stack) {
        Objects.requireNonNull(stack);
        return testType(stack.getChemicalHolder());
    }

    /**
     * Evaluates this predicate on the given argument, ignoring any size data.
     *
     * @param chemical Input argument.
     *
     * @return {@code true} if the input argument matches the predicate, otherwise {@code false}
     *
     * @deprecated Use {@link #testType(Holder)} instead
     */
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    public boolean testType(Chemical chemical) {
        Objects.requireNonNull(chemical);
        return ingredient.test(chemical);
    }

    /**
     * Evaluates this predicate on the given argument, ignoring any size data.
     *
     * @param chemical Input argument.
     *
     * @return {@code true} if the input argument matches the predicate, otherwise {@code false}
     *
     * @since 10.7.11
     */
    public boolean testType(Holder<Chemical> chemical) {
        Objects.requireNonNull(chemical);
        return ingredient.test(chemical);
    }

    @Override
    public ChemicalStack getMatchingInstance(ChemicalStack stack) {
        return test(stack) ? stack.copyWithAmount(amount) : ChemicalStack.EMPTY;
    }

    @Override
    public long getNeededAmount(ChemicalStack stack) {
        return testType(stack) ? amount : 0;
    }

    @Override
    public boolean hasNoMatchingInstances() {
        return ingredient.hasNoChemicals();
    }

    @Override
    public void logMissingTags() {
        ingredient.logMissingTags();
    }

    @Override
    public List<ChemicalStack> getRepresentations() {
        if (this.representations == null) {
            this.representations = ingredient.getChemicalHolders().stream()
                  .map(chemical -> new ChemicalStack(chemical, amount))
                  .toList();
        }
        return representations;
    }

    /**
     * For use in recipe input caching. Gets the internal Chemical Ingredient.
     *
     * @since 10.6.0
     */
    public ChemicalIngredient ingredient() {
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
        ChemicalStackIngredient other = (ChemicalStackIngredient) o;
        return amount == other.amount && ingredient.equals(other.ingredient);
    }

    @Override
    public int hashCode() {
        return 31 * ingredient.hashCode() + Long.hashCode(amount);
    }

    @Override
    public String toString() {
        return amount + "x " + ingredient;
    }
}
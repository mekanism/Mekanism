package mekanism.api.recipes.ingredients;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.display.slot.WithAmountSlotDisplay;
import mekanism.api.recipes.ingredients.chemical.ChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.display.ChemicalStackContentsFactory;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.core.Holder;
import net.minecraft.core.TypedInstance;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.context.ContextMap;
import org.jspecify.annotations.Nullable;

/// Base implementation for a ChemicalIngredient with an amount.
///
/// [ChemicalIngredient], like its item counterpart, explicitly does not perform count checks, so this class is used to (a) wrap a standard ChemicalIngredient with an
/// amount and (b) provide a standard serialization format for mods to use.
///
/// @see net.neoforged.neoforge.common.crafting.SizedIngredient
public final class ChemicalStackIngredient implements InputIngredient<Chemical, ChemicalStack> {

    /// The "flat" codec for [ChemicalStackIngredient].
    ///
    /// The amount is serialized inline with the rest of the ingredient, for example:
    /// ```json
    /// {
    ///     "chemical": "mekanism:hydrogen",
    ///     "amount": 250
    /// }
    /// ```
    ///
    /// Compound chemical ingredients are always serialized using the map codec, i.e.
    /// ```json
    /// {
    ///     "type": "mekanism:compound",
    ///     "ingredients": [
    ///         { "chemical": "mekanism:hydrogen" },
    ///         { "chemical": "mekanism:oxygen" }
    ///     ],
    ///     "amount": 500
    /// }
    /// ```
    ///
    /// @since 10.6.0
    public static final Codec<ChemicalStackIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          IngredientCreatorAccess.chemical().codec().fieldOf(SerializationConstants.INGREDIENT).forGetter(ChemicalStackIngredient::ingredient),
          ExtraCodecs.POSITIVE_INT.fieldOf(SerializationConstants.AMOUNT).forGetter(ChemicalStackIngredient::amount)
    ).apply(instance, ChemicalStackIngredient::new));

    /// A stream codec for sending [ChemicalStackIngredient]s over the network.
    ///
    /// @since 10.6.0
    public static final StreamCodec<RegistryFriendlyByteBuf, ChemicalStackIngredient> STREAM_CODEC = StreamCodec.composite(
          IngredientCreatorAccess.chemical().streamCodec(), ChemicalStackIngredient::ingredient,
          ByteBufCodecs.VAR_INT, ChemicalStackIngredient::amount,
          ChemicalStackIngredient::new
    );

    /// Creates a Chemical Stack Ingredient that matches a given ingredient and amount. Prefer calling via
    /// [mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#chemical()] and
    /// [mekanism.api.recipes.ingredients.creator.IChemicalStackIngredientCreator#from(ChemicalIngredient, int)].
    ///
    /// @param ingredient Ingredient to match.
    /// @param amount     Amount to match.
    ///
    /// @throws NullPointerException     if the given instance is null.
    /// @throws IllegalArgumentException if the given instance is empty.
    /// @since 10.6.0
    public static ChemicalStackIngredient of(ChemicalIngredient ingredient, int amount) {
        Objects.requireNonNull(ingredient, "ChemicalStackIngredients cannot be created from a null ingredient.");
        if (amount <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }
        return new ChemicalStackIngredient(ingredient, amount);
    }

    private final ChemicalIngredient ingredient;
    private final int amount;

    private ChemicalStackIngredient(ChemicalIngredient ingredient, int amount) {
        this.ingredient = ingredient;
        this.amount = amount;
    }

    @Nullable
    private List<ChemicalStack> representations;

    @Override
    public boolean test(ChemicalStack stack) {
        return testType(stack) && stack.amount() >= amount;
    }

    @Override
    public boolean testType(TypedInstance<Chemical> stack) {
        Objects.requireNonNull(stack);
        return switch (stack) {
            case ChemicalResource resource -> ingredient.test(resource);
            default -> testType(stack.typeHolder());
        };
    }

    /// Evaluates this predicate on the given argument, ignoring any size data.
    ///
    /// @param chemical Input argument.
    ///
    /// @return `true` if the input argument matches the predicate, otherwise `false`
    ///
    /// @since 10.7.11
    public boolean testType(Holder<Chemical> chemical) {
        Objects.requireNonNull(chemical);
        return ingredient.test(chemical);
    }

    @Override
    public ChemicalStack getMatchingInstance(ChemicalStack stack) {
        return test(stack) ? stack.copyWithAmount(amount) : ChemicalStack.EMPTY;
    }

    @Override
    public int getNeededAmount(TypedInstance<Chemical> stack) {
        return testType(stack) ? amount : 0;
    }

    @Override
    public boolean hasNoMatchingInstances() {
        return ingredient.isEmpty();
    }

    @Override
    public void logMissingTags() {
        ingredient.logMissingTags();
    }

    @Override
    public List<ChemicalStack> getRepresentations(ContextMap context) {
        //TODO - 26.2: Should we still be caching the representations in all our stack ingredients? What if different ContextMaps are passed
        if (this.representations == null) {
            this.representations = display().resolve(context, ChemicalStackContentsFactory.INSTANCE).toList();
        }
        return representations;
    }

    @Override
    public WithAmountSlotDisplay display() {
        return new WithAmountSlotDisplay(ingredient.display(), amount);
    }

    /// For use in recipe input caching. Gets the internal Chemical Ingredient.
    ///
    /// @since 10.6.0
    public ChemicalIngredient ingredient() {
        return ingredient;
    }

    /// For use in recipe input caching. Gets the internal amount this ingredient represents.
    ///
    /// @since 10.6.0
    public int amount() {
        return amount;
    }

    @Override
    public boolean equals(@Nullable Object o) {
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
        return 31 * ingredient.hashCode() + amount;
    }

    @Override
    public String toString() {
        return amount + "x " + ingredient;
    }
}
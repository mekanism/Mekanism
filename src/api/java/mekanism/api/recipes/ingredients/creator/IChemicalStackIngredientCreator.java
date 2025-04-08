package mekanism.api.recipes.ingredients.creator;

import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.chemical.ChemicalIngredient;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;

@NothingNullByDefault
public interface IChemicalStackIngredientCreator extends IIngredientCreator<Chemical, ChemicalStack, ChemicalStackIngredient> {

    @Override
    default ChemicalStackIngredient from(ChemicalStack instance) {
        Objects.requireNonNull(instance, "ChemicalStackIngredients cannot be created from a null ChemicalStack.");
        return fromHolder(instance.getChemicalHolder(), instance.getAmount());
    }

    @Override
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    default ChemicalStackIngredient from(Chemical instance, int amount) {
        return fromHolder(instance.getAsHolder(), amount);
    }

    @Override
    default ChemicalStackIngredient fromHolder(Holder<Chemical> instance, int amount) {
        return fromHolder(instance, (long) amount);
    }

    /**
     * Creates a Chemical Stack Ingredient that matches a provided chemical and amount.
     *
     * @param provider Chemical provider that provides the chemical to match.
     * @param amount   Amount needed.
     *
     * @throws NullPointerException     if the given provider is null.
     * @throws IllegalArgumentException if the given provider is empty or an amount smaller than one.
     *
     * @deprecated Use {@link #fromHolder(Holder, long)} instead
     */
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    default ChemicalStackIngredient from(IChemicalProvider provider, long amount) {
        Objects.requireNonNull(provider, "ChemicalStackIngredients cannot be created from a null chemical provider.");
        return from(IngredientCreatorAccess.chemical().of(provider), amount);
    }

    @Override
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    default ChemicalStackIngredient from(int amount, Chemical... chemicals) {
        return from((long) amount, chemicals);
    }

    @Override
    @SuppressWarnings("unchecked")
    default ChemicalStackIngredient fromHolders(int amount, Holder<Chemical>... holders) {
        return fromHolders((long) amount, holders);
    }

    /**
     * Creates a Chemical Stack Ingredient that matches any of the provided chemicals.
     *
     * @param amount    Amount needed.
     * @param chemicals Chemicals to match.
     *
     * @throws NullPointerException     if the given instance is null.
     * @throws IllegalArgumentException if the given instance is empty or an amount smaller than one; or if no chemicals are passed.
     * @since 10.6.0
     *
     * @deprecated Use {@link #fromHolders(long, Holder[])} instead
     */
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    default ChemicalStackIngredient from(long amount, IChemicalProvider... chemicals) {
        if (chemicals.length == 0) {
            throw new IllegalArgumentException("Attempted to create a ChemicalStackIngredient with no chemicals.");
        }
        return from(IngredientCreatorAccess.chemical().of(chemicals), amount);
    }

    /**
     * Creates a Chemical Stack Ingredient that matches a provided chemical and amount.
     *
     * @param instance Chemical to match.
     * @param amount   Amount needed.
     *
     * @throws NullPointerException     if the given instance is null.
     * @throws IllegalArgumentException if the given instance is empty or an amount smaller than one.
     * @since 10.5.0
     */
    default ChemicalStackIngredient fromHolder(Holder<Chemical> instance, long amount) {
        Objects.requireNonNull(instance, "ChemicalStackIngredients cannot be created from a null chemical provider.");
        return from(IngredientCreatorAccess.chemical().of(instance), amount);
    }

    /**
     * Creates a Chemical Stack Ingredient that matches any of the provided chemicals.
     *
     * @param amount    Amount needed.
     * @param chemicals Chemicals to match.
     *
     * @throws NullPointerException     if the given instance is null.
     * @throws IllegalArgumentException if the given instance is empty or an amount smaller than one; or if no chemicals are passed.
     * @since 10.7.11
     */
    @SuppressWarnings("unchecked")
    default ChemicalStackIngredient fromHolders(long amount, Holder<Chemical>... chemicals) {
        if (chemicals.length == 0) {
            throw new IllegalArgumentException("Attempted to create a ChemicalStackIngredient with no chemicals.");
        }
        return from(IngredientCreatorAccess.chemical().ofHolders(chemicals), amount);
    }

    @Override
    default ChemicalStackIngredient from(TagKey<Chemical> tag, int amount) {
        return from(tag, (long) amount);
    }

    /**
     * Creates a Chemical Stack Ingredient that matches a given chemical tag and amount.
     *
     * @param tag    Tag to match.
     * @param amount Amount needed.
     *
     * @throws NullPointerException     if the given tag is null.
     * @throws IllegalArgumentException if the given amount smaller than one.
     */
    default ChemicalStackIngredient from(TagKey<Chemical> tag, long amount) {
        Objects.requireNonNull(tag, "ChemicalStackIngredients cannot be created from a null tag.");
        return from(IngredientCreatorAccess.chemical().tag(tag), amount);
    }

    /**
     * Creates a Chemical Stack Ingredient that matches a given chemical ingredient and amount.
     *
     * @param ingredient Ingredient to match.
     * @param amount     Amount needed.
     *
     * @throws NullPointerException     if the given ingredient is null.
     * @throws IllegalArgumentException if the ingredient is explicitly empty or the given amount smaller than one.
     */
    ChemicalStackIngredient from(ChemicalIngredient ingredient, long amount);
}
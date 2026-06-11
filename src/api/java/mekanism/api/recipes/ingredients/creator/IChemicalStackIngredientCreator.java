package mekanism.api.recipes.ingredients.creator;

import java.util.Objects;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.chemical.ChemicalIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.TypedInstance;
import net.minecraft.tags.TagKey;

public interface IChemicalStackIngredientCreator extends IIngredientCreator<Chemical, ChemicalStack, ChemicalStackIngredient> {

    @Override
    default ChemicalStackIngredient from(ChemicalStack instance) {
        Objects.requireNonNull(instance, "ChemicalStackIngredients cannot be created from a null ChemicalStack.");
        return fromHolder(instance.typeHolder(), instance.amount());
    }

    @Override
    default ChemicalStackIngredient fromHolder(Holder<Chemical> instance, int amount) {
        Objects.requireNonNull(instance, "ChemicalStackIngredients cannot be created from a null chemical provider.");
        return from(IngredientCreatorAccess.chemical().of(instance), amount);
    }

    @Override
    @SuppressWarnings("unchecked")
    default ChemicalStackIngredient fromHolders(int amount, Holder<Chemical>... holders) {
        if (holders.length == 0) {
            throw new IllegalArgumentException("Attempted to create a ChemicalStackIngredient with no chemicals.");
        }
        return from(IngredientCreatorAccess.chemical().of(holders), amount);
    }

    @Override
    default ChemicalStackIngredient from(HolderGetter<Chemical> lookup, TagKey<Chemical> tag, int amount) {
        Objects.requireNonNull(tag, "ChemicalStackIngredients cannot be created from a null tag.");
        //TODO - 26.1: Make use of this holder getter
        return from(IngredientCreatorAccess.chemical().tag(tag), amount);
    }

    /// Creates a Chemical Stack Ingredient that matches a given chemical ingredient and amount.
    ///
    /// @param ingredient Ingredient to match.
    /// @param amount     Amount needed.
    ///
    /// @throws NullPointerException     if the given ingredient is null.
    /// @throws IllegalArgumentException if the ingredient is explicitly empty or the given amount smaller than one.
    default ChemicalStackIngredient from(ChemicalIngredient ingredient, int amount) {
        Objects.requireNonNull(ingredient, "ChemicalStackIngredients cannot be created from a null ingredient.");
        return ChemicalStackIngredient.of(ingredient, amount);
    }

    @Override
    default ChemicalStack createStack(TypedInstance<Chemical> instance) {
        Objects.requireNonNull(instance, "Instance cannot be null.");
        return switch (instance) {
            case ChemicalStack stack -> stack;
            default -> new ChemicalStack(instance.typeHolder(), 1);
        };
    }
}
package mekanism.common.recipe.lookup;

import java.util.function.BiPredicate;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.common.recipe.lookup.IRecipeLookupHandler.IRecipeTypedLookupHandler;
import mekanism.common.recipe.lookup.cache.DoubleInputRecipeCache;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.DoubleItem;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.FluidChemical;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.ItemChemical;
import net.minecraft.core.TypedInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.RegisteredResource;
import org.jetbrains.annotations.Nullable;

/**
 * Helper expansion of {@link IRecipeLookupHandler} for easily implementing contains and find recipe lookups for recipes that takes two inputs.
 */
public interface IDoubleRecipeLookupHandler<A_TYPE, A_RESOURCE extends RegisteredResource<A_TYPE>, INPUT_A extends TypedInstance<A_TYPE>,
      B_TYPE, B_RESOURCE extends RegisteredResource<B_TYPE>, INPUT_B extends TypedInstance<B_TYPE>, RECIPE extends MekanismRecipe<?> & BiPredicate<INPUT_A, INPUT_B>,
      INPUT_CACHE extends DoubleInputRecipeCache<A_TYPE, A_RESOURCE, INPUT_A, ?, B_TYPE, B_RESOURCE, INPUT_B, ?, RECIPE, ?, ?>> extends IRecipeTypedLookupHandler<RECIPE, INPUT_CACHE> {

    /**
     * Checks if there is a matching recipe of type {@link #getRecipeType()} that has the given inputs.
     *
     * @param inputA Recipe input a.
     * @param inputB Recipe input b.
     *
     * @return {@code true} if there is a match, {@code false} if there isn't.
     *
     * @apiNote See {@link DoubleInputRecipeCache#containsInputAB(Level, TypedInstance, TypedInstance)} and
     * {@link DoubleInputRecipeCache#containsInputBA(Level, TypedInstance, TypedInstance)} for more details about when this method should be called versus when
     * {@link #containsRecipeBA(TypedInstance, TypedInstance)} should be called.
     */
    default boolean containsRecipeAB(INPUT_A inputA, INPUT_B inputB) {
        return getRecipeType().getInputCache().containsInputAB(getLevel(), inputA, inputB);
    }

    /**
     * Checks if there is a matching recipe of type {@link #getRecipeType()} that has the given inputs.
     *
     * @param inputA Recipe input a.
     * @param inputB Recipe input b.
     *
     * @return {@code true} if there is a match, {@code false} if there isn't.
     *
     * @apiNote See {@link DoubleInputRecipeCache#containsInputAB(Level, RegisteredResource, RegisteredResource)} and
     * {@link DoubleInputRecipeCache#containsInputBA(Level, RegisteredResource, RegisteredResource)} for more details about when this method should be called versus when
     * {@link #containsRecipeBA(RegisteredResource, RegisteredResource)} should be called.
     */
    default boolean containsRecipeAB(A_RESOURCE inputA, B_RESOURCE inputB) {
        return getRecipeType().getInputCache().containsInputAB(getLevel(), inputA, inputB);
    }

    /**
     * Checks if there is a matching recipe of type {@link #getRecipeType()} that has the given inputs.
     *
     * @param inputA Recipe input a.
     * @param inputB Recipe input b.
     *
     * @return {@code true} if there is a match, {@code false} if there isn't.
     *
     * @apiNote See {@link DoubleInputRecipeCache#containsInputAB(Level, TypedInstance, TypedInstance)} and
     * {@link DoubleInputRecipeCache#containsInputBA(Level, TypedInstance, TypedInstance)} for more details about when this method should be called versus when
     * {@link #containsRecipeAB(TypedInstance, TypedInstance)} should be called.
     */
    default boolean containsRecipeBA(INPUT_A inputA, INPUT_B inputB) {
        return getRecipeType().getInputCache().containsInputBA(getLevel(), inputA, inputB);
    }

    /**
     * Checks if there is a matching recipe of type {@link #getRecipeType()} that has the given inputs.
     *
     * @param inputA Recipe input a.
     * @param inputB Recipe input b.
     *
     * @return {@code true} if there is a match, {@code false} if there isn't.
     *
     * @apiNote See {@link DoubleInputRecipeCache#containsInputAB(Level, RegisteredResource, RegisteredResource)} and
     * {@link DoubleInputRecipeCache#containsInputBA(Level, RegisteredResource, RegisteredResource)} for more details about when this method should be called versus when
     * {@link #containsRecipeAB(RegisteredResource, RegisteredResource)} should be called.
     */
    default boolean containsRecipeBA(A_RESOURCE inputA, B_RESOURCE inputB) {
        return getRecipeType().getInputCache().containsInputBA(getLevel(), inputA, inputB);
    }

    /**
     * Checks if there is a matching recipe of type {@link #getRecipeType()} that has the given input.
     *
     * @param input Recipe input.
     *
     * @return {@code true} if there is a match, {@code false} if there isn't.
     */
    default boolean containsRecipeA(INPUT_A input) {
        return getRecipeType().getInputCache().containsInputA(getLevel(), input);
    }

    /**
     * Checks if there is a matching recipe of type {@link #getRecipeType()} that has the given input.
     *
     * @param input Recipe input.
     *
     * @return {@code true} if there is a match, {@code false} if there isn't.
     */
    default boolean containsRecipeA(A_RESOURCE input) {
        return getRecipeType().getInputCache().containsInputA(getLevel(), input);
    }

    /**
     * Checks if there is a matching recipe of type {@link #getRecipeType()} that has the given input.
     *
     * @param input Recipe input.
     *
     * @return {@code true} if there is a match, {@code false} if there isn't.
     */
    default boolean containsRecipeB(INPUT_B input) {
        return getRecipeType().getInputCache().containsInputB(getLevel(), input);
    }

    /**
     * Checks if there is a matching recipe of type {@link #getRecipeType()} that has the given input.
     *
     * @param input Recipe input.
     *
     * @return {@code true} if there is a match, {@code false} if there isn't.
     */
    default boolean containsRecipeB(B_RESOURCE input) {
        return getRecipeType().getInputCache().containsInputB(getLevel(), input);
    }

    /**
     * Finds the first recipe for the type of recipe we handle ({@link #getRecipeType()}) by looking up the given inputs against the recipe type's input cache.
     *
     * @param inputA Recipe input a.
     * @param inputB Recipe input b.
     *
     * @return Recipe matching the given inputs, or {@code null} if no recipe matches.
     */
    @Nullable
    default RECIPE findFirstRecipe(INPUT_A inputA, INPUT_B inputB) {
        return getRecipeType().getInputCache().findFirstRecipe(getLevel(), inputA, inputB);
    }

    /**
     * Finds the first recipe for the type of recipe we handle ({@link #getRecipeType()}) by looking up the given inputs against the recipe type's input cache.
     *
     * @param inputAHandler Input handler to grab the first recipe input from.
     * @param inputBHandler Input handler to grab the second recipe input from.
     *
     * @return Recipe matching the given inputs, or {@code null} if no recipe matches.
     */
    @Nullable
    default RECIPE findFirstRecipe(IInputHandler<A_TYPE, A_RESOURCE, INPUT_A> inputAHandler, IInputHandler<B_TYPE, B_RESOURCE, INPUT_B> inputBHandler) {
        return findFirstRecipe(inputAHandler.getInput(), inputBHandler.getInput());
    }

    /**
     * Helper interface to make the generics that we have to pass to {@link IDoubleRecipeLookupHandler} not as messy.
     */
    interface DoubleItemRecipeLookupHandler<RECIPE extends MekanismRecipe<?> & BiPredicate<ItemStack, ItemStack>> extends
          IDoubleRecipeLookupHandler<Item, ItemResource, ItemStack, Item, ItemResource, ItemStack, RECIPE, DoubleItem<RECIPE>> {
    }

    /**
     * Helper interface to make the generics that we have to pass to {@link IDoubleRecipeLookupHandler} not as messy.
     */
    interface ItemChemicalRecipeLookupHandler<RECIPE extends MekanismRecipe<?> & BiPredicate<ItemStack, ChemicalStack>> extends
          IDoubleRecipeLookupHandler<Item, ItemResource, ItemStack, Chemical, ChemicalResource, ChemicalStack, RECIPE, ItemChemical<RECIPE>> {
    }

    /**
     * Helper interface to make the generics that we have to pass to {@link IDoubleRecipeLookupHandler} not as messy.
     */
    interface FluidChemicalRecipeLookupHandler<RECIPE extends MekanismRecipe<?> & BiPredicate<FluidStack, ChemicalStack>> extends
          IDoubleRecipeLookupHandler<Fluid, FluidResource, FluidStack, Chemical, ChemicalResource, ChemicalStack, RECIPE, FluidChemical<RECIPE>> {
    }
}
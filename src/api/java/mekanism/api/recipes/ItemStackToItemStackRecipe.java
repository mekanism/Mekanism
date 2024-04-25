package mekanism.api.recipes;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Input: ItemStack
 * <br>
 * Output: ItemStack
 *
 * @apiNote There are currently three types of ItemStack to ItemStack recipe types:
 * <ul>
 *     <li>Crushing: Can be processed in Crushers and Crushing Factories.</li>
 *     <li>Enriching: Can be processed in Enrichment Chambers and Enriching Factories.</li>
 *     <li>Smelting: Can be processed in Energized Smelters, Smelting Factories, and Robits.</li>
 * </ul>
 */
@NothingNullByDefault
public abstract class ItemStackToItemStackRecipe extends MekanismRecipe implements Predicate<@NotNull ItemStack> {

    protected final RecipeType<ItemStackToItemStackRecipe> recipeType;

    public ItemStackToItemStackRecipe(RecipeType<ItemStackToItemStackRecipe> recipeType) {
        this.recipeType = Objects.requireNonNull(recipeType, "Recipe type cannot be null");
    }

    @Override
    public abstract boolean test(ItemStack input);

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
     * outputs where things like NBT may be different.
     * @implNote The passed in input should <strong>NOT</strong> be modified.
     */
    @Contract(value = "_ -> new", pure = true)
    public abstract ItemStack getOutput(ItemStack input);

    @NotNull
    @Override
    public abstract ItemStack getResultItem(@NotNull HolderLookup.Provider provider);

    /**
     * For JEI, gets the output representations to display.
     *
     * @return Representation of the output, <strong>MUST NOT</strong> be modified.
     */
    public abstract List<ItemStack> getOutputDefinition();

    @Override
    public boolean isIncomplete() {
        return getInput().hasNoMatchingInstances();
    }

    @Override
    public final RecipeType<ItemStackToItemStackRecipe> getType() {
        return this.recipeType;
    }
}

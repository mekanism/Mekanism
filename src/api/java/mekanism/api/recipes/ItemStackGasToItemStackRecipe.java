package mekanism.api.recipes;

import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Input: ItemStack
 * <br>
 * Input: Chemical (Base value, will be multiplied by a per tick amount)
 * <br>
 * Output: ItemStack
 *
 * @apiNote There are currently three types of ItemStack Chemical to ItemStack recipe types:
 * <ul>
 *     <li>Compressing: Can be processed in Osmium Compressors and Compressing Factories.</li>
 *     <li>Injecting: Can be processed in Chemical Injection Chambers and Injecting Factories.</li>
 *     <li>Purifying: Can be processed in Purification Chambers and Purifying Factories.</li>
 * </ul>
 */
@NothingNullByDefault
public abstract class ItemStackGasToItemStackRecipe extends ItemStackChemicalToItemStackRecipe {

    private final RecipeType<? extends ItemStackGasToItemStackRecipe> recipeType;

    protected ItemStackGasToItemStackRecipe(RecipeType<? extends ItemStackGasToItemStackRecipe> recipeType) {
        this.recipeType = Objects.requireNonNull(recipeType, "Recipe type cannot be null");
    }

    @Override
    public abstract ItemStackIngredient getItemInput();

    @Override
    public abstract ChemicalStackIngredient getChemicalInput();

    @Override
    @Contract(value = "_, _ -> new", pure = true)
    public abstract ItemStack getOutput(ItemStack inputItem, ChemicalStack inputChemical);

    @NotNull
    @Override
    public abstract ItemStack getResultItem(@NotNull HolderLookup.Provider provider);

    @Override
    public abstract boolean test(ItemStack itemStack, ChemicalStack chemicalStack);

    @Override
    public abstract List<@NotNull ItemStack> getOutputDefinition();

    @Override
    public final RecipeType<? extends ItemStackGasToItemStackRecipe> getType() {
        return recipeType;
    }
}

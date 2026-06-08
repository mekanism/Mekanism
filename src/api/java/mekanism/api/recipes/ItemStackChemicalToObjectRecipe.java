package mekanism.api.recipes;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.vanilla_input.SingleItemChemicalRecipeInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Base class for defining item chemical to item recipes.
 * <br>
 * Input: ItemStack
 * <br>
 * Input: Chemical
 * <br>
 * Output: OUTPUT
 *
 * @since 10.7.0
 */
@NothingNullByDefault
public abstract class ItemStackChemicalToObjectRecipe<OUTPUT> extends TwoInputMekRecipe<Item, ItemStack, ItemStackIngredient, Chemical, ChemicalStack, ChemicalStackIngredient, SingleItemChemicalRecipeInput, OUTPUT> {

    /**
     * Represents whether this recipe consumes the chemical each tick.
     *
     * @since 10.7.0
     */
    public abstract boolean perTickUsage();

    /**
     * Gets the input item ingredient.
     */
    public abstract ItemStackIngredient getItemInput();

    @Override
    public final ItemStackIngredient getInputA() {
        return getItemInput();
    }

    /**
     * Gets the input chemical ingredient.
     */
    public abstract ChemicalStackIngredient getChemicalInput();

    @Override
    public final ChemicalStackIngredient getInputB() {
        return getChemicalInput();
    }

    @Override
    public boolean matches(SingleItemChemicalRecipeInput input, Level level) {
        //Don't match incomplete recipes or ones that don't match
        return !isIncomplete() && test(input.item(), input.chemical());
    }
}
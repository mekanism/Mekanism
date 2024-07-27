package mekanism.api.recipes;

import java.util.List;
import java.util.function.Predicate;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
/**
 * Input: ItemStack
 * <br>
 * Output: Chemical
 *
 * @apiNote Chemical Oxidizers can process this recipe type.
 */
@NothingNullByDefault
public abstract class ChemicalOxidizerRecipe extends MekanismRecipe<SingleRecipeInput> implements Predicate<@NotNull ItemStack> {

    private static final Holder<Item> CHEMICAL_OXIDIZER = DeferredHolder.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "chemical_oxidizer"));

    /**
     * Gets the input item ingredient.
     */
    public abstract ItemStackIngredient getInput();

    /**
     * Gets a new output based on the given input.
     *
     * @param input Specific input.
     *
     * @return New output.
     *
     * @apiNote While Mekanism does not currently make use of the inputs, it is important to support it and pass the proper value in case any addons define input based
     * outputs where things like NBT may be different.
     * @implNote The passed in inputs should <strong>NOT</strong> be modified.
     */
    @Contract(value = "_ -> new", pure = true)
    public abstract BoxedChemicalStack getOutput(ItemStack input);

    @Override
    public abstract boolean test(ItemStack itemStack);

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        //Don't match incomplete recipes or ones that don't match
        return !isIncomplete() && test(input.item());
    }

    /**
     * For JEI, gets the output representations to display.
     *
     * @return Representation of the output, <strong>MUST NOT</strong> be modified.
     */
    public abstract List<BoxedChemicalStack> getOutputDefinition();

    @Override
    public boolean isIncomplete() {
        return getInput().hasNoMatchingInstances();
    }

    @Override
    public final RecipeType<ChemicalOxidizerRecipe> getType() {
        return MekanismRecipeTypes.TYPE_OXIDIZING.value();
    }

    @Override
    public String getGroup() {
        return "chemical_oxidizer";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(CHEMICAL_OXIDIZER);
    }
}
package mekanism.api.recipes;

import java.util.List;
import java.util.function.Predicate;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.vanilla_input.SingleChemicalRecipeInput;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Input: Chemical
 * <br>
 * Output: ItemStack
 *
 * @apiNote Chemical Crystallizers can process this recipe type.
 */
@NothingNullByDefault
public abstract class ChemicalCrystallizerRecipe extends MekanismRecipe<SingleChemicalRecipeInput> implements Predicate<@NotNull ChemicalStack> {

    private static final Holder<Item> CHEMICAL_CRYSTALLIZER = DeferredHolder.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "chemical_crystallizer"));

    /**
     * Gets the output based on the given input.
     *
     * @param input Specific input.
     *
     * @return Output as a constant.
     *
     * @apiNote While Mekanism does not currently make use of the input, it is important to support it and pass the proper value in case any addons define input based
     * outputs where things like NBT may be different.
     * @implNote The passed in input should <strong>NOT</strong> be modified.
     */
    @Contract(value = "_ -> new", pure = true)
    public abstract ItemStack getOutput(ChemicalStack input);

    /**
     * For JEI, gets the output representations to display.
     *
     * @return Representation of the output, <strong>MUST NOT</strong> be modified.
     */
    public abstract List<ItemStack> getOutputDefinition();

    @NotNull
    @Override
    public ItemStack assemble(SingleChemicalRecipeInput input, HolderLookup.Provider provider) {
        if (!isIncomplete() && test(input.chemical())) {
            return getOutput(input.chemical());
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean matches(SingleChemicalRecipeInput input, Level level) {
        //Don't match incomplete recipes or ones that don't match
        return !isIncomplete() && test(input.chemical());
    }

    /**
     * Helper to test this recipe against a chemical stack without having to first box it up.
     *
     * @param stack Input stack.
     *
     * @return {@code true} if the stack matches the input.
     */
    @Override
    public abstract boolean test(ChemicalStack stack);

    /**
     * Helper to test this recipe against a chemical stack's type without having to first box it up.
     *
     * @param stack Input stack.
     *
     * @return {@code true} if the stack's type matches the input.
     *
     */
    public abstract boolean testType(ChemicalStack stack);

    /**
     * Gets the input ingredient.
     */
    public abstract ChemicalStackIngredient getInput();

    @Override
    public boolean isIncomplete() {
        return getInput().hasNoMatchingInstances();
    }

    @Override
    public void logMissingTags() {
        getInput().logMissingTags();
    }

    @Override
    public final RecipeType<ChemicalCrystallizerRecipe> getType() {
        return MekanismRecipeTypes.TYPE_CRYSTALLIZING.value();
    }

    @Override
    public String getGroup() {
        return "chemical_crystallizer";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(CHEMICAL_CRYSTALLIZER);
    }
}
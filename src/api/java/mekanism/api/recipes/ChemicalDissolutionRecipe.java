package mekanism.api.recipes;

import java.util.List;
import java.util.function.BiPredicate;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.vanilla_input.SingleItemChemicalRecipeInput;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public abstract class ChemicalDissolutionRecipe extends MekanismRecipe<SingleItemChemicalRecipeInput>
      implements BiPredicate<@NotNull ItemStack, @NotNull ChemicalStack> {

    private static final Holder<Item> CHEMICAL_DISSOLUTION_CHAMBER = DeferredHolder.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "chemical_dissolution_chamber"));

    /**
     * Gets the input item ingredient.
     */
    public abstract ItemStackIngredient getItemInput();

    /**
     * Gets the input chemical ingredient.
     */
    public abstract ChemicalStackIngredient getChemicalInput();

    /**
     * Gets a new output based on the given inputs.
     *
     * @param inputItem     Specific item input.
     * @param inputChemical Specific chemical input.
     *
     * @return New output.
     *
     * @apiNote While Mekanism does not currently make use of the inputs, it is important to support it and pass the proper value in case any addons define input based
     * outputs where things like NBT may be different.
     * @implNote The passed in inputs should <strong>NOT</strong> be modified.
     */
    @Contract(value = "_, _ -> new", pure = true)
    public abstract ChemicalStack getOutput(ItemStack inputItem, ChemicalStack inputChemical);

    @Override
    public abstract boolean test(ItemStack itemStack, ChemicalStack chemicalStack);

    @Override
    public boolean matches(SingleItemChemicalRecipeInput input, Level level) {
        //Don't match incomplete recipes or ones that don't match
        return !isIncomplete() && test(input.item(), input.chemical());
    }

    /**
     * For JEI, gets the output representations to display.
     *
     * @return Representation of the output, <strong>MUST NOT</strong> be modified.
     */
    public abstract List<ChemicalStack> getOutputDefinition();

    @Override
    public boolean isIncomplete() {
        return getItemInput().hasNoMatchingInstances() || getChemicalInput().hasNoMatchingInstances();
    }

    @Override
    public final RecipeType<ChemicalDissolutionRecipe> getType() {
        return MekanismRecipeTypes.TYPE_DISSOLUTION.value();
    }

    @Override
    public String getGroup() {
        return "chemical_dissolution_chamber";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(CHEMICAL_DISSOLUTION_CHAMBER);
    }
}

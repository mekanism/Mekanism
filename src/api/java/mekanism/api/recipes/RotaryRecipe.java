package mekanism.api.recipes;

import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.vanilla_input.RotaryRecipeInput;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Contract;

/**
 * Input: FluidStack
 * <br>
 * Output: ChemicalStack
 * <br><br>
 * Input: ChemicalStack
 * <br>
 * Output: FluidStack
 *
 * @apiNote Rotary Condensentrators can process this recipe type. Converting from fluid to chemical when set to Decondensentrating and converting from chemical to fluid
 * when set to Condensentrating.
 */
@NothingNullByDefault
public abstract class RotaryRecipe extends MekanismRecipe<RotaryRecipeInput> {

    private static final Holder<Item> ROTARY_CONDENSENTRATOR = DeferredHolder.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "rotary_condensentrator"));

    /**
     * @return {@code true} if this recipe knows how to convert a chemical to a fluid.
     */
    public abstract boolean hasChemicalToFluid();

    /**
     * @return {@code true} if this recipe knows how to convert a fluid to a chemical.
     */
    public abstract boolean hasFluidToChemical();

    /**
     * Checks if this recipe can convert fluids to chemicals, and evaluates this recipe on the given input.
     *
     * @param fluidStack Fluid input.
     *
     * @return {@code true} if the input is valid for this recipe.
     */
    public abstract boolean test(FluidStack fluidStack);

    /**
     * Checks if this recipe can convert chemicals to fluids, and evaluates this recipe on the given input.
     *
     * @param chemicalStack Chemical input.
     *
     * @return {@code true} if the input is valid for this recipe.
     */
    public abstract boolean test(ChemicalStack chemicalStack);

    @Override
    public boolean matches(RotaryRecipeInput input, Level level) {
        //Don't match incomplete recipes or ones that don't match
        return !isIncomplete() && input.input().map(this::test, this::test);
    }

    /**
     * Gets the fluid input ingredient.
     *
     * @throws IllegalStateException if {@link #hasFluidToChemical()} is {@code false}.
     */
    public abstract FluidStackIngredient getFluidInput();

    /**
     * Gets the chemical input ingredient.
     *
     * @throws IllegalStateException if {@link #hasChemicalToFluid()} is {@code false}.
     */
    public abstract ChemicalStackIngredient getChemicalInput();

    /**
     * For JEI, gets the chemical output representations to display.
     *
     * @return Representation of the chemical output, <strong>MUST NOT</strong> be modified.
     *
     * @throws IllegalStateException if {@link #hasFluidToChemical()} is {@code false}.
     */
    public abstract List<ChemicalStack> getChemicalOutputDefinition();

    /**
     * For JEI, gets the fluid output representations to display.
     *
     * @return Representation of the output, <strong>MUST NOT</strong> be modified.
     *
     * @throws IllegalStateException if {@link #hasChemicalToFluid()} is {@code false}.
     */
    public abstract List<FluidStack> getFluidOutputDefinition();

    /**
     * Gets a new chemical output based on the given input.
     *
     * @param input Specific fluid input.
     *
     * @return New chemical output.
     *
     * @throws IllegalStateException if {@link #hasFluidToChemical()} is {@code false}.
     * @apiNote While Mekanism does not currently make use of the input, it is important to support it and pass the proper value in case any addons define input based
     * outputs where things like NBT may be different.
     * @implNote The passed in input should <strong>NOT</strong> be modified.
     */
    @Contract(value = "_ -> new", pure = true)
    public abstract ChemicalStack getChemicalOutput(FluidStack input);

    /**
     * Gets a new fluid output based on the given input.
     *
     * @param input Specific chemical input.
     *
     * @return New fluid output.
     *
     * @throws IllegalStateException if {@link #hasChemicalToFluid()} is {@code false}.
     * @apiNote While Mekanism does not currently make use of the input, it is important to support it and pass the proper value in case any addons define input based
     * outputs where things like NBT may be different.
     * @implNote The passed in input should <strong>NOT</strong> be modified.
     */
    @Contract(value = "_ -> new", pure = true)
    public abstract FluidStack getFluidOutput(ChemicalStack input);

    @Override
    public boolean isIncomplete() {
        return (hasFluidToChemical() && getFluidInput().hasNoMatchingInstances()) || (hasChemicalToFluid() && getChemicalInput().hasNoMatchingInstances());
    }

    @Override
    public void logMissingTags() {
        if (hasFluidToChemical()) {
            getFluidInput().logMissingTags();
        }
        if (hasChemicalToFluid()) {
            getChemicalInput().logMissingTags();
        }
    }

    @Override
    public final RecipeType<RotaryRecipe> getType() {
        return MekanismRecipeTypes.TYPE_ROTARY.value();
    }

    @Override
    public String getGroup() {
        return "rotary_condensentrator";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ROTARY_CONDENSENTRATOR);
    }

}

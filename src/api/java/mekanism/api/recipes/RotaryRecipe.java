package mekanism.api.recipes;

import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ingredients.GasStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Contract;

/**
 * Input: FluidStack
 * <br>
 * Output: GasStack
 * <br><br>
 * Input: GasStack
 * <br>
 * Output: FluidStack
 *
 * @apiNote Rotary Condensentrators can process this recipe type. Converting from fluid to gas when set to Decondensentrating and converting from gas to fluid when set to
 * Condensentrating.
 */
@NothingNullByDefault
public abstract class RotaryRecipe extends MekanismRecipe {

    private static final Holder<Item> ROTARY_CONDENSENTRATOR = DeferredHolder.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "rotary_condensentrator"));

    /**
     * @return {@code true} if this recipe knows how to convert a gas to a fluid.
     */
    public abstract boolean hasGasToFluid();

    /**
     * @return {@code true} if this recipe knows how to convert a fluid to a gas.
     */
    public abstract boolean hasFluidToGas();

    /**
     * Checks if this recipe can convert fluids to gases, and evaluates this recipe on the given input.
     *
     * @param fluidStack Fluid input.
     *
     * @return {@code true} if the input is valid for this recipe.
     */
    public abstract boolean test(FluidStack fluidStack);

    /**
     * Checks if this recipe can convert gases to fluids, and evaluates this recipe on the given input.
     *
     * @param gasStack Gas input.
     *
     * @return {@code true} if the input is valid for this recipe.
     */
    public abstract boolean test(GasStack gasStack);

    /**
     * Gets the fluid input ingredient.
     *
     * @throws IllegalStateException if {@link #hasFluidToGas()} is {@code false}.
     */
    public abstract FluidStackIngredient getFluidInput();

    /**
     * Gets the gas input ingredient.
     *
     * @throws IllegalStateException if {@link #hasGasToFluid()} is {@code false}.
     */
    public abstract GasStackIngredient getGasInput();

    /**
     * For JEI, gets the gas output representations to display.
     *
     * @return Representation of the gas output, <strong>MUST NOT</strong> be modified.
     *
     * @throws IllegalStateException if {@link #hasFluidToGas()} is {@code false}.
     */
    public abstract List<GasStack> getGasOutputDefinition();

    /**
     * For JEI, gets the fluid output representations to display.
     *
     * @return Representation of the output, <strong>MUST NOT</strong> be modified.
     *
     * @throws IllegalStateException if {@link #hasGasToFluid()} is {@code false}.
     */
    public abstract List<FluidStack> getFluidOutputDefinition();

    /**
     * Gets a new gas output based on the given input.
     *
     * @param input Specific fluid input.
     *
     * @return New gas output.
     *
     * @throws IllegalStateException if {@link #hasFluidToGas()} is {@code false}.
     * @apiNote While Mekanism does not currently make use of the input, it is important to support it and pass the proper value in case any addons define input based
     * outputs where things like NBT may be different.
     * @implNote The passed in input should <strong>NOT</strong> be modified.
     */
    @Contract(value = "_ -> new", pure = true)
    public abstract GasStack getGasOutput(FluidStack input);

    /**
     * Gets a new fluid output based on the given input.
     *
     * @param input Specific gas input.
     *
     * @return New fluid output.
     *
     * @throws IllegalStateException if {@link #hasGasToFluid()} is {@code false}.
     * @apiNote While Mekanism does not currently make use of the input, it is important to support it and pass the proper value in case any addons define input based
     * outputs where things like NBT may be different.
     * @implNote The passed in input should <strong>NOT</strong> be modified.
     */
    @Contract(value = "_ -> new", pure = true)
    public abstract FluidStack getFluidOutput(GasStack input);

    @Override
    public boolean isIncomplete() {
        return (hasFluidToGas() && getFluidInput().hasNoMatchingInstances()) || (hasGasToFluid() && getGasInput().hasNoMatchingInstances());
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

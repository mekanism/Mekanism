package mekanism.api.recipes;

import java.util.List;
import java.util.function.Predicate;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.vanilla_input.SingleFluidRecipeInput;
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
import org.jetbrains.annotations.NotNull;

/**
 * Input: FluidStack
 * <br>
 * Output: FluidStack
 *
 * @apiNote Thermal Evaporation Towers can process this recipe type.
 */
@NothingNullByDefault
public abstract class FluidToFluidRecipe extends MekanismRecipe<SingleFluidRecipeInput> implements Predicate<@NotNull FluidStack> {

    private static final Holder<Item> THERMAL_EVAPORATION_CONTROLLER = DeferredHolder.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "thermal_evaporation_controller"));

    @Override
    public abstract boolean test(FluidStack fluidStack);

    @Override
    public boolean matches(SingleFluidRecipeInput input, Level level) {
        //Don't match incomplete recipes or ones that don't match
        return !isIncomplete() && test(input.fluid());
    }

    /**
     * Gets the input ingredient.
     */
    public abstract FluidStackIngredient getInput();

    /**
     * For JEI, gets the output representations to display.
     *
     * @return Representation of the output, <strong>MUST NOT</strong> be modified.
     */
    public abstract List<FluidStack> getOutputDefinition();

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
    @Contract(value = "_ ->new", pure = true)
    public abstract FluidStack getOutput(FluidStack input);

    @Override
    public boolean isIncomplete() {
        return getInput().hasNoMatchingInstances();
    }

    @Override
    public void logMissingTags() {
        getInput().logMissingTags();
    }

    @Override
    public final RecipeType<FluidToFluidRecipe> getType() {
        return MekanismRecipeTypes.TYPE_EVAPORATING.get();
    }

    @Override
    public String getGroup() {
        return "thermal_evaporation_controller";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(THERMAL_EVAPORATION_CONTROLLER);
    }
}

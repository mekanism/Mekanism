package buildcraft.api.recipes;

import java.util.Set;

import com.google.common.base.Predicate;

import net.minecraftforge.fluids.FluidStack;

public interface IComplexRefineryRecipeManager {
    IHeatableRecipe createHeatingRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo, int ticks);

    IHeatableRecipe addHeatableRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo, int ticks, boolean replaceExisting);

    ICoolableRecipe createCoolableRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo, int ticks);

    ICoolableRecipe addCoolableRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo, int ticks, boolean replaceExisting);

    IDistilationRecipe createDistilationRecipe(FluidStack in, FluidStack outGas, FluidStack outLiquid, int ticks);

    IDistilationRecipe addDistilationRecipe(FluidStack in, FluidStack outGas, FluidStack outLiquid, int ticks, boolean replaceExisting);

    IComplexRefineryRegistry<IHeatableRecipe> getHeatableRegistry();

    IComplexRefineryRegistry<ICoolableRecipe> getCoolableRegistry();

    IComplexRefineryRegistry<IDistilationRecipe> getDistilationRegistry();

    public interface IComplexRefineryRegistry<R extends IComplexRefineryRecipe> {
        /** @return an unmodifiable set containing all of the distilation recipies that satisfy the given predicate. All
         *         of the recipe objects are guarenteed to never be null. */
        Set<R> getRecipes(Predicate<R> toReturn);

        /** @return an unmodifiable set containing all of the distilation recipies. */
        Set<R> getAllRecipes();

        R getRecipeForInput(FluidStack fluid);

        Set<R> removeRecipes(Predicate<R> toRemove);

        R addRecipe(R recipe, boolean replaceExisting);
    }

    public interface IComplexRefineryRecipe {
        int ticks();

        FluidStack in();
    }

    public interface IHeatExchangerRecipe extends IComplexRefineryRecipe {
        FluidStack out();

        int heatFrom();

        int heatTo();
    }

    public interface IHeatableRecipe extends IHeatExchangerRecipe {}

    public interface ICoolableRecipe extends IHeatExchangerRecipe {}

    public interface IDistilationRecipe extends IComplexRefineryRecipe {
        FluidStack outGas();

        FluidStack outLiquid();
    }
}

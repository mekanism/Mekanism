package mekanism.common.attachments.containers.fluid;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.container.LargeResourceStack;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.common.attachments.containers.AttachedResources;
import mekanism.common.attachments.containers.ContainsRecipe;
import mekanism.common.attachments.containers.creator.BaseContainerCreator;
import mekanism.common.attachments.containers.creator.IBasicContainerCreator;
import mekanism.common.config.MekanismConfig;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.lookup.cache.IInputRecipeCache;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

public class FluidTanksBuilder {

    public static FluidTanksBuilder builder() {
        return new FluidTanksBuilder();
    }

    private final List<IBasicContainerCreator<? extends ComponentBackedFluidTank>> tankCreators = new ArrayList<>();

    private FluidTanksBuilder() {
    }

    public BaseContainerCreator<AttachedResources<FluidResource>, ComponentBackedFluidTank> build() {
        return new BaseFluidTankCreator(tankCreators);
    }

    public <VANILLA_INPUT extends RecipeInput, RECIPE extends MekanismRecipe<VANILLA_INPUT>, INPUT_CACHE extends IInputRecipeCache> FluidTanksBuilder addBasic(long capacity,
          IMekanismRecipeTypeProvider<VANILLA_INPUT, RECIPE, INPUT_CACHE> recipeType, ContainsRecipe<INPUT_CACHE, FluidResource> containsRecipe) {
        return addBasic(capacity, fluid -> containsRecipe.check(recipeType.getInputCache(), null, fluid));
    }

    public FluidTanksBuilder addBasic(long capacity, Predicate<FluidResource> isValid) {
        return addBasic(() -> capacity, isValid);
    }

    public FluidTanksBuilder addBasic(LongSupplier capacity, Predicate<FluidResource> isValid) {
        return addTank((type, attachedTo, containerIndex) -> new ComponentBackedFluidTank(attachedTo,
              containerIndex, ConstantPredicates.manualOnly(), ConstantPredicates.alwaysTrueBi(), isValid, MekanismConfig.general.fluidItemFillRate, capacity));
    }

    public FluidTanksBuilder addBasic(long capacity) {
        return addBasic(() -> capacity);
    }

    public FluidTanksBuilder addBasic(LongSupplier capacity) {
        return addTank((type, attachedTo, containerIndex) -> new ComponentBackedFluidTank(attachedTo,
              containerIndex, ConstantPredicates.manualOnly(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue(),
              MekanismConfig.general.fluidItemFillRate, capacity));
    }

    public FluidTanksBuilder addBasicExtractable(IntSupplier rate, LongSupplier capacity, Predicate<FluidResource> isValid) {
        return addTank((type, attachedTo, containerIndex) -> new ComponentBackedFluidTank(attachedTo,
              containerIndex, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), isValid, rate, capacity));
    }

    public FluidTanksBuilder addTank(IBasicContainerCreator<? extends ComponentBackedFluidTank> tank) {
        tankCreators.add(tank);
        return this;
    }

    private static class BaseFluidTankCreator extends BaseContainerCreator<AttachedResources<FluidResource>, ComponentBackedFluidTank> {

        public BaseFluidTankCreator(List<IBasicContainerCreator<? extends ComponentBackedFluidTank>> creators) {
            super(creators);
        }

        @Override
        public AttachedResources<FluidResource> initStorage(int containers) {
            return AttachedResources.create(containers, LargeResourceStack.EMPTY_FLUID_STACK);
        }
    }
}
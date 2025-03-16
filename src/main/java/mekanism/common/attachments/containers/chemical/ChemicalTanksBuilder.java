package mekanism.common.attachments.containers.chemical;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.common.attachments.containers.ContainsRecipe;
import mekanism.common.attachments.containers.creator.BaseContainerCreator;
import mekanism.common.attachments.containers.creator.IBasicContainerCreator;
import mekanism.common.config.MekanismConfig;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.lookup.cache.IInputRecipeCache;
import net.minecraft.world.item.crafting.RecipeInput;

public class ChemicalTanksBuilder {

    public static ChemicalTanksBuilder builder() {
        return new ChemicalTanksBuilder();
    }

    protected final List<IBasicContainerCreator<? extends ComponentBackedChemicalTank>> tankCreators = new ArrayList<>();

    protected ChemicalTanksBuilder() {
    }

    public BaseContainerCreator<AttachedChemicals, ComponentBackedChemicalTank> build() {
        return new BaseChemicalTankBuilder(tankCreators);
    }

    public <VANILLA_INPUT extends RecipeInput, RECIPE extends MekanismRecipe<VANILLA_INPUT>, INPUT_CACHE extends IInputRecipeCache> ChemicalTanksBuilder addBasic(long capacity,
          IMekanismRecipeTypeProvider<VANILLA_INPUT, RECIPE, INPUT_CACHE> recipeType, ContainsRecipe<INPUT_CACHE, ChemicalStack> containsRecipe) {
        return addBasic(capacity, chemical -> containsRecipe.check(recipeType.getInputCache(), null, chemical));
    }

    public ChemicalTanksBuilder addBasic(long capacity, Predicate<ChemicalStack> isValid) {
        return addBasic(() -> capacity, isValid);
    }

    public ChemicalTanksBuilder addBasic(LongSupplier capacity, Predicate<ChemicalStack> isValid) {
        return addTank((type, attachedTo, containerIndex) -> new ComponentBackedChemicalTank(attachedTo,
              containerIndex, ConstantPredicates.manualOnly(), ConstantPredicates.alwaysTrueBi(), isValid, MekanismConfig.general.chemicalItemFillRate, capacity, null));
    }

    public ChemicalTanksBuilder addBasic(long capacity) {
        return addBasic(() -> capacity);
    }

    public ChemicalTanksBuilder addBasic(LongSupplier capacity) {
        return addTank((type, attachedTo, containerIndex) -> new ComponentBackedChemicalTank(attachedTo,
              containerIndex, ConstantPredicates.manualOnly(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue(),
              MekanismConfig.general.chemicalItemFillRate, capacity, null));
    }

    public ChemicalTanksBuilder addInternalStorage(LongSupplier rate, LongSupplier capacity, Predicate<ChemicalStack> isValid) {
        return addTank((type, attachedTo, containerIndex) -> new ComponentBackedChemicalTank(attachedTo,
              containerIndex, ConstantPredicates.notExternal(), ConstantPredicates.alwaysTrueBi(), isValid, rate, capacity, null));
    }

    public ChemicalTanksBuilder addTank(IBasicContainerCreator<? extends ComponentBackedChemicalTank> tank) {
        tankCreators.add(tank);
        return this;
    }

    private static class BaseChemicalTankBuilder extends BaseContainerCreator<AttachedChemicals, ComponentBackedChemicalTank> {

        public BaseChemicalTankBuilder(List<IBasicContainerCreator<? extends ComponentBackedChemicalTank>> creators) {
            super(creators);
        }

        @Override
        public AttachedChemicals initStorage(int containers) {
            return AttachedChemicals.create(containers);
        }
    }
}
package mekanism.common.attachments.containers.chemical;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.common.attachments.containers.ContainsRecipe;
import mekanism.common.attachments.containers.creator.BaseContainerCreator;
import mekanism.common.attachments.containers.creator.IBasicContainerCreator;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.lookup.cache.IInputRecipeCache;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.NotNull;

public abstract class ChemicalTanksBuilder<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      TANK extends ComponentBackedChemicalTank<CHEMICAL, STACK, ?>, BUILDER extends ChemicalTanksBuilder<CHEMICAL, STACK, TANK, BUILDER>> {

    protected final List<IBasicContainerCreator<? extends TANK>> tankCreators = new ArrayList<>();

    protected ChemicalTanksBuilder() {
    }

    public abstract BaseContainerCreator<?, TANK> build();

    @SuppressWarnings("unchecked")
    public <VANILLA_INPUT extends RecipeInput, RECIPE extends MekanismRecipe<VANILLA_INPUT>, INPUT_CACHE extends IInputRecipeCache> BUILDER addBasic(long capacity,
          IMekanismRecipeTypeProvider<VANILLA_INPUT, RECIPE, INPUT_CACHE> recipeType, ContainsRecipe<INPUT_CACHE, STACK> containsRecipe) {
        return addBasic(capacity, chemical -> containsRecipe.check(recipeType.getInputCache(), null, (STACK) chemical.getStack(1)));
    }

    public BUILDER addBasic(long capacity, Predicate<@NotNull CHEMICAL> isValid) {
        return addBasic(() -> capacity, isValid);
    }

    public abstract BUILDER addBasic(LongSupplier capacity, Predicate<@NotNull CHEMICAL> isValid);

    public BUILDER addBasic(long capacity) {
        return addBasic(() -> capacity);
    }

    public abstract BUILDER addBasic(LongSupplier capacity);

    public abstract BUILDER addInternalStorage(LongSupplier rate, LongSupplier capacity, Predicate<@NotNull CHEMICAL> isValid);

    @SuppressWarnings("unchecked")
    public BUILDER addTank(IBasicContainerCreator<? extends TANK> tank) {
        tankCreators.add(tank);
        return (BUILDER) this;
    }
}
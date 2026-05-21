package mekanism.common.attachments.containers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.attachments.containers.creator.BaseContainerCreator;
import mekanism.common.attachments.containers.creator.IBasicContainerCreator;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.lookup.cache.IInputRecipeCache;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.resource.Resource;

public abstract class ResourceContainersBuilder<RESOURCE extends Resource, CONTAINER extends ComponentBackedResourceContainer<RESOURCE>,
      BUILDER extends ResourceContainersBuilder<RESOURCE, CONTAINER, BUILDER>> {

    protected final List<IBasicContainerCreator<? extends CONTAINER>> containerCreators = new ArrayList<>();

    protected ResourceContainersBuilder() {
    }

    public abstract BaseContainerCreator<AttachedResources<RESOURCE>, CONTAINER> build();

    protected abstract IntSupplier defaultRate();

    @SuppressWarnings("unchecked")
    public final BUILDER addContainer(IBasicContainerCreator<? extends CONTAINER> tank) {
        containerCreators.add(tank);
        return (BUILDER) this;
    }

    protected abstract CONTAINER createBasicContainer(ItemAccess attachedAccess, int tankIndex, BiPredicate<RESOURCE, AutomationType> canExtract,
          BiPredicate<RESOURCE, AutomationType> canInsert, Predicate<RESOURCE> validator, IntSupplier rate, LongSupplier capacity);

    public final <VANILLA_INPUT extends RecipeInput, RECIPE extends MekanismRecipe<VANILLA_INPUT>, INPUT_CACHE extends IInputRecipeCache> BUILDER addBasic(long capacity,
          IMekanismRecipeTypeProvider<VANILLA_INPUT, RECIPE, INPUT_CACHE> recipeType, ContainsRecipe<INPUT_CACHE, RESOURCE> containsRecipe) {
        return addBasic(capacity, resource -> containsRecipe.check(recipeType.getInputCache(), null, resource));
    }

    public final BUILDER addBasic(long capacity, Predicate<RESOURCE> isValid) {
        return addBasic(() -> capacity, isValid);
    }

    public final BUILDER addBasic(LongSupplier capacity, Predicate<RESOURCE> isValid) {
        return addContainer((_, attachedAccess, containerIndex) -> createBasicContainer(attachedAccess,
              containerIndex, ConstantPredicates.manualOnly(), ConstantPredicates.alwaysTrueBi(), isValid, defaultRate(), capacity));
    }

    public final BUILDER addBasic(long capacity) {
        return addBasic(() -> capacity);
    }

    public final BUILDER addBasic(LongSupplier capacity) {
        return addContainer((_, attachedAccess, containerIndex) -> createBasicContainer(attachedAccess,
              containerIndex, ConstantPredicates.manualOnly(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue(),
              defaultRate(), capacity));
    }

    public final BUILDER addBasicExtractable(IntSupplier rate, LongSupplier capacity, Predicate<RESOURCE> isValid) {
        return addContainer((_, attachedAccess, containerIndex) -> createBasicContainer(attachedAccess,
              containerIndex, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), isValid, rate, capacity));
    }

    public final BUILDER addInternalStorage(IntSupplier rate, LongSupplier capacity, Predicate<RESOURCE> isValid) {
        return addContainer((_, attachedAccess, containerIndex) -> createBasicContainer(attachedAccess,
              containerIndex, ConstantPredicates.notExternal(), ConstantPredicates.alwaysTrueBi(), isValid, rate, capacity));
    }

    public static class BaseContainerBuilder<RESOURCE extends Resource, CONTAINER extends ComponentBackedResourceContainer<RESOURCE>> extends
          BaseContainerCreator<AttachedResources<RESOURCE>, CONTAINER> {

        private final LargeResourceStack.StackHelper<RESOURCE> stackHelper;

        public BaseContainerBuilder(List<IBasicContainerCreator<? extends CONTAINER>> creators, LargeResourceStack.StackHelper<RESOURCE> stackHelper) {
            super(creators);
            this.stackHelper = stackHelper;
        }

        @Override
        public AttachedResources<RESOURCE> initStorage(int containers) {
            return AttachedResources.create(containers, stackHelper.empty());
        }
    }
}
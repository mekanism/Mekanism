package mekanism.common.integration.projecte.mappers;

import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.INSSFakeGroupManager;
import moze_intel.projecte.api.mapper.recipe.IRecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;

public abstract class TypedMekanismRecipeMapper<RECIPE extends Recipe<?>> implements IRecipeTypeMapper {

    private final Holder<RecipeType<?>>[] supportedTypes;
    private final Class<RECIPE> recipeClass;

    @SafeVarargs
    protected TypedMekanismRecipeMapper(Class<RECIPE> recipeClass, DeferredHolder<RecipeType<?>, ? extends RecipeType<? extends RECIPE>>... supportedTypes) {
        this.supportedTypes = supportedTypes;
        this.recipeClass = recipeClass;
    }

    @Override
    public final boolean canHandle(RecipeType<?> recipeType) {
        for (Holder<RecipeType<?>> supportedType : supportedTypes) {
            if (supportedType.value() == recipeType) {
                return true;
            }
        }
        return false;
    }

    protected abstract boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, RECIPE recipe);

    @Override
    public final boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, RecipeHolder<?> recipeHolder, RegistryAccess registryAccess,
          INSSFakeGroupManager groupManager) {
        Recipe<?> recipe = recipeHolder.value();
        if (recipeClass.isInstance(recipe)) {
            //Double check that we have a type of recipe we know how to handle
            return handleRecipe(mapper, recipeClass.cast(recipe));
        }
        return false;
    }
}
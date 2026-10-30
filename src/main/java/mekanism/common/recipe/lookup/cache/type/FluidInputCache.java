package mekanism.common.recipe.lookup.cache.type;

import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.CompoundFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.DataComponentFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SimpleFluidIngredient;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jspecify.annotations.Nullable;

public class FluidInputCache<RECIPE extends MekanismRecipe<?, ?, ?>> extends ComponentSensitiveInputCache<Fluid, FluidStack, FluidStackIngredient, RECIPE> {

    @Override
    public boolean mapInputs(RecipeHolder<RECIPE> recipeHolder, FluidStackIngredient inputIngredient) {
        return mapIngredient(recipeHolder, inputIngredient.ingredient().ingredient());
    }

    private boolean mapIngredient(RecipeHolder<RECIPE> recipeHolder, FluidIngredient input) {
        if (input.isSimple() && input instanceof SimpleFluidIngredient simpleFluidIngredient) {
            //Simple ingredients don't actually check anything related to NBT,
            // so we can add the items to our base/raw input cache directly
            mapPlainFluidSet(recipeHolder, simpleFluidIngredient.fluidSet());
        } else if (input instanceof CompoundFluidIngredient compoundIngredient) {
            //Special handling for neo's compound ingredient to map all children as best as we can
            // as maybe some of them are simple
            boolean result = false;
            for (FluidIngredient child : compoundIngredient.children()) {
                result |= mapIngredient(recipeHolder, child);
            }
            return result;
        } else if (input instanceof DataComponentFluidIngredient componentIngredient && componentIngredient.isStrict()) {
            //Special handling for neo's NBT Ingredient as it requires an exact component match (when isStrict())
            DataComponentExactPredicate componentPredicate = componentIngredient.components();
            if (componentPredicate.alwaysMatches()) {
                //why is this a component ingredient? lol
                mapPlainFluidSet(recipeHolder, componentIngredient.fluidSet());
                return false;
            }
            DataComponentPatch patch = componentIngredient.components().asPatch();
            for (Holder<Fluid> holder : componentIngredient.fluidSet()) {
                addComponentInputCache(holder, patch, recipeHolder);
            }
        } else {
            //Else it is a custom ingredient, so we don't have a great way of handling it using the normal extraction checks
            // and instead have to just mark it as complex and test as needed
            return true;
        }
        return false;
    }

    private void mapPlainFluidSet(RecipeHolder<RECIPE> recipeHolder, HolderSet<Fluid> fluidSet) {
        for (Holder<Fluid> fluid : fluidSet) {
            if (fluid.isBound()) {
                //Ignore unbound holders just in case
                addInputCache(fluid, recipeHolder);
            }
        }
    }

    @Override
    public boolean isEmpty(@Nullable TypedInstance<Fluid> input) {
        return switch (input) {
            case FluidStack stack -> stack.isEmpty();
            case FluidResource resource -> resource.isEmpty();
            case null -> true;
            default -> false;
        };
    }
}
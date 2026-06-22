package mekanism.common.recipe;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStackTemplate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStackTemplate;

/// Interface for helping split the recipe provider over multiple classes to make it a bit easier to interact with
public interface ISubRecipeProvider {

    void addRecipes(RecipeOutput output, HolderLookup.Provider registries);

    HolderGetter<Item> items();

    HolderGetter<Chemical> chemicals();

    HolderGetter<Fluid> fluids();

    default ItemStackTemplate template(ResourceKey<Item> item) {
        return RecipeProviderUtil.template(items(), item);
    }

    default ItemStackTemplate template(ResourceKey<Item> item, int amount) {
        return RecipeProviderUtil.template(items(), item, amount);
    }

    default ItemStackTemplate template(BlockItemId item) {
        return RecipeProviderUtil.template(items(), item);
    }

    default ItemStackTemplate template(BlockItemId item, int amount) {
        return RecipeProviderUtil.template(items(), item, amount);
    }

    default ChemicalStackTemplate chemicalTemplate(ResourceKey<Chemical> chemical, int amount) {
        return RecipeProviderUtil.chemicalTemplate(chemicals(), chemical, amount);
    }

    default FluidStackTemplate fluidTemplate(ResourceKey<Fluid> fluid, int amount) {
        return RecipeProviderUtil.fluidTemplate(fluids(), fluid, amount);
    }
}
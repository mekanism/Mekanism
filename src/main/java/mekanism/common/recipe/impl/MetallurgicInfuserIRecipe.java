package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.basic.BasicMetallurgicInfuserRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.InfusionStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

@NothingNullByDefault
public class MetallurgicInfuserIRecipe extends BasicMetallurgicInfuserRecipe {

    public MetallurgicInfuserIRecipe(ItemStackIngredient itemInput, InfusionStackIngredient infusionInput, ItemStack output) {
        super(itemInput, infusionInput, output);
    }

    @Override
    public RecipeSerializer<BasicMetallurgicInfuserRecipe> getSerializer() {
        return MekanismRecipeSerializers.METALLURGIC_INFUSING.get();
    }

}
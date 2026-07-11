package mekanism.common.integration.recipe_stages.impl;

import com.blamejared.recipestages.RecipeStagesUtil;
import com.blamejared.recipestages.recipes.IStagedRecipe;
import java.util.Optional;
import mekanism.common.integration.recipe_stages.IRecipeStagesHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public class RecipeStagesHelper implements IRecipeStagesHelper {

    @Override
    public Optional<ItemStack> tryAssembleRecipe(CraftingInput craftingInput, CraftingRecipe recipe) {
        if (recipe instanceof IStagedRecipe stagedRecipe) {
            //Force assemble it as we handle validating if specific players can see/grab the output ourselves
            return Optional.of(stagedRecipe.forceAssemble(craftingInput));
        }
        return Optional.empty();
    }

    @Override
    public boolean hasStageForRecipe(RecipeHolder<CraftingRecipe> recipe, ServerPlayer player) {
        return RecipeStagesUtil.hasStageForRecipe(recipe.value(), player);
    }
}
package mekanism.common.integration.recipe_stages;

import java.util.Optional;
import mekanism.api.MekanismAPI;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jspecify.annotations.Nullable;

public interface IRecipeStagesHelper {

    @Nullable
    IRecipeStagesHelper INSTANCE = MekanismAPI.getOptionalService(IRecipeStagesHelper.class);

    Optional<ItemStack> tryAssembleRecipe(CraftingInput craftingInput, CraftingRecipe recipe);

    boolean hasStageForRecipe(RecipeHolder<CraftingRecipe> recipe, ServerPlayer player);
}
package mekanism.defense.common;

import java.util.concurrent.CompletableFuture;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.recipe.BaseRecipeProvider;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

@NothingNullByDefault
public class DefenseRecipeProvider extends BaseRecipeProvider {

    public DefenseRecipeProvider(PackOutput output, ExistingFileHelper existingFileHelper, CompletableFuture<Provider> lookupProvider) {
        super(output, existingFileHelper, MekanismDefense.MODID, lookupProvider);
    }

    @Override
    protected void addRecipes(RecipeOutput consumer) {
    }
}
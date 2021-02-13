package mekanism.common.recipe.compat;

import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.common.recipe.ISubRecipeProvider;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.AndCondition;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;

//TODO: Decide if we should have compat recipes go into their own data packs
@ParametersAreNonnullByDefault
public abstract class CompatRecipeProvider implements ISubRecipeProvider {

    protected final String modid;
    protected final ICondition modLoaded;
    protected final ICondition allModsLoaded;

    protected CompatRecipeProvider(String modid, String... secondaryMods) {
        this.modid = modid;
        this.modLoaded = new ModLoadedCondition(modid);
        if (secondaryMods.length == 0) {
            allModsLoaded = modLoaded;
        } else {
            ICondition combined = modLoaded;
            for (String secondaryMod : secondaryMods) {
                combined = new AndCondition(combined, new ModLoadedCondition(secondaryMod));
            }
            allModsLoaded = combined;
        }
    }

    @Override
    public final void addRecipes(Consumer<IFinishedRecipe> consumer) {
        registerRecipes(consumer, getBasePath());
    }

    protected abstract void registerRecipes(Consumer<IFinishedRecipe> consumer, String basePath);

    protected String getBasePath() {
        return "compat/" + modid + "/";
    }

    protected ResourceLocation rl(String path) {
        return new ResourceLocation(modid, path);
    }
}
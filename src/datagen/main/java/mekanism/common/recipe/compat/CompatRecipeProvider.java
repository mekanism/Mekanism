package mekanism.common.recipe.compat;

import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.common.recipe.ISubRecipeProvider;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;

//TODO: Decide if we should have compat recipes go into their own data packs
@ParametersAreNonnullByDefault
public abstract class CompatRecipeProvider implements ISubRecipeProvider {

    protected final String modid;
    protected final ICondition modLoaded;

    protected CompatRecipeProvider(String modid) {
        this.modid = modid;
        this.modLoaded = new ModLoadedCondition(modid);
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
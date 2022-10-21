package mekanism.common.recipe.compat;

import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.recipe.ISubRecipeProvider;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.crafting.conditions.AndCondition;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;

//TODO: Decide if we should have compat recipes go into their own data packs
@NothingNullByDefault
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
    public final void addRecipes(Consumer<FinishedRecipe> consumer) {
        registerRecipes(consumer, getBasePath());
    }

    protected abstract void registerRecipes(Consumer<FinishedRecipe> consumer, String basePath);

    protected String getBasePath() {
        return "compat/" + modid + "/";
    }

    protected ResourceLocation rl(String path) {
        return new ResourceLocation(modid, path);
    }

    protected TagKey<Item> tag(String path) {
        return ItemTags.create(rl(path));
    }
}
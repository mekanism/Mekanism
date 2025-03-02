package mekanism.common.recipe.compat;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.recipe.ISubRecipeProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.conditions.AndCondition;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;

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
            List<ICondition> combined = new ArrayList<>();
            combined.add(modLoaded);
            for (String secondaryMod : secondaryMods) {
                combined.add(new ModLoadedCondition(secondaryMod));
            }
            allModsLoaded = new AndCondition(combined);
        }
    }

    @Override
    public final void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = getBasePath();
        registerRecipes(consumer, basePath, registries);
    }

    protected abstract void registerRecipes(RecipeOutput consumer, String basePath, HolderLookup.Provider registries);

    protected String getBasePath() {
        return "compat/" + modid + "/";
    }

    protected ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(modid, path);
    }

    protected TagKey<Item> tag(String path) {
        return ItemTags.create(rl(path));
    }

    protected Holder<Item> foreignItem(HolderLookup.Provider registries, ResourceLocation id) {
        return registries.lookupOrThrow(Registries.ITEM).getOrThrow(ResourceKey.create(Registries.ITEM, id));
    }

    protected ItemStack foreignItemStack(HolderLookup.Provider registries, ResourceLocation id, int count) {
        return new ItemStack(foreignItem(registries, id), count);
    }

    protected ItemStack foreignItemStack(HolderLookup.Provider registries, ResourceLocation id) {
        return foreignItemStack(registries, id, 1);
    }
}
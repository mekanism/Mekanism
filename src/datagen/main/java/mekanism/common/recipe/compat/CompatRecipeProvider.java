package mekanism.common.recipe.compat;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.common.recipe.impl.BaseSubRecipeProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.neoforged.neoforge.common.conditions.AndCondition;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;

//TODO: Decide if we should have compat recipes go into their own data packs
public abstract class CompatRecipeProvider extends BaseSubRecipeProvider {

    protected final HolderLookup.Provider registries;
    protected final String modid;
    protected final ICondition modLoaded;
    protected final ICondition allModsLoaded;

    protected CompatRecipeProvider(HolderLookup.Provider registries, String modid, String... secondaryMods) {
        super(registries.lookupOrThrow(Registries.ITEM), registries.lookupOrThrow(Registries.FLUID), registries.lookupOrThrow(MekanismAPI.CHEMICAL_REGISTRY_NAME));
        this.registries = registries;
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

    protected Identifier rl(String path) {
        return Identifier.fromNamespaceAndPath(modid, path);
    }

    protected TagKey<Item> tag(String path) {
        return ItemTags.create(rl(path));
    }

    protected Holder<Item> foreignItem(Identifier id) {
        return items.getOrThrow(ResourceKey.create(Registries.ITEM, id));
    }

    protected ItemStackTemplate foreignItemStack(Identifier id, int count) {
        return new ItemStackTemplate(foreignItem(id), count);
    }

    protected ItemStackTemplate foreignItemStack(Identifier id) {
        return foreignItemStack(id, 1);
    }
}
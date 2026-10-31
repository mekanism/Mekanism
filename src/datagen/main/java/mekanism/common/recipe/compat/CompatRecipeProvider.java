package mekanism.common.recipe.compat;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.MekanismRegistries;
import mekanism.common.recipe.impl.BaseSubRecipeProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.worldgen.BootstrapContextAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.conditions.AndCondition;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.common.data.ScopedRecipeOutput;

//TODO: Decide if we should have compat recipes go into their own data packs
public abstract class CompatRecipeProvider extends BaseSubRecipeProvider {

    protected final HolderGetter<Block> blocks;
    protected final String modid;
    private final ICondition modLoaded;
    private final ICondition allModsLoaded;

    protected CompatRecipeProvider(BootstrapContextAccess contextAccess, String modid, String... secondaryMods) {
        super(contextAccess.lookup(Registries.ITEM), contextAccess.lookup(Registries.FLUID), contextAccess.lookup(MekanismRegistries.Keys.CHEMICAL));
        this.modid = modid;
        this.modLoaded = new ModLoadedCondition(modid);
        this.blocks = contextAccess.lookup(Registries.BLOCK);
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
    public final void addRecipes(RecipeOutput consumer) {
        String basePath = getBasePath();
        try (ScopedRecipeOutput output = consumer.withContext(builder -> builder.withConditions(allModsLoaded))) {
            registerRecipes(output, basePath);
        }
    }

    protected abstract void registerRecipes(RecipeOutput consumer, String basePath);

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

    protected Holder<Block> foreignBlock(Identifier id) {
        return blocks.getOrThrow(ResourceKey.create(Registries.BLOCK, id));
    }

    protected ItemStackTemplate foreignItemStack(Identifier id, int count) {
        return new ItemStackTemplate(foreignItem(id), count);
    }

    protected ItemStackTemplate foreignItemStack(Identifier id) {
        return foreignItemStack(id, 1);
    }
}
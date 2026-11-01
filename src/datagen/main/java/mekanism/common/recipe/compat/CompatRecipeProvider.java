package mekanism.common.recipe.compat;

import mekanism.api.datagen.recipe.RecipeCriterion;
import mekanism.common.Mekanism;
import mekanism.common.recipe.BaseRecipeProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;

public abstract class CompatRecipeProvider extends BaseRecipeProvider {

    protected final String modid;

    protected CompatRecipeProvider(BootstrapContext<Recipe<?>> recipeOutput, BootstrapContext<Advancement> advancementOutput, String modid) {
        super(recipeOutput, advancementOutput, Mekanism.MODID);
        this.modid = modid;
    }

    protected String getBasePath() {
        return "compat/" + modid + "/";
    }

    protected TagKey<Item> tag(String path) {
        return ItemTags.create(Identifier.fromNamespaceAndPath(modid, path));
    }

    private Holder<Item> foreignItem(Identifier id) {
        return items.getOrThrow(ResourceKey.create(Registries.ITEM, id));
    }

    protected RecipeCriterion hasForeignCriterion(Identifier id) {
        return hasCriterion(foreignItem(id));
    }

    protected ItemStackTemplate foreignTemplate(Identifier id, int count) {
        return new ItemStackTemplate(foreignItem(id), count);
    }

    protected ItemStackTemplate foreignTemplate(Identifier id) {
        return foreignTemplate(id, 1);
    }
}
package mekanism.common.recipe.impl;

import mekanism.api.datagen.recipe.builder.ItemStackChemicalToItemStackRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.recipe.BaseRecipeProvider;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.references.BlockItemId;
import net.minecraft.references.BlockItemIds;
import net.minecraft.references.ItemIds;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.block.ColorCollection;

class PaintingRecipeProvider implements ISubRecipeProvider {

    private final HolderGetter<Item> items;

    public PaintingRecipeProvider(HolderGetter<Item> items) {
        this.items = items;
    }

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "painting/";
        addDyeRecipes(consumer, basePath);
        int oneAtATime = PigmentExtractingRecipeProvider.DYE_RATE;
        int eightAtATime = oneAtATime / 8;
        //Some base input tags are effectively duplicates of vanilla, but are done to make sure we don't change
        // things that make no sense to be colored, such as some sort of fancy carpets, or a unique type of glass that
        // is tagged as glass, but shouldn't be able to be converted directly into stained-glass
        addRecoloringRecipes(consumer, MekanismTags.Items.COLORABLE_BEDS, oneAtATime, BlockItemIds.BED, basePath + "bed/");
        addRecoloringRecipes(consumer, MekanismTags.Items.COLORABLE_WOOL, oneAtATime, BlockItemIds.WOOL, basePath + "wool/");
        addRecoloringRecipes(consumer, MekanismTags.Items.COLORABLE_GLASS, eightAtATime, BlockItemIds.STAINED_GLASS, basePath + "glass/");
        addRecoloringRecipes(consumer, MekanismTags.Items.COLORABLE_TERRACOTTA, eightAtATime, BlockItemIds.DYED_TERRACOTTA, basePath + "terracotta/");
        addRecoloringRecipes(consumer, MekanismTags.Items.COLORABLE_GLASS_PANES, eightAtATime, BlockItemIds.STAINED_GLASS_PANE, basePath + "glass_pane/");
        addRecoloringRecipes(consumer, MekanismTags.Items.COLORABLE_CARPETS, eightAtATime, BlockItemIds.CARPET, basePath + "carpet/");
        //TODO: Eventually we may want to consider taking patterns into account
        addRecoloringRecipes(consumer, MekanismTags.Items.COLORABLE_BANNERS, oneAtATime, BlockItemIds.BANNER, basePath + "banner/");
        //TODO: Shulker boxes?
        //TODO - 26.2: Glazed terracotta?
        addRecoloringRecipes(consumer, MekanismTags.Items.COLORABLE_CONCRETE, eightAtATime, BlockItemIds.CONCRETE, basePath + "concrete/");
        addRecoloringRecipes(consumer, MekanismTags.Items.COLORABLE_CONCRETE_POWDER, eightAtATime, BlockItemIds.CONCRETE_POWDER, basePath + "concrete_powder/");
        addRecoloringRecipes(consumer, MekanismTags.Items.COLORABLE_CANDLE, oneAtATime, BlockItemIds.DYED_CANDLE, basePath + "candle/");
    }

    private void addDyeRecipes(RecipeOutput consumer, String basePath) {
        basePath += "dye/";
        for (EnumColor color : EnumUtils.COLORS) {
            DyeColor dyeColor = color.getDyeColor();
            if (dyeColor != null) {
                ItemStackChemicalToItemStackRecipeBuilder.painting(
                      IngredientCreatorAccess.item().from(MekanismItems.DYE_BASE),
                      IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.PIGMENT_COLOR_LOOKUP.get(color), PigmentExtractingRecipeProvider.DYE_RATE),
                      new ItemStackTemplate(items.getOrThrow(ItemIds.DYE.pick(dyeColor))),
                      false
                ).save(consumer, Mekanism.rl(basePath + dyeColor));
            }
        }
    }

    private void addRecoloringRecipes(RecipeOutput consumer, TagKey<Item> input, int rate, ColorCollection<BlockItemId> outputs, String basePath) {
        HolderSet<Item> inputTag = this.items.getOrThrow(input);
        for (EnumColor color : EnumUtils.COLORS) {
            DyeColor dyeColor = color.getDyeColor();
            if (dyeColor != null) {
                Holder<Item> result = items.getOrThrow(outputs.pick(dyeColor).item());
                ItemStackChemicalToItemStackRecipeBuilder.painting(
                      IngredientCreatorAccess.item().from(BaseRecipeProvider.difference(inputTag, result)),
                      IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.PIGMENT_COLOR_LOOKUP.get(color), rate),
                      new ItemStackTemplate(result),
                      false
                ).save(consumer, Mekanism.rl(basePath + dyeColor));
            }
        }
    }
}
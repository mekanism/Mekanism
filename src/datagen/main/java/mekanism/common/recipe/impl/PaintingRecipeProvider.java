package mekanism.common.recipe.impl;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.datagen.recipe.builder.ItemStackChemicalToItemStackRecipeBuilder;
import mekanism.api.recipes.book.PaintingBookCategory;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.BaseRecipeProvider;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.references.BlockItemId;
import net.minecraft.references.BlockItemIds;
import net.minecraft.references.ItemIds;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.block.ColorCollection;
import net.minecraft.world.level.material.Fluid;

public class PaintingRecipeProvider extends BaseSubRecipeProvider {

    public static final int ONE_AT_A_TIME = PigmentExtractingRecipeProvider.DYE_RATE;
    public static final int FOUR_AT_A_TIME = ONE_AT_A_TIME / 4;
    public static final int EIGHT_AT_A_TIME = ONE_AT_A_TIME / 8;

    PaintingRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput output) {
        String basePath = "painting/";
        addDyeRecipes(output, basePath);
        //Some base input tags are effectively duplicates of vanilla, but are done to make sure we don't change
        // things that make no sense to be colored, such as some sort of fancy carpets, or a unique type of glass that
        // is tagged as glass, but shouldn't be able to be converted directly into stained-glass
        addRecoloringRecipes(output, MekanismTags.Items.COLORABLE_BEDS, ONE_AT_A_TIME, BlockItemIds.BED, "bed_painting", basePath + "bed/");
        addRecoloringRecipes(output, MekanismTags.Items.COLORABLE_WOOL, ONE_AT_A_TIME, BlockItemIds.WOOL, "wool_painting", basePath + "wool/");
        addRecoloringRecipes(output, MekanismTags.Items.COLORABLE_WOOL_SLABS, ONE_AT_A_TIME, BlockItemIds.WOOL_SLAB, "wool_slab_painting", basePath + "wool/slabs/");
        addRecoloringRecipes(output, MekanismTags.Items.COLORABLE_WOOL_STAIRS, ONE_AT_A_TIME, BlockItemIds.WOOL_STAIRS, "wool_stairs_painting", basePath + "wool/stairs/");
        addRecoloringRecipes(output, MekanismTags.Items.COLORABLE_GLASS, EIGHT_AT_A_TIME, BlockItemIds.STAINED_GLASS, "glass_painting", basePath + "glass/");
        addRecoloringRecipes(output, MekanismTags.Items.COLORABLE_TERRACOTTA, EIGHT_AT_A_TIME, BlockItemIds.DYED_TERRACOTTA, "terracotta_painting", basePath + "terracotta/");
        addRecoloringRecipes(output, MekanismTags.Items.COLORABLE_GLAZED_TERRACOTTA, FOUR_AT_A_TIME, BlockItemIds.GLAZED_TERRACOTTA, "glazed_terracotta_painting", basePath + "terracotta/glazed/");
        addRecoloringRecipes(output, MekanismTags.Items.COLORABLE_GLASS_PANES, EIGHT_AT_A_TIME, BlockItemIds.STAINED_GLASS_PANE, "glass_pane_painting", basePath + "glass_pane/");
        addRecoloringRecipes(output, MekanismTags.Items.COLORABLE_CARPETS, EIGHT_AT_A_TIME, BlockItemIds.CARPET, "carpet_painting", basePath + "carpet/");
        //TODO: Eventually we may want to consider taking patterns into account
        addRecoloringRecipes(output, MekanismTags.Items.COLORABLE_BANNERS, ONE_AT_A_TIME, BlockItemIds.BANNER, "banner_painting", basePath + "banner/");
        //TODO: Shulker boxes and dyed bundles? Would require us to add support for copying data
        addRecoloringRecipes(output, MekanismTags.Items.COLORABLE_CONCRETE, EIGHT_AT_A_TIME, BlockItemIds.CONCRETE, "concrete_painting", basePath + "concrete/");
        addRecoloringRecipes(output, MekanismTags.Items.COLORABLE_CONCRETE_SLABS, EIGHT_AT_A_TIME, BlockItemIds.CONCRETE_SLAB, "concrete_slab_painting", basePath + "concrete/slabs/");
        addRecoloringRecipes(output, MekanismTags.Items.COLORABLE_CONCRETE_STAIRS, EIGHT_AT_A_TIME, BlockItemIds.CONCRETE_STAIRS, "concrete_stairs_painting", basePath + "concrete/stairs/");
        addRecoloringRecipes(output, MekanismTags.Items.COLORABLE_CONCRETE_POWDER, EIGHT_AT_A_TIME, BlockItemIds.CONCRETE_POWDER, "concrete_powder_painting", basePath + "concrete_powder/");
        addRecoloringRecipes(output, MekanismTags.Items.COLORABLE_CANDLE, ONE_AT_A_TIME, BlockItemIds.DYED_CANDLE, "candle_painting", basePath + "candle/");
        addItemRecoloringRecipes(output, MekanismTags.Items.COLORABLE_HARNESSES, ONE_AT_A_TIME, ItemIds.HARNESS, "harness_painting", basePath + "harness/");
        addItemRecoloringRecipes(output, MekanismTags.Items.COLORABLE_CUSHIONS, ONE_AT_A_TIME, ItemIds.CUSHION, "cushion_painting", basePath + "cushion/");
    }

    private void addDyeRecipes(RecipeOutput output, String basePath) {
        basePath += "dye/";
        for (DyeColor dyeColor : DyeColor.VALUES) {
            ResourceKey<Item> dye = ItemIds.DYE.pick(dyeColor);
            ItemStackChemicalToItemStackRecipeBuilder.painting(
                        IngredientCreatorAccess.item().fromHolder(MekanismItems.DYE_BASE),
                        IngredientCreatorAccess.chemicalStack().from(chemicals, ChemicalIds.SIMPLE_PIGMENTS.pick(dyeColor), PigmentExtractingRecipeProvider.DYE_RATE),
                        false,
                        new ItemStackTemplate(items.getOrThrow(dye))
                  ).category(PaintingBookCategory.DYE)
                  .group(dye)
                  .unlockedBy("has_needed_dye", has(dyeColor.getTag()))
                  .save(output, basePath + dyeColor);
        }
    }

    private void addRecoloringRecipes(RecipeOutput output, TagKey<Item> input, int rate, ColorCollection<BlockItemId> outputs, String group, String basePath) {
        addItemRecoloringRecipes(output, input, rate, outputs.map(BlockItemId::item), group, basePath);
    }

    private void addItemRecoloringRecipes(RecipeOutput output, TagKey<Item> input, int rate, ColorCollection<ResourceKey<Item>> outputs, String group, String basePath) {
        HolderSet<Item> inputTag = this.items.getOrThrow(input);
        for (DyeColor dyeColor : DyeColor.VALUES) {
            Holder<Item> result = items.getOrThrow(outputs.pick(dyeColor));
            ItemStackChemicalToItemStackRecipeBuilder.painting(
                        IngredientCreatorAccess.item().from(BaseRecipeProvider.difference(inputTag, result)),
                        IngredientCreatorAccess.chemicalStack().from(chemicals, ChemicalIds.SIMPLE_PIGMENTS.pick(dyeColor), rate),
                        false,
                        new ItemStackTemplate(result)
                  ).group(Mekanism.rl(group))
                  .unlockedBy("has_needed_dye", has(dyeColor.getTag()))
                  .save(output, basePath + dyeColor);
        }
    }
}
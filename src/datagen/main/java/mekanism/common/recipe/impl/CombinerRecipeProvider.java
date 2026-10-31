package mekanism.common.recipe.impl;

import java.util.Map;
import mekanism.api.chemical.Chemical;
import mekanism.api.datagen.recipe.builder.CombinerRecipeBuilder;
import mekanism.api.recipes.book.CombiningBookCategory;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.RegistryUtils;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.references.BlockItemIds;
import net.minecraft.references.ItemIds;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;

class CombinerRecipeProvider extends BaseSubRecipeProvider {

    CombinerRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput output) {
        String basePath = "combining/";
        addCombinerDyeRecipes(output, basePath + "dye/");
        addCombinerGlowRecipes(output, basePath + "glow/");
        addCombinerWaxingRecipes(output, basePath + "wax/");
        //Gravel
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(this.items, ItemIds.FLINT),
              IngredientCreatorAccess.item().from(this.items, Tags.BlockItems.COBBLESTONES_NORMAL),
              template(BlockItemIds.GRAVEL)
        ).category(CombiningBookCategory.BUILDING)
              .unlockedBy(hasCriterion(ItemIds.FLINT))
              .save(output, basePath + "gravel");
        //Obsidian
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.DUSTS_OBSIDIAN, 4),
              IngredientCreatorAccess.item().from(this.items, Tags.BlockItems.COBBLESTONES_DEEPSLATE),
              template(BlockItemIds.OBSIDIAN)
        ).category(CombiningBookCategory.BUILDING)
              .unlockedBy("has_obsidian", has(MekanismTags.Items.DUSTS_OBSIDIAN))
              .save(output, basePath + "obsidian");
        //Rooted Dirt
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.HANGING_ROOTS, 3),
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.DIRT),
              template(BlockItemIds.ROOTED_DIRT)
        ).category(CombiningBookCategory.BUILDING)
              .unlockedBy(hasCriterion(BlockItemIds.DIRT))
              .save(output, basePath + "rooted_dirt");
        //Packed mud
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.CROPS_WHEAT),
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.MUD),
              template(BlockItemIds.PACKED_MUD)
        ).category(CombiningBookCategory.BUILDING)
              .unlockedBy(hasCriterion(BlockItemIds.MUD))
              .save(output, basePath + "packed_mud");
        //Muddy mangrove roots
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.MANGROVE_ROOTS),
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.MUD),
              template(BlockItemIds.MUDDY_MANGROVE_ROOTS)
        ).category(CombiningBookCategory.BUILDING)
              .unlockedBy(hasCriterion(BlockItemIds.MUD))
              .save(output, basePath + "muddy_mangrove_roots");
    }

    private void addCombinerDyeRecipes(RecipeOutput output, String basePath) {
        //Black + white -> light gray
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.DYES_BLACK),
              IngredientCreatorAccess.item().from(this.items, Tags.Items.DYES_WHITE, 2),
              template(ItemIds.DYE.lightGray(), 6)
        ).category(CombiningBookCategory.DECORATIVE)
              .group(ItemIds.DYE.lightGray())
              .unlockedBy("has_black_dye", has(Tags.Items.DYES_BLACK))
              .unlockedBy("has_white_dye", has(Tags.Items.DYES_WHITE))
              .save(output, basePath + "black_to_light_gray");
        //Blue + green -> cyan
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.DYES_BLUE),
              IngredientCreatorAccess.item().from(this.items, Tags.Items.DYES_GREEN),
              template(ItemIds.DYE.cyan(), 4)
        ).category(CombiningBookCategory.DECORATIVE)
              .group(ItemIds.DYE.cyan())
              .unlockedBy("has_blue_dye", has(Tags.Items.DYES_BLUE))
              .unlockedBy("has_green_dye", has(Tags.Items.DYES_GREEN))
              .save(output, basePath + "cyan");
        //Gray + white -> light gray
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.DYES_GRAY),
              IngredientCreatorAccess.item().from(this.items, Tags.Items.DYES_WHITE),
              template(ItemIds.DYE.lightGray(), 4)
        ).category(CombiningBookCategory.DECORATIVE)
              .group(ItemIds.DYE.lightGray())
              .unlockedBy("has_gray_dye", has(Tags.Items.DYES_GRAY))
              .unlockedBy("has_white_dye", has(Tags.Items.DYES_WHITE))
              .save(output, basePath + "gray_to_light_gray");
        //Blue + white -> light blue
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.DYES_BLUE),
              IngredientCreatorAccess.item().from(this.items, Tags.Items.DYES_WHITE),
              template(ItemIds.DYE.lightBlue(), 4)
        ).category(CombiningBookCategory.DECORATIVE)
              .group(ItemIds.DYE.lightBlue())
              .unlockedBy("has_blueu_dye", has(Tags.Items.DYES_BLUE))
              .unlockedBy("has_white_dye", has(Tags.Items.DYES_WHITE))
              .save(output, basePath + "light_blue");
        //Green + white -> lime
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.DYES_GREEN),
              IngredientCreatorAccess.item().from(this.items, Tags.Items.DYES_WHITE),
              template(ItemIds.DYE.lime(), 4)
        ).category(CombiningBookCategory.DECORATIVE)
              .group(ItemIds.DYE.lime())
              .unlockedBy("has_green_dye", has(Tags.Items.DYES_GREEN))
              .unlockedBy("has_white_dye", has(Tags.Items.DYES_WHITE))
              .save(output, basePath + "lime");
        //Purple + pink -> magenta
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.DYES_PURPLE),
              IngredientCreatorAccess.item().from(this.items, Tags.Items.DYES_PINK),
              template(ItemIds.DYE.magenta(), 4)
        ).category(CombiningBookCategory.DECORATIVE)
              .group(ItemIds.DYE.magenta())
              .unlockedBy("has_purple_dye", has(Tags.Items.DYES_PURPLE))
              .unlockedBy("has_pink_dye", has(Tags.Items.DYES_PINK))
              .save(output, basePath + "magenta");
        //Red + yellow -> orange
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.DYES_RED),
              IngredientCreatorAccess.item().from(this.items, Tags.Items.DYES_YELLOW),
              template(ItemIds.DYE.orange(), 4)
        ).category(CombiningBookCategory.DECORATIVE)
              .group(ItemIds.DYE.orange())
              .unlockedBy("has_red_dye", has(Tags.Items.DYES_RED))
              .unlockedBy("has_yellow_dye", has(Tags.Items.DYES_YELLOW))
              .save(output, basePath + "orange");
        //Red + white -> pink
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.DYES_RED),
              IngredientCreatorAccess.item().from(this.items, Tags.Items.DYES_WHITE),
              template(ItemIds.DYE.pink(), 4)
        ).category(CombiningBookCategory.DECORATIVE)
              .group(ItemIds.DYE.pink())
              .unlockedBy("has_red_dye", has(Tags.Items.DYES_RED))
              .unlockedBy("has_white_dye", has(Tags.Items.DYES_WHITE))
              .save(output, basePath + "pink");
        //Blue + red -> purple
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.DYES_BLUE),
              IngredientCreatorAccess.item().from(this.items, Tags.Items.DYES_RED),
              template(ItemIds.DYE.purple(), 4)
        ).category(CombiningBookCategory.DECORATIVE)
              .group(ItemIds.DYE.purple())
              .unlockedBy("has_blue_dye", has(Tags.Items.DYES_BLUE))
              .unlockedBy("has_red_dye", has(Tags.Items.DYES_RED))
              .save(output, basePath + "purple");
    }

    private void addCombinerGlowRecipes(RecipeOutput output, String basePath) {
        ItemStackIngredient glow = IngredientCreatorAccess.item().from(this.items, Tags.Items.DUSTS_GLOWSTONE);
        //Sweet Berries -> Glow Berries
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.SWEET_BERRY_CROP),
              glow,
              template(BlockItemIds.GLOW_BERRY_CROP)
        ).unlockedBy("has_glowstone", has(Tags.Items.DUSTS_GLOWSTONE))
              .save(output, basePath + "berries");
        //Ink Sac -> Glow Ink Sac
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(this.items, ItemIds.INK_SAC),
              glow,
              template(ItemIds.GLOW_INK_SAC)
        ).unlockedBy("has_glowstone", has(Tags.Items.DUSTS_GLOWSTONE))
              .save(output, basePath + "ink_sac");
        //Item Frame -> Glow Item Frame
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(this.items, ItemIds.ITEM_FRAME),
              glow,
              template(ItemIds.GLOW_ITEM_FRAME)
        ).unlockedBy("has_glowstone", has(Tags.Items.DUSTS_GLOWSTONE))
              .save(output, basePath + "item_frame");
    }

    private void addCombinerWaxingRecipes(RecipeOutput output, String basePath) {
        //Generate baseline recipes from waxing recipe set
        ItemStackIngredient wax = IngredientCreatorAccess.item().from(this.items, ItemIds.HONEYCOMB);
        for (Map.Entry<Block, Block> entry : HoneycombItem.WAXABLES.get().entrySet()) {
            Block result = entry.getValue();
            String path = RegistryUtils.getPath(result);
            String group;
            //Note: This is post wax addition so we don't need to check without wax from the name
            if (path.startsWith("waxed_oxidized_")) {
                group = "wax_oxidized";
            } else if (path.startsWith("waxed_weathered_")) {
                group = "wax_weathered";
            } else if (path.startsWith("waxed_exposed_")) {
                group = "wax_exposed";
            } else {
                group = "wax_unaffected";
            }
            CombinerRecipeBuilder.combining(
                  IngredientCreatorAccess.item().from(entry.getKey()),
                  wax,
                  new ItemStackTemplate(result.asItem())
            ).category(CombiningBookCategory.BUILDING)
                  .group(group)
                  .unlockedBy("has_wax", has(ItemIds.HONEYCOMB))
                  .save(output, basePath + path);
        }
    }
}
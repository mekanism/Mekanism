package mekanism.common.recipe.impl;

import java.util.Map;
import mekanism.api.chemical.Chemical;
import mekanism.api.datagen.recipe.builder.CombinerRecipeBuilder;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.RegistryUtils;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
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
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "combining/";
        addCombinerDyeRecipes(consumer, basePath + "dye/");
        addCombinerGlowRecipes(consumer, basePath + "glow/");
        addCombinerWaxingRecipes(consumer, basePath + "wax/");
        //Gravel
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(this.items, ItemIds.FLINT),
              IngredientCreatorAccess.item().from(this.items, Tags.Items.COBBLESTONES_NORMAL),
              template(BlockItemIds.GRAVEL)
        ).save(consumer, Mekanism.rl(basePath + "gravel"));
        //Obsidian
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.DUSTS_OBSIDIAN, 4),
              IngredientCreatorAccess.item().from(this.items, Tags.Items.COBBLESTONES_DEEPSLATE),
              template(BlockItemIds.OBSIDIAN)
        ).save(consumer, Mekanism.rl(basePath + "obsidian"));
        //Rooted Dirt
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.HANGING_ROOTS, 3),
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.DIRT),
              template(BlockItemIds.ROOTED_DIRT)
        ).save(consumer, Mekanism.rl(basePath + "rooted_dirt"));
        //Packed mud
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.CROPS_WHEAT),
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.MUD),
              template(BlockItemIds.PACKED_MUD)
        ).save(consumer, Mekanism.rl(basePath + "packed_mud"));
        //Muddy mangrove roots
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.MANGROVE_ROOTS),
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.MUD),
              template(BlockItemIds.MUDDY_MANGROVE_ROOTS)
        ).save(consumer, Mekanism.rl(basePath + "muddy_mangrove_roots"));
    }

    private void addCombinerDyeRecipes(RecipeOutput consumer, String basePath) {
        //Black + white -> light gray
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.DYES_BLACK),
              IngredientCreatorAccess.item().from(this.items, Tags.Items.DYES_WHITE, 2),
              template(ItemIds.DYE.lightGray(), 6)
        ).save(consumer, Mekanism.rl(basePath + "black_to_light_gray"));
        //Blue + green -> cyan
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.DYES_BLUE),
              IngredientCreatorAccess.item().from(this.items, Tags.Items.DYES_GREEN),
              template(ItemIds.DYE.cyan(), 4)
        ).save(consumer, Mekanism.rl(basePath + "cyan"));
        //Gray + white -> light gray
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.DYES_GRAY),
              IngredientCreatorAccess.item().from(this.items, Tags.Items.DYES_WHITE),
              template(ItemIds.DYE.lightGray(), 4)
        ).save(consumer, Mekanism.rl(basePath + "gray_to_light_gray"));
        //Blue + white -> light blue
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.DYES_BLUE),
              IngredientCreatorAccess.item().from(this.items, Tags.Items.DYES_WHITE),
              template(ItemIds.DYE.lightBlue(), 4)
        ).save(consumer, Mekanism.rl(basePath + "light_blue"));
        //Green + white -> lime
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.DYES_GREEN),
              IngredientCreatorAccess.item().from(this.items, Tags.Items.DYES_WHITE),
              template(ItemIds.DYE.lime(), 4)
        ).save(consumer, Mekanism.rl(basePath + "lime"));
        //Purple + pink -> magenta
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.DYES_PURPLE),
              IngredientCreatorAccess.item().from(this.items, Tags.Items.DYES_PINK),
              template(ItemIds.DYE.magenta(), 4)
        ).save(consumer, Mekanism.rl(basePath + "magenta"));
        //Red + yellow -> orange
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.DYES_RED),
              IngredientCreatorAccess.item().from(this.items, Tags.Items.DYES_YELLOW),
              template(ItemIds.DYE.orange(), 4)
        ).save(consumer, Mekanism.rl(basePath + "orange"));
        //Red + white -> pink
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.DYES_RED),
              IngredientCreatorAccess.item().from(this.items, Tags.Items.DYES_WHITE),
              template(ItemIds.DYE.pink(), 4)
        ).save(consumer, Mekanism.rl(basePath + "pink"));
        //Blue + red -> purple
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.DYES_BLUE),
              IngredientCreatorAccess.item().from(this.items, Tags.Items.DYES_RED),
              template(ItemIds.DYE.purple(), 4)
        ).save(consumer, Mekanism.rl(basePath + "purple"));
    }

    private void addCombinerGlowRecipes(RecipeOutput consumer, String basePath) {
        ItemStackIngredient glow = IngredientCreatorAccess.item().from(this.items, Tags.Items.DUSTS_GLOWSTONE);
        //Sweet Berries -> Glow Berries
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.SWEET_BERRY_CROP),
              glow,
              template(BlockItemIds.GLOW_BERRY_CROP)
        ).save(consumer, Mekanism.rl(basePath + "berries"));
        //Ink Sac -> Glow Ink Sac
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(this.items, ItemIds.INK_SAC),
              glow,
              template(ItemIds.GLOW_INK_SAC)
        ).save(consumer, Mekanism.rl(basePath + "ink_sac"));
        //Item Frame -> Glow Item Frame
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(this.items, ItemIds.ITEM_FRAME),
              glow,
              template(ItemIds.GLOW_ITEM_FRAME)
        ).save(consumer, Mekanism.rl(basePath + "item_frame"));
    }

    private void addCombinerWaxingRecipes(RecipeOutput consumer, String basePath) {
        //Generate baseline recipes from waxing recipe set
        ItemStackIngredient wax = IngredientCreatorAccess.item().from(this.items, ItemIds.HONEYCOMB);
        for (Map.Entry<Block, Block> entry : HoneycombItem.WAXABLES.get().entrySet()) {
            Block result = entry.getValue();
            CombinerRecipeBuilder.combining(
                  IngredientCreatorAccess.item().from(entry.getKey()),
                  wax,
                  new ItemStackTemplate(result.asItem())
            ).save(consumer, Mekanism.rl(basePath + RegistryUtils.getPath(result)));
        }
    }
}
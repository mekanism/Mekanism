package mekanism.common.recipe.generation;

import mekanism.tools.common.ToolsItems;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/**
 * Created by Tom Elfring on 24-6-2017.
 */
public class MekanismToolsRecipes {

    public static void generate() {
        RecipeGenerator recipeGenerator = new RecipeGenerator("mekanismtools");

        //Crafting Recipes
        //Base
        recipeGenerator
              .addShapedRecipe(new ItemStack(ToolsItems.WoodPaxel, 1), "XYZ", " T ", " T ", 'X', Items.WOODEN_AXE, 'Y',
                    Items.WOODEN_PICKAXE, 'Z', Items.WOODEN_SHOVEL, 'T', Items.STICK);
        recipeGenerator
              .addShapedRecipe(new ItemStack(ToolsItems.StonePaxel, 1), "XYZ", " T ", " T ", 'X', Items.STONE_AXE, 'Y',
                    Items.STONE_PICKAXE, 'Z', Items.STONE_SHOVEL, 'T', Items.STICK);
        recipeGenerator
              .addShapedRecipe(new ItemStack(ToolsItems.IronPaxel, 1), "XYZ", " T ", " T ", 'X', Items.IRON_AXE, 'Y',
                    Items.IRON_PICKAXE, 'Z', Items.IRON_SHOVEL, 'T', Items.STICK);
        recipeGenerator
              .addShapedRecipe(new ItemStack(ToolsItems.DiamondPaxel, 1), "XYZ", " T ", " T ", 'X', Items.DIAMOND_AXE,
                    'Y', Items.DIAMOND_PICKAXE, 'Z', Items.DIAMOND_SHOVEL, 'T', Items.STICK);
        recipeGenerator
              .addShapedRecipe(new ItemStack(ToolsItems.GoldPaxel, 1), "XYZ", " T ", " T ", 'X', Items.GOLDEN_AXE, 'Y',
                    Items.GOLDEN_PICKAXE, 'Z', Items.GOLDEN_SHOVEL, 'T', Items.STICK);

        //Obsidian
        recipeGenerator
              .addShapedRecipe(new ItemStack(ToolsItems.ObsidianHelmet, 1), "***", "* *", '*', "ingotRefinedObsidian");
        recipeGenerator.addShapedRecipe(new ItemStack(ToolsItems.ObsidianChestplate, 1), "* *", "***", "***", '*',
              "ingotRefinedObsidian");
        recipeGenerator.addShapedRecipe(new ItemStack(ToolsItems.ObsidianLeggings, 1), "***", "* *", "* *", '*',
              "ingotRefinedObsidian");
        recipeGenerator
              .addShapedRecipe(new ItemStack(ToolsItems.ObsidianBoots, 1), "* *", "* *", '*', "ingotRefinedObsidian");
        recipeGenerator.addShapedRecipe(new ItemStack(ToolsItems.ObsidianPaxel, 1), "XYZ", " T ", " T ", 'X',
              ToolsItems.ObsidianAxe, 'Y', ToolsItems.ObsidianPickaxe, 'Z', ToolsItems.ObsidianShovel, 'T',
              Items.STICK);
        recipeGenerator.addShapedRecipe(new ItemStack(ToolsItems.ObsidianPickaxe, 1), "XXX", " T ", " T ", 'X',
              "ingotRefinedObsidian", 'T', Items.STICK);
        recipeGenerator
              .addShapedRecipe(new ItemStack(ToolsItems.ObsidianAxe, 1), "XX", "XT", " T", 'X', "ingotRefinedObsidian",
                    'T', Items.STICK);
        recipeGenerator
              .addShapedRecipe(new ItemStack(ToolsItems.ObsidianShovel, 1), "X", "T", "T", 'X', "ingotRefinedObsidian",
                    'T', Items.STICK);
        recipeGenerator
              .addShapedRecipe(new ItemStack(ToolsItems.ObsidianHoe, 1), "XX", " T", " T", 'X', "ingotRefinedObsidian",
                    'T', Items.STICK);
        recipeGenerator
              .addShapedRecipe(new ItemStack(ToolsItems.ObsidianSword, 1), "X", "X", "T", 'X', "ingotRefinedObsidian",
                    'T', Items.STICK);

        //Glowstone
        recipeGenerator.addShapedRecipe(new ItemStack(ToolsItems.GlowstonePaxel, 1), "XYZ", " T ", " T ", 'X',
              ToolsItems.GlowstoneAxe, 'Y', ToolsItems.GlowstonePickaxe, 'Z', ToolsItems.GlowstoneShovel, 'T',
              Items.STICK);
        recipeGenerator.addShapedRecipe(new ItemStack(ToolsItems.GlowstonePickaxe, 1), "XXX", " T ", " T ", 'X',
              "ingotRefinedGlowstone", 'T', Items.STICK);
        recipeGenerator.addShapedRecipe(new ItemStack(ToolsItems.GlowstoneAxe, 1), "XX", "XT", " T", 'X',
              "ingotRefinedGlowstone", 'T', Items.STICK);
        recipeGenerator.addShapedRecipe(new ItemStack(ToolsItems.GlowstoneShovel, 1), "X", "T", "T", 'X',
              "ingotRefinedGlowstone", 'T', Items.STICK);
        recipeGenerator.addShapedRecipe(new ItemStack(ToolsItems.GlowstoneHoe, 1), "XX", " T", " T", 'X',
              "ingotRefinedGlowstone", 'T', Items.STICK);
        recipeGenerator
              .addShapedRecipe(new ItemStack(ToolsItems.GlowstoneSword, 1), "X", "X", "T", 'X', "ingotRefinedGlowstone",
                    'T', Items.STICK);
        recipeGenerator.addShapedRecipe(new ItemStack(ToolsItems.GlowstoneHelmet, 1), "***", "* *", '*',
              "ingotRefinedGlowstone");
        recipeGenerator.addShapedRecipe(new ItemStack(ToolsItems.GlowstoneChestplate, 1), "* *", "***", "***", '*',
              "ingotRefinedGlowstone");
        recipeGenerator.addShapedRecipe(new ItemStack(ToolsItems.GlowstoneLeggings, 1), "***", "* *", "* *", '*',
              "ingotRefinedGlowstone");
        recipeGenerator
              .addShapedRecipe(new ItemStack(ToolsItems.GlowstoneBoots, 1), "* *", "* *", '*', "ingotRefinedGlowstone");

        //Lazuli
        recipeGenerator.addShapedRecipe(new ItemStack(ToolsItems.LazuliHelmet, 1), "***", "* *", '*',
              new ItemStack(Items.DYE, 1, 4));
        recipeGenerator.addShapedRecipe(new ItemStack(ToolsItems.LazuliChestplate, 1), "* *", "***", "***", '*',
              new ItemStack(Items.DYE, 1, 4));
        recipeGenerator.addShapedRecipe(new ItemStack(ToolsItems.LazuliLeggings, 1), "***", "* *", "* *", '*',
              new ItemStack(Items.DYE, 1, 4));
        recipeGenerator.addShapedRecipe(new ItemStack(ToolsItems.LazuliBoots, 1), "* *", "* *", '*',
              new ItemStack(Items.DYE, 1, 4));
        recipeGenerator
              .addShapedRecipe(new ItemStack(ToolsItems.LazuliPaxel, 1), "XYZ", " T ", " T ", 'X', ToolsItems.LazuliAxe,
                    'Y', ToolsItems.LazuliPickaxe, 'Z', ToolsItems.LazuliShovel, 'T', Items.STICK);
        recipeGenerator.addShapedRecipe(new ItemStack(ToolsItems.LazuliPickaxe, 1), "XXX", " T ", " T ", 'X',
              new ItemStack(Items.DYE, 1, 4), 'T', Items.STICK);
        recipeGenerator.addShapedRecipe(new ItemStack(ToolsItems.LazuliAxe, 1), "XX", "XT", " T", 'X',
              new ItemStack(Items.DYE, 1, 4), 'T', Items.STICK);
        recipeGenerator.addShapedRecipe(new ItemStack(ToolsItems.LazuliShovel, 1), "X", "T", "T", 'X',
              new ItemStack(Items.DYE, 1, 4), 'T', Items.STICK);
        recipeGenerator.addShapedRecipe(new ItemStack(ToolsItems.LazuliHoe, 1), "XX", " T", " T", 'X',
              new ItemStack(Items.DYE, 1, 4), 'T', Items.STICK);
        recipeGenerator.addShapedRecipe(new ItemStack(ToolsItems.LazuliSword, 1), "X", "X", "T", 'X',
              new ItemStack(Items.DYE, 1, 4), 'T', Items.STICK);

        //Osmium
        recipeGenerator
              .addShapedRecipe(new ItemStack(ToolsItems.OsmiumPaxel, 1), "XYZ", " T ", " T ", 'X', ToolsItems.OsmiumAxe,
                    'Y', ToolsItems.OsmiumPickaxe, 'Z', ToolsItems.OsmiumShovel, 'T', Items.STICK);
        recipeGenerator
              .addShapedRecipe(new ItemStack(ToolsItems.OsmiumPickaxe, 1), "XXX", " T ", " T ", 'X', "ingotOsmium", 'T',
                    Items.STICK);
        recipeGenerator
              .addShapedRecipe(new ItemStack(ToolsItems.OsmiumAxe, 1), "XX", "XT", " T", 'X', "ingotOsmium", 'T',
                    Items.STICK);
        recipeGenerator
              .addShapedRecipe(new ItemStack(ToolsItems.OsmiumShovel, 1), "X", "T", "T", 'X', "ingotOsmium", 'T',
                    Items.STICK);
        recipeGenerator
              .addShapedRecipe(new ItemStack(ToolsItems.OsmiumHoe, 1), "XX", " T", " T", 'X', "ingotOsmium", 'T',
                    Items.STICK);
        recipeGenerator
              .addShapedRecipe(new ItemStack(ToolsItems.OsmiumSword, 1), "X", "X", "T", 'X', "ingotOsmium", 'T',
                    Items.STICK);
        recipeGenerator.addShapedRecipe(new ItemStack(ToolsItems.OsmiumHelmet, 1), "***", "* *", '*', "ingotOsmium");
        recipeGenerator
              .addShapedRecipe(new ItemStack(ToolsItems.OsmiumChestplate, 1), "* *", "***", "***", '*', "ingotOsmium");
        recipeGenerator
              .addShapedRecipe(new ItemStack(ToolsItems.OsmiumLeggings, 1), "***", "* *", "* *", '*', "ingotOsmium");
        recipeGenerator.addShapedRecipe(new ItemStack(ToolsItems.OsmiumBoots, 1), "* *", "* *", '*', "ingotOsmium");

        //Bronze
        recipeGenerator
              .addShapedRecipe(new ItemStack(ToolsItems.BronzePaxel, 1), "XYZ", " T ", " T ", 'X', ToolsItems.BronzeAxe,
                    'Y', ToolsItems.BronzePickaxe, 'Z', ToolsItems.BronzeShovel, 'T', Items.STICK);
        recipeGenerator
              .addShapedRecipe(new ItemStack(ToolsItems.BronzePickaxe, 1), "XXX", " T ", " T ", 'X', "ingotBronze", 'T',
                    Items.STICK);
        recipeGenerator
              .addShapedRecipe(new ItemStack(ToolsItems.BronzeAxe, 1), "XX", "XT", " T", 'X', "ingotBronze", 'T',
                    Items.STICK);
        recipeGenerator
              .addShapedRecipe(new ItemStack(ToolsItems.BronzeShovel, 1), "X", "T", "T", 'X', "ingotBronze", 'T',
                    Items.STICK);
        recipeGenerator
              .addShapedRecipe(new ItemStack(ToolsItems.BronzeHoe, 1), "XX", " T", " T", 'X', "ingotBronze", 'T',
                    Items.STICK);
        recipeGenerator
              .addShapedRecipe(new ItemStack(ToolsItems.BronzeSword, 1), "X", "X", "T", 'X', "ingotBronze", 'T',
                    Items.STICK);
        recipeGenerator.addShapedRecipe(new ItemStack(ToolsItems.BronzeHelmet, 1), "***", "* *", '*', "ingotBronze");
        recipeGenerator
              .addShapedRecipe(new ItemStack(ToolsItems.BronzeChestplate, 1), "* *", "***", "***", '*', "ingotBronze");
        recipeGenerator
              .addShapedRecipe(new ItemStack(ToolsItems.BronzeLeggings, 1), "***", "* *", "* *", '*', "ingotBronze");
        recipeGenerator.addShapedRecipe(new ItemStack(ToolsItems.BronzeBoots, 1), "* *", "* *", '*', "ingotBronze");

        //Steel
        recipeGenerator
              .addShapedRecipe(new ItemStack(ToolsItems.SteelPaxel, 1), "XYZ", " I ", " I ", 'X', ToolsItems.SteelAxe,
                    'Y', ToolsItems.SteelPickaxe, 'Z', ToolsItems.SteelShovel, 'I', Items.IRON_INGOT);
        recipeGenerator
              .addShapedRecipe(new ItemStack(ToolsItems.SteelPickaxe, 1), "XXX", " I ", " I ", 'X', "ingotSteel", 'I',
                    Items.IRON_INGOT);
        recipeGenerator.addShapedRecipe(new ItemStack(ToolsItems.SteelAxe, 1), "XX", "XI", " I", 'X', "ingotSteel", 'I',
              Items.IRON_INGOT);
        recipeGenerator.addShapedRecipe(new ItemStack(ToolsItems.SteelShovel, 1), "X", "I", "I", 'X', "ingotSteel", 'I',
              Items.IRON_INGOT);
        recipeGenerator.addShapedRecipe(new ItemStack(ToolsItems.SteelHoe, 1), "XX", " I", " I", 'X', "ingotSteel", 'I',
              Items.IRON_INGOT);
        recipeGenerator.addShapedRecipe(new ItemStack(ToolsItems.SteelSword, 1), "X", "X", "I", 'X', "ingotSteel", 'I',
              Items.IRON_INGOT);
        recipeGenerator.addShapedRecipe(new ItemStack(ToolsItems.SteelHelmet, 1), "***", "I I", '*', "ingotSteel", 'I',
              Items.IRON_INGOT);
        recipeGenerator
              .addShapedRecipe(new ItemStack(ToolsItems.SteelChestplate, 1), "I I", "*I*", "***", '*', "ingotSteel",
                    'I', Items.IRON_INGOT);
        recipeGenerator
              .addShapedRecipe(new ItemStack(ToolsItems.SteelLeggings, 1), "I*I", "* *", "* *", '*', "ingotSteel", 'I',
                    Items.IRON_INGOT);
        recipeGenerator.addShapedRecipe(new ItemStack(ToolsItems.SteelBoots, 1), "I *", "* I", '*', "ingotSteel", 'I',
              Items.IRON_INGOT);
    }
}

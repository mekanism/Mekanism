package mekanism.additions.common.recipe;

import java.util.List;
import mekanism.additions.common.AdditionsTags;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.additions.common.registries.AdditionsItems;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.datagen.recipe.builder.ItemStackChemicalToItemStackRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.api.text.EnumColorCollection;
import mekanism.common.recipe.BaseRecipeProvider;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.common.recipe.builder.ExtendedShapelessRecipeBuilder;
import mekanism.common.recipe.impl.PigmentExtractingRecipeProvider;
import mekanism.common.recipe.pattern.Pattern;
import mekanism.common.recipe.pattern.RecipePattern;
import mekanism.common.recipe.pattern.RecipePattern.TripleLine;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tags.MekanismTags;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.references.BlockItemIds;
import net.minecraft.references.ItemIds;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.Tags;

public class AdditionsRecipeProvider extends BaseRecipeProvider {

    static final char TNT_CHAR = 'T';
    static final char OBSIDIAN_CHAR = 'O';
    static final char GLASS_PANES_CHAR = 'P';
    static final char PLASTIC_SHEET_CHAR = 'H';
    static final char PLASTIC_ROD_CHAR = 'R';
    static final char SAND_CHAR = 'S';
    static final char SLIME_CHAR = 'S';


    private static final RecipePattern GLOW_PANEL = RecipePattern.createPattern(
          TripleLine.of(GLASS_PANES_CHAR, PLASTIC_SHEET_CHAR, GLASS_PANES_CHAR),
          TripleLine.of(PLASTIC_SHEET_CHAR, Pattern.DYE, PLASTIC_SHEET_CHAR),
          TripleLine.of(Pattern.GLOWSTONE, PLASTIC_SHEET_CHAR, Pattern.GLOWSTONE));

    public AdditionsRecipeProvider(BootstrapContext<Recipe<?>> recipeOutput, BootstrapContext<Advancement> advancementOutput) {
        super(recipeOutput, advancementOutput);
    }

    @Override
    protected void addRecipes() {
        ExtendedShapedRecipeBuilder.shaped(AdditionsItems.WALKIE_TALKIE)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.EMPTY, Pattern.OSMIUM),
                    TripleLine.of(Pattern.STEEL, Pattern.CIRCUIT, Pattern.STEEL),
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY))
              ).define(Pattern.OSMIUM, osmiumIngot(this.items))
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .define(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .unlockedBy("has_circuit", has(MekanismTags.Items.CIRCUITS_BASIC))
              .save(output);
        ExtendedShapedRecipeBuilder.shaped(AdditionsBlocks.OBSIDIAN_TNT)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(OBSIDIAN_CHAR, OBSIDIAN_CHAR, OBSIDIAN_CHAR),
                    TripleLine.of(TNT_CHAR, TNT_CHAR, TNT_CHAR),
                    TripleLine.of(OBSIDIAN_CHAR, OBSIDIAN_CHAR, OBSIDIAN_CHAR))
              ).define(OBSIDIAN_CHAR, this.items, Tags.Items.OBSIDIANS_NORMAL)
              .define(TNT_CHAR, this.items, BlockItemIds.TNT)
              .category(RecipeCategory.REDSTONE)
              .unlockedBy("has_gunpowder", has(ItemIds.GUNPOWDER))
              .unlockedBy("has_tnt", has(BlockItemIds.TNT))
              .save(output);
        registerBalloons();
        registerGlowPanels();
    }

    @Override
    protected List<ISubRecipeProvider> getSubRecipeProviders() {
        return List.of(
              new PigmentExtractingPlasticRecipeProvider(this.items, this.fluids, this.chemicals),
              new PlasticBlockRecipeProvider(this.items, this.fluids, this.chemicals),
              new PlasticFencesRecipeProvider(this.items, this.fluids, this.chemicals),
              new PlasticSlabsRecipeProvider(this.items, this.fluids, this.chemicals),
              new PlasticStairsRecipeProvider(this.items, this.fluids, this.chemicals)
        );
    }

    private void registerBalloons() {
        final String basePath = "balloon/";
        HolderSet<Item> allBalloons = this.items.getOrThrow(AdditionsTags.Items.BALLOONS);
        EnumColorCollection.zipApply(EnumColorCollection.VALUES, AdditionsItems.BALLOONS, (color, balloon) -> {
            String colorString = color.getRegistryPrefix();
            Ingredient recolorInput = difference(allBalloons, balloon);
            DyeColor dye = color.getDyeColor();
            if (dye != null) {
                ExtendedShapelessRecipeBuilder.shapeless(balloon, 2)
                      .requires(this.items, Tags.Items.LEATHERS)
                      .requires(this.items, Tags.Items.STRINGS)
                      .requires(this.items, dye.getTag())
                      .category(RecipeCategory.DECORATIONS)
                      .group(MekanismAdditions.rl("balloon"))
                      .unlockedBy("has_leather", has(Tags.Items.LEATHERS))
                      .save(output, MekanismAdditions.rl(basePath + colorString));
                ExtendedShapelessRecipeBuilder.shapeless(balloon)
                      .requires(recolorInput)
                      .requires(this.items, dye.getTag())
                      .category(RecipeCategory.DECORATIONS)
                      .group(MekanismAdditions.rl("balloon_recolor"))
                      .unlockedBy("has_needed_dye", has(dye.getTag()))
                      .save(output, MekanismAdditions.rl(basePath + "recolor/" + colorString));
            }
            ItemStackChemicalToItemStackRecipeBuilder.painting(
                  IngredientCreatorAccess.item().from(recolorInput),
                  IngredientCreatorAccess.chemicalStack().from(chemicals, ChemicalIds.SIMPLE_PIGMENTS.pick(color), PigmentExtractingRecipeProvider.DYE_RATE),
                  false,
                  new ItemStackTemplate(balloon)
            ).save(output, MekanismAdditions.rl(basePath + "recolor/painting/" + colorString));
        });
    }

    private void registerGlowPanels() {
        final String basePath = "glow_panel/";
        HolderSet<Item> glowPanelTag = this.items.getOrThrow(AdditionsTags.BlockItems.GLOW_PANELS.item());
        EnumColorCollection.zipApply(EnumColorCollection.VALUES, AdditionsBlocks.GLOW_PANELS, (color, block) -> {
            Holder<Item> glowPanel = block.getItemHolder();
            DyeColor dye = color.getDyeColor();
            if (dye != null) {
                ExtendedShapedRecipeBuilder.shaped(glowPanel, 2)
                      .pattern(GLOW_PANEL)
                      .define(PLASTIC_SHEET_CHAR, MekanismItems.HDPE_SHEET)
                      .define(GLASS_PANES_CHAR, this.items, Tags.Items.GLASS_PANES)
                      .define(Pattern.GLOWSTONE, this.items, Tags.Items.DUSTS_GLOWSTONE)
                      .define(Pattern.DYE, this.items, dye.getTag())
                      .category(RecipeCategory.BUILDING_BLOCKS)
                      .group(MekanismAdditions.rl("glow_panels"))
                      .unlockedBy("has_plastic", has(MekanismItems.HDPE_PELLET))
                      .unlockedBy("has_plastic_sheet", has(MekanismItems.HDPE_SHEET))
                      .unlockedBy("has_glowstone", has(Tags.Items.DUSTS_GLOWSTONE))
                      .save(output, MekanismAdditions.rl(basePath + color.getRegistryPrefix()));
            }
            PlasticBlockRecipeProvider.registerRecolor(output, this.items, this.chemicals, glowPanel, glowPanelTag, color, basePath, "glow_panels_recolor");
        });
    }
}
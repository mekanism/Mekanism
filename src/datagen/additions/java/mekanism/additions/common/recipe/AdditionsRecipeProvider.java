package mekanism.additions.common.recipe;

import java.util.List;
import java.util.Map;
import mekanism.additions.common.AdditionsTags;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.additions.common.registries.AdditionsItems;
import mekanism.api.datagen.recipe.builder.ItemStackChemicalToItemStackRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.api.text.EnumColor;
import mekanism.common.recipe.BaseRecipeProvider;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.common.recipe.builder.ExtendedShapelessRecipeBuilder;
import mekanism.common.recipe.impl.PigmentExtractingRecipeProvider;
import mekanism.common.recipe.pattern.Pattern;
import mekanism.common.recipe.pattern.RecipePattern;
import mekanism.common.recipe.pattern.RecipePattern.TripleLine;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.references.BlockItemIds;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
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

    public AdditionsRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(output, registries);
    }

    @Override
    protected void addRecipes(HolderLookup.Provider registries) {
        ExtendedShapedRecipeBuilder.shapedRecipe(AdditionsItems.WALKIE_TALKIE)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.EMPTY, Pattern.OSMIUM),
                    TripleLine.of(Pattern.STEEL, Pattern.CIRCUIT, Pattern.STEEL),
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY))
              ).key(Pattern.OSMIUM, osmiumIngot(this.items))
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .save(output);
        ExtendedShapedRecipeBuilder.shapedRecipe(AdditionsBlocks.OBSIDIAN_TNT)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(OBSIDIAN_CHAR, OBSIDIAN_CHAR, OBSIDIAN_CHAR),
                    TripleLine.of(TNT_CHAR, TNT_CHAR, TNT_CHAR),
                    TripleLine.of(OBSIDIAN_CHAR, OBSIDIAN_CHAR, OBSIDIAN_CHAR))
              ).key(OBSIDIAN_CHAR, this.items, Tags.Items.OBSIDIANS_NORMAL)
              .key(TNT_CHAR, this.items, BlockItemIds.TNT)
              .category(RecipeCategory.REDSTONE)
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
        for (Map.Entry<EnumColor, ? extends Holder<Item>> entry : AdditionsItems.BALLOONS.entrySet()) {
            EnumColor color = entry.getKey();
            Holder<Item> balloon = entry.getValue();
            String colorString = color.getRegistryPrefix();
            Ingredient recolorInput = difference(allBalloons, balloon);
            DyeColor dye = color.getDyeColor();
            if (dye != null) {
                ExtendedShapelessRecipeBuilder.shapelessRecipe(balloon, 2)
                      .addIngredient(this.items, Tags.Items.LEATHERS)
                      .addIngredient(this.items, Tags.Items.STRINGS)
                      .addIngredient(this.items, dye.getTag())
                      .category(RecipeCategory.DECORATIONS)
                      .save(output, MekanismAdditions.rl(basePath + colorString));
                ExtendedShapelessRecipeBuilder.shapelessRecipe(balloon)
                      .addIngredient(recolorInput)
                      .addIngredient(this.items, dye.getTag())
                      .category(RecipeCategory.DECORATIONS)
                      .save(output, MekanismAdditions.rl(basePath + "recolor/" + colorString));
            }
            ItemStackChemicalToItemStackRecipeBuilder.painting(
                  IngredientCreatorAccess.item().from(recolorInput),
                  IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.PIGMENT_COLOR_LOOKUP.get(color), PigmentExtractingRecipeProvider.DYE_RATE),
                  new ItemStackTemplate(balloon),
                  false
            ).save(output, MekanismAdditions.rl(basePath + "recolor/painting/" + colorString));
        }
    }

    private void registerGlowPanels() {
        final String basePath = "glow_panel/";
        HolderSet<Item> glowPanelTag = this.items.getOrThrow(AdditionsTags.Items.GLOW_PANELS);
        for (Map.Entry<EnumColor, ? extends BlockRegistryObject<?, ?>> entry : AdditionsBlocks.GLOW_PANELS.entrySet()) {
            EnumColor color = entry.getKey();
            Holder<Item> glowPanel = entry.getValue().getItemHolder();
            DyeColor dye = color.getDyeColor();
            if (dye != null) {
                ExtendedShapedRecipeBuilder.shapedRecipe(glowPanel, 2)
                      .pattern(GLOW_PANEL)
                      .key(PLASTIC_SHEET_CHAR, MekanismItems.HDPE_SHEET)
                      .key(GLASS_PANES_CHAR, this.items, Tags.Items.GLASS_PANES)
                      .key(Pattern.GLOWSTONE, this.items, Tags.Items.DUSTS_GLOWSTONE)
                      .key(Pattern.DYE, this.items, dye.getTag())
                      .category(RecipeCategory.BUILDING_BLOCKS)
                      .save(output, MekanismAdditions.rl(basePath + color.getRegistryPrefix()));
            }
            PlasticBlockRecipeProvider.registerRecolor(output, this.items, glowPanel, glowPanelTag, color, basePath);
        }
    }
}
package mekanism.additions.common.recipe;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import mekanism.additions.common.AdditionsTags;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.additions.common.registries.AdditionsItems;
import mekanism.api.annotations.NothingNullByDefault;
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
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

@NothingNullByDefault
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

    public AdditionsRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, ExistingFileHelper existingFileHelper) {
        super(output, provider, existingFileHelper);
    }

    @Override
    protected void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        ExtendedShapedRecipeBuilder.shapedRecipe(AdditionsItems.WALKIE_TALKIE)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.EMPTY, Pattern.OSMIUM),
                    TripleLine.of(Pattern.STEEL, Pattern.CIRCUIT, Pattern.STEEL),
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY))
              ).key(Pattern.OSMIUM, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM))
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .build(consumer);
        ExtendedShapedRecipeBuilder.shapedRecipe(AdditionsBlocks.OBSIDIAN_TNT)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(OBSIDIAN_CHAR, OBSIDIAN_CHAR, OBSIDIAN_CHAR),
                    TripleLine.of(TNT_CHAR, TNT_CHAR, TNT_CHAR),
                    TripleLine.of(OBSIDIAN_CHAR, OBSIDIAN_CHAR, OBSIDIAN_CHAR))
              ).key(OBSIDIAN_CHAR, Tags.Items.OBSIDIANS_NORMAL)
              .key(TNT_CHAR, Items.TNT)
              .category(RecipeCategory.REDSTONE)
              .build(consumer);
        registerBalloons(consumer);
        registerGlowPanels(consumer);
    }

    @Override
    protected List<ISubRecipeProvider> getSubRecipeProviders() {
        return List.of(
              new PigmentExtractingPlasticRecipeProvider(),
              new PlasticBlockRecipeProvider(),
              new PlasticFencesRecipeProvider(),
              new PlasticSlabsRecipeProvider(),
              new PlasticStairsRecipeProvider()
        );
    }

    private void registerBalloons(RecipeOutput consumer) {
        final String basePath = "balloon/";
        for (Map.Entry<EnumColor, ? extends Holder<Item>> entry : AdditionsItems.BALLOONS.entrySet()) {
            EnumColor color = entry.getKey();
            Holder<Item> balloon = entry.getValue();
            String colorString = color.getRegistryPrefix();
            Ingredient recolorInput = difference(AdditionsTags.Items.BALLOONS, balloon);
            DyeColor dye = color.getDyeColor();
            if (dye != null) {
                ExtendedShapelessRecipeBuilder.shapelessRecipe(balloon, 2)
                      .addIngredient(Tags.Items.LEATHERS)
                      .addIngredient(Tags.Items.STRINGS)
                      .addIngredient(dye.getTag())
                      .category(RecipeCategory.DECORATIONS)
                      .build(consumer, MekanismAdditions.rl(basePath + colorString));
                ExtendedShapelessRecipeBuilder.shapelessRecipe(balloon)
                      .addIngredient(recolorInput)
                      .addIngredient(dye.getTag())
                      .category(RecipeCategory.DECORATIONS)
                      .build(consumer, MekanismAdditions.rl(basePath + "recolor/" + colorString));
            }
            ItemStackChemicalToItemStackRecipeBuilder.painting(
                  IngredientCreatorAccess.item().from(recolorInput),
                  IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.PIGMENT_COLOR_LOOKUP.get(color), PigmentExtractingRecipeProvider.DYE_RATE),
                  new ItemStack(balloon),
                  false
            ).build(consumer, MekanismAdditions.rl(basePath + "recolor/painting/" + colorString));
        }
    }

    private void registerGlowPanels(RecipeOutput consumer) {
        final String basePath = "glow_panel/";
        for (Map.Entry<EnumColor, ? extends BlockRegistryObject<?, ?>> entry : AdditionsBlocks.GLOW_PANELS.entrySet()) {
            EnumColor color = entry.getKey();
            Holder<Item> glowPanel = entry.getValue().getItemHolder();
            DyeColor dye = color.getDyeColor();
            if (dye != null) {
                ExtendedShapedRecipeBuilder.shapedRecipe(glowPanel, 2)
                      .pattern(GLOW_PANEL)
                      .key(PLASTIC_SHEET_CHAR, MekanismItems.HDPE_SHEET)
                      .key(GLASS_PANES_CHAR, Tags.Items.GLASS_PANES)
                      .key(Pattern.GLOWSTONE, Tags.Items.DUSTS_GLOWSTONE)
                      .key(Pattern.DYE, dye.getTag())
                      .category(RecipeCategory.BUILDING_BLOCKS)
                      .build(consumer, MekanismAdditions.rl(basePath + color.getRegistryPrefix()));
            }
            PlasticBlockRecipeProvider.registerRecolor(consumer, glowPanel, AdditionsTags.Items.GLOW_PANELS, color, basePath);
        }
    }
}
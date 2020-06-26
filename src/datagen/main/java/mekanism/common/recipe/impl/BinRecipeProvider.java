package mekanism.common.recipe.impl;

import java.util.function.Consumer;
import mekanism.api.providers.IItemProvider;
import mekanism.common.Mekanism;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.basic.BlockBin;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.common.recipe.builder.MekDataShapedRecipeBuilder;
import mekanism.common.recipe.builder.SpecialRecipeBuilder;
import mekanism.common.recipe.pattern.Pattern;
import mekanism.common.recipe.pattern.RecipePattern;
import mekanism.common.recipe.pattern.RecipePattern.TripleLine;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import mekanism.common.tags.MekanismTags;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraftforge.common.Tags;

class BinRecipeProvider implements ISubRecipeProvider {

    private static final RecipePattern BIN_PATTERN = RecipePattern.createPattern(
          TripleLine.of(Pattern.COBBLESTONE, Pattern.CIRCUIT, Pattern.COBBLESTONE),
          TripleLine.of(Pattern.ALLOY, Pattern.PREVIOUS, Pattern.ALLOY),
          TripleLine.of(Pattern.COBBLESTONE, Pattern.COBBLESTONE, Pattern.COBBLESTONE));

    @Override
    public void addRecipes(Consumer<IFinishedRecipe> consumer) {
        //Special recipes (bins)
        SpecialRecipeBuilder.build(consumer, MekanismRecipeSerializers.BIN_INSERT);
        SpecialRecipeBuilder.build(consumer, MekanismRecipeSerializers.BIN_EXTRACT);
        //Recipes for making bins
        String basePath = "bin/";
        //Note: For the basic bin, we have to handle the empty slot differently than batching it against our bin pattern
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.BASIC_BIN)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.COBBLESTONE, Pattern.CIRCUIT, Pattern.COBBLESTONE),
                    TripleLine.of(Pattern.ALLOY, Pattern.EMPTY, Pattern.ALLOY),
                    TripleLine.of(Pattern.COBBLESTONE, Pattern.COBBLESTONE, Pattern.COBBLESTONE))
              ).key(Pattern.COBBLESTONE, Tags.Items.COBBLESTONE)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_BASIC)
              .build(consumer, Mekanism.rl(basePath + "basic"));
        addTieredBin(consumer, basePath, MekanismBlocks.ADVANCED_BIN, MekanismBlocks.BASIC_BIN, MekanismTags.Items.CIRCUITS_ADVANCED, MekanismTags.Items.ALLOYS_INFUSED);
        addTieredBin(consumer, basePath, MekanismBlocks.ELITE_BIN, MekanismBlocks.ADVANCED_BIN, MekanismTags.Items.CIRCUITS_ELITE, MekanismTags.Items.ALLOYS_REINFORCED);
        addTieredBin(consumer, basePath, MekanismBlocks.ULTIMATE_BIN, MekanismBlocks.ELITE_BIN, MekanismTags.Items.CIRCUITS_ULTIMATE, MekanismTags.Items.ALLOYS_ATOMIC);
    }

    private void addTieredBin(Consumer<IFinishedRecipe> consumer, String basePath, BlockRegistryObject<BlockBin, ?> bin, IItemProvider previousBin, ITag<Item> circuitTag,
          ITag<Item> alloyTag) {
        String tierName = Attribute.getBaseTier(bin.getBlock()).getLowerName();
        MekDataShapedRecipeBuilder.shapedRecipe(bin)
              .pattern(BIN_PATTERN)
              .key(Pattern.PREVIOUS, previousBin)
              .key(Pattern.COBBLESTONE, Tags.Items.COBBLESTONE)
              .key(Pattern.CIRCUIT, circuitTag)
              .key(Pattern.ALLOY, alloyTag)
              .build(consumer, Mekanism.rl(basePath + tierName));
    }
}
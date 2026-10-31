package mekanism.common.recipe.impl;

import mekanism.api.MekanismAPITags;
import mekanism.api.chemical.Chemical;
import mekanism.api.datagen.recipe.builder.ItemStackChemicalToItemStackRecipeBuilder;
import mekanism.api.recipes.book.MetallurgicInfusingBookCategory;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.references.BlockItemIds;
import net.minecraft.references.ItemIds;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;

class MetallurgicInfuserRecipeProvider extends BaseSubRecipeProvider {

    MetallurgicInfuserRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput output) {
        String basePath = "metallurgic_infusing/";
        addMetallurgicInfuserAlloyRecipes(output, basePath + "alloy/");
        addMetallurgicInfuserMossyRecipes(output, basePath + "mossy/");
        addMiscBioRecipes(output, basePath);
        //Dirt -> mycelium
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.DIRT),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.FUNGI, 10),
              false,
              template(BlockItemIds.MYCELIUM)
        ).category(MetallurgicInfusingBookCategory.BUILDING)
              .unlockedBy(hasCriterion(BlockItemIds.DIRT))
              .save(output, basePath + "dirt_to_mycelium");
        //Netherrack -> crimson nylium
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, Tags.BlockItems.NETHERRACKS),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.FUNGI, 10),
              false,
              template(BlockItemIds.CRIMSON_NYLIUM)
        ).category(MetallurgicInfusingBookCategory.BUILDING)
              .unlockedBy("has_netherrack", has(Tags.BlockItems.NETHERRACKS))
              .save(output, basePath + "netherrack_to_crimson_nylium");
        //Crimson nylium -> warped nylium
        //Note: We use crimson as the base so that it is easy to "specify" which output is desired
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CRIMSON_NYLIUM),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.FUNGI, 10),
              false,
              template(BlockItemIds.WARPED_NYLIUM)
        ).category(MetallurgicInfusingBookCategory.BUILDING)
              .unlockedBy(hasCriterion(BlockItemIds.CRIMSON_NYLIUM))
              .save(output, basePath + "crimson_nylium_to_warped_nylium");
        //Blackstone -> Gilded Blackstone
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.BLACKSTONE),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.GOLD, 100),
              false,
              template(BlockItemIds.GILDED_BLACKSTONE)
        ).category(MetallurgicInfusingBookCategory.BUILDING)
              .unlockedBy(hasCriterion(BlockItemIds.BLACKSTONE))
              .save(output, basePath + "blackstone_to_gilded_blackstone");
    }

    private void addMetallurgicInfuserAlloyRecipes(RecipeOutput output, String basePath) {
        //Infused
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.INGOTS_COPPER),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.REDSTONE, 10),
              false,
              MekanismItems.INFUSED_ALLOY.asTemplate()
        ).unlockedBy("has_copper_ingot", has(Tags.Items.INGOTS_COPPER))
              .save(output, basePath + "infused");
        //Reinforced
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.ALLOYS_INFUSED),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.DIAMOND, 20),
              false,
              MekanismItems.REINFORCED_ALLOY.asTemplate()
        ).unlockedBy("has_alloy", has(MekanismTags.Items.ALLOYS_INFUSED))
              .save(output, basePath + "reinforced");
        //Atomic
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.ALLOYS_REINFORCED),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.REFINED_OBSIDIAN, 40),
              false,
              MekanismItems.ATOMIC_ALLOY.asTemplate()
        ).unlockedBy("has_alloy", has(MekanismTags.Items.ALLOYS_REINFORCED))
              .save(output, basePath + "atomic");
    }

    private void addMetallurgicInfuserMossyRecipes(RecipeOutput output, String basePath) {
        //Cobblestone
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, Tags.BlockItems.COBBLESTONES_NORMAL),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.BIO, 10),
              false,
              template(BlockItemIds.MOSSY_COBBLESTONE)
        ).category(MetallurgicInfusingBookCategory.BUILDING)
              .group(Mekanism.rl("mossy_cobble_addition"))
              .unlockedBy("has_cobblestone", has(Tags.BlockItems.COBBLESTONES_NORMAL))
              .save(output, basePath + "cobblestone");
        //Cobblestone slab
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.COBBLESTONE_SLAB),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.BIO, 10),
              false,
              template(BlockItemIds.MOSSY_COBBLESTONE_SLAB)
        ).category(MetallurgicInfusingBookCategory.BUILDING)
              .group(Mekanism.rl("mossy_cobble_addition"))
              .unlockedBy(hasCriterion(BlockItemIds.COBBLESTONE_SLAB))
              .save(output, basePath + "cobblestone_slab");
        //Cobblestone stairs
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.COBBLESTONE_STAIRS),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.BIO, 10),
              false,
              template(BlockItemIds.MOSSY_COBBLESTONE_STAIRS)
        ).category(MetallurgicInfusingBookCategory.BUILDING)
              .group(Mekanism.rl("mossy_cobble_addition"))
              .unlockedBy(hasCriterion(BlockItemIds.COBBLESTONE_STAIRS))
              .save(output, basePath + "cobblestone_stairs");
        //Cobblestone wall
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.COBBLESTONE_WALL),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.BIO, 10),
              false,
              template(BlockItemIds.MOSSY_COBBLESTONE_WALL)
        ).category(MetallurgicInfusingBookCategory.BUILDING)
              .group(Mekanism.rl("mossy_cobble_addition"))
              .unlockedBy(hasCriterion(BlockItemIds.COBBLESTONE_WALL))
              .save(output, basePath + "cobblestone_wall");

        //Stone brick
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.STONE_BRICKS),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.BIO, 10),
              false,
              template(BlockItemIds.MOSSY_STONE_BRICKS)
        ).category(MetallurgicInfusingBookCategory.BUILDING)
              .group(Mekanism.rl("mossy_stone_brick_addition"))
              .unlockedBy(hasCriterion(BlockItemIds.STONE_BRICKS))
              .save(output, basePath + "stone_brick");
        //Stone brick slab
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.STONE_BRICK_SLAB),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.BIO, 10),
              false,
              template(BlockItemIds.MOSSY_STONE_BRICK_SLAB)
        ).category(MetallurgicInfusingBookCategory.BUILDING)
              .group(Mekanism.rl("mossy_stone_brick_addition"))
              .unlockedBy(hasCriterion(BlockItemIds.STONE_BRICK_SLAB))
              .save(output, basePath + "stone_brick_slab");
        //Stone brick stairs
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.STONE_BRICK_STAIRS),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.BIO, 10),
              false,
              template(BlockItemIds.MOSSY_STONE_BRICK_STAIRS)
        ).category(MetallurgicInfusingBookCategory.BUILDING)
              .group(Mekanism.rl("mossy_stone_brick_addition"))
              .unlockedBy(hasCriterion(BlockItemIds.STONE_BRICK_STAIRS))
              .save(output, basePath + "stone_brick_stairs");
        //Stone brick wall
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.STONE_BRICK_WALL),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.BIO, 10),
              false,
              template(BlockItemIds.MOSSY_STONE_BRICK_WALL)
        ).category(MetallurgicInfusingBookCategory.BUILDING)
              .group(Mekanism.rl("mossy_stone_brick_addition"))
              .unlockedBy(hasCriterion(BlockItemIds.STONE_BRICK_WALL))
              .save(output, basePath + "stone_brick_wall");
    }

    private void addMiscBioRecipes(RecipeOutput output, String basePath) {
        //Dirt -> podzol
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.DIRT),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.BIO, 10),
              false,
              template(BlockItemIds.PODZOL)
        ).category(MetallurgicInfusingBookCategory.BUILDING)
              .unlockedBy(hasCriterion(BlockItemIds.DIRT))
              .save(output, basePath + "dirt_to_podzol");
        //Sand -> dirt
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, Tags.BlockItems.SANDS),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.BIO, 10),
              false,
              template(BlockItemIds.DIRT)
        ).category(MetallurgicInfusingBookCategory.BUILDING)
              .unlockedBy("has_sand", has(Tags.BlockItems.SANDS))
              .save(output, basePath + "sand_to_dirt");
        //slime ball
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, ItemIds.CLAY_BALL),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.BIO, 10),
              false,
              template(ItemIds.SLIME_BALL)
        ).group(Mekanism.rl("slime"))
              .unlockedBy("has_clay", has(ItemIds.CLAY_BALL))
              .save(output, basePath + "clay_to_slime_ball");
        //slime block
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CLAY),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.BIO, 40),
              false,
              template(ItemIds.SLIME_BALL, 4)
        ).group(Mekanism.rl("slime"))
              //Only require normal clay
              .unlockedBy("has_clay", has(ItemIds.CLAY_BALL))
              .save(output, basePath + "clay_to_slime_block");
    }
}
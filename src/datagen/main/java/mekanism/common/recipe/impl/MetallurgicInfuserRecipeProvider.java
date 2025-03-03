package mekanism.common.recipe.impl;

import mekanism.api.MekanismAPITags;
import mekanism.api.datagen.recipe.builder.ItemStackChemicalToItemStackRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

class MetallurgicInfuserRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "metallurgic_infusing/";
        addMetallurgicInfuserAlloyRecipes(consumer, basePath + "alloy/");
        addMetallurgicInfuserMossyRecipes(consumer, basePath + "mossy/");
        addMiscBioRecipes(consumer, basePath);
        //Dirt -> mycelium
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(Items.DIRT),
              IngredientCreatorAccess.chemicalStack().from(MekanismAPITags.Chemicals.FUNGI, 10),
              new ItemStack(Items.MYCELIUM),
              false
        ).build(consumer, Mekanism.rl(basePath + "dirt_to_mycelium"));
        //Netherrack -> crimson nylium
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(Items.NETHERRACK),
              IngredientCreatorAccess.chemicalStack().from(MekanismAPITags.Chemicals.FUNGI, 10),
              new ItemStack(Items.CRIMSON_NYLIUM),
              false
        ).build(consumer, Mekanism.rl(basePath + "netherrack_to_crimson_nylium"));
        //Crimson nylium -> warped nylium
        //Note: We use crimson as the base so that it is easy to "specify" which output is desired
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(Items.CRIMSON_NYLIUM),
              IngredientCreatorAccess.chemicalStack().from(MekanismAPITags.Chemicals.FUNGI, 10),
              new ItemStack(Items.WARPED_NYLIUM),
              false
        ).build(consumer, Mekanism.rl(basePath + "crimson_nylium_to_warped_nylium"));
        //Blackstone -> Gilded Blackstone
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(Items.BLACKSTONE),
              IngredientCreatorAccess.chemicalStack().from(MekanismAPITags.Chemicals.GOLD, 100),
              new ItemStack(Items.GILDED_BLACKSTONE),
              false
        ).build(consumer, Mekanism.rl(basePath + "blackstone_to_gilded_blackstone"));
    }

    private void addMetallurgicInfuserAlloyRecipes(RecipeOutput consumer, String basePath) {
        //Infused
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(Tags.Items.INGOTS_COPPER),
              IngredientCreatorAccess.chemicalStack().from(MekanismAPITags.Chemicals.REDSTONE, 10),
              MekanismItems.INFUSED_ALLOY.asStack(),
              false
        ).build(consumer, Mekanism.rl(basePath + "infused"));
        //Reinforced
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(MekanismTags.Items.ALLOYS_INFUSED),
              IngredientCreatorAccess.chemicalStack().from(MekanismAPITags.Chemicals.DIAMOND, 20),
              MekanismItems.REINFORCED_ALLOY.asStack(),
              false
        ).build(consumer, Mekanism.rl(basePath + "reinforced"));
        //Atomic
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(MekanismTags.Items.ALLOYS_REINFORCED),
              IngredientCreatorAccess.chemicalStack().from(MekanismAPITags.Chemicals.REFINED_OBSIDIAN, 40),
              MekanismItems.ATOMIC_ALLOY.asStack(),
              false
        ).build(consumer, Mekanism.rl(basePath + "atomic"));
    }

    private void addMetallurgicInfuserMossyRecipes(RecipeOutput consumer, String basePath) {
        //Cobblestone
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(Items.COBBLESTONE),
              IngredientCreatorAccess.chemicalStack().from(MekanismAPITags.Chemicals.BIO, 10),
              new ItemStack(Items.MOSSY_COBBLESTONE),
              false
        ).build(consumer, Mekanism.rl(basePath + "cobblestone"));
        //Cobblestone slab
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(Items.COBBLESTONE_SLAB),
              IngredientCreatorAccess.chemicalStack().from(MekanismAPITags.Chemicals.BIO, 10),
              new ItemStack(Items.MOSSY_COBBLESTONE_SLAB),
              false
        ).build(consumer, Mekanism.rl(basePath + "cobblestone_slab"));
        //Cobblestone stairs
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(Items.COBBLESTONE_STAIRS),
              IngredientCreatorAccess.chemicalStack().from(MekanismAPITags.Chemicals.BIO, 10),
              new ItemStack(Items.MOSSY_COBBLESTONE_STAIRS),
              false
        ).build(consumer, Mekanism.rl(basePath + "cobblestone_stairs"));
        //Cobblestone wall
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(Items.COBBLESTONE_WALL),
              IngredientCreatorAccess.chemicalStack().from(MekanismAPITags.Chemicals.BIO, 10),
              new ItemStack(Items.MOSSY_COBBLESTONE_WALL),
              false
        ).build(consumer, Mekanism.rl(basePath + "cobblestone_wall"));

        //Stone brick
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(Items.STONE_BRICKS),
              IngredientCreatorAccess.chemicalStack().from(MekanismAPITags.Chemicals.BIO, 10),
              new ItemStack(Items.MOSSY_STONE_BRICKS),
              false
        ).build(consumer, Mekanism.rl(basePath + "stone_brick"));
        //Stone brick slab
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(Items.STONE_BRICK_SLAB),
              IngredientCreatorAccess.chemicalStack().from(MekanismAPITags.Chemicals.BIO, 10),
              new ItemStack(Items.MOSSY_STONE_BRICK_SLAB),
              false
        ).build(consumer, Mekanism.rl(basePath + "stone_brick_slab"));
        //Stone brick stairs
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(Items.STONE_BRICK_STAIRS),
              IngredientCreatorAccess.chemicalStack().from(MekanismAPITags.Chemicals.BIO, 10),
              new ItemStack(Items.MOSSY_STONE_BRICK_STAIRS),
              false
        ).build(consumer, Mekanism.rl(basePath + "stone_brick_stairs"));
        //Stone brick wall
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(Items.STONE_BRICK_WALL),
              IngredientCreatorAccess.chemicalStack().from(MekanismAPITags.Chemicals.BIO, 10),
              new ItemStack(Items.MOSSY_STONE_BRICK_WALL),
              false
        ).build(consumer, Mekanism.rl(basePath + "stone_brick_wall"));
    }

    private void addMiscBioRecipes(RecipeOutput consumer, String basePath) {
        //Dirt -> podzol
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(Items.DIRT),
              IngredientCreatorAccess.chemicalStack().from(MekanismAPITags.Chemicals.BIO, 10),
              new ItemStack(Items.PODZOL),
              false
        ).build(consumer, Mekanism.rl(basePath + "dirt_to_podzol"));
        //Sand -> dirt
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(Tags.Items.SANDS),
              IngredientCreatorAccess.chemicalStack().from(MekanismAPITags.Chemicals.BIO, 10),
              new ItemStack(Items.DIRT),
              false
        ).build(consumer, Mekanism.rl(basePath + "sand_to_dirt"));
        //slime ball
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(Items.CLAY_BALL),
              IngredientCreatorAccess.chemicalStack().from(MekanismAPITags.Chemicals.BIO, 10),
              new ItemStack(Items.SLIME_BALL),
              false
        ).build(consumer, Mekanism.rl(basePath + "clay_to_slime_ball"));
        //slime block
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(Items.CLAY),
              IngredientCreatorAccess.chemicalStack().from(MekanismAPITags.Chemicals.BIO, 40),
              new ItemStack(Items.SLIME_BALL, 4),
              false
        ).build(consumer, Mekanism.rl(basePath + "clay_to_slime_block"));
    }
}
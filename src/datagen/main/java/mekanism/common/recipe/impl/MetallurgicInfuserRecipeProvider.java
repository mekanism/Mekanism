package mekanism.common.recipe.impl;

import mekanism.api.MekanismAPITags;
import mekanism.api.chemical.Chemical;
import mekanism.api.datagen.recipe.builder.ItemStackChemicalToItemStackRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
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
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "metallurgic_infusing/";
        addMetallurgicInfuserAlloyRecipes(consumer, basePath + "alloy/");
        addMetallurgicInfuserMossyRecipes(consumer, basePath + "mossy/");
        addMiscBioRecipes(consumer, basePath);
        //Dirt -> mycelium
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.DIRT),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.FUNGI, 10),
              template(BlockItemIds.MYCELIUM),
              false
        ).save(consumer, Mekanism.rl(basePath + "dirt_to_mycelium"));
        //Netherrack -> crimson nylium
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.NETHERRACK),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.FUNGI, 10),
              template(BlockItemIds.CRIMSON_NYLIUM),
              false
        ).save(consumer, Mekanism.rl(basePath + "netherrack_to_crimson_nylium"));
        //Crimson nylium -> warped nylium
        //Note: We use crimson as the base so that it is easy to "specify" which output is desired
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CRIMSON_NYLIUM),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.FUNGI, 10),
              template(BlockItemIds.WARPED_NYLIUM),
              false
        ).save(consumer, Mekanism.rl(basePath + "crimson_nylium_to_warped_nylium"));
        //Blackstone -> Gilded Blackstone
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.BLACKSTONE),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.GOLD, 100),
              template(BlockItemIds.GILDED_BLACKSTONE),
              false
        ).save(consumer, Mekanism.rl(basePath + "blackstone_to_gilded_blackstone"));
    }

    private void addMetallurgicInfuserAlloyRecipes(RecipeOutput consumer, String basePath) {
        //Infused
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.INGOTS_COPPER),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.REDSTONE, 10),
              MekanismItems.INFUSED_ALLOY.asTemplate(),
              false
        ).save(consumer, Mekanism.rl(basePath + "infused"));
        //Reinforced
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.ALLOYS_INFUSED),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.DIAMOND, 20),
              MekanismItems.REINFORCED_ALLOY.asTemplate(),
              false
        ).save(consumer, Mekanism.rl(basePath + "reinforced"));
        //Atomic
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.ALLOYS_REINFORCED),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.REFINED_OBSIDIAN, 40),
              MekanismItems.ATOMIC_ALLOY.asTemplate(),
              false
        ).save(consumer, Mekanism.rl(basePath + "atomic"));
    }

    private void addMetallurgicInfuserMossyRecipes(RecipeOutput consumer, String basePath) {
        //Cobblestone
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.COBBLESTONE),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.BIO, 10),
              template(BlockItemIds.MOSSY_COBBLESTONE),
              false
        ).save(consumer, Mekanism.rl(basePath + "cobblestone"));
        //Cobblestone slab
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.COBBLESTONE_SLAB),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.BIO, 10),
              template(BlockItemIds.MOSSY_COBBLESTONE_SLAB),
              false
        ).save(consumer, Mekanism.rl(basePath + "cobblestone_slab"));
        //Cobblestone stairs
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.COBBLESTONE_STAIRS),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.BIO, 10),
              template(BlockItemIds.MOSSY_COBBLESTONE_STAIRS),
              false
        ).save(consumer, Mekanism.rl(basePath + "cobblestone_stairs"));
        //Cobblestone wall
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.COBBLESTONE_WALL),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.BIO, 10),
              template(BlockItemIds.MOSSY_COBBLESTONE_WALL),
              false
        ).save(consumer, Mekanism.rl(basePath + "cobblestone_wall"));

        //Stone brick
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.STONE_BRICKS),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.BIO, 10),
              template(BlockItemIds.MOSSY_STONE_BRICKS),
              false
        ).save(consumer, Mekanism.rl(basePath + "stone_brick"));
        //Stone brick slab
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.STONE_BRICK_SLAB),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.BIO, 10),
              template(BlockItemIds.MOSSY_STONE_BRICK_SLAB),
              false
        ).save(consumer, Mekanism.rl(basePath + "stone_brick_slab"));
        //Stone brick stairs
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.STONE_BRICK_STAIRS),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.BIO, 10),
              template(BlockItemIds.MOSSY_STONE_BRICK_STAIRS),
              false
        ).save(consumer, Mekanism.rl(basePath + "stone_brick_stairs"));
        //Stone brick wall
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.STONE_BRICK_WALL),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.BIO, 10),
              template(BlockItemIds.MOSSY_STONE_BRICK_WALL),
              false
        ).save(consumer, Mekanism.rl(basePath + "stone_brick_wall"));
    }

    private void addMiscBioRecipes(RecipeOutput consumer, String basePath) {
        //Dirt -> podzol
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.DIRT),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.BIO, 10),
              template(BlockItemIds.PODZOL),
              false
        ).save(consumer, Mekanism.rl(basePath + "dirt_to_podzol"));
        //Sand -> dirt
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.SANDS),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.BIO, 10),
              template(BlockItemIds.DIRT),
              false
        ).save(consumer, Mekanism.rl(basePath + "sand_to_dirt"));
        //slime ball
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, ItemIds.CLAY_BALL),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.BIO, 10),
              template(ItemIds.SLIME_BALL),
              false
        ).save(consumer, Mekanism.rl(basePath + "clay_to_slime_ball"));
        //slime block
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CLAY),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.BIO, 40),
              template(ItemIds.SLIME_BALL, 4),
              false
        ).save(consumer, Mekanism.rl(basePath + "clay_to_slime_block"));
    }
}
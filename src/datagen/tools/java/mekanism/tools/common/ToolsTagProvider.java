package mekanism.tools.common;

import java.util.concurrent.CompletableFuture;
import mekanism.common.tag.BaseTagProvider;
import mekanism.common.tag.MekanismTagBuilder;
import mekanism.tools.common.material.MaterialType;
import mekanism.tools.common.registries.ToolsItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.Tags;

public class ToolsTagProvider extends BaseTagProvider {

    public ToolsTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, MekanismTools.MODID);
    }

    @Override
    protected void registerTags(HolderLookup.Provider registries) {
        addToolTags();
        MekanismTagBuilder<Item> piglinLoved = getBuilder(ItemTags.PIGLIN_LOVED).add(ToolsItems.GOLD_PAXEL);
        ToolsItems.REFINED_GLOWSTONE_ARMOR.forEach(piglinLoved::add);
        ToolsItems.REFINED_GLOWSTONE_TOOLS.forEach(piglinLoved::add);
        ToolsItems.REFINED_GLOWSTONE_ARMOR.forEach(getBuilder(ItemTags.PIGLIN_SAFE_ARMOR)::add);
        getBuilder(ItemTags.PIGLIN_PREFERRED_WEAPONS).add(ToolsItems.REFINED_GLOWSTONE_TOOLS.spear());
        //Make refined glowstone armor make you immune to freezing because of the light it gives off
        ToolsItems.REFINED_GLOWSTONE_ARMOR.forEach(getBuilder(ItemTags.FREEZE_IMMUNE_WEARABLES)::add);
        getBuilder(ToolsTags.Blocks.MINEABLE_WITH_PAXEL).add(
              BlockTags.MINEABLE_WITH_AXE,
              BlockTags.MINEABLE_WITH_PICKAXE,
              BlockTags.MINEABLE_WITH_SHOVEL
        );
        //TODO - 1.21: Re-evaluate these, as I am fairly certain it may not be being done correctly
        getBuilder(ToolsTags.Blocks.INCORRECT_FOR_BRONZE_TOOL).add(BlockTags.INCORRECT_FOR_IRON_TOOL);
        getBuilder(ToolsTags.Blocks.INCORRECT_FOR_LAPIS_LAZULI_TOOL).add(BlockTags.INCORRECT_FOR_STONE_TOOL);
        getBuilder(ToolsTags.Blocks.INCORRECT_FOR_OSMIUM_TOOL).add(BlockTags.INCORRECT_FOR_IRON_TOOL);
        getBuilder(ToolsTags.Blocks.INCORRECT_FOR_REFINED_GLOWSTONE_TOOL).add(BlockTags.INCORRECT_FOR_DIAMOND_TOOL);
        getBuilder(ToolsTags.Blocks.INCORRECT_FOR_REFINED_OBSIDIAN_TOOL).add(BlockTags.INCORRECT_FOR_NETHERITE_TOOL);
        getBuilder(ToolsTags.Blocks.INCORRECT_FOR_STEEL_TOOL).add(BlockTags.INCORRECT_FOR_DIAMOND_TOOL);
        MekanismTagBuilder<Item> clusterMaxHarvestables = getBuilder(ItemTags.CLUSTER_MAX_HARVESTABLES);
        MekanismTagBuilder<Item> miningTools = getBuilder(Tags.Items.MINING_TOOL_TOOLS);
        MekanismTagBuilder<Item> meleeWeapons = getBuilder(Tags.Items.MELEE_WEAPON_TOOLS);
        ToolsItems.vanillaPaxels().forEach(paxel -> {
            clusterMaxHarvestables.add(paxel);
            miningTools.add(paxel);
            meleeWeapons.add(paxel);
        });
        for (MaterialType material : MaterialType.VALUES) {
            clusterMaxHarvestables.add(material.tools.pickaxe(), material.tools.paxel());
            miningTools.add(material.tools.pickaxe(), material.tools.paxel());
            meleeWeapons.add(material.tools.sword(), material.tools.axe(), material.tools.paxel(), material.tools.spear());
        }
    }

    private void addToolTags() {
        addPaxels();
        addSwords();
        addSpears();
        addAxes();
        addPickaxes();
        addShovels();
        addHoes();
        addShields();
        //Armor
        addHelmets();
        addChestplates();
        addLeggings();
        addBoots();
    }

    private void addPaxels() {
        getBuilder(ItemTags.DOUSES_CAMPFIRES).add(ToolsTags.Items.TOOLS_PAXEL);
        getBuilder(ItemTags.BREAKS_DECORATED_POTS).add(ToolsTags.Items.TOOLS_PAXEL);
        getBuilder(ItemTags.WEAPON_ENCHANTABLE).add(ToolsTags.Items.TOOLS_PAXEL);
        getBuilder(ItemTags.SHARP_WEAPON_ENCHANTABLE).add(ToolsTags.Items.TOOLS_PAXEL);
        getBuilder(ItemTags.MINING_ENCHANTABLE).add(ToolsTags.Items.TOOLS_PAXEL);
        getBuilder(ItemTags.MINING_LOOT_ENCHANTABLE).add(ToolsTags.Items.TOOLS_PAXEL);
        getBuilder(ItemTags.DURABILITY_ENCHANTABLE).add(ToolsTags.Items.TOOLS_PAXEL);
        getBuilder(Tags.Items.TOOLS).add(ToolsTags.Items.TOOLS_PAXEL);
        getBuilder(ItemTags.PICKAXES).add(ToolsTags.Items.TOOLS_PAXEL);
        getBuilder(ItemTags.AXES).add(ToolsTags.Items.TOOLS_PAXEL);
        getBuilder(ItemTags.SHOVELS).add(ToolsTags.Items.TOOLS_PAXEL);
        MekanismTagBuilder<Item> builder = getBuilder(ToolsTags.Items.TOOLS_PAXEL);
        //Vanilla Paxels
        ToolsItems.vanillaPaxels().forEach(builder::add);
        //Our paxels
        for (MaterialType material : MaterialType.VALUES) {
            builder.add(material.tools.paxel());
        }
    }

    private void addSwords() {
        MekanismTagBuilder<Item> builder = getBuilder(ItemTags.SWORDS);
        for (MaterialType material : MaterialType.VALUES) {
            builder.add(material.tools.sword());
        }
    }

    private void addSpears() {
        MekanismTagBuilder<Item> builder = getBuilder(ItemTags.SPEARS);
        for (MaterialType material : MaterialType.VALUES) {
            builder.add(material.tools.spear());
        }
    }

    private void addAxes() {
        MekanismTagBuilder<Item> builder = getBuilder(ItemTags.AXES);
        for (MaterialType material : MaterialType.VALUES) {
            builder.add(material.tools.axe());
        }
    }

    private void addPickaxes() {
        MekanismTagBuilder<Item> builder = getBuilder(ItemTags.PICKAXES);
        for (MaterialType material : MaterialType.VALUES) {
            builder.add(material.tools.pickaxe());
        }
    }

    private void addShovels() {
        MekanismTagBuilder<Item> builder = getBuilder(ItemTags.SHOVELS);
        for (MaterialType material : MaterialType.VALUES) {
            builder.add(material.tools.shovel());
        }
    }

    private void addHoes() {
        MekanismTagBuilder<Item> builder = getBuilder(ItemTags.HOES);
        for (MaterialType material : MaterialType.VALUES) {
            builder.add(material.tools.hoe());
        }
    }

    private void addShields() {
        MekanismTagBuilder<Item> shields = getBuilder(Tags.Items.TOOLS_SHIELD);
        MekanismTagBuilder<Item> durabilityEnchantable = getBuilder(ItemTags.DURABILITY_ENCHANTABLE);
        for (MaterialType material : MaterialType.VALUES) {
            shields.add(material.tools.shield());
            durabilityEnchantable.add(material.tools.shield());
        }
    }

    private void addHelmets() {
        MekanismTagBuilder<Item> builder = getBuilder(ItemTags.HEAD_ARMOR);
        for (MaterialType material : MaterialType.VALUES) {
            builder.add(material.armor.helmet());
        }
    }

    private void addChestplates() {
        MekanismTagBuilder<Item> builder = getBuilder(ItemTags.CHEST_ARMOR);
        for (MaterialType material : MaterialType.VALUES) {
            builder.add(material.armor.chestplate());
        }
    }

    private void addLeggings() {
        MekanismTagBuilder<Item> builder = getBuilder(ItemTags.LEG_ARMOR);
        for (MaterialType material : MaterialType.VALUES) {
            builder.add(material.armor.leggings());
        }
    }

    private void addBoots() {
        MekanismTagBuilder<Item> builder = getBuilder(ItemTags.FOOT_ARMOR);
        for (MaterialType material : MaterialType.VALUES) {
            builder.add(material.armor.boots());
        }
    }
}
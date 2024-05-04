package mekanism.additions.client;

import java.util.Map;
import mekanism.additions.common.AdditionsLang;
import mekanism.additions.common.AdditionsTags;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.advancements.AdditionsAdvancements;
import mekanism.additions.common.item.ItemBalloon;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.additions.common.registries.AdditionsEntityTypes;
import mekanism.additions.common.registries.AdditionsItems;
import mekanism.additions.common.registries.AdditionsSounds;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.text.EnumColor;
import mekanism.client.lang.BaseLanguageProvider;
import mekanism.common.registration.impl.ItemRegistryObject;
import net.minecraft.data.PackOutput;

public class AdditionsLangProvider extends BaseLanguageProvider {

    public AdditionsLangProvider(PackOutput output) {
        super(output, MekanismAdditions.MODID, MekanismAdditions.instance);
    }

    @Override
    protected void addTranslations() {
        addTags();
        addItems();
        addBlocks();
        addEntities();
        addSubtitles();
        addAdvancements();
        addMisc();
    }

    private void addTags() {
        addTag(AdditionsTags.Items.BALLOONS, "Balloons");

        addTag(AdditionsTags.Items.FENCES_PLASTIC, "Plastic Fences");
        addTag(AdditionsTags.Items.FENCE_GATES_PLASTIC, "Plastic Fence Gates");
        addTag(AdditionsTags.Items.STAIRS_PLASTIC, "Plastic Stairs");
        addTag(AdditionsTags.Items.SLABS_PLASTIC, "Plastic Slabs");
        addTag(AdditionsTags.Items.STAIRS_PLASTIC_GLOW, "Glow Plastic Stairs");
        addTag(AdditionsTags.Items.SLABS_PLASTIC_GLOW, "Glow Plastic Slabs");
        addTag(AdditionsTags.Items.STAIRS_PLASTIC_TRANSPARENT, "Transparent Plastic Stairs");
        addTag(AdditionsTags.Items.SLABS_PLASTIC_TRANSPARENT, "Transparent Plastic Slabs");

        addTag(AdditionsTags.Items.GLOW_PANELS, "Glow Panels");

        addTag(AdditionsTags.Items.PLASTIC_BLOCKS, "All Types of Plastic Blocks");
        addTag(AdditionsTags.Items.PLASTIC_BLOCKS_GLOW, "Glow Plastic Blocks");
        addTag(AdditionsTags.Items.PLASTIC_BLOCKS_PLASTIC, "Plastic Blocks");
        addTag(AdditionsTags.Items.PLASTIC_BLOCKS, "Plastic Blocks");
        addTag(AdditionsTags.Items.PLASTIC_BLOCKS_REINFORCED, "Reinforced Plastic Blocks");
        addTag(AdditionsTags.Items.PLASTIC_BLOCKS_ROAD, "Plastic Roads");
        addTag(AdditionsTags.Items.PLASTIC_BLOCKS_SLICK, "Slick Plastic Blocks");
        addTag(AdditionsTags.Items.PLASTIC_BLOCKS_TRANSPARENT, "Transparent Plastic Blocks");

    }

    private void addItems() {
        add(AdditionsItems.BABY_CREEPER_SPAWN_EGG, "Baby Creeper Spawn Egg");
        add(AdditionsItems.BABY_ENDERMAN_SPAWN_EGG, "Baby Enderman Spawn Egg");
        add(AdditionsItems.BABY_SKELETON_SPAWN_EGG, "Baby Skeleton Spawn Egg");
        add(AdditionsItems.BABY_STRAY_SPAWN_EGG, "Baby Stray Spawn Egg");
        add(AdditionsItems.BABY_WITHER_SKELETON_SPAWN_EGG, "Baby Wither Skeleton Spawn Egg");
        add(AdditionsItems.WALKIE_TALKIE, "Walkie-Talkie");
        for (Map.Entry<EnumColor, ItemRegistryObject<ItemBalloon>> entry : AdditionsItems.BALLOONS.entrySet()) {
            add(entry.getValue(), entry.getKey().getEnglishName() + " Balloon");
        }
    }

    private void addBlocks() {
        add(AdditionsBlocks.OBSIDIAN_TNT, "Obsidian TNT");
        addColoredBlocks(AdditionsBlocks.GLOW_PANELS, "Glow Panel");
        addColoredBlocks(AdditionsBlocks.PLASTIC_BLOCKS, "Plastic Block");
        addColoredBlocks(AdditionsBlocks.SLICK_PLASTIC_BLOCKS, "Slick Plastic Block");
        addColoredBlocks(AdditionsBlocks.PLASTIC_GLOW_BLOCKS, "Glow Plastic Block");
        addColoredBlocks(AdditionsBlocks.REINFORCED_PLASTIC_BLOCKS, "Reinforced Plastic Block");
        addColoredBlocks(AdditionsBlocks.PLASTIC_ROADS, "Plastic Road");
        addColoredBlocks(AdditionsBlocks.TRANSPARENT_PLASTIC_BLOCKS, "Transparent Plastic Block");
        addColoredBlocks(AdditionsBlocks.PLASTIC_STAIRS, "Plastic Stairs");
        addColoredBlocks(AdditionsBlocks.PLASTIC_SLABS, "Plastic Slab");
        addColoredBlocks(AdditionsBlocks.PLASTIC_FENCES, "Plastic Barrier");
        addColoredBlocks(AdditionsBlocks.PLASTIC_FENCE_GATES, "Plastic Gate");
        addColoredBlocks(AdditionsBlocks.PLASTIC_GLOW_STAIRS, "Glow Plastic Stairs");
        addColoredBlocks(AdditionsBlocks.PLASTIC_GLOW_SLABS, "Glow Plastic Slab");
        addColoredBlocks(AdditionsBlocks.TRANSPARENT_PLASTIC_STAIRS, "Transparent Plastic Stairs");
        addColoredBlocks(AdditionsBlocks.TRANSPARENT_PLASTIC_SLABS, "Transparent Plastic Slab");
    }

    private void addEntities() {
        addEntity(AdditionsEntityTypes.BABY_CREEPER, "Baby Creeper");
        addEntity(AdditionsEntityTypes.BABY_ENDERMAN, "Baby Enderman");
        addEntity(AdditionsEntityTypes.BABY_SKELETON, "Baby Skeleton");
        addEntity(AdditionsEntityTypes.BABY_STRAY, "Baby Stray");
        addEntity(AdditionsEntityTypes.BABY_WITHER_SKELETON, "Baby Wither Skeleton");
        addEntity(AdditionsEntityTypes.BALLOON, "Balloon");
        addEntity(AdditionsEntityTypes.OBSIDIAN_TNT, "Obsidian TNT");
    }

    private void addSubtitles() {
        add(AdditionsSounds.POP, "Balloon pops");
    }

    private void addAdvancements() {
        add(AdditionsAdvancements.BALLOON, "Reach for the Skies", "Craft any color Balloon");
        add(AdditionsAdvancements.POP_POP, "Pop Pop", "Pop a balloon");
        add(AdditionsAdvancements.GLOW_IN_THE_DARK, "Glow in the Dark", "Craft any color Glow Panel");
        add(AdditionsAdvancements.HURT_BY_BABIES, "Don't Try Taking Candy From Those Babies", "Get injured by all baby mobs from " + basicModName);
        add(AdditionsAdvancements.NOT_THE_BABIES, "Not the Babies", "Kill any baby " + basicModName + " mob");
    }

    private void addMisc() {
        addModInfo("Additions module for Mekanism, contains things that don't quite fit in the other modules");
        addPackData(AdditionsLang.MEKANISM_ADDITIONS, AdditionsLang.PACK_DESCRIPTION);
        add(AdditionsLang.CHANNEL, "Channel: %1$s");
        add(AdditionsLang.CHANNEL_CHANGE, "Channel changed to: %1$s");
        add(AdditionsLang.WALKIE_DISABLED, "Voice server disabled.");
        add(AdditionsLang.KEY_VOICE, "Voice");

        add(AdditionsLang.DESCRIPTION_GLOW_PANEL, "A modern, ever-lasting light source. Now in many colors!");
        add(AdditionsLang.DESCRIPTION_OBSIDIAN_TNT, "An extremely powerful, obsidian-infused block of TNT. Use at your own peril.");
    }

    private void addColoredBlocks(Map<EnumColor, ? extends IBlockProvider> blocks, String suffix) {
        for (Map.Entry<EnumColor, ? extends IBlockProvider> entry : blocks.entrySet()) {
            add(entry.getValue(), entry.getKey().getEnglishName() + " " + suffix);
        }
    }
}
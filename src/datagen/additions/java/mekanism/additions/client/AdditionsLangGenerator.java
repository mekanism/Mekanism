package mekanism.additions.client;

import mekanism.additions.common.AdditionsBlock;
import mekanism.additions.common.AdditionsItem;
import mekanism.additions.common.AdditionsLang;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.block.BlockGlowPanel;
import mekanism.additions.common.block.plastic.BlockPlastic;
import mekanism.additions.common.block.plastic.BlockPlasticFence;
import mekanism.additions.common.block.plastic.BlockPlasticFenceGate;
import mekanism.additions.common.block.plastic.BlockPlasticGlow;
import mekanism.additions.common.block.plastic.BlockPlasticReinforced;
import mekanism.additions.common.block.plastic.BlockPlasticRoad;
import mekanism.additions.common.block.plastic.BlockPlasticSlab;
import mekanism.additions.common.block.plastic.BlockPlasticSlick;
import mekanism.additions.common.block.plastic.BlockPlasticStairs;
import mekanism.additions.common.entity.AdditionsEntityType;
import mekanism.additions.common.item.ItemBalloon;
import mekanism.api.block.IColoredBlock;
import mekanism.client.lang.BaseLanguageProvider;
import mekanism.common.item.block.ItemBlockColoredName;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.ItemRegistryObject;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;

public class AdditionsLangGenerator extends BaseLanguageProvider {

    public AdditionsLangGenerator(DataGenerator gen) {
        super(gen, MekanismAdditions.MODID);
    }

    @Override
    protected void addTranslations() {
        add(AdditionsLang.CHANNEL, "Channel: %s");
        add(AdditionsLang.WALKIE_DISABLED, "Voice server disabled.");
        add(AdditionsLang.KEY_VOICE, "Voice");
        //Entity types
        add(AdditionsEntityType.BABY_SKELETON, "Baby Skeleton");
        add(AdditionsEntityType.BALLOON, "Balloon");
        add(AdditionsEntityType.OBSIDIAN_TNT, "Obsidian TNT");
        //Items and blocks
        add(AdditionsItem.BABY_SKELETON_SPAWN_EGG, "Baby Skeleton Spawn Egg");
        add(AdditionsItem.WALKIE_TALKIE, "Walkie-Talkie");
        add(AdditionsBlock.OBSIDIAN_TNT, "Obsidian TNT");
        //Balloons
        addBalloon(AdditionsItem.BLACK_BALLOON);
        addBalloon(AdditionsItem.RED_BALLOON);
        addBalloon(AdditionsItem.GREEN_BALLOON);
        addBalloon(AdditionsItem.BROWN_BALLOON);
        addBalloon(AdditionsItem.BLUE_BALLOON);
        addBalloon(AdditionsItem.PURPLE_BALLOON);
        addBalloon(AdditionsItem.CYAN_BALLOON);
        addBalloon(AdditionsItem.LIGHT_GRAY_BALLOON);
        addBalloon(AdditionsItem.GRAY_BALLOON);
        addBalloon(AdditionsItem.PINK_BALLOON);
        addBalloon(AdditionsItem.LIME_BALLOON);
        addBalloon(AdditionsItem.YELLOW_BALLOON);
        addBalloon(AdditionsItem.LIGHT_BLUE_BALLOON);
        addBalloon(AdditionsItem.MAGENTA_BALLOON);
        addBalloon(AdditionsItem.ORANGE_BALLOON);
        addBalloon(AdditionsItem.WHITE_BALLOON);
        //Glow Panels
        addGlowPanel(AdditionsBlock.BLACK_GLOW_PANEL);
        addGlowPanel(AdditionsBlock.RED_GLOW_PANEL);
        addGlowPanel(AdditionsBlock.GREEN_GLOW_PANEL);
        addGlowPanel(AdditionsBlock.BROWN_GLOW_PANEL);
        addGlowPanel(AdditionsBlock.BLUE_GLOW_PANEL);
        addGlowPanel(AdditionsBlock.PURPLE_GLOW_PANEL);
        addGlowPanel(AdditionsBlock.CYAN_GLOW_PANEL);
        addGlowPanel(AdditionsBlock.LIGHT_GRAY_GLOW_PANEL);
        addGlowPanel(AdditionsBlock.GRAY_GLOW_PANEL);
        addGlowPanel(AdditionsBlock.PINK_GLOW_PANEL);
        addGlowPanel(AdditionsBlock.LIME_GLOW_PANEL);
        addGlowPanel(AdditionsBlock.YELLOW_GLOW_PANEL);
        addGlowPanel(AdditionsBlock.LIGHT_BLUE_GLOW_PANEL);
        addGlowPanel(AdditionsBlock.MAGENTA_GLOW_PANEL);
        addGlowPanel(AdditionsBlock.ORANGE_GLOW_PANEL);
        addGlowPanel(AdditionsBlock.WHITE_GLOW_PANEL);
        //Plastic Blocks
        addPlasticBlock(AdditionsBlock.BLACK_PLASTIC_BLOCK);
        addPlasticBlock(AdditionsBlock.RED_PLASTIC_BLOCK);
        addPlasticBlock(AdditionsBlock.GREEN_PLASTIC_BLOCK);
        addPlasticBlock(AdditionsBlock.BROWN_PLASTIC_BLOCK);
        addPlasticBlock(AdditionsBlock.BLUE_PLASTIC_BLOCK);
        addPlasticBlock(AdditionsBlock.PURPLE_PLASTIC_BLOCK);
        addPlasticBlock(AdditionsBlock.CYAN_PLASTIC_BLOCK);
        addPlasticBlock(AdditionsBlock.LIGHT_GRAY_PLASTIC_BLOCK);
        addPlasticBlock(AdditionsBlock.GRAY_PLASTIC_BLOCK);
        addPlasticBlock(AdditionsBlock.PINK_PLASTIC_BLOCK);
        addPlasticBlock(AdditionsBlock.LIME_PLASTIC_BLOCK);
        addPlasticBlock(AdditionsBlock.YELLOW_PLASTIC_BLOCK);
        addPlasticBlock(AdditionsBlock.LIGHT_BLUE_PLASTIC_BLOCK);
        addPlasticBlock(AdditionsBlock.MAGENTA_PLASTIC_BLOCK);
        addPlasticBlock(AdditionsBlock.ORANGE_PLASTIC_BLOCK);
        addPlasticBlock(AdditionsBlock.WHITE_PLASTIC_BLOCK);
        //Slick Plastic
        addSlickPlasticBlock(AdditionsBlock.BLACK_SLICK_PLASTIC_BLOCK);
        addSlickPlasticBlock(AdditionsBlock.RED_SLICK_PLASTIC_BLOCK);
        addSlickPlasticBlock(AdditionsBlock.GREEN_SLICK_PLASTIC_BLOCK);
        addSlickPlasticBlock(AdditionsBlock.BROWN_SLICK_PLASTIC_BLOCK);
        addSlickPlasticBlock(AdditionsBlock.BLUE_SLICK_PLASTIC_BLOCK);
        addSlickPlasticBlock(AdditionsBlock.PURPLE_SLICK_PLASTIC_BLOCK);
        addSlickPlasticBlock(AdditionsBlock.CYAN_SLICK_PLASTIC_BLOCK);
        addSlickPlasticBlock(AdditionsBlock.LIGHT_GRAY_SLICK_PLASTIC_BLOCK);
        addSlickPlasticBlock(AdditionsBlock.GRAY_SLICK_PLASTIC_BLOCK);
        addSlickPlasticBlock(AdditionsBlock.PINK_SLICK_PLASTIC_BLOCK);
        addSlickPlasticBlock(AdditionsBlock.LIME_SLICK_PLASTIC_BLOCK);
        addSlickPlasticBlock(AdditionsBlock.YELLOW_SLICK_PLASTIC_BLOCK);
        addSlickPlasticBlock(AdditionsBlock.LIGHT_BLUE_SLICK_PLASTIC_BLOCK);
        addSlickPlasticBlock(AdditionsBlock.MAGENTA_SLICK_PLASTIC_BLOCK);
        addSlickPlasticBlock(AdditionsBlock.ORANGE_SLICK_PLASTIC_BLOCK);
        addSlickPlasticBlock(AdditionsBlock.WHITE_SLICK_PLASTIC_BLOCK);
        //Glow Plastic Blocks
        addGlowPlasticBlock(AdditionsBlock.BLACK_PLASTIC_GLOW_BLOCK);
        addGlowPlasticBlock(AdditionsBlock.RED_PLASTIC_GLOW_BLOCK);
        addGlowPlasticBlock(AdditionsBlock.GREEN_PLASTIC_GLOW_BLOCK);
        addGlowPlasticBlock(AdditionsBlock.BROWN_PLASTIC_GLOW_BLOCK);
        addGlowPlasticBlock(AdditionsBlock.BLUE_PLASTIC_GLOW_BLOCK);
        addGlowPlasticBlock(AdditionsBlock.PURPLE_PLASTIC_GLOW_BLOCK);
        addGlowPlasticBlock(AdditionsBlock.CYAN_PLASTIC_GLOW_BLOCK);
        addGlowPlasticBlock(AdditionsBlock.LIGHT_GRAY_PLASTIC_GLOW_BLOCK);
        addGlowPlasticBlock(AdditionsBlock.GRAY_PLASTIC_GLOW_BLOCK);
        addGlowPlasticBlock(AdditionsBlock.PINK_PLASTIC_GLOW_BLOCK);
        addGlowPlasticBlock(AdditionsBlock.LIME_PLASTIC_GLOW_BLOCK);
        addGlowPlasticBlock(AdditionsBlock.YELLOW_PLASTIC_GLOW_BLOCK);
        addGlowPlasticBlock(AdditionsBlock.LIGHT_BLUE_PLASTIC_GLOW_BLOCK);
        addGlowPlasticBlock(AdditionsBlock.MAGENTA_PLASTIC_GLOW_BLOCK);
        addGlowPlasticBlock(AdditionsBlock.ORANGE_PLASTIC_GLOW_BLOCK);
        addGlowPlasticBlock(AdditionsBlock.WHITE_PLASTIC_GLOW_BLOCK);
        //Reinforced Plastic Blocks
        addReinforcedPlasticBlock(AdditionsBlock.BLACK_REINFORCED_PLASTIC_BLOCK);
        addReinforcedPlasticBlock(AdditionsBlock.RED_REINFORCED_PLASTIC_BLOCK);
        addReinforcedPlasticBlock(AdditionsBlock.GREEN_REINFORCED_PLASTIC_BLOCK);
        addReinforcedPlasticBlock(AdditionsBlock.BROWN_REINFORCED_PLASTIC_BLOCK);
        addReinforcedPlasticBlock(AdditionsBlock.BLUE_REINFORCED_PLASTIC_BLOCK);
        addReinforcedPlasticBlock(AdditionsBlock.PURPLE_REINFORCED_PLASTIC_BLOCK);
        addReinforcedPlasticBlock(AdditionsBlock.CYAN_REINFORCED_PLASTIC_BLOCK);
        addReinforcedPlasticBlock(AdditionsBlock.LIGHT_GRAY_REINFORCED_PLASTIC_BLOCK);
        addReinforcedPlasticBlock(AdditionsBlock.GRAY_REINFORCED_PLASTIC_BLOCK);
        addReinforcedPlasticBlock(AdditionsBlock.PINK_REINFORCED_PLASTIC_BLOCK);
        addReinforcedPlasticBlock(AdditionsBlock.LIME_REINFORCED_PLASTIC_BLOCK);
        addReinforcedPlasticBlock(AdditionsBlock.YELLOW_REINFORCED_PLASTIC_BLOCK);
        addReinforcedPlasticBlock(AdditionsBlock.LIGHT_BLUE_REINFORCED_PLASTIC_BLOCK);
        addReinforcedPlasticBlock(AdditionsBlock.MAGENTA_REINFORCED_PLASTIC_BLOCK);
        addReinforcedPlasticBlock(AdditionsBlock.ORANGE_REINFORCED_PLASTIC_BLOCK);
        addReinforcedPlasticBlock(AdditionsBlock.WHITE_REINFORCED_PLASTIC_BLOCK);
        //Plastic Road
        addPlasticRoad(AdditionsBlock.BLACK_PLASTIC_ROAD);
        addPlasticRoad(AdditionsBlock.RED_PLASTIC_ROAD);
        addPlasticRoad(AdditionsBlock.GREEN_PLASTIC_ROAD);
        addPlasticRoad(AdditionsBlock.BROWN_PLASTIC_ROAD);
        addPlasticRoad(AdditionsBlock.BLUE_PLASTIC_ROAD);
        addPlasticRoad(AdditionsBlock.PURPLE_PLASTIC_ROAD);
        addPlasticRoad(AdditionsBlock.CYAN_PLASTIC_ROAD);
        addPlasticRoad(AdditionsBlock.LIGHT_GRAY_PLASTIC_ROAD);
        addPlasticRoad(AdditionsBlock.GRAY_PLASTIC_ROAD);
        addPlasticRoad(AdditionsBlock.PINK_PLASTIC_ROAD);
        addPlasticRoad(AdditionsBlock.LIME_PLASTIC_ROAD);
        addPlasticRoad(AdditionsBlock.YELLOW_PLASTIC_ROAD);
        addPlasticRoad(AdditionsBlock.LIGHT_BLUE_PLASTIC_ROAD);
        addPlasticRoad(AdditionsBlock.MAGENTA_PLASTIC_ROAD);
        addPlasticRoad(AdditionsBlock.ORANGE_PLASTIC_ROAD);
        addPlasticRoad(AdditionsBlock.WHITE_PLASTIC_ROAD);
        //Plastic Stairs
        addPlasticStairs(AdditionsBlock.BLACK_PLASTIC_STAIRS);
        addPlasticStairs(AdditionsBlock.RED_PLASTIC_STAIRS);
        addPlasticStairs(AdditionsBlock.GREEN_PLASTIC_STAIRS);
        addPlasticStairs(AdditionsBlock.BROWN_PLASTIC_STAIRS);
        addPlasticStairs(AdditionsBlock.BLUE_PLASTIC_STAIRS);
        addPlasticStairs(AdditionsBlock.PURPLE_PLASTIC_STAIRS);
        addPlasticStairs(AdditionsBlock.CYAN_PLASTIC_STAIRS);
        addPlasticStairs(AdditionsBlock.LIGHT_GRAY_PLASTIC_STAIRS);
        addPlasticStairs(AdditionsBlock.GRAY_PLASTIC_STAIRS);
        addPlasticStairs(AdditionsBlock.PINK_PLASTIC_STAIRS);
        addPlasticStairs(AdditionsBlock.LIME_PLASTIC_STAIRS);
        addPlasticStairs(AdditionsBlock.YELLOW_PLASTIC_STAIRS);
        addPlasticStairs(AdditionsBlock.LIGHT_BLUE_PLASTIC_STAIRS);
        addPlasticStairs(AdditionsBlock.MAGENTA_PLASTIC_STAIRS);
        addPlasticStairs(AdditionsBlock.ORANGE_PLASTIC_STAIRS);
        addPlasticStairs(AdditionsBlock.WHITE_PLASTIC_STAIRS);
        //Plastic Slabs
        addPlasticSlab(AdditionsBlock.BLACK_PLASTIC_SLAB);
        addPlasticSlab(AdditionsBlock.RED_PLASTIC_SLAB);
        addPlasticSlab(AdditionsBlock.GREEN_PLASTIC_SLAB);
        addPlasticSlab(AdditionsBlock.BROWN_PLASTIC_SLAB);
        addPlasticSlab(AdditionsBlock.BLUE_PLASTIC_SLAB);
        addPlasticSlab(AdditionsBlock.PURPLE_PLASTIC_SLAB);
        addPlasticSlab(AdditionsBlock.CYAN_PLASTIC_SLAB);
        addPlasticSlab(AdditionsBlock.LIGHT_GRAY_PLASTIC_SLAB);
        addPlasticSlab(AdditionsBlock.GRAY_PLASTIC_SLAB);
        addPlasticSlab(AdditionsBlock.PINK_PLASTIC_SLAB);
        addPlasticSlab(AdditionsBlock.LIME_PLASTIC_SLAB);
        addPlasticSlab(AdditionsBlock.YELLOW_PLASTIC_SLAB);
        addPlasticSlab(AdditionsBlock.LIGHT_BLUE_PLASTIC_SLAB);
        addPlasticSlab(AdditionsBlock.MAGENTA_PLASTIC_SLAB);
        addPlasticSlab(AdditionsBlock.ORANGE_PLASTIC_SLAB);
        addPlasticSlab(AdditionsBlock.WHITE_PLASTIC_SLAB);
        //Plastic Fence
        addPlasticFence(AdditionsBlock.BLACK_PLASTIC_FENCE);
        addPlasticFence(AdditionsBlock.RED_PLASTIC_FENCE);
        addPlasticFence(AdditionsBlock.GREEN_PLASTIC_FENCE);
        addPlasticFence(AdditionsBlock.BROWN_PLASTIC_FENCE);
        addPlasticFence(AdditionsBlock.BLUE_PLASTIC_FENCE);
        addPlasticFence(AdditionsBlock.PURPLE_PLASTIC_FENCE);
        addPlasticFence(AdditionsBlock.CYAN_PLASTIC_FENCE);
        addPlasticFence(AdditionsBlock.LIGHT_GRAY_PLASTIC_FENCE);
        addPlasticFence(AdditionsBlock.GRAY_PLASTIC_FENCE);
        addPlasticFence(AdditionsBlock.PINK_PLASTIC_FENCE);
        addPlasticFence(AdditionsBlock.LIME_PLASTIC_FENCE);
        addPlasticFence(AdditionsBlock.YELLOW_PLASTIC_FENCE);
        addPlasticFence(AdditionsBlock.LIGHT_BLUE_PLASTIC_FENCE);
        addPlasticFence(AdditionsBlock.MAGENTA_PLASTIC_FENCE);
        addPlasticFence(AdditionsBlock.ORANGE_PLASTIC_FENCE);
        addPlasticFence(AdditionsBlock.WHITE_PLASTIC_FENCE);
        //Plastic Fence Gate
        addPlasticFenceGate(AdditionsBlock.BLACK_PLASTIC_FENCE_GATE);
        addPlasticFenceGate(AdditionsBlock.RED_PLASTIC_FENCE_GATE);
        addPlasticFenceGate(AdditionsBlock.GREEN_PLASTIC_FENCE_GATE);
        addPlasticFenceGate(AdditionsBlock.BROWN_PLASTIC_FENCE_GATE);
        addPlasticFenceGate(AdditionsBlock.BLUE_PLASTIC_FENCE_GATE);
        addPlasticFenceGate(AdditionsBlock.PURPLE_PLASTIC_FENCE_GATE);
        addPlasticFenceGate(AdditionsBlock.CYAN_PLASTIC_FENCE_GATE);
        addPlasticFenceGate(AdditionsBlock.LIGHT_GRAY_PLASTIC_FENCE_GATE);
        addPlasticFenceGate(AdditionsBlock.GRAY_PLASTIC_FENCE_GATE);
        addPlasticFenceGate(AdditionsBlock.PINK_PLASTIC_FENCE_GATE);
        addPlasticFenceGate(AdditionsBlock.LIME_PLASTIC_FENCE_GATE);
        addPlasticFenceGate(AdditionsBlock.YELLOW_PLASTIC_FENCE_GATE);
        addPlasticFenceGate(AdditionsBlock.LIGHT_BLUE_PLASTIC_FENCE_GATE);
        addPlasticFenceGate(AdditionsBlock.MAGENTA_PLASTIC_FENCE_GATE);
        addPlasticFenceGate(AdditionsBlock.ORANGE_PLASTIC_FENCE_GATE);
        addPlasticFenceGate(AdditionsBlock.WHITE_PLASTIC_FENCE_GATE);
    }

    private void addGlowPanel(BlockRegistryObject<BlockGlowPanel, ItemBlockColoredName> glowPanel) {
        addColoredBlock(glowPanel, " Glow Panel");
    }

    private void addPlasticBlock(BlockRegistryObject<BlockPlastic, ItemBlockColoredName> plasticBlock) {
        addColoredBlock(plasticBlock, " Plastic Block");
    }

    private void addSlickPlasticBlock(BlockRegistryObject<BlockPlasticSlick, ItemBlockColoredName> slickPlastic) {
        addColoredBlock(slickPlastic, " Slick Plastic Block");
    }

    private void addGlowPlasticBlock(BlockRegistryObject<BlockPlasticGlow, ItemBlockColoredName> plasticGlow) {
        addColoredBlock(plasticGlow, " Glow Plastic Block");
    }

    private void addReinforcedPlasticBlock(BlockRegistryObject<BlockPlasticReinforced, ItemBlockColoredName> reinforcedPlastic) {
        addColoredBlock(reinforcedPlastic, " Reinforced Plastic Block");
    }

    private void addPlasticRoad(BlockRegistryObject<BlockPlasticRoad, ItemBlockColoredName> plasticRoad) {
        addColoredBlock(plasticRoad, " Plastic Road");
    }

    private void addPlasticStairs(BlockRegistryObject<BlockPlasticStairs, ItemBlockColoredName> plasticStairs) {
        addColoredBlock(plasticStairs, " Plastic Stairs");
    }

    private void addPlasticSlab(BlockRegistryObject<BlockPlasticSlab, ItemBlockColoredName> plasticSlab) {
        addColoredBlock(plasticSlab, " Plastic Slab");
    }

    private void addPlasticFence(BlockRegistryObject<BlockPlasticFence, ItemBlockColoredName> plasticFence) {
        addColoredBlock(plasticFence, " Plastic Barrier");
    }

    private void addPlasticFenceGate(BlockRegistryObject<BlockPlasticFenceGate, ItemBlockColoredName> plasticFenceGate) {
        addColoredBlock(plasticFenceGate, " Plastic Gate");
    }

    private void addBalloon(ItemRegistryObject<ItemBalloon> balloonRO) {
        add(balloonRO.getTranslationKey(), balloonRO.getItem().getColor().getEnglishName() + " Balloon");
    }

    private <BLOCK extends Block & IColoredBlock> void addColoredBlock(BlockRegistryObject<BLOCK, ItemBlockColoredName> blockRO, String suffix) {
        add(blockRO.getTranslationKey(), blockRO.getBlock().getColor().getEnglishName() + suffix);
    }
}
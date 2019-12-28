package mekanism.additions.client;

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
import mekanism.additions.common.item.ItemBalloon;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.additions.common.registries.AdditionsEntityTypes;
import mekanism.additions.common.registries.AdditionsItems;
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
        addItems();
        addBlocks();
        addEntities();
        addMisc();
    }

    private void addItems() {
        add(AdditionsItems.BABY_SKELETON_SPAWN_EGG, "Baby Skeleton Spawn Egg");
        add(AdditionsItems.WALKIE_TALKIE, "Walkie-Talkie");
        //Balloons
        addBalloon(AdditionsItems.BLACK_BALLOON);
        addBalloon(AdditionsItems.RED_BALLOON);
        addBalloon(AdditionsItems.GREEN_BALLOON);
        addBalloon(AdditionsItems.BROWN_BALLOON);
        addBalloon(AdditionsItems.BLUE_BALLOON);
        addBalloon(AdditionsItems.PURPLE_BALLOON);
        addBalloon(AdditionsItems.CYAN_BALLOON);
        addBalloon(AdditionsItems.LIGHT_GRAY_BALLOON);
        addBalloon(AdditionsItems.GRAY_BALLOON);
        addBalloon(AdditionsItems.PINK_BALLOON);
        addBalloon(AdditionsItems.LIME_BALLOON);
        addBalloon(AdditionsItems.YELLOW_BALLOON);
        addBalloon(AdditionsItems.LIGHT_BLUE_BALLOON);
        addBalloon(AdditionsItems.MAGENTA_BALLOON);
        addBalloon(AdditionsItems.ORANGE_BALLOON);
        addBalloon(AdditionsItems.WHITE_BALLOON);
    }

    private void addBlocks() {
        add(AdditionsBlocks.OBSIDIAN_TNT, "Obsidian TNT");
        //Glow Panels
        addGlowPanel(AdditionsBlocks.BLACK_GLOW_PANEL);
        addGlowPanel(AdditionsBlocks.RED_GLOW_PANEL);
        addGlowPanel(AdditionsBlocks.GREEN_GLOW_PANEL);
        addGlowPanel(AdditionsBlocks.BROWN_GLOW_PANEL);
        addGlowPanel(AdditionsBlocks.BLUE_GLOW_PANEL);
        addGlowPanel(AdditionsBlocks.PURPLE_GLOW_PANEL);
        addGlowPanel(AdditionsBlocks.CYAN_GLOW_PANEL);
        addGlowPanel(AdditionsBlocks.LIGHT_GRAY_GLOW_PANEL);
        addGlowPanel(AdditionsBlocks.GRAY_GLOW_PANEL);
        addGlowPanel(AdditionsBlocks.PINK_GLOW_PANEL);
        addGlowPanel(AdditionsBlocks.LIME_GLOW_PANEL);
        addGlowPanel(AdditionsBlocks.YELLOW_GLOW_PANEL);
        addGlowPanel(AdditionsBlocks.LIGHT_BLUE_GLOW_PANEL);
        addGlowPanel(AdditionsBlocks.MAGENTA_GLOW_PANEL);
        addGlowPanel(AdditionsBlocks.ORANGE_GLOW_PANEL);
        addGlowPanel(AdditionsBlocks.WHITE_GLOW_PANEL);
        //Plastic Blocks
        addPlasticBlock(AdditionsBlocks.BLACK_PLASTIC_BLOCK);
        addPlasticBlock(AdditionsBlocks.RED_PLASTIC_BLOCK);
        addPlasticBlock(AdditionsBlocks.GREEN_PLASTIC_BLOCK);
        addPlasticBlock(AdditionsBlocks.BROWN_PLASTIC_BLOCK);
        addPlasticBlock(AdditionsBlocks.BLUE_PLASTIC_BLOCK);
        addPlasticBlock(AdditionsBlocks.PURPLE_PLASTIC_BLOCK);
        addPlasticBlock(AdditionsBlocks.CYAN_PLASTIC_BLOCK);
        addPlasticBlock(AdditionsBlocks.LIGHT_GRAY_PLASTIC_BLOCK);
        addPlasticBlock(AdditionsBlocks.GRAY_PLASTIC_BLOCK);
        addPlasticBlock(AdditionsBlocks.PINK_PLASTIC_BLOCK);
        addPlasticBlock(AdditionsBlocks.LIME_PLASTIC_BLOCK);
        addPlasticBlock(AdditionsBlocks.YELLOW_PLASTIC_BLOCK);
        addPlasticBlock(AdditionsBlocks.LIGHT_BLUE_PLASTIC_BLOCK);
        addPlasticBlock(AdditionsBlocks.MAGENTA_PLASTIC_BLOCK);
        addPlasticBlock(AdditionsBlocks.ORANGE_PLASTIC_BLOCK);
        addPlasticBlock(AdditionsBlocks.WHITE_PLASTIC_BLOCK);
        //Slick Plastic
        addSlickPlasticBlock(AdditionsBlocks.BLACK_SLICK_PLASTIC_BLOCK);
        addSlickPlasticBlock(AdditionsBlocks.RED_SLICK_PLASTIC_BLOCK);
        addSlickPlasticBlock(AdditionsBlocks.GREEN_SLICK_PLASTIC_BLOCK);
        addSlickPlasticBlock(AdditionsBlocks.BROWN_SLICK_PLASTIC_BLOCK);
        addSlickPlasticBlock(AdditionsBlocks.BLUE_SLICK_PLASTIC_BLOCK);
        addSlickPlasticBlock(AdditionsBlocks.PURPLE_SLICK_PLASTIC_BLOCK);
        addSlickPlasticBlock(AdditionsBlocks.CYAN_SLICK_PLASTIC_BLOCK);
        addSlickPlasticBlock(AdditionsBlocks.LIGHT_GRAY_SLICK_PLASTIC_BLOCK);
        addSlickPlasticBlock(AdditionsBlocks.GRAY_SLICK_PLASTIC_BLOCK);
        addSlickPlasticBlock(AdditionsBlocks.PINK_SLICK_PLASTIC_BLOCK);
        addSlickPlasticBlock(AdditionsBlocks.LIME_SLICK_PLASTIC_BLOCK);
        addSlickPlasticBlock(AdditionsBlocks.YELLOW_SLICK_PLASTIC_BLOCK);
        addSlickPlasticBlock(AdditionsBlocks.LIGHT_BLUE_SLICK_PLASTIC_BLOCK);
        addSlickPlasticBlock(AdditionsBlocks.MAGENTA_SLICK_PLASTIC_BLOCK);
        addSlickPlasticBlock(AdditionsBlocks.ORANGE_SLICK_PLASTIC_BLOCK);
        addSlickPlasticBlock(AdditionsBlocks.WHITE_SLICK_PLASTIC_BLOCK);
        //Glow Plastic Blocks
        addGlowPlasticBlock(AdditionsBlocks.BLACK_PLASTIC_GLOW_BLOCK);
        addGlowPlasticBlock(AdditionsBlocks.RED_PLASTIC_GLOW_BLOCK);
        addGlowPlasticBlock(AdditionsBlocks.GREEN_PLASTIC_GLOW_BLOCK);
        addGlowPlasticBlock(AdditionsBlocks.BROWN_PLASTIC_GLOW_BLOCK);
        addGlowPlasticBlock(AdditionsBlocks.BLUE_PLASTIC_GLOW_BLOCK);
        addGlowPlasticBlock(AdditionsBlocks.PURPLE_PLASTIC_GLOW_BLOCK);
        addGlowPlasticBlock(AdditionsBlocks.CYAN_PLASTIC_GLOW_BLOCK);
        addGlowPlasticBlock(AdditionsBlocks.LIGHT_GRAY_PLASTIC_GLOW_BLOCK);
        addGlowPlasticBlock(AdditionsBlocks.GRAY_PLASTIC_GLOW_BLOCK);
        addGlowPlasticBlock(AdditionsBlocks.PINK_PLASTIC_GLOW_BLOCK);
        addGlowPlasticBlock(AdditionsBlocks.LIME_PLASTIC_GLOW_BLOCK);
        addGlowPlasticBlock(AdditionsBlocks.YELLOW_PLASTIC_GLOW_BLOCK);
        addGlowPlasticBlock(AdditionsBlocks.LIGHT_BLUE_PLASTIC_GLOW_BLOCK);
        addGlowPlasticBlock(AdditionsBlocks.MAGENTA_PLASTIC_GLOW_BLOCK);
        addGlowPlasticBlock(AdditionsBlocks.ORANGE_PLASTIC_GLOW_BLOCK);
        addGlowPlasticBlock(AdditionsBlocks.WHITE_PLASTIC_GLOW_BLOCK);
        //Reinforced Plastic Blocks
        addReinforcedPlasticBlock(AdditionsBlocks.BLACK_REINFORCED_PLASTIC_BLOCK);
        addReinforcedPlasticBlock(AdditionsBlocks.RED_REINFORCED_PLASTIC_BLOCK);
        addReinforcedPlasticBlock(AdditionsBlocks.GREEN_REINFORCED_PLASTIC_BLOCK);
        addReinforcedPlasticBlock(AdditionsBlocks.BROWN_REINFORCED_PLASTIC_BLOCK);
        addReinforcedPlasticBlock(AdditionsBlocks.BLUE_REINFORCED_PLASTIC_BLOCK);
        addReinforcedPlasticBlock(AdditionsBlocks.PURPLE_REINFORCED_PLASTIC_BLOCK);
        addReinforcedPlasticBlock(AdditionsBlocks.CYAN_REINFORCED_PLASTIC_BLOCK);
        addReinforcedPlasticBlock(AdditionsBlocks.LIGHT_GRAY_REINFORCED_PLASTIC_BLOCK);
        addReinforcedPlasticBlock(AdditionsBlocks.GRAY_REINFORCED_PLASTIC_BLOCK);
        addReinforcedPlasticBlock(AdditionsBlocks.PINK_REINFORCED_PLASTIC_BLOCK);
        addReinforcedPlasticBlock(AdditionsBlocks.LIME_REINFORCED_PLASTIC_BLOCK);
        addReinforcedPlasticBlock(AdditionsBlocks.YELLOW_REINFORCED_PLASTIC_BLOCK);
        addReinforcedPlasticBlock(AdditionsBlocks.LIGHT_BLUE_REINFORCED_PLASTIC_BLOCK);
        addReinforcedPlasticBlock(AdditionsBlocks.MAGENTA_REINFORCED_PLASTIC_BLOCK);
        addReinforcedPlasticBlock(AdditionsBlocks.ORANGE_REINFORCED_PLASTIC_BLOCK);
        addReinforcedPlasticBlock(AdditionsBlocks.WHITE_REINFORCED_PLASTIC_BLOCK);
        //Plastic Road
        addPlasticRoad(AdditionsBlocks.BLACK_PLASTIC_ROAD);
        addPlasticRoad(AdditionsBlocks.RED_PLASTIC_ROAD);
        addPlasticRoad(AdditionsBlocks.GREEN_PLASTIC_ROAD);
        addPlasticRoad(AdditionsBlocks.BROWN_PLASTIC_ROAD);
        addPlasticRoad(AdditionsBlocks.BLUE_PLASTIC_ROAD);
        addPlasticRoad(AdditionsBlocks.PURPLE_PLASTIC_ROAD);
        addPlasticRoad(AdditionsBlocks.CYAN_PLASTIC_ROAD);
        addPlasticRoad(AdditionsBlocks.LIGHT_GRAY_PLASTIC_ROAD);
        addPlasticRoad(AdditionsBlocks.GRAY_PLASTIC_ROAD);
        addPlasticRoad(AdditionsBlocks.PINK_PLASTIC_ROAD);
        addPlasticRoad(AdditionsBlocks.LIME_PLASTIC_ROAD);
        addPlasticRoad(AdditionsBlocks.YELLOW_PLASTIC_ROAD);
        addPlasticRoad(AdditionsBlocks.LIGHT_BLUE_PLASTIC_ROAD);
        addPlasticRoad(AdditionsBlocks.MAGENTA_PLASTIC_ROAD);
        addPlasticRoad(AdditionsBlocks.ORANGE_PLASTIC_ROAD);
        addPlasticRoad(AdditionsBlocks.WHITE_PLASTIC_ROAD);
        //Plastic Stairs
        addPlasticStairs(AdditionsBlocks.BLACK_PLASTIC_STAIRS);
        addPlasticStairs(AdditionsBlocks.RED_PLASTIC_STAIRS);
        addPlasticStairs(AdditionsBlocks.GREEN_PLASTIC_STAIRS);
        addPlasticStairs(AdditionsBlocks.BROWN_PLASTIC_STAIRS);
        addPlasticStairs(AdditionsBlocks.BLUE_PLASTIC_STAIRS);
        addPlasticStairs(AdditionsBlocks.PURPLE_PLASTIC_STAIRS);
        addPlasticStairs(AdditionsBlocks.CYAN_PLASTIC_STAIRS);
        addPlasticStairs(AdditionsBlocks.LIGHT_GRAY_PLASTIC_STAIRS);
        addPlasticStairs(AdditionsBlocks.GRAY_PLASTIC_STAIRS);
        addPlasticStairs(AdditionsBlocks.PINK_PLASTIC_STAIRS);
        addPlasticStairs(AdditionsBlocks.LIME_PLASTIC_STAIRS);
        addPlasticStairs(AdditionsBlocks.YELLOW_PLASTIC_STAIRS);
        addPlasticStairs(AdditionsBlocks.LIGHT_BLUE_PLASTIC_STAIRS);
        addPlasticStairs(AdditionsBlocks.MAGENTA_PLASTIC_STAIRS);
        addPlasticStairs(AdditionsBlocks.ORANGE_PLASTIC_STAIRS);
        addPlasticStairs(AdditionsBlocks.WHITE_PLASTIC_STAIRS);
        //Plastic Slabs
        addPlasticSlab(AdditionsBlocks.BLACK_PLASTIC_SLAB);
        addPlasticSlab(AdditionsBlocks.RED_PLASTIC_SLAB);
        addPlasticSlab(AdditionsBlocks.GREEN_PLASTIC_SLAB);
        addPlasticSlab(AdditionsBlocks.BROWN_PLASTIC_SLAB);
        addPlasticSlab(AdditionsBlocks.BLUE_PLASTIC_SLAB);
        addPlasticSlab(AdditionsBlocks.PURPLE_PLASTIC_SLAB);
        addPlasticSlab(AdditionsBlocks.CYAN_PLASTIC_SLAB);
        addPlasticSlab(AdditionsBlocks.LIGHT_GRAY_PLASTIC_SLAB);
        addPlasticSlab(AdditionsBlocks.GRAY_PLASTIC_SLAB);
        addPlasticSlab(AdditionsBlocks.PINK_PLASTIC_SLAB);
        addPlasticSlab(AdditionsBlocks.LIME_PLASTIC_SLAB);
        addPlasticSlab(AdditionsBlocks.YELLOW_PLASTIC_SLAB);
        addPlasticSlab(AdditionsBlocks.LIGHT_BLUE_PLASTIC_SLAB);
        addPlasticSlab(AdditionsBlocks.MAGENTA_PLASTIC_SLAB);
        addPlasticSlab(AdditionsBlocks.ORANGE_PLASTIC_SLAB);
        addPlasticSlab(AdditionsBlocks.WHITE_PLASTIC_SLAB);
        //Plastic Fence
        addPlasticFence(AdditionsBlocks.BLACK_PLASTIC_FENCE);
        addPlasticFence(AdditionsBlocks.RED_PLASTIC_FENCE);
        addPlasticFence(AdditionsBlocks.GREEN_PLASTIC_FENCE);
        addPlasticFence(AdditionsBlocks.BROWN_PLASTIC_FENCE);
        addPlasticFence(AdditionsBlocks.BLUE_PLASTIC_FENCE);
        addPlasticFence(AdditionsBlocks.PURPLE_PLASTIC_FENCE);
        addPlasticFence(AdditionsBlocks.CYAN_PLASTIC_FENCE);
        addPlasticFence(AdditionsBlocks.LIGHT_GRAY_PLASTIC_FENCE);
        addPlasticFence(AdditionsBlocks.GRAY_PLASTIC_FENCE);
        addPlasticFence(AdditionsBlocks.PINK_PLASTIC_FENCE);
        addPlasticFence(AdditionsBlocks.LIME_PLASTIC_FENCE);
        addPlasticFence(AdditionsBlocks.YELLOW_PLASTIC_FENCE);
        addPlasticFence(AdditionsBlocks.LIGHT_BLUE_PLASTIC_FENCE);
        addPlasticFence(AdditionsBlocks.MAGENTA_PLASTIC_FENCE);
        addPlasticFence(AdditionsBlocks.ORANGE_PLASTIC_FENCE);
        addPlasticFence(AdditionsBlocks.WHITE_PLASTIC_FENCE);
        //Plastic Fence Gate
        addPlasticFenceGate(AdditionsBlocks.BLACK_PLASTIC_FENCE_GATE);
        addPlasticFenceGate(AdditionsBlocks.RED_PLASTIC_FENCE_GATE);
        addPlasticFenceGate(AdditionsBlocks.GREEN_PLASTIC_FENCE_GATE);
        addPlasticFenceGate(AdditionsBlocks.BROWN_PLASTIC_FENCE_GATE);
        addPlasticFenceGate(AdditionsBlocks.BLUE_PLASTIC_FENCE_GATE);
        addPlasticFenceGate(AdditionsBlocks.PURPLE_PLASTIC_FENCE_GATE);
        addPlasticFenceGate(AdditionsBlocks.CYAN_PLASTIC_FENCE_GATE);
        addPlasticFenceGate(AdditionsBlocks.LIGHT_GRAY_PLASTIC_FENCE_GATE);
        addPlasticFenceGate(AdditionsBlocks.GRAY_PLASTIC_FENCE_GATE);
        addPlasticFenceGate(AdditionsBlocks.PINK_PLASTIC_FENCE_GATE);
        addPlasticFenceGate(AdditionsBlocks.LIME_PLASTIC_FENCE_GATE);
        addPlasticFenceGate(AdditionsBlocks.YELLOW_PLASTIC_FENCE_GATE);
        addPlasticFenceGate(AdditionsBlocks.LIGHT_BLUE_PLASTIC_FENCE_GATE);
        addPlasticFenceGate(AdditionsBlocks.MAGENTA_PLASTIC_FENCE_GATE);
        addPlasticFenceGate(AdditionsBlocks.ORANGE_PLASTIC_FENCE_GATE);
        addPlasticFenceGate(AdditionsBlocks.WHITE_PLASTIC_FENCE_GATE);
    }

    private void addEntities() {
        add(AdditionsEntityTypes.BABY_SKELETON, "Baby Skeleton");
        add(AdditionsEntityTypes.BALLOON, "Balloon");
        add(AdditionsEntityTypes.OBSIDIAN_TNT, "Obsidian TNT");
    }

    private void addMisc() {
        add(AdditionsLang.CHANNEL, "Channel: %s");
        add(AdditionsLang.WALKIE_DISABLED, "Voice server disabled.");
        add(AdditionsLang.KEY_VOICE, "Voice");
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
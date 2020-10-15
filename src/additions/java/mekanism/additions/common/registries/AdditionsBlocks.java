package mekanism.additions.common.registries;

import java.util.function.Function;
import java.util.function.UnaryOperator;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.block.BlockGlowPanel;
import mekanism.additions.common.block.BlockObsidianTNT;
import mekanism.additions.common.block.plastic.BlockPlastic;
import mekanism.additions.common.block.plastic.BlockPlasticFence;
import mekanism.additions.common.block.plastic.BlockPlasticFenceGate;
import mekanism.additions.common.block.plastic.BlockPlasticRoad;
import mekanism.additions.common.block.plastic.BlockPlasticSlab;
import mekanism.additions.common.block.plastic.BlockPlasticStairs;
import mekanism.additions.common.block.plastic.BlockPlasticTransparent;
import mekanism.additions.common.block.plastic.BlockPlasticTransparentSlab;
import mekanism.additions.common.block.plastic.BlockPlasticTransparentStairs;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.text.EnumColor;
import mekanism.common.block.interfaces.IColoredBlock;
import mekanism.common.item.block.ItemBlockColoredName;
import mekanism.common.registration.impl.BlockDeferredRegister;
import mekanism.common.registration.impl.BlockRegistryObject;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;

public class AdditionsBlocks {

    private AdditionsBlocks() {
    }

    public static final BlockDeferredRegister BLOCKS = new BlockDeferredRegister(MekanismAdditions.MODID);

    public static final BlockRegistryObject<BlockObsidianTNT, BlockItem> OBSIDIAN_TNT = BLOCKS.register("obsidian_tnt", BlockObsidianTNT::new);

    public static final BlockRegistryObject<BlockGlowPanel, ItemBlockColoredName> BLACK_GLOW_PANEL = registerGlowPanel(EnumColor.BLACK);
    public static final BlockRegistryObject<BlockGlowPanel, ItemBlockColoredName> RED_GLOW_PANEL = registerGlowPanel(EnumColor.RED);
    public static final BlockRegistryObject<BlockGlowPanel, ItemBlockColoredName> GREEN_GLOW_PANEL = registerGlowPanel(EnumColor.DARK_GREEN);
    public static final BlockRegistryObject<BlockGlowPanel, ItemBlockColoredName> BROWN_GLOW_PANEL = registerGlowPanel(EnumColor.BROWN);
    public static final BlockRegistryObject<BlockGlowPanel, ItemBlockColoredName> BLUE_GLOW_PANEL = registerGlowPanel(EnumColor.DARK_BLUE);
    public static final BlockRegistryObject<BlockGlowPanel, ItemBlockColoredName> PURPLE_GLOW_PANEL = registerGlowPanel(EnumColor.PURPLE);
    public static final BlockRegistryObject<BlockGlowPanel, ItemBlockColoredName> CYAN_GLOW_PANEL = registerGlowPanel(EnumColor.DARK_AQUA);
    public static final BlockRegistryObject<BlockGlowPanel, ItemBlockColoredName> LIGHT_GRAY_GLOW_PANEL = registerGlowPanel(EnumColor.GRAY);
    public static final BlockRegistryObject<BlockGlowPanel, ItemBlockColoredName> GRAY_GLOW_PANEL = registerGlowPanel(EnumColor.DARK_GRAY);
    public static final BlockRegistryObject<BlockGlowPanel, ItemBlockColoredName> PINK_GLOW_PANEL = registerGlowPanel(EnumColor.BRIGHT_PINK);
    public static final BlockRegistryObject<BlockGlowPanel, ItemBlockColoredName> LIME_GLOW_PANEL = registerGlowPanel(EnumColor.BRIGHT_GREEN);
    public static final BlockRegistryObject<BlockGlowPanel, ItemBlockColoredName> YELLOW_GLOW_PANEL = registerGlowPanel(EnumColor.YELLOW);
    public static final BlockRegistryObject<BlockGlowPanel, ItemBlockColoredName> LIGHT_BLUE_GLOW_PANEL = registerGlowPanel(EnumColor.INDIGO);
    public static final BlockRegistryObject<BlockGlowPanel, ItemBlockColoredName> MAGENTA_GLOW_PANEL = registerGlowPanel(EnumColor.PINK);
    public static final BlockRegistryObject<BlockGlowPanel, ItemBlockColoredName> ORANGE_GLOW_PANEL = registerGlowPanel(EnumColor.ORANGE);
    public static final BlockRegistryObject<BlockGlowPanel, ItemBlockColoredName> WHITE_GLOW_PANEL = registerGlowPanel(EnumColor.WHITE);

    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> BLACK_PLASTIC_BLOCK = registerPlastic(EnumColor.BLACK);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> RED_PLASTIC_BLOCK = registerPlastic(EnumColor.RED);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> GREEN_PLASTIC_BLOCK = registerPlastic(EnumColor.DARK_GREEN);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> BROWN_PLASTIC_BLOCK = registerPlastic(EnumColor.BROWN);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> BLUE_PLASTIC_BLOCK = registerPlastic(EnumColor.DARK_BLUE);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> PURPLE_PLASTIC_BLOCK = registerPlastic(EnumColor.PURPLE);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> CYAN_PLASTIC_BLOCK = registerPlastic(EnumColor.DARK_AQUA);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> LIGHT_GRAY_PLASTIC_BLOCK = registerPlastic(EnumColor.GRAY);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> GRAY_PLASTIC_BLOCK = registerPlastic(EnumColor.DARK_GRAY);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> PINK_PLASTIC_BLOCK = registerPlastic(EnumColor.BRIGHT_PINK);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> LIME_PLASTIC_BLOCK = registerPlastic(EnumColor.BRIGHT_GREEN);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> YELLOW_PLASTIC_BLOCK = registerPlastic(EnumColor.YELLOW);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> LIGHT_BLUE_PLASTIC_BLOCK = registerPlastic(EnumColor.INDIGO);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> MAGENTA_PLASTIC_BLOCK = registerPlastic(EnumColor.PINK);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> ORANGE_PLASTIC_BLOCK = registerPlastic(EnumColor.ORANGE);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> WHITE_PLASTIC_BLOCK = registerPlastic(EnumColor.WHITE);

    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> BLACK_SLICK_PLASTIC_BLOCK = registerSlickPlastic(EnumColor.BLACK);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> RED_SLICK_PLASTIC_BLOCK = registerSlickPlastic(EnumColor.RED);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> GREEN_SLICK_PLASTIC_BLOCK = registerSlickPlastic(EnumColor.DARK_GREEN);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> BROWN_SLICK_PLASTIC_BLOCK = registerSlickPlastic(EnumColor.BROWN);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> BLUE_SLICK_PLASTIC_BLOCK = registerSlickPlastic(EnumColor.DARK_BLUE);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> PURPLE_SLICK_PLASTIC_BLOCK = registerSlickPlastic(EnumColor.PURPLE);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> CYAN_SLICK_PLASTIC_BLOCK = registerSlickPlastic(EnumColor.DARK_AQUA);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> LIGHT_GRAY_SLICK_PLASTIC_BLOCK = registerSlickPlastic(EnumColor.GRAY);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> GRAY_SLICK_PLASTIC_BLOCK = registerSlickPlastic(EnumColor.DARK_GRAY);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> PINK_SLICK_PLASTIC_BLOCK = registerSlickPlastic(EnumColor.BRIGHT_PINK);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> LIME_SLICK_PLASTIC_BLOCK = registerSlickPlastic(EnumColor.BRIGHT_GREEN);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> YELLOW_SLICK_PLASTIC_BLOCK = registerSlickPlastic(EnumColor.YELLOW);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> LIGHT_BLUE_SLICK_PLASTIC_BLOCK = registerSlickPlastic(EnumColor.INDIGO);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> MAGENTA_SLICK_PLASTIC_BLOCK = registerSlickPlastic(EnumColor.PINK);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> ORANGE_SLICK_PLASTIC_BLOCK = registerSlickPlastic(EnumColor.ORANGE);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> WHITE_SLICK_PLASTIC_BLOCK = registerSlickPlastic(EnumColor.WHITE);

    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> BLACK_PLASTIC_GLOW_BLOCK = registerGlowPlastic(EnumColor.BLACK);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> RED_PLASTIC_GLOW_BLOCK = registerGlowPlastic(EnumColor.RED);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> GREEN_PLASTIC_GLOW_BLOCK = registerGlowPlastic(EnumColor.DARK_GREEN);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> BROWN_PLASTIC_GLOW_BLOCK = registerGlowPlastic(EnumColor.BROWN);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> BLUE_PLASTIC_GLOW_BLOCK = registerGlowPlastic(EnumColor.DARK_BLUE);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> PURPLE_PLASTIC_GLOW_BLOCK = registerGlowPlastic(EnumColor.PURPLE);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> CYAN_PLASTIC_GLOW_BLOCK = registerGlowPlastic(EnumColor.DARK_AQUA);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> LIGHT_GRAY_PLASTIC_GLOW_BLOCK = registerGlowPlastic(EnumColor.GRAY);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> GRAY_PLASTIC_GLOW_BLOCK = registerGlowPlastic(EnumColor.DARK_GRAY);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> PINK_PLASTIC_GLOW_BLOCK = registerGlowPlastic(EnumColor.BRIGHT_PINK);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> LIME_PLASTIC_GLOW_BLOCK = registerGlowPlastic(EnumColor.BRIGHT_GREEN);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> YELLOW_PLASTIC_GLOW_BLOCK = registerGlowPlastic(EnumColor.YELLOW);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> LIGHT_BLUE_PLASTIC_GLOW_BLOCK = registerGlowPlastic(EnumColor.INDIGO);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> MAGENTA_PLASTIC_GLOW_BLOCK = registerGlowPlastic(EnumColor.PINK);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> ORANGE_PLASTIC_GLOW_BLOCK = registerGlowPlastic(EnumColor.ORANGE);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> WHITE_PLASTIC_GLOW_BLOCK = registerGlowPlastic(EnumColor.WHITE);

    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> BLACK_REINFORCED_PLASTIC_BLOCK = registerReinforcedPlastic(EnumColor.BLACK);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> RED_REINFORCED_PLASTIC_BLOCK = registerReinforcedPlastic(EnumColor.RED);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> GREEN_REINFORCED_PLASTIC_BLOCK = registerReinforcedPlastic(EnumColor.DARK_GREEN);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> BROWN_REINFORCED_PLASTIC_BLOCK = registerReinforcedPlastic(EnumColor.BROWN);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> BLUE_REINFORCED_PLASTIC_BLOCK = registerReinforcedPlastic(EnumColor.DARK_BLUE);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> PURPLE_REINFORCED_PLASTIC_BLOCK = registerReinforcedPlastic(EnumColor.PURPLE);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> CYAN_REINFORCED_PLASTIC_BLOCK = registerReinforcedPlastic(EnumColor.DARK_AQUA);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> LIGHT_GRAY_REINFORCED_PLASTIC_BLOCK = registerReinforcedPlastic(EnumColor.GRAY);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> GRAY_REINFORCED_PLASTIC_BLOCK = registerReinforcedPlastic(EnumColor.DARK_GRAY);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> PINK_REINFORCED_PLASTIC_BLOCK = registerReinforcedPlastic(EnumColor.BRIGHT_PINK);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> LIME_REINFORCED_PLASTIC_BLOCK = registerReinforcedPlastic(EnumColor.BRIGHT_GREEN);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> YELLOW_REINFORCED_PLASTIC_BLOCK = registerReinforcedPlastic(EnumColor.YELLOW);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> LIGHT_BLUE_REINFORCED_PLASTIC_BLOCK = registerReinforcedPlastic(EnumColor.INDIGO);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> MAGENTA_REINFORCED_PLASTIC_BLOCK = registerReinforcedPlastic(EnumColor.PINK);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> ORANGE_REINFORCED_PLASTIC_BLOCK = registerReinforcedPlastic(EnumColor.ORANGE);
    public static final BlockRegistryObject<BlockPlastic, ItemBlockColoredName> WHITE_REINFORCED_PLASTIC_BLOCK = registerReinforcedPlastic(EnumColor.WHITE);

    public static final BlockRegistryObject<BlockPlasticRoad, ItemBlockColoredName> BLACK_PLASTIC_ROAD = registerPlasticRoad(EnumColor.BLACK);
    public static final BlockRegistryObject<BlockPlasticRoad, ItemBlockColoredName> RED_PLASTIC_ROAD = registerPlasticRoad(EnumColor.RED);
    public static final BlockRegistryObject<BlockPlasticRoad, ItemBlockColoredName> GREEN_PLASTIC_ROAD = registerPlasticRoad(EnumColor.DARK_GREEN);
    public static final BlockRegistryObject<BlockPlasticRoad, ItemBlockColoredName> BROWN_PLASTIC_ROAD = registerPlasticRoad(EnumColor.BROWN);
    public static final BlockRegistryObject<BlockPlasticRoad, ItemBlockColoredName> BLUE_PLASTIC_ROAD = registerPlasticRoad(EnumColor.DARK_BLUE);
    public static final BlockRegistryObject<BlockPlasticRoad, ItemBlockColoredName> PURPLE_PLASTIC_ROAD = registerPlasticRoad(EnumColor.PURPLE);
    public static final BlockRegistryObject<BlockPlasticRoad, ItemBlockColoredName> CYAN_PLASTIC_ROAD = registerPlasticRoad(EnumColor.DARK_AQUA);
    public static final BlockRegistryObject<BlockPlasticRoad, ItemBlockColoredName> LIGHT_GRAY_PLASTIC_ROAD = registerPlasticRoad(EnumColor.GRAY);
    public static final BlockRegistryObject<BlockPlasticRoad, ItemBlockColoredName> GRAY_PLASTIC_ROAD = registerPlasticRoad(EnumColor.DARK_GRAY);
    public static final BlockRegistryObject<BlockPlasticRoad, ItemBlockColoredName> PINK_PLASTIC_ROAD = registerPlasticRoad(EnumColor.BRIGHT_PINK);
    public static final BlockRegistryObject<BlockPlasticRoad, ItemBlockColoredName> LIME_PLASTIC_ROAD = registerPlasticRoad(EnumColor.BRIGHT_GREEN);
    public static final BlockRegistryObject<BlockPlasticRoad, ItemBlockColoredName> YELLOW_PLASTIC_ROAD = registerPlasticRoad(EnumColor.YELLOW);
    public static final BlockRegistryObject<BlockPlasticRoad, ItemBlockColoredName> LIGHT_BLUE_PLASTIC_ROAD = registerPlasticRoad(EnumColor.INDIGO);
    public static final BlockRegistryObject<BlockPlasticRoad, ItemBlockColoredName> MAGENTA_PLASTIC_ROAD = registerPlasticRoad(EnumColor.PINK);
    public static final BlockRegistryObject<BlockPlasticRoad, ItemBlockColoredName> ORANGE_PLASTIC_ROAD = registerPlasticRoad(EnumColor.ORANGE);
    public static final BlockRegistryObject<BlockPlasticRoad, ItemBlockColoredName> WHITE_PLASTIC_ROAD = registerPlasticRoad(EnumColor.WHITE);

    public static final BlockRegistryObject<BlockPlasticTransparent, ItemBlockColoredName> BLACK_PLASTIC_TRANSPARENT_BLOCK = registerTransparentPlastic(EnumColor.BLACK);
    public static final BlockRegistryObject<BlockPlasticTransparent, ItemBlockColoredName> RED_PLASTIC_TRANSPARENT_BLOCK = registerTransparentPlastic(EnumColor.RED);
    public static final BlockRegistryObject<BlockPlasticTransparent, ItemBlockColoredName> GREEN_PLASTIC_TRANSPARENT_BLOCK = registerTransparentPlastic(EnumColor.DARK_GREEN);
    public static final BlockRegistryObject<BlockPlasticTransparent, ItemBlockColoredName> BROWN_PLASTIC_TRANSPARENT_BLOCK = registerTransparentPlastic(EnumColor.BROWN);
    public static final BlockRegistryObject<BlockPlasticTransparent, ItemBlockColoredName> BLUE_PLASTIC_TRANSPARENT_BLOCK = registerTransparentPlastic(EnumColor.DARK_BLUE);
    public static final BlockRegistryObject<BlockPlasticTransparent, ItemBlockColoredName> PURPLE_PLASTIC_TRANSPARENT_BLOCK = registerTransparentPlastic(EnumColor.PURPLE);
    public static final BlockRegistryObject<BlockPlasticTransparent, ItemBlockColoredName> CYAN_PLASTIC_TRANSPARENT_BLOCK = registerTransparentPlastic(EnumColor.DARK_AQUA);
    public static final BlockRegistryObject<BlockPlasticTransparent, ItemBlockColoredName> LIGHT_GRAY_PLASTIC_TRANSPARENT_BLOCK = registerTransparentPlastic(EnumColor.GRAY);
    public static final BlockRegistryObject<BlockPlasticTransparent, ItemBlockColoredName> GRAY_PLASTIC_TRANSPARENT_BLOCK = registerTransparentPlastic(EnumColor.DARK_GRAY);
    public static final BlockRegistryObject<BlockPlasticTransparent, ItemBlockColoredName> PINK_PLASTIC_TRANSPARENT_BLOCK = registerTransparentPlastic(EnumColor.BRIGHT_PINK);
    public static final BlockRegistryObject<BlockPlasticTransparent, ItemBlockColoredName> LIME_PLASTIC_TRANSPARENT_BLOCK = registerTransparentPlastic(EnumColor.BRIGHT_GREEN);
    public static final BlockRegistryObject<BlockPlasticTransparent, ItemBlockColoredName> YELLOW_PLASTIC_TRANSPARENT_BLOCK = registerTransparentPlastic(EnumColor.YELLOW);
    public static final BlockRegistryObject<BlockPlasticTransparent, ItemBlockColoredName> LIGHT_BLUE_PLASTIC_TRANSPARENT_BLOCK = registerTransparentPlastic(EnumColor.INDIGO);
    public static final BlockRegistryObject<BlockPlasticTransparent, ItemBlockColoredName> MAGENTA_PLASTIC_TRANSPARENT_BLOCK = registerTransparentPlastic(EnumColor.PINK);
    public static final BlockRegistryObject<BlockPlasticTransparent, ItemBlockColoredName> ORANGE_PLASTIC_TRANSPARENT_BLOCK = registerTransparentPlastic(EnumColor.ORANGE);
    public static final BlockRegistryObject<BlockPlasticTransparent, ItemBlockColoredName> WHITE_PLASTIC_TRANSPARENT_BLOCK = registerTransparentPlastic(EnumColor.WHITE);

    public static final BlockRegistryObject<BlockPlasticStairs, ItemBlockColoredName> BLACK_PLASTIC_STAIRS = registerPlasticStairs(BLACK_PLASTIC_BLOCK, EnumColor.BLACK);
    public static final BlockRegistryObject<BlockPlasticStairs, ItemBlockColoredName> RED_PLASTIC_STAIRS = registerPlasticStairs(RED_PLASTIC_BLOCK, EnumColor.RED);
    public static final BlockRegistryObject<BlockPlasticStairs, ItemBlockColoredName> GREEN_PLASTIC_STAIRS = registerPlasticStairs(GREEN_PLASTIC_BLOCK, EnumColor.DARK_GREEN);
    public static final BlockRegistryObject<BlockPlasticStairs, ItemBlockColoredName> BROWN_PLASTIC_STAIRS = registerPlasticStairs(BROWN_PLASTIC_BLOCK, EnumColor.BROWN);
    public static final BlockRegistryObject<BlockPlasticStairs, ItemBlockColoredName> BLUE_PLASTIC_STAIRS = registerPlasticStairs(BLUE_PLASTIC_BLOCK, EnumColor.DARK_BLUE);
    public static final BlockRegistryObject<BlockPlasticStairs, ItemBlockColoredName> PURPLE_PLASTIC_STAIRS = registerPlasticStairs(PURPLE_PLASTIC_BLOCK, EnumColor.PURPLE);
    public static final BlockRegistryObject<BlockPlasticStairs, ItemBlockColoredName> CYAN_PLASTIC_STAIRS = registerPlasticStairs(CYAN_PLASTIC_BLOCK, EnumColor.DARK_AQUA);
    public static final BlockRegistryObject<BlockPlasticStairs, ItemBlockColoredName> LIGHT_GRAY_PLASTIC_STAIRS = registerPlasticStairs(LIGHT_GRAY_PLASTIC_BLOCK, EnumColor.GRAY);
    public static final BlockRegistryObject<BlockPlasticStairs, ItemBlockColoredName> GRAY_PLASTIC_STAIRS = registerPlasticStairs(GRAY_PLASTIC_BLOCK, EnumColor.DARK_GRAY);
    public static final BlockRegistryObject<BlockPlasticStairs, ItemBlockColoredName> PINK_PLASTIC_STAIRS = registerPlasticStairs(PINK_PLASTIC_BLOCK, EnumColor.BRIGHT_PINK);
    public static final BlockRegistryObject<BlockPlasticStairs, ItemBlockColoredName> LIME_PLASTIC_STAIRS = registerPlasticStairs(LIME_PLASTIC_BLOCK, EnumColor.BRIGHT_GREEN);
    public static final BlockRegistryObject<BlockPlasticStairs, ItemBlockColoredName> YELLOW_PLASTIC_STAIRS = registerPlasticStairs(YELLOW_PLASTIC_BLOCK, EnumColor.YELLOW);
    public static final BlockRegistryObject<BlockPlasticStairs, ItemBlockColoredName> LIGHT_BLUE_PLASTIC_STAIRS = registerPlasticStairs(LIGHT_BLUE_PLASTIC_BLOCK, EnumColor.INDIGO);
    public static final BlockRegistryObject<BlockPlasticStairs, ItemBlockColoredName> MAGENTA_PLASTIC_STAIRS = registerPlasticStairs(MAGENTA_PLASTIC_BLOCK, EnumColor.PINK);
    public static final BlockRegistryObject<BlockPlasticStairs, ItemBlockColoredName> ORANGE_PLASTIC_STAIRS = registerPlasticStairs(ORANGE_PLASTIC_BLOCK, EnumColor.ORANGE);
    public static final BlockRegistryObject<BlockPlasticStairs, ItemBlockColoredName> WHITE_PLASTIC_STAIRS = registerPlasticStairs(WHITE_PLASTIC_BLOCK, EnumColor.WHITE);

    public static final BlockRegistryObject<BlockPlasticSlab, ItemBlockColoredName> BLACK_PLASTIC_SLAB = registerPlasticSlab(EnumColor.BLACK);
    public static final BlockRegistryObject<BlockPlasticSlab, ItemBlockColoredName> RED_PLASTIC_SLAB = registerPlasticSlab(EnumColor.RED);
    public static final BlockRegistryObject<BlockPlasticSlab, ItemBlockColoredName> GREEN_PLASTIC_SLAB = registerPlasticSlab(EnumColor.DARK_GREEN);
    public static final BlockRegistryObject<BlockPlasticSlab, ItemBlockColoredName> BROWN_PLASTIC_SLAB = registerPlasticSlab(EnumColor.BROWN);
    public static final BlockRegistryObject<BlockPlasticSlab, ItemBlockColoredName> BLUE_PLASTIC_SLAB = registerPlasticSlab(EnumColor.DARK_BLUE);
    public static final BlockRegistryObject<BlockPlasticSlab, ItemBlockColoredName> PURPLE_PLASTIC_SLAB = registerPlasticSlab(EnumColor.PURPLE);
    public static final BlockRegistryObject<BlockPlasticSlab, ItemBlockColoredName> CYAN_PLASTIC_SLAB = registerPlasticSlab(EnumColor.DARK_AQUA);
    public static final BlockRegistryObject<BlockPlasticSlab, ItemBlockColoredName> LIGHT_GRAY_PLASTIC_SLAB = registerPlasticSlab(EnumColor.GRAY);
    public static final BlockRegistryObject<BlockPlasticSlab, ItemBlockColoredName> GRAY_PLASTIC_SLAB = registerPlasticSlab(EnumColor.DARK_GRAY);
    public static final BlockRegistryObject<BlockPlasticSlab, ItemBlockColoredName> PINK_PLASTIC_SLAB = registerPlasticSlab(EnumColor.BRIGHT_PINK);
    public static final BlockRegistryObject<BlockPlasticSlab, ItemBlockColoredName> LIME_PLASTIC_SLAB = registerPlasticSlab(EnumColor.BRIGHT_GREEN);
    public static final BlockRegistryObject<BlockPlasticSlab, ItemBlockColoredName> YELLOW_PLASTIC_SLAB = registerPlasticSlab(EnumColor.YELLOW);
    public static final BlockRegistryObject<BlockPlasticSlab, ItemBlockColoredName> LIGHT_BLUE_PLASTIC_SLAB = registerPlasticSlab(EnumColor.INDIGO);
    public static final BlockRegistryObject<BlockPlasticSlab, ItemBlockColoredName> MAGENTA_PLASTIC_SLAB = registerPlasticSlab(EnumColor.PINK);
    public static final BlockRegistryObject<BlockPlasticSlab, ItemBlockColoredName> ORANGE_PLASTIC_SLAB = registerPlasticSlab(EnumColor.ORANGE);
    public static final BlockRegistryObject<BlockPlasticSlab, ItemBlockColoredName> WHITE_PLASTIC_SLAB = registerPlasticSlab(EnumColor.WHITE);

    public static final BlockRegistryObject<BlockPlasticFence, ItemBlockColoredName> BLACK_PLASTIC_FENCE = registerPlasticFence(EnumColor.BLACK);
    public static final BlockRegistryObject<BlockPlasticFence, ItemBlockColoredName> RED_PLASTIC_FENCE = registerPlasticFence(EnumColor.RED);
    public static final BlockRegistryObject<BlockPlasticFence, ItemBlockColoredName> GREEN_PLASTIC_FENCE = registerPlasticFence(EnumColor.DARK_GREEN);
    public static final BlockRegistryObject<BlockPlasticFence, ItemBlockColoredName> BROWN_PLASTIC_FENCE = registerPlasticFence(EnumColor.BROWN);
    public static final BlockRegistryObject<BlockPlasticFence, ItemBlockColoredName> BLUE_PLASTIC_FENCE = registerPlasticFence(EnumColor.DARK_BLUE);
    public static final BlockRegistryObject<BlockPlasticFence, ItemBlockColoredName> PURPLE_PLASTIC_FENCE = registerPlasticFence(EnumColor.PURPLE);
    public static final BlockRegistryObject<BlockPlasticFence, ItemBlockColoredName> CYAN_PLASTIC_FENCE = registerPlasticFence(EnumColor.DARK_AQUA);
    public static final BlockRegistryObject<BlockPlasticFence, ItemBlockColoredName> LIGHT_GRAY_PLASTIC_FENCE = registerPlasticFence(EnumColor.GRAY);
    public static final BlockRegistryObject<BlockPlasticFence, ItemBlockColoredName> GRAY_PLASTIC_FENCE = registerPlasticFence(EnumColor.DARK_GRAY);
    public static final BlockRegistryObject<BlockPlasticFence, ItemBlockColoredName> PINK_PLASTIC_FENCE = registerPlasticFence(EnumColor.BRIGHT_PINK);
    public static final BlockRegistryObject<BlockPlasticFence, ItemBlockColoredName> LIME_PLASTIC_FENCE = registerPlasticFence(EnumColor.BRIGHT_GREEN);
    public static final BlockRegistryObject<BlockPlasticFence, ItemBlockColoredName> YELLOW_PLASTIC_FENCE = registerPlasticFence(EnumColor.YELLOW);
    public static final BlockRegistryObject<BlockPlasticFence, ItemBlockColoredName> LIGHT_BLUE_PLASTIC_FENCE = registerPlasticFence(EnumColor.INDIGO);
    public static final BlockRegistryObject<BlockPlasticFence, ItemBlockColoredName> MAGENTA_PLASTIC_FENCE = registerPlasticFence(EnumColor.PINK);
    public static final BlockRegistryObject<BlockPlasticFence, ItemBlockColoredName> ORANGE_PLASTIC_FENCE = registerPlasticFence(EnumColor.ORANGE);
    public static final BlockRegistryObject<BlockPlasticFence, ItemBlockColoredName> WHITE_PLASTIC_FENCE = registerPlasticFence(EnumColor.WHITE);

    public static final BlockRegistryObject<BlockPlasticFenceGate, ItemBlockColoredName> BLACK_PLASTIC_FENCE_GATE = registerPlasticFenceGate(EnumColor.BLACK);
    public static final BlockRegistryObject<BlockPlasticFenceGate, ItemBlockColoredName> RED_PLASTIC_FENCE_GATE = registerPlasticFenceGate(EnumColor.RED);
    public static final BlockRegistryObject<BlockPlasticFenceGate, ItemBlockColoredName> GREEN_PLASTIC_FENCE_GATE = registerPlasticFenceGate(EnumColor.DARK_GREEN);
    public static final BlockRegistryObject<BlockPlasticFenceGate, ItemBlockColoredName> BROWN_PLASTIC_FENCE_GATE = registerPlasticFenceGate(EnumColor.BROWN);
    public static final BlockRegistryObject<BlockPlasticFenceGate, ItemBlockColoredName> BLUE_PLASTIC_FENCE_GATE = registerPlasticFenceGate(EnumColor.DARK_BLUE);
    public static final BlockRegistryObject<BlockPlasticFenceGate, ItemBlockColoredName> PURPLE_PLASTIC_FENCE_GATE = registerPlasticFenceGate(EnumColor.PURPLE);
    public static final BlockRegistryObject<BlockPlasticFenceGate, ItemBlockColoredName> CYAN_PLASTIC_FENCE_GATE = registerPlasticFenceGate(EnumColor.DARK_AQUA);
    public static final BlockRegistryObject<BlockPlasticFenceGate, ItemBlockColoredName> LIGHT_GRAY_PLASTIC_FENCE_GATE = registerPlasticFenceGate(EnumColor.GRAY);
    public static final BlockRegistryObject<BlockPlasticFenceGate, ItemBlockColoredName> GRAY_PLASTIC_FENCE_GATE = registerPlasticFenceGate(EnumColor.DARK_GRAY);
    public static final BlockRegistryObject<BlockPlasticFenceGate, ItemBlockColoredName> PINK_PLASTIC_FENCE_GATE = registerPlasticFenceGate(EnumColor.BRIGHT_PINK);
    public static final BlockRegistryObject<BlockPlasticFenceGate, ItemBlockColoredName> LIME_PLASTIC_FENCE_GATE = registerPlasticFenceGate(EnumColor.BRIGHT_GREEN);
    public static final BlockRegistryObject<BlockPlasticFenceGate, ItemBlockColoredName> YELLOW_PLASTIC_FENCE_GATE = registerPlasticFenceGate(EnumColor.YELLOW);
    public static final BlockRegistryObject<BlockPlasticFenceGate, ItemBlockColoredName> LIGHT_BLUE_PLASTIC_FENCE_GATE = registerPlasticFenceGate(EnumColor.INDIGO);
    public static final BlockRegistryObject<BlockPlasticFenceGate, ItemBlockColoredName> MAGENTA_PLASTIC_FENCE_GATE = registerPlasticFenceGate(EnumColor.PINK);
    public static final BlockRegistryObject<BlockPlasticFenceGate, ItemBlockColoredName> ORANGE_PLASTIC_FENCE_GATE = registerPlasticFenceGate(EnumColor.ORANGE);
    public static final BlockRegistryObject<BlockPlasticFenceGate, ItemBlockColoredName> WHITE_PLASTIC_FENCE_GATE = registerPlasticFenceGate(EnumColor.WHITE);

    public static final BlockRegistryObject<BlockPlasticStairs, ItemBlockColoredName> BLACK_PLASTIC_GLOW_STAIRS = registerGlowPlasticStairs(BLACK_PLASTIC_GLOW_BLOCK, EnumColor.BLACK);
    public static final BlockRegistryObject<BlockPlasticStairs, ItemBlockColoredName> RED_PLASTIC_GLOW_STAIRS = registerGlowPlasticStairs(RED_PLASTIC_GLOW_BLOCK, EnumColor.RED);
    public static final BlockRegistryObject<BlockPlasticStairs, ItemBlockColoredName> GREEN_PLASTIC_GLOW_STAIRS = registerGlowPlasticStairs(GREEN_PLASTIC_GLOW_BLOCK, EnumColor.DARK_GREEN);
    public static final BlockRegistryObject<BlockPlasticStairs, ItemBlockColoredName> BROWN_PLASTIC_GLOW_STAIRS = registerGlowPlasticStairs(BROWN_PLASTIC_GLOW_BLOCK, EnumColor.BROWN);
    public static final BlockRegistryObject<BlockPlasticStairs, ItemBlockColoredName> BLUE_PLASTIC_GLOW_STAIRS = registerGlowPlasticStairs(BLUE_PLASTIC_GLOW_BLOCK, EnumColor.DARK_BLUE);
    public static final BlockRegistryObject<BlockPlasticStairs, ItemBlockColoredName> PURPLE_PLASTIC_GLOW_STAIRS = registerGlowPlasticStairs(PURPLE_PLASTIC_GLOW_BLOCK, EnumColor.PURPLE);
    public static final BlockRegistryObject<BlockPlasticStairs, ItemBlockColoredName> CYAN_PLASTIC_GLOW_STAIRS = registerGlowPlasticStairs(CYAN_PLASTIC_GLOW_BLOCK, EnumColor.DARK_AQUA);
    public static final BlockRegistryObject<BlockPlasticStairs, ItemBlockColoredName> LIGHT_GRAY_PLASTIC_GLOW_STAIRS = registerGlowPlasticStairs(LIGHT_GRAY_PLASTIC_GLOW_BLOCK, EnumColor.GRAY);
    public static final BlockRegistryObject<BlockPlasticStairs, ItemBlockColoredName> GRAY_PLASTIC_GLOW_STAIRS = registerGlowPlasticStairs(GRAY_PLASTIC_GLOW_BLOCK, EnumColor.DARK_GRAY);
    public static final BlockRegistryObject<BlockPlasticStairs, ItemBlockColoredName> PINK_PLASTIC_GLOW_STAIRS = registerGlowPlasticStairs(PINK_PLASTIC_GLOW_BLOCK, EnumColor.BRIGHT_PINK);
    public static final BlockRegistryObject<BlockPlasticStairs, ItemBlockColoredName> LIME_PLASTIC_GLOW_STAIRS = registerGlowPlasticStairs(LIME_PLASTIC_GLOW_BLOCK, EnumColor.BRIGHT_GREEN);
    public static final BlockRegistryObject<BlockPlasticStairs, ItemBlockColoredName> YELLOW_PLASTIC_GLOW_STAIRS = registerGlowPlasticStairs(YELLOW_PLASTIC_GLOW_BLOCK, EnumColor.YELLOW);
    public static final BlockRegistryObject<BlockPlasticStairs, ItemBlockColoredName> LIGHT_BLUE_PLASTIC_GLOW_STAIRS = registerGlowPlasticStairs(LIGHT_BLUE_PLASTIC_GLOW_BLOCK, EnumColor.INDIGO);
    public static final BlockRegistryObject<BlockPlasticStairs, ItemBlockColoredName> MAGENTA_PLASTIC_GLOW_STAIRS = registerGlowPlasticStairs(MAGENTA_PLASTIC_GLOW_BLOCK, EnumColor.PINK);
    public static final BlockRegistryObject<BlockPlasticStairs, ItemBlockColoredName> ORANGE_PLASTIC_GLOW_STAIRS = registerGlowPlasticStairs(ORANGE_PLASTIC_GLOW_BLOCK, EnumColor.ORANGE);
    public static final BlockRegistryObject<BlockPlasticStairs, ItemBlockColoredName> WHITE_PLASTIC_GLOW_STAIRS = registerGlowPlasticStairs(WHITE_PLASTIC_GLOW_BLOCK, EnumColor.WHITE);

    public static final BlockRegistryObject<BlockPlasticSlab, ItemBlockColoredName> BLACK_PLASTIC_GLOW_SLAB = registerGlowPlasticSlab(EnumColor.BLACK);
    public static final BlockRegistryObject<BlockPlasticSlab, ItemBlockColoredName> RED_PLASTIC_GLOW_SLAB = registerGlowPlasticSlab(EnumColor.RED);
    public static final BlockRegistryObject<BlockPlasticSlab, ItemBlockColoredName> GREEN_PLASTIC_GLOW_SLAB = registerGlowPlasticSlab(EnumColor.DARK_GREEN);
    public static final BlockRegistryObject<BlockPlasticSlab, ItemBlockColoredName> BROWN_PLASTIC_GLOW_SLAB = registerGlowPlasticSlab(EnumColor.BROWN);
    public static final BlockRegistryObject<BlockPlasticSlab, ItemBlockColoredName> BLUE_PLASTIC_GLOW_SLAB = registerGlowPlasticSlab(EnumColor.DARK_BLUE);
    public static final BlockRegistryObject<BlockPlasticSlab, ItemBlockColoredName> PURPLE_PLASTIC_GLOW_SLAB = registerGlowPlasticSlab(EnumColor.PURPLE);
    public static final BlockRegistryObject<BlockPlasticSlab, ItemBlockColoredName> CYAN_PLASTIC_GLOW_SLAB = registerGlowPlasticSlab(EnumColor.DARK_AQUA);
    public static final BlockRegistryObject<BlockPlasticSlab, ItemBlockColoredName> LIGHT_GRAY_PLASTIC_GLOW_SLAB = registerGlowPlasticSlab(EnumColor.GRAY);
    public static final BlockRegistryObject<BlockPlasticSlab, ItemBlockColoredName> GRAY_PLASTIC_GLOW_SLAB = registerGlowPlasticSlab(EnumColor.DARK_GRAY);
    public static final BlockRegistryObject<BlockPlasticSlab, ItemBlockColoredName> PINK_PLASTIC_GLOW_SLAB = registerGlowPlasticSlab(EnumColor.BRIGHT_PINK);
    public static final BlockRegistryObject<BlockPlasticSlab, ItemBlockColoredName> LIME_PLASTIC_GLOW_SLAB = registerGlowPlasticSlab(EnumColor.BRIGHT_GREEN);
    public static final BlockRegistryObject<BlockPlasticSlab, ItemBlockColoredName> YELLOW_PLASTIC_GLOW_SLAB = registerGlowPlasticSlab(EnumColor.YELLOW);
    public static final BlockRegistryObject<BlockPlasticSlab, ItemBlockColoredName> LIGHT_BLUE_PLASTIC_GLOW_SLAB = registerGlowPlasticSlab(EnumColor.INDIGO);
    public static final BlockRegistryObject<BlockPlasticSlab, ItemBlockColoredName> MAGENTA_PLASTIC_GLOW_SLAB = registerGlowPlasticSlab(EnumColor.PINK);
    public static final BlockRegistryObject<BlockPlasticSlab, ItemBlockColoredName> ORANGE_PLASTIC_GLOW_SLAB = registerGlowPlasticSlab(EnumColor.ORANGE);
    public static final BlockRegistryObject<BlockPlasticSlab, ItemBlockColoredName> WHITE_PLASTIC_GLOW_SLAB = registerGlowPlasticSlab(EnumColor.WHITE);

    public static final BlockRegistryObject<BlockPlasticTransparentStairs, ItemBlockColoredName> BLACK_PLASTIC_TRANSPARENT_STAIRS = registerTransparentPlasticStairs(BLACK_PLASTIC_TRANSPARENT_BLOCK, EnumColor.BLACK);
    public static final BlockRegistryObject<BlockPlasticTransparentStairs, ItemBlockColoredName> RED_PLASTIC_TRANSPARENT_STAIRS = registerTransparentPlasticStairs(RED_PLASTIC_TRANSPARENT_BLOCK, EnumColor.RED);
    public static final BlockRegistryObject<BlockPlasticTransparentStairs, ItemBlockColoredName> GREEN_PLASTIC_TRANSPARENT_STAIRS = registerTransparentPlasticStairs(GREEN_PLASTIC_TRANSPARENT_BLOCK, EnumColor.DARK_GREEN);
    public static final BlockRegistryObject<BlockPlasticTransparentStairs, ItemBlockColoredName> BROWN_PLASTIC_TRANSPARENT_STAIRS = registerTransparentPlasticStairs(BROWN_PLASTIC_TRANSPARENT_BLOCK, EnumColor.BROWN);
    public static final BlockRegistryObject<BlockPlasticTransparentStairs, ItemBlockColoredName> BLUE_PLASTIC_TRANSPARENT_STAIRS = registerTransparentPlasticStairs(BLUE_PLASTIC_TRANSPARENT_BLOCK, EnumColor.DARK_BLUE);
    public static final BlockRegistryObject<BlockPlasticTransparentStairs, ItemBlockColoredName> PURPLE_PLASTIC_TRANSPARENT_STAIRS = registerTransparentPlasticStairs(PURPLE_PLASTIC_TRANSPARENT_BLOCK, EnumColor.PURPLE);
    public static final BlockRegistryObject<BlockPlasticTransparentStairs, ItemBlockColoredName> CYAN_PLASTIC_TRANSPARENT_STAIRS = registerTransparentPlasticStairs(CYAN_PLASTIC_TRANSPARENT_BLOCK, EnumColor.DARK_AQUA);
    public static final BlockRegistryObject<BlockPlasticTransparentStairs, ItemBlockColoredName> LIGHT_GRAY_PLASTIC_TRANSPARENT_STAIRS = registerTransparentPlasticStairs(LIGHT_GRAY_PLASTIC_TRANSPARENT_BLOCK, EnumColor.GRAY);
    public static final BlockRegistryObject<BlockPlasticTransparentStairs, ItemBlockColoredName> GRAY_PLASTIC_TRANSPARENT_STAIRS = registerTransparentPlasticStairs(GRAY_PLASTIC_TRANSPARENT_BLOCK, EnumColor.DARK_GRAY);
    public static final BlockRegistryObject<BlockPlasticTransparentStairs, ItemBlockColoredName> PINK_PLASTIC_TRANSPARENT_STAIRS = registerTransparentPlasticStairs(PINK_PLASTIC_TRANSPARENT_BLOCK, EnumColor.BRIGHT_PINK);
    public static final BlockRegistryObject<BlockPlasticTransparentStairs, ItemBlockColoredName> LIME_PLASTIC_TRANSPARENT_STAIRS = registerTransparentPlasticStairs(LIME_PLASTIC_TRANSPARENT_BLOCK, EnumColor.BRIGHT_GREEN);
    public static final BlockRegistryObject<BlockPlasticTransparentStairs, ItemBlockColoredName> YELLOW_PLASTIC_TRANSPARENT_STAIRS = registerTransparentPlasticStairs(YELLOW_PLASTIC_TRANSPARENT_BLOCK, EnumColor.YELLOW);
    public static final BlockRegistryObject<BlockPlasticTransparentStairs, ItemBlockColoredName> LIGHT_BLUE_PLASTIC_TRANSPARENT_STAIRS = registerTransparentPlasticStairs(LIGHT_BLUE_PLASTIC_TRANSPARENT_BLOCK, EnumColor.INDIGO);
    public static final BlockRegistryObject<BlockPlasticTransparentStairs, ItemBlockColoredName> MAGENTA_PLASTIC_TRANSPARENT_STAIRS = registerTransparentPlasticStairs(MAGENTA_PLASTIC_TRANSPARENT_BLOCK, EnumColor.PINK);
    public static final BlockRegistryObject<BlockPlasticTransparentStairs, ItemBlockColoredName> ORANGE_PLASTIC_TRANSPARENT_STAIRS = registerTransparentPlasticStairs(ORANGE_PLASTIC_TRANSPARENT_BLOCK, EnumColor.ORANGE);
    public static final BlockRegistryObject<BlockPlasticTransparentStairs, ItemBlockColoredName> WHITE_PLASTIC_TRANSPARENT_STAIRS = registerTransparentPlasticStairs(WHITE_PLASTIC_TRANSPARENT_BLOCK, EnumColor.WHITE);

    public static final BlockRegistryObject<BlockPlasticTransparentSlab, ItemBlockColoredName> BLACK_PLASTIC_TRANSPARENT_SLAB = registerTransparentPlasticSlab(EnumColor.BLACK);
    public static final BlockRegistryObject<BlockPlasticTransparentSlab, ItemBlockColoredName> RED_PLASTIC_TRANSPARENT_SLAB = registerTransparentPlasticSlab(EnumColor.RED);
    public static final BlockRegistryObject<BlockPlasticTransparentSlab, ItemBlockColoredName> GREEN_PLASTIC_TRANSPARENT_SLAB = registerTransparentPlasticSlab(EnumColor.DARK_GREEN);
    public static final BlockRegistryObject<BlockPlasticTransparentSlab, ItemBlockColoredName> BROWN_PLASTIC_TRANSPARENT_SLAB = registerTransparentPlasticSlab(EnumColor.BROWN);
    public static final BlockRegistryObject<BlockPlasticTransparentSlab, ItemBlockColoredName> BLUE_PLASTIC_TRANSPARENT_SLAB = registerTransparentPlasticSlab(EnumColor.DARK_BLUE);
    public static final BlockRegistryObject<BlockPlasticTransparentSlab, ItemBlockColoredName> PURPLE_PLASTIC_TRANSPARENT_SLAB = registerTransparentPlasticSlab(EnumColor.PURPLE);
    public static final BlockRegistryObject<BlockPlasticTransparentSlab, ItemBlockColoredName> CYAN_PLASTIC_TRANSPARENT_SLAB = registerTransparentPlasticSlab(EnumColor.DARK_AQUA);
    public static final BlockRegistryObject<BlockPlasticTransparentSlab, ItemBlockColoredName> LIGHT_GRAY_PLASTIC_TRANSPARENT_SLAB = registerTransparentPlasticSlab(EnumColor.GRAY);
    public static final BlockRegistryObject<BlockPlasticTransparentSlab, ItemBlockColoredName> GRAY_PLASTIC_TRANSPARENT_SLAB = registerTransparentPlasticSlab(EnumColor.DARK_GRAY);
    public static final BlockRegistryObject<BlockPlasticTransparentSlab, ItemBlockColoredName> PINK_PLASTIC_TRANSPARENT_SLAB = registerTransparentPlasticSlab(EnumColor.BRIGHT_PINK);
    public static final BlockRegistryObject<BlockPlasticTransparentSlab, ItemBlockColoredName> LIME_PLASTIC_TRANSPARENT_SLAB = registerTransparentPlasticSlab(EnumColor.BRIGHT_GREEN);
    public static final BlockRegistryObject<BlockPlasticTransparentSlab, ItemBlockColoredName> YELLOW_PLASTIC_TRANSPARENT_SLAB = registerTransparentPlasticSlab(EnumColor.YELLOW);
    public static final BlockRegistryObject<BlockPlasticTransparentSlab, ItemBlockColoredName> LIGHT_BLUE_PLASTIC_TRANSPARENT_SLAB = registerTransparentPlasticSlab(EnumColor.INDIGO);
    public static final BlockRegistryObject<BlockPlasticTransparentSlab, ItemBlockColoredName> MAGENTA_PLASTIC_TRANSPARENT_SLAB = registerTransparentPlasticSlab(EnumColor.PINK);
    public static final BlockRegistryObject<BlockPlasticTransparentSlab, ItemBlockColoredName> ORANGE_PLASTIC_TRANSPARENT_SLAB = registerTransparentPlasticSlab(EnumColor.ORANGE);
    public static final BlockRegistryObject<BlockPlasticTransparentSlab, ItemBlockColoredName> WHITE_PLASTIC_TRANSPARENT_SLAB = registerTransparentPlasticSlab(EnumColor.WHITE);

    private static BlockRegistryObject<BlockGlowPanel, ItemBlockColoredName> registerGlowPanel(EnumColor color) {
        return registerColoredBlock(BlockGlowPanel::new, "_glow_panel", color);
    }

    private static BlockRegistryObject<BlockPlastic, ItemBlockColoredName> registerPlastic(EnumColor color, String blockTypeSuffix,
          UnaryOperator<AbstractBlock.Properties> propertyModifier) {
        return registerColoredBlock(c -> new BlockPlastic(c, propertyModifier), blockTypeSuffix, color);
    }

    private static BlockRegistryObject<BlockPlasticSlab, ItemBlockColoredName> registerPlasticSlab(EnumColor color, String blockTypeSuffix,
          UnaryOperator<AbstractBlock.Properties> propertyModifier) {
        return registerColoredBlock(c -> new BlockPlasticSlab(c, propertyModifier), blockTypeSuffix, color);
    }

    private static BlockRegistryObject<BlockPlasticStairs, ItemBlockColoredName> registerPlasticStairs(IBlockProvider baseBlock, EnumColor color, String blockTypeSuffix,
          UnaryOperator<AbstractBlock.Properties> propertyModifier) {
        return registerColoredBlock(c -> new BlockPlasticStairs(baseBlock, c, propertyModifier), blockTypeSuffix, color);
    }

    private static BlockRegistryObject<BlockPlastic, ItemBlockColoredName> registerPlastic(EnumColor color) {
        return registerPlastic(color, "_plastic", properties -> properties.hardnessAndResistance(5F, 10F));
    }

    private static BlockRegistryObject<BlockPlastic, ItemBlockColoredName> registerSlickPlastic(EnumColor color) {
        return registerPlastic(color, "_slick_plastic", properties -> properties.hardnessAndResistance(5F, 10F).slipperiness(0.98F));
    }

    private static BlockRegistryObject<BlockPlastic, ItemBlockColoredName> registerGlowPlastic(EnumColor color) {
        return registerPlastic(color, "_plastic_glow", properties -> properties.hardnessAndResistance(5F, 10F).setLightLevel(state -> 10));
    }

    private static BlockRegistryObject<BlockPlastic, ItemBlockColoredName> registerReinforcedPlastic(EnumColor color) {
        return registerPlastic(color, "_reinforced_plastic", properties -> properties.hardnessAndResistance(50F, 2_000F));
    }

    private static BlockRegistryObject<BlockPlasticRoad, ItemBlockColoredName> registerPlasticRoad(EnumColor color) {
        return registerColoredBlock(BlockPlasticRoad::new, "_plastic_road", color);
    }

    private static BlockRegistryObject<BlockPlasticTransparent, ItemBlockColoredName> registerTransparentPlastic(EnumColor color) {
        return registerColoredBlock(BlockPlasticTransparent::new, "_plastic_transparent", color);
    }

    private static BlockRegistryObject<BlockPlasticStairs, ItemBlockColoredName> registerPlasticStairs(IBlockProvider baseBlock, EnumColor color) {
        return registerPlasticStairs(baseBlock, color, "_plastic_stairs", UnaryOperator.identity());
    }

    private static BlockRegistryObject<BlockPlasticSlab, ItemBlockColoredName> registerPlasticSlab(EnumColor color) {
        return registerPlasticSlab(color, "_plastic_slab", UnaryOperator.identity());
    }

    private static BlockRegistryObject<BlockPlasticFence, ItemBlockColoredName> registerPlasticFence(EnumColor color) {
        return registerColoredBlock(BlockPlasticFence::new, "_plastic_fence", color);
    }

    private static BlockRegistryObject<BlockPlasticFenceGate, ItemBlockColoredName> registerPlasticFenceGate(EnumColor color) {
        return registerColoredBlock(BlockPlasticFenceGate::new, "_plastic_fence_gate", color);
    }

    private static BlockRegistryObject<BlockPlasticStairs, ItemBlockColoredName> registerGlowPlasticStairs(IBlockProvider baseBlock, EnumColor color) {
        return registerPlasticStairs(baseBlock, color, "_plastic_glow_stairs", properties -> properties.setLightLevel(state -> 10));
    }

    private static BlockRegistryObject<BlockPlasticSlab, ItemBlockColoredName> registerGlowPlasticSlab(EnumColor color) {
        return registerPlasticSlab(color, "_plastic_glow_slab", properties -> properties.setLightLevel(state -> 10));
    }

    private static BlockRegistryObject<BlockPlasticTransparentStairs, ItemBlockColoredName> registerTransparentPlasticStairs(IBlockProvider baseBlock, EnumColor color) {
        return BLOCKS.register(color.getRegistryPrefix() + "_plastic_transparent_stairs", () -> new BlockPlasticTransparentStairs(baseBlock, color), ItemBlockColoredName::new);
    }

    private static BlockRegistryObject<BlockPlasticTransparentSlab, ItemBlockColoredName> registerTransparentPlasticSlab(EnumColor color) {
        return registerColoredBlock(BlockPlasticTransparentSlab::new, "_plastic_transparent_slab", color);
    }

    private static <BLOCK extends Block & IColoredBlock> BlockRegistryObject<BLOCK, ItemBlockColoredName> registerColoredBlock(Function<EnumColor, BLOCK> blockCreator,
          String blockTypeSuffix, EnumColor color) {
        return BLOCKS.register(color.getRegistryPrefix() + blockTypeSuffix, () -> blockCreator.apply(color), ItemBlockColoredName::new);
    }
}
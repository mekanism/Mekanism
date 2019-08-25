package mekanism.additions.common;

import java.util.function.Function;
import javax.annotation.Nonnull;
import mekanism.additions.common.block.BlockGlowPanel;
import mekanism.additions.common.block.BlockObsidianTNT;
import mekanism.additions.common.block.plastic.BlockPlastic;
import mekanism.additions.common.block.plastic.BlockPlasticFence;
import mekanism.additions.common.block.plastic.BlockPlasticFenceGate;
import mekanism.additions.common.block.plastic.BlockPlasticGlow;
import mekanism.additions.common.block.plastic.BlockPlasticReinforced;
import mekanism.additions.common.block.plastic.BlockPlasticRoad;
import mekanism.additions.common.block.plastic.BlockPlasticSlab;
import mekanism.additions.common.block.plastic.BlockPlasticSlick;
import mekanism.additions.common.block.plastic.BlockPlasticStairs;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.providers.IItemProvider;
import mekanism.api.text.EnumColor;
import mekanism.common.item.IItemMekanism;
import mekanism.common.item.block.ItemBlockColoredName;
import mekanism.common.item.block.ItemBlockMekanism;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;

public enum AdditionsBlock implements IBlockProvider {
    OBSIDIAN_TNT(new BlockObsidianTNT()),

    BLACK_GLOW_PANEL(new BlockGlowPanel(EnumColor.BLACK), ItemBlockColoredName::new),
    RED_GLOW_PANEL(new BlockGlowPanel(EnumColor.RED), ItemBlockColoredName::new),
    GREEN_GLOW_PANEL(new BlockGlowPanel(EnumColor.DARK_GREEN), ItemBlockColoredName::new),
    BROWN_GLOW_PANEL(new BlockGlowPanel(EnumColor.BROWN), ItemBlockColoredName::new),
    BLUE_GLOW_PANEL(new BlockGlowPanel(EnumColor.DARK_BLUE), ItemBlockColoredName::new),
    PURPLE_GLOW_PANEL(new BlockGlowPanel(EnumColor.PURPLE), ItemBlockColoredName::new),
    CYAN_GLOW_PANEL(new BlockGlowPanel(EnumColor.DARK_AQUA), ItemBlockColoredName::new),
    LIGHT_GRAY_GLOW_PANEL(new BlockGlowPanel(EnumColor.GRAY), ItemBlockColoredName::new),
    GRAY_GLOW_PANEL(new BlockGlowPanel(EnumColor.DARK_GRAY), ItemBlockColoredName::new),
    PINK_GLOW_PANEL(new BlockGlowPanel(EnumColor.BRIGHT_PINK), ItemBlockColoredName::new),
    LIME_GLOW_PANEL(new BlockGlowPanel(EnumColor.BRIGHT_GREEN), ItemBlockColoredName::new),
    YELLOW_GLOW_PANEL(new BlockGlowPanel(EnumColor.YELLOW), ItemBlockColoredName::new),
    LIGHT_BLUE_GLOW_PANEL(new BlockGlowPanel(EnumColor.INDIGO), ItemBlockColoredName::new),
    MAGENTA_GLOW_PANEL(new BlockGlowPanel(EnumColor.PINK), ItemBlockColoredName::new),
    ORANGE_GLOW_PANEL(new BlockGlowPanel(EnumColor.ORANGE), ItemBlockColoredName::new),
    WHITE_GLOW_PANEL(new BlockGlowPanel(EnumColor.WHITE), ItemBlockColoredName::new),

    BLACK_PLASTIC_BLOCK(new BlockPlastic(EnumColor.BLACK), ItemBlockColoredName::new),
    RED_PLASTIC_BLOCK(new BlockPlastic(EnumColor.RED), ItemBlockColoredName::new),
    GREEN_PLASTIC_BLOCK(new BlockPlastic(EnumColor.DARK_GREEN), ItemBlockColoredName::new),
    BROWN_PLASTIC_BLOCK(new BlockPlastic(EnumColor.BROWN), ItemBlockColoredName::new),
    BLUE_PLASTIC_BLOCK(new BlockPlastic(EnumColor.DARK_BLUE), ItemBlockColoredName::new),
    PURPLE_PLASTIC_BLOCK(new BlockPlastic(EnumColor.PURPLE), ItemBlockColoredName::new),
    CYAN_PLASTIC_BLOCK(new BlockPlastic(EnumColor.DARK_AQUA), ItemBlockColoredName::new),
    LIGHT_GRAY_PLASTIC_BLOCK(new BlockPlastic(EnumColor.GRAY), ItemBlockColoredName::new),
    GRAY_PLASTIC_BLOCK(new BlockPlastic(EnumColor.DARK_GRAY), ItemBlockColoredName::new),
    PINK_PLASTIC_BLOCK(new BlockPlastic(EnumColor.BRIGHT_PINK), ItemBlockColoredName::new),
    LIME_PLASTIC_BLOCK(new BlockPlastic(EnumColor.BRIGHT_GREEN), ItemBlockColoredName::new),
    YELLOW_PLASTIC_BLOCK(new BlockPlastic(EnumColor.YELLOW), ItemBlockColoredName::new),
    LIGHT_BLUE_PLASTIC_BLOCK(new BlockPlastic(EnumColor.INDIGO), ItemBlockColoredName::new),
    MAGENTA_PLASTIC_BLOCK(new BlockPlastic(EnumColor.PINK), ItemBlockColoredName::new),
    ORANGE_PLASTIC_BLOCK(new BlockPlastic(EnumColor.ORANGE), ItemBlockColoredName::new),
    WHITE_PLASTIC_BLOCK(new BlockPlastic(EnumColor.WHITE), ItemBlockColoredName::new),

    BLACK_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.BLACK), ItemBlockColoredName::new),
    RED_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.RED), ItemBlockColoredName::new),
    GREEN_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.DARK_GREEN), ItemBlockColoredName::new),
    BROWN_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.BROWN), ItemBlockColoredName::new),
    BLUE_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.DARK_BLUE), ItemBlockColoredName::new),
    PURPLE_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.PURPLE), ItemBlockColoredName::new),
    CYAN_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.DARK_AQUA), ItemBlockColoredName::new),
    LIGHT_GRAY_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.GRAY), ItemBlockColoredName::new),
    GRAY_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.DARK_GRAY), ItemBlockColoredName::new),
    PINK_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.BRIGHT_PINK), ItemBlockColoredName::new),
    LIME_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.BRIGHT_GREEN), ItemBlockColoredName::new),
    YELLOW_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.YELLOW), ItemBlockColoredName::new),
    LIGHT_BLUE_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.INDIGO), ItemBlockColoredName::new),
    MAGENTA_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.PINK), ItemBlockColoredName::new),
    ORANGE_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.ORANGE), ItemBlockColoredName::new),
    WHITE_SLICK_PLASTIC_BLOCK(new BlockPlasticSlick(EnumColor.WHITE), ItemBlockColoredName::new),

    BLACK_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.BLACK), ItemBlockColoredName::new),
    RED_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.RED), ItemBlockColoredName::new),
    GREEN_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.DARK_GREEN), ItemBlockColoredName::new),
    BROWN_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.BROWN), ItemBlockColoredName::new),
    BLUE_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.DARK_BLUE), ItemBlockColoredName::new),
    PURPLE_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.PURPLE), ItemBlockColoredName::new),
    CYAN_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.DARK_AQUA), ItemBlockColoredName::new),
    LIGHT_GRAY_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.GRAY), ItemBlockColoredName::new),
    GRAY_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.DARK_GRAY), ItemBlockColoredName::new),
    PINK_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.BRIGHT_PINK), ItemBlockColoredName::new),
    LIME_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.BRIGHT_GREEN), ItemBlockColoredName::new),
    YELLOW_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.YELLOW), ItemBlockColoredName::new),
    LIGHT_BLUE_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.INDIGO), ItemBlockColoredName::new),
    MAGENTA_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.PINK), ItemBlockColoredName::new),
    ORANGE_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.ORANGE), ItemBlockColoredName::new),
    WHITE_PLASTIC_GLOW_BLOCK(new BlockPlasticGlow(EnumColor.WHITE), ItemBlockColoredName::new),

    BLACK_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.BLACK), ItemBlockColoredName::new),
    RED_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.RED), ItemBlockColoredName::new),
    GREEN_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.DARK_GREEN), ItemBlockColoredName::new),
    BROWN_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.BROWN), ItemBlockColoredName::new),
    BLUE_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.DARK_BLUE), ItemBlockColoredName::new),
    PURPLE_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.PURPLE), ItemBlockColoredName::new),
    CYAN_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.DARK_AQUA), ItemBlockColoredName::new),
    LIGHT_GRAY_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.GRAY), ItemBlockColoredName::new),
    GRAY_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.DARK_GRAY), ItemBlockColoredName::new),
    PINK_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.BRIGHT_PINK), ItemBlockColoredName::new),
    LIME_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.BRIGHT_GREEN), ItemBlockColoredName::new),
    YELLOW_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.YELLOW), ItemBlockColoredName::new),
    LIGHT_BLUE_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.INDIGO), ItemBlockColoredName::new),
    MAGENTA_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.PINK), ItemBlockColoredName::new),
    ORANGE_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.ORANGE), ItemBlockColoredName::new),
    WHITE_REINFORCED_PLASTIC_BLOCK(new BlockPlasticReinforced(EnumColor.WHITE), ItemBlockColoredName::new),

    BLACK_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.BLACK), ItemBlockColoredName::new),
    RED_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.RED), ItemBlockColoredName::new),
    GREEN_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.DARK_GREEN), ItemBlockColoredName::new),
    BROWN_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.BROWN), ItemBlockColoredName::new),
    BLUE_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.DARK_BLUE), ItemBlockColoredName::new),
    PURPLE_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.PURPLE), ItemBlockColoredName::new),
    CYAN_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.DARK_AQUA), ItemBlockColoredName::new),
    LIGHT_GRAY_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.GRAY), ItemBlockColoredName::new),
    GRAY_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.DARK_GRAY), ItemBlockColoredName::new),
    PINK_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.BRIGHT_PINK), ItemBlockColoredName::new),
    LIME_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.BRIGHT_GREEN), ItemBlockColoredName::new),
    YELLOW_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.YELLOW), ItemBlockColoredName::new),
    LIGHT_BLUE_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.INDIGO), ItemBlockColoredName::new),
    MAGENTA_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.PINK), ItemBlockColoredName::new),
    ORANGE_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.ORANGE), ItemBlockColoredName::new),
    WHITE_PLASTIC_ROAD(new BlockPlasticRoad(EnumColor.WHITE), ItemBlockColoredName::new),

    BLACK_PLASTIC_STAIRS(new BlockPlasticStairs(BLACK_PLASTIC_BLOCK, EnumColor.BLACK), ItemBlockColoredName::new),
    RED_PLASTIC_STAIRS(new BlockPlasticStairs(RED_PLASTIC_BLOCK, EnumColor.RED), ItemBlockColoredName::new),
    GREEN_PLASTIC_STAIRS(new BlockPlasticStairs(GREEN_PLASTIC_BLOCK, EnumColor.DARK_GREEN), ItemBlockColoredName::new),
    BROWN_PLASTIC_STAIRS(new BlockPlasticStairs(BROWN_PLASTIC_BLOCK, EnumColor.BROWN), ItemBlockColoredName::new),
    BLUE_PLASTIC_STAIRS(new BlockPlasticStairs(BLUE_PLASTIC_BLOCK, EnumColor.DARK_BLUE), ItemBlockColoredName::new),
    PURPLE_PLASTIC_STAIRS(new BlockPlasticStairs(PURPLE_PLASTIC_BLOCK, EnumColor.PURPLE), ItemBlockColoredName::new),
    CYAN_PLASTIC_STAIRS(new BlockPlasticStairs(CYAN_PLASTIC_BLOCK, EnumColor.DARK_AQUA), ItemBlockColoredName::new),
    LIGHT_GRAY_PLASTIC_STAIRS(new BlockPlasticStairs(LIGHT_GRAY_PLASTIC_BLOCK, EnumColor.GRAY), ItemBlockColoredName::new),
    GRAY_PLASTIC_STAIRS(new BlockPlasticStairs(GRAY_PLASTIC_BLOCK, EnumColor.DARK_GRAY), ItemBlockColoredName::new),
    PINK_PLASTIC_STAIRS(new BlockPlasticStairs(PINK_PLASTIC_BLOCK, EnumColor.BRIGHT_PINK), ItemBlockColoredName::new),
    LIME_PLASTIC_STAIRS(new BlockPlasticStairs(LIME_PLASTIC_BLOCK, EnumColor.BRIGHT_GREEN), ItemBlockColoredName::new),
    YELLOW_PLASTIC_STAIRS(new BlockPlasticStairs(YELLOW_PLASTIC_BLOCK, EnumColor.YELLOW), ItemBlockColoredName::new),
    LIGHT_BLUE_PLASTIC_STAIRS(new BlockPlasticStairs(LIGHT_BLUE_PLASTIC_BLOCK, EnumColor.INDIGO), ItemBlockColoredName::new),
    MAGENTA_PLASTIC_STAIRS(new BlockPlasticStairs(MAGENTA_PLASTIC_BLOCK, EnumColor.PINK), ItemBlockColoredName::new),
    ORANGE_PLASTIC_STAIRS(new BlockPlasticStairs(ORANGE_PLASTIC_BLOCK, EnumColor.ORANGE), ItemBlockColoredName::new),
    WHITE_PLASTIC_STAIRS(new BlockPlasticStairs(WHITE_PLASTIC_BLOCK, EnumColor.WHITE), ItemBlockColoredName::new),

    BLACK_PLASTIC_SLAB(new BlockPlasticSlab(EnumColor.BLACK), ItemBlockColoredName::new),
    RED_PLASTIC_SLAB(new BlockPlasticSlab(EnumColor.RED), ItemBlockColoredName::new),
    GREEN_PLASTIC_SLAB(new BlockPlasticSlab(EnumColor.DARK_GREEN), ItemBlockColoredName::new),
    BROWN_PLASTIC_SLAB(new BlockPlasticSlab(EnumColor.BROWN), ItemBlockColoredName::new),
    BLUE_PLASTIC_SLAB(new BlockPlasticSlab(EnumColor.DARK_BLUE), ItemBlockColoredName::new),
    PURPLE_PLASTIC_SLAB(new BlockPlasticSlab(EnumColor.PURPLE), ItemBlockColoredName::new),
    CYAN_PLASTIC_SLAB(new BlockPlasticSlab(EnumColor.DARK_AQUA), ItemBlockColoredName::new),
    LIGHT_GRAY_PLASTIC_SLAB(new BlockPlasticSlab(EnumColor.GRAY), ItemBlockColoredName::new),
    GRAY_PLASTIC_SLAB(new BlockPlasticSlab(EnumColor.DARK_GRAY), ItemBlockColoredName::new),
    PINK_PLASTIC_SLAB(new BlockPlasticSlab(EnumColor.BRIGHT_PINK), ItemBlockColoredName::new),
    LIME_PLASTIC_SLAB(new BlockPlasticSlab(EnumColor.BRIGHT_GREEN), ItemBlockColoredName::new),
    YELLOW_PLASTIC_SLAB(new BlockPlasticSlab(EnumColor.YELLOW), ItemBlockColoredName::new),
    LIGHT_BLUE_PLASTIC_SLAB(new BlockPlasticSlab(EnumColor.INDIGO), ItemBlockColoredName::new),
    MAGENTA_PLASTIC_SLAB(new BlockPlasticSlab(EnumColor.PINK), ItemBlockColoredName::new),
    ORANGE_PLASTIC_SLAB(new BlockPlasticSlab(EnumColor.ORANGE), ItemBlockColoredName::new),
    WHITE_PLASTIC_SLAB(new BlockPlasticSlab(EnumColor.WHITE), ItemBlockColoredName::new),

    BLACK_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.BLACK), ItemBlockColoredName::new),
    RED_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.RED), ItemBlockColoredName::new),
    GREEN_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.DARK_GREEN), ItemBlockColoredName::new),
    BROWN_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.BROWN), ItemBlockColoredName::new),
    BLUE_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.DARK_BLUE), ItemBlockColoredName::new),
    PURPLE_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.PURPLE), ItemBlockColoredName::new),
    CYAN_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.DARK_AQUA), ItemBlockColoredName::new),
    LIGHT_GRAY_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.GRAY), ItemBlockColoredName::new),
    GRAY_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.DARK_GRAY), ItemBlockColoredName::new),
    PINK_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.BRIGHT_PINK), ItemBlockColoredName::new),
    LIME_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.BRIGHT_GREEN), ItemBlockColoredName::new),
    YELLOW_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.YELLOW), ItemBlockColoredName::new),
    LIGHT_BLUE_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.INDIGO), ItemBlockColoredName::new),
    MAGENTA_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.PINK), ItemBlockColoredName::new),
    ORANGE_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.ORANGE), ItemBlockColoredName::new),
    WHITE_PLASTIC_FENCE(new BlockPlasticFence(EnumColor.WHITE), ItemBlockColoredName::new),

    BLACK_PLASTIC_FENCE_GATE(new BlockPlasticFenceGate(EnumColor.BLACK), ItemBlockColoredName::new),
    RED_PLASTIC_FENCE_GATE(new BlockPlasticFenceGate(EnumColor.RED), ItemBlockColoredName::new),
    GREEN_PLASTIC_FENCE_GATE(new BlockPlasticFenceGate(EnumColor.DARK_GREEN), ItemBlockColoredName::new),
    BROWN_PLASTIC_FENCE_GATE(new BlockPlasticFenceGate(EnumColor.BROWN), ItemBlockColoredName::new),
    BLUE_PLASTIC_FENCE_GATE(new BlockPlasticFenceGate(EnumColor.DARK_BLUE), ItemBlockColoredName::new),
    PURPLE_PLASTIC_FENCE_GATE(new BlockPlasticFenceGate(EnumColor.PURPLE), ItemBlockColoredName::new),
    CYAN_PLASTIC_FENCE_GATE(new BlockPlasticFenceGate(EnumColor.DARK_AQUA), ItemBlockColoredName::new),
    LIGHT_GRAY_PLASTIC_FENCE_GATE(new BlockPlasticFenceGate(EnumColor.GRAY), ItemBlockColoredName::new),
    GRAY_PLASTIC_FENCE_GATE(new BlockPlasticFenceGate(EnumColor.DARK_GRAY), ItemBlockColoredName::new),
    PINK_PLASTIC_FENCE_GATE(new BlockPlasticFenceGate(EnumColor.BRIGHT_PINK), ItemBlockColoredName::new),
    LIME_PLASTIC_FENCE_GATE(new BlockPlasticFenceGate(EnumColor.BRIGHT_GREEN), ItemBlockColoredName::new),
    YELLOW_PLASTIC_FENCE_GATE(new BlockPlasticFenceGate(EnumColor.YELLOW), ItemBlockColoredName::new),
    LIGHT_BLUE_PLASTIC_FENCE_GATE(new BlockPlasticFenceGate(EnumColor.INDIGO), ItemBlockColoredName::new),
    MAGENTA_PLASTIC_FENCE_GATE(new BlockPlasticFenceGate(EnumColor.PINK), ItemBlockColoredName::new),
    ORANGE_PLASTIC_FENCE_GATE(new BlockPlasticFenceGate(EnumColor.ORANGE), ItemBlockColoredName::new),
    WHITE_PLASTIC_FENCE_GATE(new BlockPlasticFenceGate(EnumColor.WHITE), ItemBlockColoredName::new);

    private final BlockItem item;
    private final Block block;

    AdditionsBlock(@Nonnull Block block) {
        this(block, ItemBlockMekanism::new);
    }

    <ITEM extends BlockItem & IItemMekanism, BLOCK extends Block> AdditionsBlock(BLOCK block, Function<BLOCK, ITEM> itemCreator) {
        this.block = block;
        this.item = itemCreator.apply(block);
    }

    @Nonnull
    @Override
    public Block getBlock() {
        return block;
    }

    @Nonnull
    @Override
    public BlockItem getItem() {
        return item;
    }

    public static void registerBlocks(IForgeRegistry<Block> registry) {
        for (IBlockProvider blockProvider : values()) {
            registry.register(blockProvider.getBlock());
        }
    }

    public static void registerItemBlocks(IForgeRegistry<Item> registry) {
        for (IItemProvider itemProvider : values()) {
            registry.register(itemProvider.getItem());
        }
    }
}
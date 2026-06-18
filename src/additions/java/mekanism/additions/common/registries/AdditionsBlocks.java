package mekanism.additions.common.registries;

import java.util.function.BiFunction;
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
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.text.EnumColor;
import mekanism.api.text.EnumColorCollection;
import mekanism.common.block.interfaces.IColoredBlock;
import mekanism.common.item.block.ItemBlockMekanism;
import mekanism.common.registration.impl.BlockDeferredRegister;
import mekanism.common.registration.impl.BlockRegistryObject;
import net.minecraft.core.Holder;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class AdditionsBlocks {

    private AdditionsBlocks() {
    }

    public static final BlockDeferredRegister BLOCKS = new BlockDeferredRegister(MekanismAdditions.MODID);

    public static final BlockRegistryObject<BlockObsidianTNT, BlockItem> OBSIDIAN_TNT = BLOCKS.register("obsidian_tnt", BlockObsidianTNT::new);

    public static final EnumColorCollection<BlockRegistryObject<BlockGlowPanel, ItemBlockMekanism<BlockGlowPanel>>> GLOW_PANELS = EnumColorCollection.VALUES
          .map(color -> registerColoredBlock(BlockGlowPanel::new, "_glow_panel", color));
    public static final EnumColorCollection<BlockRegistryObject<BlockPlastic, ItemBlockMekanism<BlockPlastic>>> PLASTIC_BLOCKS = EnumColorCollection.VALUES
          .map(color -> registerPlastic(color, "_plastic", properties -> properties.strength(5, 6)));
    public static final EnumColorCollection<BlockRegistryObject<BlockPlastic, ItemBlockMekanism<BlockPlastic>>> SLICK_PLASTIC_BLOCKS = EnumColorCollection.VALUES
          .map(color -> registerPlastic(color, "_slick_plastic", properties -> properties.strength(5, 6).friction(0.98F)));
    public static final EnumColorCollection<BlockRegistryObject<BlockPlastic, ItemBlockMekanism<BlockPlastic>>> PLASTIC_GLOW_BLOCKS = EnumColorCollection.VALUES
          .map(color -> registerPlastic(color, "_plastic_glow", properties -> properties.strength(5, 6)
                .lightLevel(_ -> 10).emissiveRendering(ConstantPredicates.alwaysTrue())));
    public static final EnumColorCollection<BlockRegistryObject<BlockPlastic, ItemBlockMekanism<BlockPlastic>>> REINFORCED_PLASTIC_BLOCKS = EnumColorCollection.VALUES
          .map(color -> registerPlastic(color, "_reinforced_plastic", properties -> properties.strength(50, 1_200)));
    public static final EnumColorCollection<BlockRegistryObject<BlockPlasticRoad, ItemBlockMekanism<BlockPlasticRoad>>> PLASTIC_ROADS = EnumColorCollection.VALUES
          .map(color -> registerColoredBlock(BlockPlasticRoad::new, "_plastic_road", color));
    public static final EnumColorCollection<BlockRegistryObject<BlockPlasticTransparent, ItemBlockMekanism<BlockPlasticTransparent>>> TRANSPARENT_PLASTIC_BLOCKS = EnumColorCollection.VALUES
          .map(color -> registerColoredBlock(BlockPlasticTransparent::new, "_plastic_transparent", color));
    public static final EnumColorCollection<BlockRegistryObject<BlockPlasticStairs, ItemBlockMekanism<BlockPlasticStairs>>> PLASTIC_STAIRS = EnumColorCollection.zipMap(
          EnumColorCollection.VALUES, PLASTIC_BLOCKS,
          (color, baseBlock) -> registerPlasticStairs(baseBlock, color, "_plastic_stairs", UnaryOperator.identity())
    );
    public static final EnumColorCollection<BlockRegistryObject<BlockPlasticSlab, ItemBlockMekanism<BlockPlasticSlab>>> PLASTIC_SLABS = EnumColorCollection.VALUES
          .map(color -> registerPlasticSlab(color, "_plastic_slab", UnaryOperator.identity()));
    public static final EnumColorCollection<BlockRegistryObject<BlockPlasticFence, ItemBlockMekanism<BlockPlasticFence>>> PLASTIC_FENCES = EnumColorCollection.VALUES
          .map(color -> registerColoredBlock(BlockPlasticFence::new, "_plastic_fence", color));
    public static final EnumColorCollection<BlockRegistryObject<BlockPlasticFenceGate, ItemBlockMekanism<BlockPlasticFenceGate>>> PLASTIC_FENCE_GATES = EnumColorCollection.VALUES
          .map(color -> registerColoredBlock(BlockPlasticFenceGate::new, "_plastic_fence_gate", color));
    public static final EnumColorCollection<BlockRegistryObject<BlockPlasticStairs, ItemBlockMekanism<BlockPlasticStairs>>> PLASTIC_GLOW_STAIRS = EnumColorCollection.zipMap(
          EnumColorCollection.VALUES, PLASTIC_GLOW_BLOCKS,
          (color, baseBlock) -> registerPlasticStairs(baseBlock, color, "_plastic_glow_stairs",
                properties -> properties.lightLevel(_ -> 10).emissiveRendering(ConstantPredicates.alwaysTrue()))
    );
    public static final EnumColorCollection<BlockRegistryObject<BlockPlasticSlab, ItemBlockMekanism<BlockPlasticSlab>>> PLASTIC_GLOW_SLABS = EnumColorCollection.VALUES
          .map(color -> registerPlasticSlab(color, "_plastic_glow_slab", properties -> properties.lightLevel(_ -> 10).emissiveRendering(ConstantPredicates.alwaysTrue())));
    public static final EnumColorCollection<BlockRegistryObject<BlockPlasticTransparentStairs, ItemBlockMekanism<BlockPlasticTransparentStairs>>> TRANSPARENT_PLASTIC_STAIRS = EnumColorCollection.zipMap(
          EnumColorCollection.VALUES, TRANSPARENT_PLASTIC_BLOCKS,
          (color, baseBlock) -> registerColoredBlock((properties, c) ->
                new BlockPlasticTransparentStairs(baseBlock.defaultState(), properties, c), "_plastic_transparent_stairs", color)
    );
    public static final EnumColorCollection<BlockRegistryObject<BlockPlasticTransparentSlab, ItemBlockMekanism<BlockPlasticTransparentSlab>>> TRANSPARENT_PLASTIC_SLABS = EnumColorCollection.VALUES
          .map(color -> registerColoredBlock(BlockPlasticTransparentSlab::new, "_plastic_transparent_slab", color));

    private static BlockRegistryObject<BlockPlastic, ItemBlockMekanism<BlockPlastic>> registerPlastic(EnumColor color, String blockTypeSuffix,
          UnaryOperator<BlockBehaviour.Properties> propertyModifier) {
        return registerColoredBlock((properties, c) -> new BlockPlastic(propertyModifier.apply(properties), c), blockTypeSuffix, color);
    }

    private static BlockRegistryObject<BlockPlasticSlab, ItemBlockMekanism<BlockPlasticSlab>> registerPlasticSlab(EnumColor color, String blockTypeSuffix,
          UnaryOperator<BlockBehaviour.Properties> propertyModifier) {
        return registerColoredBlock((properties, c) -> new BlockPlasticSlab(propertyModifier.apply(properties), c), blockTypeSuffix, color);
    }

    private static BlockRegistryObject<BlockPlasticStairs, ItemBlockMekanism<BlockPlasticStairs>> registerPlasticStairs(Holder<Block> baseBlock, EnumColor color, String blockTypeSuffix,
          UnaryOperator<BlockBehaviour.Properties> propertyModifier) {
        return registerColoredBlock((properties, c) -> new BlockPlasticStairs(baseBlock.value().defaultBlockState(), propertyModifier.apply(properties), c), blockTypeSuffix, color);
    }

    private static <BLOCK extends Block & IColoredBlock> BlockRegistryObject<BLOCK, ItemBlockMekanism<BLOCK>> registerColoredBlock(
          BiFunction<BlockBehaviour.Properties, EnumColor, BLOCK> blockCreator,
          String blockTypeSuffix, EnumColor color) {
        return BLOCKS.register(color.getRegistryPrefix() + blockTypeSuffix, properties -> blockCreator.apply(properties, color), ItemBlockMekanism::new);
    }
}
package mekanism.common.block.prefab;

import javax.annotation.Nonnull;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.WorldUtils;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

public class BlockBasicMultiblock<TILE extends TileEntityMekanism> extends BlockTile<TILE, BlockTypeTile<TILE>> {

    public BlockBasicMultiblock(BlockTypeTile<TILE> type) {
        this(type, AbstractBlock.Properties.create(Material.IRON).hardnessAndResistance(5, 9).setRequiresTool());
    }

    public BlockBasicMultiblock(BlockTypeTile<TILE> type, AbstractBlock.Properties properties) {
        super(type, properties);
    }

    @Nonnull
    @Override
    @Deprecated
    public ActionResultType onBlockActivated(@Nonnull BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull PlayerEntity player, @Nonnull Hand hand,
          @Nonnull BlockRayTraceResult hit) {
        TileEntityMultiblock<?> tile = WorldUtils.getTileEntity(TileEntityMultiblock.class, world, pos);
        if (tile == null) {
            return ActionResultType.PASS;
        }
        if (world.isRemote) {
            ItemStack stack = player.getHeldItem(hand);
            if (stack.getItem() instanceof BlockItem && new BlockItemUseContext(player, hand, stack, hit).canPlace()) {
                if (!tile.hasGui() || !tile.getMultiblock().isFormed()) {
                    //If the block doesn't have a gui (frames of things like the evaporation plant), or the multiblock is not formed then pass
                    return ActionResultType.PASS;
                }
            }
            return ActionResultType.SUCCESS;
        }
        if (tile.tryWrench(state, player, hand, hit) != WrenchResult.PASS) {
            return ActionResultType.SUCCESS;
        }
        return tile.onActivate(player, hand, player.getHeldItem(hand));
    }
}
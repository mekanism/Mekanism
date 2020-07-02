package mekanism.common.block.basic;

import java.util.Map;
import javax.annotation.Nonnull;
import mekanism.common.block.prefab.BlockTileGlass;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.lib.multiblock.IMultiblock;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.lib.multiblock.Structure;
import mekanism.common.tile.prefab.TileEntityStructuralMultiblock;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

public class BlockStructuralGlass<TILE extends TileEntityStructuralMultiblock> extends BlockTileGlass<TILE, BlockTypeTile<TILE>> {

    public BlockStructuralGlass(BlockTypeTile<TILE> type) {
        super(type);
    }

    @Nonnull
    @Override
    @Deprecated
    public ActionResultType onBlockActivated(@Nonnull BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull PlayerEntity player, @Nonnull Hand hand,
          @Nonnull BlockRayTraceResult hit) {
        TileEntityStructuralMultiblock tile = MekanismUtils.getTileEntity(TileEntityStructuralMultiblock.class, world, pos);
        if (tile != null) {
            if (world.isRemote) {
                ItemStack stack = player.getHeldItem(hand);
                if (stack.getItem() instanceof BlockItem && new BlockItemUseContext(player, hand, stack, hit).canPlace()) {
                    return ActionResultType.PASS;
                }
                for (Map.Entry<MultiblockManager<?>, Structure> entry : tile.getStructureMap().entrySet()) {
                    IMultiblock<?> master = entry.getValue().getController();
                    if (master != null && tile.getMultiblockData(entry.getKey()).isFormed()) {
                        // make sure this block is on the structure first
                        if (entry.getValue().getMultiblockData().getBounds().getRelativeLocation(tile.getPos()).isWall()) {
                            return ActionResultType.PASS;
                        }
                    }
                }
                return ActionResultType.SUCCESS;
            }
            return tile.onActivate(player, hand, player.getHeldItem(hand));
        }
        return ActionResultType.PASS;
    }
}

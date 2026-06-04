package mekanism.common.block.basic;

import mekanism.api.security.IBlockSecurityUtils;
import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.resource.BlockResourceInfo;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.util.FluidUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;

public class BlockFluidTank extends BlockTileModel<TileEntityFluidTank, Machine<TileEntityFluidTank>> {

    public BlockFluidTank(Machine<TileEntityFluidTank> type, BlockBehaviour.Properties properties) {
        super(type, defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor()));
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
        int ambientLight = super.getLightEmission(state, world, pos);
        if (ambientLight == 15) {
            //If we are already at the max light value don't bother looking up the tile to see if it has a fluid that gives off light
            return ambientLight;
        }
        TileEntityFluidTank tile = WorldUtils.getTileEntity(TileEntityFluidTank.class, world, pos);
        if (tile != null && !tile.fluidTank.isEmpty()) {
            ambientLight = Math.max(ambientLight, tile.fluidTank.resource().getFluidType().getLightLevel());
        }
        return ambientLight;
    }

    @NotNull
    @Override
    protected InteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Player player,
          @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (stack.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        TileEntityFluidTank tile = WorldUtils.getTileEntity(TileEntityFluidTank.class, world, pos, true);
        if (tile == null) {
            return InteractionResult.FAIL;
        } else if (world.isClientSide()) {
            return genericClientActivated(stack, tile);
        }
        InteractionResult wrenchResult = tile.tryWrench(state, player, stack).getInteractionResult();
        if (wrenchResult != InteractionResult.PASS) {
            return wrenchResult;
        }
        //Handle filling fluid tank
        if (!player.isShiftKeyDown()) {
            if (!IBlockSecurityUtils.INSTANCE.canAccessOrDisplayError(player, world, pos, tile)) {
                return InteractionResult.FAIL;
            }
            try (Transaction transaction = MekanismUtils.openTransactionSafe()) {
                if (FluidUtils.handleTankInteraction(player, hand, stack, tile.fluidTank, transaction)) {
                    transaction.commit();
                    //TODO - 26.1: Is this call even necessary?
                    player.getInventory().setChanged();
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }
}
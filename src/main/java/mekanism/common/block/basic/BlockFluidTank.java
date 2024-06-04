package mekanism.common.block.basic;

import mekanism.api.security.IBlockSecurityUtils;
import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.resource.BlockResourceInfo;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.util.FluidUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class BlockFluidTank extends BlockTileModel<TileEntityFluidTank, Machine<TileEntityFluidTank>> {

    public BlockFluidTank(Machine<TileEntityFluidTank> type) {
        super(type, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor()));
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
        int ambientLight = super.getLightEmission(state, world, pos);
        if (ambientLight == 15) {
            //If we are already at the max light value don't bother looking up the tile to see if it has a fluid that gives off light
            return ambientLight;
        }
        TileEntityFluidTank tile = WorldUtils.getTileEntity(TileEntityFluidTank.class, world, pos);
        if (tile != null) {
            FluidStack fluid = tile.fluidTank.getFluid();
            if (!fluid.isEmpty()) {
                ambientLight = Math.max(ambientLight, fluid.getFluidType().getLightLevel(fluid));
            }
        }
        return ambientLight;
    }

    @NotNull
    @Override
    protected ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Player player,
          @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (stack.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        TileEntityFluidTank tile = WorldUtils.getTileEntity(TileEntityFluidTank.class, world, pos, true);
        if (tile == null) {
            //No tile, we can just skip trying to use without an item
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        } else if (world.isClientSide) {
            return genericClientActivated(stack, tile);
        }
        ItemInteractionResult wrenchResult = tile.tryWrench(state, player, stack).getInteractionResult();
        if (wrenchResult.result() != InteractionResult.PASS) {
            return wrenchResult;
        }
        //Handle filling fluid tank
        if (!player.isShiftKeyDown()) {
            if (!IBlockSecurityUtils.INSTANCE.canAccessOrDisplayError(player, world, pos, tile)) {
                return ItemInteractionResult.FAIL;
            } else if (FluidUtils.handleTankInteraction(player, hand, stack, tile.fluidTank)) {
                player.getInventory().setChanged();
                return ItemInteractionResult.SUCCESS;
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
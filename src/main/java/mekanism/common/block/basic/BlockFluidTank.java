package mekanism.common.block.basic;

import mekanism.api.security.IBlockSecurityUtils;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.lib.transaction.TransactionHelper;
import mekanism.common.resource.BlockResourceInfo;
import mekanism.common.tile.TileEntityFluidTank;
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
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

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

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player,
          InteractionHand hand, BlockHitResult hit) {
        if (stack.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        TileEntityFluidTank tile = WorldUtils.getTileEntity(TileEntityFluidTank.class, world, pos, true);
        if (tile == null) {
            return InteractionResult.FAIL;
        } else if (world.isClientSide()) {
            return genericClientActivated(world, stack, tile);
        }
        InteractionResult wrenchResult = tile.tryWrench(world, state, player, stack).getInteractionResult();
        if (wrenchResult != InteractionResult.PASS) {
            return wrenchResult;
        }
        //Handle filling fluid tank
        if (!player.isShiftKeyDown()) {
            if (!IBlockSecurityUtils.INSTANCE.canAccessOrDisplayError(player, world, pos, tile)) {
                return InteractionResult.FAIL;
            }
            ResourceHandler<FluidResource> tankHandler = Capabilities.FLUID.getCapabilityIfLoaded(world, pos, null, tile, hit.getDirection());
            if (tankHandler != null) {
                try (Transaction transaction = TransactionHelper.openTransactionSafe()) {
                    if (ContainerType.FLUID.interactWithHandler(player, hand, pos, tankHandler, transaction)) {
                        transaction.commit();
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }
}
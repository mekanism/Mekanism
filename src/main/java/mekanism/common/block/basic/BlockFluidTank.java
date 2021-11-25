package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.interfaces.IColoredBlock;
import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.FluidUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

public class BlockFluidTank extends BlockTileModel<TileEntityFluidTank, Machine<TileEntityFluidTank>> implements IColoredBlock {

    public BlockFluidTank(Machine<TileEntityFluidTank> type) {
        super(type);
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        int ambientLight = super.getLightValue(state, world, pos);
        if (ambientLight == 15) {
            //If we are already at the max light value don't bother looking up the tile to see if it has a fluid that gives off light
            return ambientLight;
        }
        TileEntityFluidTank tile = WorldUtils.getTileEntity(TileEntityFluidTank.class, world, pos);
        if (tile != null) {
            FluidStack fluid = tile.fluidTank.getFluid();
            if (!fluid.isEmpty()) {
                FluidAttributes fluidAttributes = fluid.getFluid().getAttributes();
                //TODO: Decide if we want to always be using the luminosity of the stack
                ambientLight = Math.max(ambientLight, world instanceof IBlockDisplayReader ? fluidAttributes.getLuminosity((IBlockDisplayReader) world, pos)
                                                                                           : fluidAttributes.getLuminosity(fluid));
            }
        }
        return ambientLight;
    }

    @Nonnull
    @Override
    @Deprecated
    public ActionResultType use(@Nonnull BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull PlayerEntity player, @Nonnull Hand hand,
          @Nonnull BlockRayTraceResult hit) {
        TileEntityFluidTank tile = WorldUtils.getTileEntity(TileEntityFluidTank.class, world, pos, true);
        if (tile == null) {
            return ActionResultType.PASS;
        } else if (world.isClientSide) {
            return genericClientActivated(player, hand);
        } else if (tile.tryWrench(state, player, hand, hit) != WrenchResult.PASS) {
            return ActionResultType.SUCCESS;
        }
        //Handle filling fluid tank
        if (!player.isShiftKeyDown()) {
            if (SecurityUtils.canAccess(player, tile)) {
                ItemStack stack = player.getItemInHand(hand);
                if (!stack.isEmpty() && FluidUtils.handleTankInteraction(player, hand, stack, tile.fluidTank)) {
                    player.inventory.setChanged();
                    return ActionResultType.SUCCESS;
                }
            } else {
                SecurityUtils.displayNoAccess(player);
                return ActionResultType.SUCCESS;
            }
        }
        return tile.openGui(player);
    }

    @Override
    public EnumColor getColor() {
        return Attribute.getBaseTier(this).getColor();
    }
}
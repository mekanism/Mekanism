package mekanism.generators.common.block.reactor;

import javax.annotation.Nonnull;
import mekanism.api.block.IHasTileEntity;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.inventory.container.reactor.ReactorLogicAdapterContainer;
import mekanism.generators.common.tile.GeneratorsTileEntityTypes;
import mekanism.generators.common.tile.reactor.TileEntityReactorLogicAdapter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockReactorLogicAdapter extends BlockMekanism implements IHasGui<TileEntityReactorLogicAdapter>, IHasTileEntity<TileEntityReactorLogicAdapter> {

    public BlockReactorLogicAdapter() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(3.5F, 8F));
    }

    @Override
    @Deprecated
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        if (!world.isRemote) {
            TileEntityMekanism tile = MekanismUtils.getTileEntity(TileEntityMekanism.class, world, pos);
            if (tile != null) {
                tile.onNeighborChange(neighborBlock);
            }
        }
    }

    @Nonnull
    @Override
    public ActionResultType func_225533_a_(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (world.isRemote) {
            return ActionResultType.SUCCESS;
        }
        TileEntityMekanism tile = MekanismUtils.getTileEntity(TileEntityMekanism.class, world, pos);
        if (tile == null) {
            return ActionResultType.PASS;
        }
        if (tile.tryWrench(state, player, hand, hit) != WrenchResult.PASS) {
            return ActionResultType.SUCCESS;
        }
        return tile.openGui(player);
    }

    @Override
    @Deprecated
    public int getWeakPower(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        TileEntityReactorLogicAdapter tile = MekanismUtils.getTileEntity(TileEntityReactorLogicAdapter.class, world, pos);
        if (tile != null) {
            return tile.checkMode() ? 15 : 0;
        }
        return 0;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        return true;
    }

    @Override
    public INamedContainerProvider getProvider(TileEntityReactorLogicAdapter tile) {
        return new ContainerProvider("mekanismgenerators.container.reactor_logic_adapter", (i, inv, player) -> new ReactorLogicAdapterContainer(i, inv, tile));
    }

    @Override
    public TileEntityType<TileEntityReactorLogicAdapter> getTileType() {
        return GeneratorsTileEntityTypes.REACTOR_LOGIC_ADAPTER.getTileEntityType();
    }
}
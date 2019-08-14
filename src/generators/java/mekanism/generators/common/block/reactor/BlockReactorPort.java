package mekanism.generators.common.block.reactor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.block.BlockTileDrops;
import mekanism.common.block.interfaces.IBlockElectric;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.states.IStateActive;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.tile.reactor.TileEntityReactorPort;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockReactorPort extends BlockTileDrops implements IStateActive, IBlockElectric, IHasTileEntity<TileEntityReactorPort> {

    public BlockReactorPort() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(3.5F, 8F));
        setRegistryName(new ResourceLocation(MekanismGenerators.MODID, "reactor_port"));
    }

    @Override
    @Deprecated
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        if (!world.isRemote) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof TileEntityMekanism) {
                ((TileEntityMekanism) tileEntity).onNeighborChange(neighborBlock);
            }
        }
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (world.isRemote) {
            return true;
        }
        TileEntityMekanism tileEntity = (TileEntityMekanism) world.getTileEntity(pos);
        if (tileEntity.tryWrench(state, player, hand, hit) != WrenchResult.PASS) {
            return true;
        }
        return false;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world) {
        return new TileEntityReactorPort();
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public double getStorage() {
        return 1;
    }

    @Nullable
    @Override
    public Class<? extends TileEntityReactorPort> getTileClass() {
        return TileEntityReactorPort.class;
    }
}
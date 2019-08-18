package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.block.IHasTileEntity;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockTileDrops;
import mekanism.common.block.states.IStateActive;
import mekanism.common.content.boiler.SynchronizedBoilerData;
import mekanism.common.tile.TileEntitySuperheatingElement;
import mekanism.common.tile.base.MekanismTileEntityTypes;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.World;

public class BlockSuperheatingElement extends BlockTileDrops implements IStateActive, IHasTileEntity<TileEntitySuperheatingElement> {

    public BlockSuperheatingElement() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(5F, 10F));
        setRegistryName(new ResourceLocation(Mekanism.MODID, "superheating_element"));
    }

    @Override
    @Deprecated
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        if (!world.isRemote) {
            TileEntity tileEntity = new Coord4D(pos, world).getTileEntity(world);
            if (tileEntity instanceof TileEntityMekanism) {
                ((TileEntityMekanism) tileEntity).onNeighborChange(neighborBlock);
            }
        }
    }

    @Override
    public int getLightValue(BlockState state, IEnviromentBlockReader world, BlockPos pos) {
        return isActive(state, world, pos) ? 15 : super.getLightValue(state, world, pos);
    }

    @Override
    public boolean isActive(@Nonnull TileEntity tile) {
        if (tile instanceof TileEntitySuperheatingElement) {
            //Should be true
            TileEntitySuperheatingElement heating = (TileEntitySuperheatingElement) tile;
            if (heating.multiblockUUID != null && SynchronizedBoilerData.clientHotMap.get(heating.multiblockUUID) != null) {
                return SynchronizedBoilerData.clientHotMap.get(heating.multiblockUUID);
            }
        }
        return false;
    }

    @Override
    public TileEntityType<TileEntitySuperheatingElement> getTileType() {
        return MekanismTileEntityTypes.SUPERHEATING_ELEMENT;
    }
}
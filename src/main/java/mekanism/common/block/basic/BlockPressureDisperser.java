package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import mekanism.api.block.IHasTileEntity;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.tile.TileEntityPressureDisperser;
import mekanism.common.tile.base.MekanismTileEntityTypes;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockPressureDisperser extends BlockMekanism implements IHasTileEntity<TileEntityPressureDisperser>, IHasDescription {

    public BlockPressureDisperser() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(5F, 10F));
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

    @Override
    public TileEntityType<TileEntityPressureDisperser> getTileType() {
        return MekanismTileEntityTypes.PRESSURE_DISPERSER.getTileEntityType();
    }

    @Nonnull
    @Override
    public ILangEntry getDescription() {
        return MekanismLang.DESCRIPTION_PRESSURE_DISPERSER;
    }
}
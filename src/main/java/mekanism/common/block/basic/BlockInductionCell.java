package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.block.IHasTileEntity;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.interfaces.ITieredBlock;
import mekanism.common.tier.InductionCellTier;
import mekanism.common.tile.TileEntityInductionCell;
import mekanism.common.tile.base.MekanismTileEntityTypes;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType;
import net.minecraft.entity.EntityType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockInductionCell extends BlockMekanism implements ITieredBlock<InductionCellTier>, IHasTileEntity<TileEntityInductionCell>, IHasDescription {

    private final InductionCellTier tier;

    public BlockInductionCell(InductionCellTier tier) {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(5F, 10F));
        this.tier = tier;
    }

    @Override
    public InductionCellTier getTier() {
        return tier;
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
    public boolean canCreatureSpawn(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos, PlacementType type, @Nullable EntityType<?> entityType) {
        return false;
    }

    @Override
    public TileEntityType<TileEntityInductionCell> getTileType() {
        switch (tier) {
            case ADVANCED:
                return MekanismTileEntityTypes.ADVANCED_INDUCTION_CELL.getTileEntityType();
            case ELITE:
                return MekanismTileEntityTypes.ELITE_INDUCTION_CELL.getTileEntityType();
            case ULTIMATE:
                return MekanismTileEntityTypes.ULTIMATE_INDUCTION_CELL.getTileEntityType();
            case BASIC:
            default:
                return MekanismTileEntityTypes.BASIC_INDUCTION_CELL.getTileEntityType();
        }
    }

    @Nonnull
    @Override
    public ILangEntry getDescription() {
        return MekanismLang.DESCRIPTION_INDUCTION_CELL;
    }
}
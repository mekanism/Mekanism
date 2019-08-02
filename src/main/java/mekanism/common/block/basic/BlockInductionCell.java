package mekanism.common.block.basic;

import java.util.Locale;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.energy.IEnergizedItem;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockTileDrops;
import mekanism.common.block.interfaces.ITieredBlock;
import mekanism.common.tier.InductionCellTier;
import mekanism.common.tile.TileEntityInductionCell;
import mekanism.common.tile.prefab.TileEntityBasicBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockInductionCell extends BlockTileDrops implements ITieredBlock<InductionCellTier> {

    private final InductionCellTier tier;

    public BlockInductionCell(InductionCellTier tier) {
        super(Material.IRON);
        this.tier = tier;
        setHardness(5F);
        setResistance(10F);
        setRegistryName(new ResourceLocation(Mekanism.MODID, tier.getBaseTier().getSimpleName().toLowerCase(Locale.ROOT) + "_induction_cell"));
    }

    @Override
    public InductionCellTier getTier() {
        return tier;
    }

    @Override
    @Deprecated
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos) {
        if (!world.isRemote) {
            TileEntity tileEntity = new Coord4D(pos, world).getTileEntity(world);
            if (tileEntity instanceof TileEntityBasicBlock) {
                ((TileEntityBasicBlock) tileEntity).onNeighborChange(neighborBlock);
            }
        }
    }

    @Nonnull
    @Override
    protected ItemStack getDropItem(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        TileEntityInductionCell tile = (TileEntityInductionCell) world.getTileEntity(pos);
        ItemStack itemStack = new ItemStack(this);
        if (tile == null) {
            return itemStack;
        }
        //This can probably be moved upwards
        IEnergizedItem energizedItem = (IEnergizedItem) itemStack.getItem();
        energizedItem.setEnergy(itemStack, tile.getEnergy());
        return itemStack;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityInductionCell();
    }
}
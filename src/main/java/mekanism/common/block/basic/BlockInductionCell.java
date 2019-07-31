package mekanism.common.block.basic;

import java.util.Locale;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.energy.IEnergizedItem;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockTileDrops;
import mekanism.common.block.interfaces.IBlockDescriptive;
import mekanism.common.tier.InductionCellTier;
import mekanism.common.tile.TileEntityInductionCell;
import mekanism.common.tile.prefab.TileEntityBasicBlock;
import mekanism.common.util.LangUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockInductionCell extends BlockTileDrops implements IBlockDescriptive {

    private final InductionCellTier tier;
    private final String name;

    public BlockInductionCell(InductionCellTier tier) {
        super(Material.IRON);
        this.tier = tier;
        setHardness(5F);
        setResistance(10F);
        setCreativeTab(Mekanism.tabMekanism);
        this.name = tier.getBaseTier().getSimpleName().toLowerCase(Locale.ROOT) + "_induction_cell";
        setTranslationKey(this.name);
        setRegistryName(new ResourceLocation(Mekanism.MODID, this.name));
    }

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

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityBasicBlock) {
            ((TileEntityBasicBlock) te).redstone = world.getRedstonePowerFromNeighbors(pos) > 0;
        }
    }

    @Nonnull
    @Override
    protected ItemStack getDropItem(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        ItemStack ret = new ItemStack(this);
        TileEntityInductionCell tileEntity = (TileEntityInductionCell) world.getTileEntity(pos);
        //This can probably be moved upwards
        IEnergizedItem energizedItem = (IEnergizedItem) ret.getItem();
        energizedItem.setEnergy(ret, ((IStrictEnergyStorage) tileEntity).getEnergy());
        return ret;
    }

    @Override
    public String getDescription() {
        //TODO: Should name just be gotten from registry name
        return LangUtils.localize("tooltip.mekanism." + this.name);
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
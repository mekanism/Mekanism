package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockTileDrops;
import mekanism.common.block.interfaces.IBlockDescriptive;
import mekanism.common.block.interfaces.IHasModel;
import mekanism.common.multiblock.IMultiblock;
import mekanism.common.multiblock.IStructuralMultiblock;
import mekanism.common.tile.TileEntityThermalEvaporationValve;
import mekanism.common.tile.prefab.TileEntityBasicBlock;
import mekanism.common.util.LangUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class BlockThermalEvaporationValve extends BlockTileDrops implements IBlockDescriptive, IHasModel {

    private final String name;

    public BlockThermalEvaporationValve() {
        super(Material.IRON);
        setHardness(5F);
        setResistance(10F);
        setCreativeTab(Mekanism.tabMekanism);
        this.name = "thermal_evaporation_valve";
        setTranslationKey(this.name);
        setRegistryName(new ResourceLocation(Mekanism.MODID, this.name));
    }

    @Override
    @Deprecated
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos) {
        if (!world.isRemote) {
            TileEntity tileEntity = new Coord4D(pos, world).getTileEntity(world);
            if (tileEntity instanceof IMultiblock) {
                ((IMultiblock<?>) tileEntity).doUpdate();
            }
            if (tileEntity instanceof TileEntityBasicBlock) {
                ((TileEntityBasicBlock) tileEntity).onNeighborChange(neighborBlock);
            }
            if (tileEntity instanceof IStructuralMultiblock) {
                ((IStructuralMultiblock) tileEntity).doUpdate();
            }
        }
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        TileEntityThermalEvaporationValve tile = (TileEntityThermalEvaporationValve) world.getTileEntity(pos);
        tile.redstone = world.getRedstonePowerFromNeighbors(pos) > 0;
    }

    @Override
    public String getDescription() {
        //TODO: Should name just be gotten from registry name
        return LangUtils.localize("tooltip.mekanism." + this.name);
    }

    @Override
    public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
        return 9F;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityThermalEvaporationValve();
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState blockState) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState blockState, World world, BlockPos pos) {
        return ((TileEntityThermalEvaporationValve) world.getTileEntity(pos)).getRedstoneLevel();
    }
}
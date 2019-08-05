package mekanism.common.block.basic;

import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockTileDrops;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.interfaces.IHasInventory;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateFacing;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockSecurityDesk extends BlockTileDrops implements IStateFacing, IHasGui, IHasInventory, IHasTileEntity<TileEntitySecurityDesk> {

    public BlockSecurityDesk() {
        super(Material.IRON);
        setHardness(5F);
        setResistance(10F);
        setRegistryName(new ResourceLocation(Mekanism.MODID, "security_desk"));
    }

    public static boolean isInstance(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemBlock && ((ItemBlock) stack.getItem()).getBlock() instanceof BlockSecurityDesk;
    }

    @Nonnull
    @Override
    public BlockStateContainer createBlockState() {
        return BlockStateHelper.getBlockState(this);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        //TODO
        return 0;
    }

    @Nonnull
    @Override
    @Deprecated
    public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess world, BlockPos pos) {
        return BlockStateHelper.getActualState(this, state, MekanismUtils.getTileEntitySafe(world, pos));
    }

    @Override
    @Deprecated
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos) {
        if (!world.isRemote) {
            TileEntity tileEntity = new Coord4D(pos, world).getTileEntity(world);
            if (tileEntity instanceof TileEntityMekanism) {
                ((TileEntityMekanism) tileEntity).onNeighborChange(neighborBlock);
            }
        }
    }

    @Override
    public void setTileData(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack, TileEntityMekanism tile) {
        if (tile instanceof TileEntitySecurityDesk) {
            ((TileEntitySecurityDesk) tile).ownerUUID = placer.getUniqueID();
        }
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
        return new TileEntitySecurityDesk();
    }

    @Override
    @Deprecated
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    @Deprecated
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    @Deprecated
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
        return 0;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        TileEntitySecurityDesk tile = (TileEntitySecurityDesk) world.getTileEntity(pos);
        //TODO
        if (tile != null) {
            if (!player.isSneaking()) {
                if (!world.isRemote) {
                    UUID ownerUUID = tile.ownerUUID;
                    if (ownerUUID == null || player.getUniqueID().equals(ownerUUID)) {
                        player.openGui(Mekanism.instance, getGuiID(), world, pos.getX(), pos.getY(), pos.getZ());
                    } else {
                        SecurityUtils.displayNoAccess(player);
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public int getGuiID() {
        return 57;
    }

    @Override
    public int getInventorySize() {
        return 2;
    }

    @Nullable
    @Override
    public Class<? extends TileEntitySecurityDesk> getTileClass() {
        return TileEntitySecurityDesk.class;
    }
}
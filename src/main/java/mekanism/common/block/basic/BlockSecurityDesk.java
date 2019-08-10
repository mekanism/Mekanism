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
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockSecurityDesk extends BlockTileDrops implements IStateFacing, IHasGui, IHasInventory, IHasTileEntity<TileEntitySecurityDesk> {

    public BlockSecurityDesk() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(5F, 10F));
        setRegistryName(new ResourceLocation(Mekanism.MODID, "security_desk"));
    }

    @Nonnull
    @Override
    public BlockStateContainer createBlockState() {
        return BlockStateHelper.getBlockState(this);
    }

    @Override
    public int getMetaFromState(BlockState state) {
        //TODO
        return 0;
    }

    @Nonnull
    @Override
    @Deprecated
    public BlockState getActualState(@Nonnull BlockState state, IBlockReader world, BlockPos pos) {
        return BlockStateHelper.getActualState(this, state, MekanismUtils.getTileEntitySafe(world, pos));
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
    public void setTileData(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack, TileEntityMekanism tile) {
        if (tile instanceof TileEntitySecurityDesk) {
            ((TileEntitySecurityDesk) tile).ownerUUID = placer.getUniqueID();
        }
    }

    @Override
    public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
        return 9F;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world) {
        return new TileEntitySecurityDesk();
    }

    @Override
    public int getLightOpacity(BlockState state, IBlockReader world, BlockPos pos) {
        return 0;
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
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
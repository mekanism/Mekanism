package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import mekanism.api.IMekWrench;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.block.BlockBasic;
import mekanism.common.block.interfaces.IBlockActiveTextured;
import mekanism.common.integration.wrenches.Wrenches;
import mekanism.common.inventory.InventoryBin;
import mekanism.common.tier.BinTier;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Plane;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockBin extends BlockBasic implements IBlockActiveTextured {

    private final BinTier tier;

    public BlockBin(BinTier tier) {
        super(tier.getBaseTier().getSimpleName() + "_bin", Plane.HORIZONTAL);
        this.tier = tier;
    }

    @Override
    public boolean hasDescription() {
        return true;
    }

    @Override
    public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
        if (!world.isRemote) {
            TileEntityBin bin = (TileEntityBin) world.getTileEntity(pos);
            RayTraceResult mop = MekanismUtils.rayTrace(world, player);

            if (mop != null && mop.sideHit == bin.facing) {
                if (!bin.bottomStack.isEmpty()) {
                    ItemStack stack;
                    if (player.isSneaking()) {
                        stack = bin.remove(1).copy();
                    } else {
                        stack = bin.removeStack().copy();
                    }
                    if (!player.inventory.addItemStackToInventory(stack)) {
                        BlockPos dropPos = pos.offset(bin.facing);
                        Entity item = new EntityItem(world, dropPos.getX() + .5f, dropPos.getY() + .3f, dropPos.getZ() + .5f, stack);
                        item.addVelocity(-item.motionX, -item.motionY, -item.motionZ);
                        world.spawnEntity(item);
                    } else {
                        world.playSound(null, pos.getX() + .5f, pos.getY() + .5f, pos.getZ() + .5f, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS,
                              0.2F, ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                    }
                }
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityplayer, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        ItemStack stack = entityplayer.getHeldItem(hand);
        TileEntityBin bin = (TileEntityBin) world.getTileEntity(pos);
        IMekWrench wrenchHandler;
        if (!stack.isEmpty() && (wrenchHandler = Wrenches.getHandler(stack)) != null) {
            RayTraceResult raytrace = new RayTraceResult(new Vec3d(hitX, hitY, hitZ), side, pos);
            if (wrenchHandler.canUseWrench(entityplayer, hand, stack, raytrace)) {
                if (!world.isRemote) {
                    wrenchHandler.wrenchUsed(entityplayer, hand, stack, raytrace);
                    if (entityplayer.isSneaking()) {
                        MekanismUtils.dismantleBlock(this, state, world, pos);
                        return true;
                    }
                    bin.setFacing(bin.facing.rotateY());
                    world.notifyNeighborsOfStateChange(pos, this, true);
                }
                return true;
            }
        }
        if (!world.isRemote) {
            if (bin.getItemCount() < bin.tier.getStorage()) {
                if (bin.addTicks == 0) {
                    if (!stack.isEmpty()) {
                        ItemStack remain = bin.add(stack);
                        entityplayer.setHeldItem(hand, remain);
                        bin.addTicks = 5;
                    }
                } else if (bin.addTicks > 0 && bin.getItemCount() > 0) {
                    NonNullList<ItemStack> inv = entityplayer.inventory.mainInventory;
                    for (int i = 0; i < inv.size(); i++) {
                        if (bin.getItemCount() == bin.tier.getStorage()) {
                            break;
                        }
                        if (!inv.get(i).isEmpty()) {
                            ItemStack remain = bin.add(inv.get(i));
                            inv.set(i, remain);
                            bin.addTicks = 5;
                        }
                        ((EntityPlayerMP) entityplayer).sendContainerToPlayer(entityplayer.openContainer);
                    }
                }
            }
        }
        return true;
    }

    @Nonnull
    @Override
    protected ItemStack getDropItem(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        ItemStack ret = new ItemStack(this);
        TileEntityBin tileEntity = (TileEntityBin) world.getTileEntity(pos);
        if (tileEntity.getItemCount() > 0) {
            InventoryBin inv = new InventoryBin(ret);
            inv.setItemCount(tileEntity.getItemCount());
            inv.setItemType(tileEntity.itemType);
        }
        return ret;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityBin();
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState blockState) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState blockState, World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof IComparatorSupport) {
            return ((IComparatorSupport) tile).getRedstoneLevel();
        }
        return 0;
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
}
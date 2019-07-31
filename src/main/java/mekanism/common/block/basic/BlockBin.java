package mekanism.common.block.basic;

import java.util.Locale;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.IMekWrench;
import mekanism.common.Mekanism;
import mekanism.common.base.IActiveState;
import mekanism.common.block.BlockTileDrops;
import mekanism.common.block.interfaces.IBlockActiveTextured;
import mekanism.common.block.interfaces.IBlockDescriptive;
import mekanism.common.block.interfaces.IHasModel;
import mekanism.common.block.interfaces.IRotatableBlock;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateActive;
import mekanism.common.block.states.IStateFacing;
import mekanism.common.integration.wrenches.Wrenches;
import mekanism.common.inventory.InventoryBin;
import mekanism.common.tier.BinTier;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.prefab.TileEntityBasicBlock;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockBin extends BlockTileDrops implements IBlockActiveTextured, IRotatableBlock, IBlockDescriptive, IHasModel, IStateFacing, IStateActive {

    private final String name;
    private final BinTier tier;

    public BlockBin(BinTier tier) {
        super(Material.IRON);
        this.tier = tier;
        setHardness(5F);
        setResistance(10F);
        setCreativeTab(Mekanism.tabMekanism);
        this.name = tier.getBaseTier().getSimpleName().toLowerCase(Locale.ROOT) + "_bin";
        setTranslationKey(this.name);
        setRegistryName(new ResourceLocation(Mekanism.MODID, this.name));
    }

    public BinTier getTier() {
        return tier;
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
            if (tileEntity instanceof TileEntityBasicBlock) {
                ((TileEntityBasicBlock) tileEntity).onNeighborChange(neighborBlock);
            }
        }
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tileEntity = MekanismUtils.getTileEntitySafe(world, pos);
        if (tileEntity instanceof IActiveState) {
            if (((IActiveState) tileEntity).getActive() && ((IActiveState) tileEntity).lightUpdate()) {
                return 15;
            }
        }
        return super.getLightValue(state, world, pos);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityBasicBlock) {
            TileEntityBasicBlock tileEntity = (TileEntityBasicBlock) te;
            EnumFacing change = EnumFacing.SOUTH;
            if (tileEntity.canSetFacing(EnumFacing.DOWN) && tileEntity.canSetFacing(EnumFacing.UP)) {
                int height = Math.round(placer.rotationPitch);
                if (height >= 65) {
                    change = EnumFacing.UP;
                } else if (height <= -65) {
                    change = EnumFacing.DOWN;
                }
            }
            if (change != EnumFacing.DOWN && change != EnumFacing.UP) {
                int side = MathHelper.floor((placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
                switch (side) {
                    case 0:
                        change = EnumFacing.NORTH;
                        break;
                    case 1:
                        change = EnumFacing.EAST;
                        break;
                    case 2:
                        change = EnumFacing.SOUTH;
                        break;
                    case 3:
                        change = EnumFacing.WEST;
                        break;
                }
            }
            tileEntity.setFacing(change);
            tileEntity.redstone = world.getRedstonePowerFromNeighbors(pos) > 0;
        }

        world.markBlockRangeForRenderUpdate(pos, pos.add(1, 1, 1));
        world.checkLightFor(EnumSkyBlock.BLOCK, pos);
        world.checkLightFor(EnumSkyBlock.SKY, pos);
    }

    @Override
    public EnumFacing[] getValidRotations(World world, @Nonnull BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        EnumFacing[] valid = new EnumFacing[6];
        if (tile instanceof TileEntityBasicBlock) {
            TileEntityBasicBlock basicTile = (TileEntityBasicBlock) tile;
            for (EnumFacing dir : EnumFacing.VALUES) {
                if (basicTile.canSetFacing(dir)) {
                    valid[dir.ordinal()] = dir;
                }
            }
        }
        return valid;
    }

    @Override
    public boolean rotateBlock(World world, @Nonnull BlockPos pos, @Nonnull EnumFacing axis) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityBasicBlock) {
            TileEntityBasicBlock basicTile = (TileEntityBasicBlock) tile;
            if (basicTile.canSetFacing(axis)) {
                basicTile.setFacing(axis);
                return true;
            }
        }
        return false;
    }

    @Override
    public String getDescription() {
        //TODO: Should name just be gotten from registry name
        return LangUtils.localize("tooltip.mekanism." + this.name);
    }

    @Override
    public boolean canRotateTo(EnumFacing side) {
        return Plane.HORIZONTAL.test(side);
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
        return ((TileEntityBin) world.getTileEntity(pos)).getRedstoneLevel();
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
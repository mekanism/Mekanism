package mekanism.common.block;

import java.util.Random;
import javax.annotation.Nonnull;
import mekanism.api.IMekWrench;
import mekanism.api.gas.IGasItem;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ISustainedInventory;
import mekanism.common.base.ITierItem;
import mekanism.common.block.states.BlockStateFacing;
import mekanism.common.block.states.BlockStateGasTank;
import mekanism.common.integration.wrenches.Wrenches;
import mekanism.common.security.ISecurityItem;
import mekanism.common.security.ISecurityTile;
import mekanism.common.tile.TileEntityGasTank;
import mekanism.common.tile.prefab.TileEntityBasicBlock;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockGasTank extends BlockContainer {

    private static final AxisAlignedBB TANK_BOUNDS = new AxisAlignedBB(0.1875F, 0.0F, 0.1875F, 0.8125F, 1.0F, 0.8125F);

    public BlockGasTank() {
        super(Material.IRON);
        setHardness(3.5F);
        setResistance(8F);
        setCreativeTab(Mekanism.tabMekanism);
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateGasTank(this);
    }

    @Nonnull
    @Override
    @Deprecated
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState();
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Nonnull
    @Override
    @Deprecated
    public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        TileEntity tile = MekanismUtils.getTileEntitySafe(worldIn, pos);

        if (tile instanceof TileEntityGasTank) {
            TileEntityGasTank tank = (TileEntityGasTank) tile;

            if (tank.facing != null) {
                state = state.withProperty(BlockStateFacing.facingProperty, tank.facing);
            }

            if (tank.tier != null) {
                state = state.withProperty(BlockStateGasTank.typeProperty, tank.tier);
            }
        }

        return state;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer,
          ItemStack stack) {
        TileEntityBasicBlock tileEntity = (TileEntityBasicBlock) world.getTileEntity(pos);

        int side = MathHelper.floor((double) (placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        int change = 3;

        switch (side) {
            case 0:
                change = 2;
                break;
            case 1:
                change = 5;
                break;
            case 2:
                change = 3;
                break;
            case 3:
                change = 4;
                break;
        }

        tileEntity.setFacing((short) change);
        tileEntity.redstone = world.getRedstonePowerFromNeighbors(pos) > 0;
    }

    @Override
    @Deprecated
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock,
          BlockPos neighborPos) {
        if (!world.isRemote) {
            TileEntity tileEntity = world.getTileEntity(pos);

            if (tileEntity instanceof TileEntityBasicBlock) {
                ((TileEntityBasicBlock) tileEntity).onNeighborChange(neighborBlock);
            }
        }
    }

    @Override
    @Deprecated
    public float getPlayerRelativeBlockHardness(IBlockState state, @Nonnull EntityPlayer player, @Nonnull World world,
          @Nonnull BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);

        return SecurityUtils.canAccess(player, tile) ? super.getPlayerRelativeBlockHardness(state, player, world, pos)
              : 0.0F;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityplayer,
          EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        }

        TileEntityGasTank tileEntity = (TileEntityGasTank) world.getTileEntity(pos);
        ItemStack stack = entityplayer.getHeldItem(hand);

        if (!stack.isEmpty()) {
            IMekWrench wrenchHandler = Wrenches.getHandler(stack);
            if (wrenchHandler != null) {
                RayTraceResult raytrace = new RayTraceResult(new Vec3d(hitX, hitY, hitZ), side, pos);
                if (wrenchHandler.canUseWrench(entityplayer, hand, stack, raytrace)) {
                    if (SecurityUtils.canAccess(entityplayer, tileEntity)) {
                        wrenchHandler.wrenchUsed(entityplayer, hand, stack, raytrace);

                        if (entityplayer.isSneaking()) {
                            dismantleBlock(state, world, pos, false);

                            return true;
                        }

                        if (tileEntity != null) {
                            int change = tileEntity.facing.rotateY().ordinal();

                            tileEntity.setFacing((short) change);
                            world.notifyNeighborsOfStateChange(pos, this, true);
                        }
                    } else {
                        SecurityUtils.displayNoAccess(entityplayer);
                    }

                    return true;
                }
            }
        }

        if (tileEntity != null) {
            if (!entityplayer.isSneaking()) {
                if (SecurityUtils.canAccess(entityplayer, tileEntity)) {
                    entityplayer.openGui(Mekanism.instance, 10, world, pos.getX(), pos.getY(), pos.getZ());
                } else {
                    SecurityUtils.displayNoAccess(entityplayer);
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public boolean removedByPlayer(@Nonnull IBlockState state, World world, @Nonnull BlockPos pos,
          @Nonnull EntityPlayer player, boolean willHarvest) {
        if (!player.capabilities.isCreativeMode && !world.isRemote && willHarvest) {
            float motion = 0.7F;
            double motionX = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
            double motionY = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
            double motionZ = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;

            EntityItem entityItem = new EntityItem(world, pos.getX() + motionX, pos.getY() + motionY,
                  pos.getZ() + motionZ, getPickBlock(state, null, world, pos, player));

            world.spawnEntity(entityItem);
        }

        return world.setBlockToAir(pos);
    }

    public ItemStack dismantleBlock(IBlockState state, World world, BlockPos pos, boolean returnBlock) {
        ItemStack itemStack = getPickBlock(state, null, world, pos, null);

        world.setBlockToAir(pos);

        if (!returnBlock) {
            float motion = 0.7F;
            double motionX = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
            double motionY = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
            double motionZ = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;

            EntityItem entityItem = new EntityItem(world, pos.getX() + motionX, pos.getY() + motionY,
                  pos.getZ() + motionZ, itemStack);

            world.spawnEntity(entityItem);
        }

        return itemStack;
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Nonnull
    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return Items.AIR;
    }

    @Override
    @Deprecated
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Nonnull
    @Override
    @Deprecated
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Nonnull
    @Override
    @Deprecated
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return TANK_BOUNDS;
    }

    @Override
    public TileEntity createNewTileEntity(@Nonnull World world, int meta) {
        return new TileEntityGasTank();
    }

    @Nonnull
    @Override
    public ItemStack getPickBlock(@Nonnull IBlockState state, RayTraceResult target, @Nonnull World world,
          @Nonnull BlockPos pos, EntityPlayer player) {
        TileEntityGasTank tileEntity = (TileEntityGasTank) world.getTileEntity(pos);
        ItemStack itemStack = new ItemStack(MekanismBlocks.GasTank);

        if (itemStack.hasTagCompound()) {
            itemStack.setTagCompound(new NBTTagCompound());
        }

        if (tileEntity != null) {
            ISecurityItem securityItem = (ISecurityItem) itemStack.getItem();

            if (securityItem.hasSecurity(itemStack)) {
                securityItem.setOwnerUUID(itemStack, ((ISecurityTile) tileEntity).getSecurity().getOwnerUUID());
                securityItem.setSecurity(itemStack, ((ISecurityTile) tileEntity).getSecurity().getMode());
            }

            ISideConfiguration config = tileEntity;

            config.getConfig().write(ItemDataUtils.getDataMap(itemStack));
            config.getEjector().write(ItemDataUtils.getDataMap(itemStack));
        }

        ITierItem tierItem = (ITierItem) itemStack.getItem();
        tierItem.setBaseTier(itemStack, tileEntity.tier.getBaseTier());

        IGasItem storageTank = (IGasItem) itemStack.getItem();
        storageTank.setGas(itemStack, tileEntity.gasTank.getGas());

        ISustainedInventory inventory = (ISustainedInventory) itemStack.getItem();
        inventory.setInventory(tileEntity.getInventory(), itemStack);

        return itemStack;
    }

    @Override
    @Deprecated
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    @Deprecated
    public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
        TileEntityGasTank tileEntity = (TileEntityGasTank) world.getTileEntity(pos);
        return tileEntity.getRedstoneLevel();
    }

    @Override
    public EnumFacing[] getValidRotations(World world, @Nonnull BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        EnumFacing[] valid = new EnumFacing[6];

        if (tile instanceof TileEntityBasicBlock) {
            TileEntityBasicBlock basicTile = (TileEntityBasicBlock) tile;

            for (EnumFacing dir : EnumFacing.VALUES) {
                if (basicTile.canSetFacing(dir.ordinal())) {
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

            if (basicTile.canSetFacing(axis.ordinal())) {
                basicTile.setFacing((short) axis.ordinal());
                return true;
            }
        }

        return false;
    }

    @Override
    @Deprecated
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    @Deprecated
    public boolean isFullCube(IBlockState state) {
        return false;
    }

	/*
	@Override
	public boolean isBlockSolid(IBlockAccess worldIn, BlockPos pos, EnumFacing side)
	{
		return false;
	}
	*/
}

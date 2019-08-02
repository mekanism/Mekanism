package mekanism.generators.common.block.generator;

import javax.annotation.Nonnull;
import mekanism.api.IMekWrench;
import mekanism.api.energy.IEnergizedItem;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.ISustainedInventory;
import mekanism.common.block.BlockMekanismContainer;
import mekanism.common.block.interfaces.IBlockElectric;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateFacing;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.wrenches.Wrenches;
import mekanism.common.security.ISecurityItem;
import mekanism.common.tile.prefab.TileEntityBasicBlock;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.tile.TileEntityGasGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockGasBurningGenerator extends BlockMekanismContainer implements IHasGui, IBlockElectric, IStateFacing {

    public BlockGasBurningGenerator() {
        super(Material.IRON);
        setHardness(3.5F);
        setResistance(8F);
        setRegistryName(new ResourceLocation(MekanismGenerators.MODID, "gas_burning_generator"));
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
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos) {
        if (!world.isRemote) {
            final TileEntity tileEntity = MekanismUtils.getTileEntity(world, pos);
            if (tileEntity instanceof TileEntityBasicBlock) {
                ((TileEntityBasicBlock) tileEntity).onNeighborChange(neighborBlock);
            }
        }
    }

    @Override
    @Deprecated
    public float getPlayerRelativeBlockHardness(IBlockState state, @Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        return SecurityUtils.canAccess(player, tile) ? super.getPlayerRelativeBlockHardness(state, player, world, pos) : 0.0F;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityplayer, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        }
        TileEntityBasicBlock tileEntity = (TileEntityBasicBlock) world.getTileEntity(pos);
        ItemStack stack = entityplayer.getHeldItem(hand);

        if (!stack.isEmpty()) {
            IMekWrench wrenchHandler = Wrenches.getHandler(stack);
            if (wrenchHandler != null) {
                RayTraceResult raytrace = new RayTraceResult(new Vec3d(hitX, hitY, hitZ), side, pos);
                if (wrenchHandler.canUseWrench(entityplayer, hand, stack, raytrace)) {
                    if (SecurityUtils.canAccess(entityplayer, tileEntity)) {
                        wrenchHandler.wrenchUsed(entityplayer, hand, stack, raytrace);
                        if (entityplayer.isSneaking()) {
                            MekanismUtils.dismantleBlock(this, state, world, pos);
                            return true;
                        }
                        if (tileEntity != null) {
                            tileEntity.setFacing(tileEntity.facing.rotateY());
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
                    entityplayer.openGui(MekanismGenerators.instance, getGuiID(), world, pos.getX(), pos.getY(), pos.getZ());
                } else {
                    SecurityUtils.displayNoAccess(entityplayer);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityGasGenerator();
    }

    @Override
    @Deprecated
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    @Deprecated
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Nonnull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Nonnull
    @Override
    protected ItemStack getDropItem(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        TileEntityGasGenerator tile = (TileEntityGasGenerator) world.getTileEntity(pos);
        ItemStack itemStack = new ItemStack(this);
        if (tile == null) {
            return itemStack;
        }
        if (!itemStack.hasTagCompound()) {
            itemStack.setTagCompound(new NBTTagCompound());
        }
        //Security
        ISecurityItem securityItem = (ISecurityItem) itemStack.getItem();
        securityItem.setOwnerUUID(itemStack, tile.getSecurity().getOwnerUUID());
        securityItem.setSecurity(itemStack, tile.getSecurity().getMode());
        //Energy
        IEnergizedItem electricItem = (IEnergizedItem) itemStack.getItem();
        electricItem.setEnergy(itemStack, tile.electricityStored);
        //Sustained Inventory
        if (tile.handleInventory()) {
            ISustainedInventory inventory = (ISustainedInventory) itemStack.getItem();
            inventory.setInventory(tile.getInventory(), itemStack);
        }
        //Sustained Data
        tile.writeSustainedData(itemStack);
        return itemStack;
    }

    @Override
    @Deprecated
    public boolean isSideSolid(IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, EnumFacing side) {
        //TODO
        return true;

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
    public boolean hasComparatorInputOverride(IBlockState blockState) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof IComparatorSupport) {
            return ((IComparatorSupport) tile).getRedstoneLevel();
        }
        return 0;
    }

    @Override
    public int getGuiID() {
        return 3;
    }

    @Override
    public double getConfigStorage() {
        return 100 * MekanismConfig.local().general.FROM_H2.val();
    }
}
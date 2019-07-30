package mekanism.common.block.machine;

import java.util.Random;
import javax.annotation.Nonnull;
import mekanism.api.IMekWrench;
import mekanism.api.energy.IEnergizedItem;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.common.Mekanism;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ISustainedData;
import mekanism.common.base.ISustainedInventory;
import mekanism.common.base.ISustainedTank;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.block.BlockMekanismContainer;
import mekanism.common.block.interfaces.IBlockActiveTextured;
import mekanism.common.block.interfaces.IBlockDescriptive;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.interfaces.IRotatableBlock;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateActive;
import mekanism.common.block.states.IStateFacing;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.wrenches.Wrenches;
import mekanism.common.security.ISecurityItem;
import mekanism.common.security.ISecurityTile;
import mekanism.common.tile.TileEntityOredictionificator;
import mekanism.common.tile.prefab.TileEntityBasicBlock;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Plane;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockOredictionificator extends BlockMekanismContainer implements IBlockActiveTextured, IRotatableBlock, IBlockDescriptive, IHasGui, IStateFacing,
      IStateActive {

    private final String name;

    public BlockOredictionificator() {
        super(Material.IRON);
        setHardness(3.5F);
        setResistance(16F);
        setCreativeTab(Mekanism.tabMekanism);
        this.name = "oredictionificator";
        setTranslationKey(this.name);
        setRegistryName(new ResourceLocation(Mekanism.MODID, this.name));
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
        return BlockStateHelper.getActualState(this, state, (TileEntityOredictionificator) MekanismUtils.getTileEntitySafe(world, pos));
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        TileEntityBasicBlock tileEntity = (TileEntityBasicBlock) world.getTileEntity(pos);
        if (tileEntity == null) {
            return;
        }

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

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random) {
        TileEntityBasicBlock tileEntity = (TileEntityBasicBlock) world.getTileEntity(pos);
        if (MekanismUtils.isActive(world, pos) && ((IActiveState) tileEntity).renderUpdate() && MekanismConfig.current().client.machineEffects.val()) {
            float xRandom = (float) pos.getX() + 0.5F;
            float yRandom = (float) pos.getY() + 0.0F + random.nextFloat() * 6.0F / 16.0F;
            float zRandom = (float) pos.getZ() + 0.5F;
            float iRandom = 0.52F;
            float jRandom = random.nextFloat() * 0.6F - 0.3F;
            EnumFacing side = tileEntity.facing;

            switch (side) {
                case WEST:
                    world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, xRandom - iRandom, yRandom, zRandom + jRandom, 0.0D, 0.0D, 0.0D);
                    world.spawnParticle(EnumParticleTypes.REDSTONE, xRandom - iRandom, yRandom, zRandom + jRandom, 0.0D, 0.0D, 0.0D);
                    break;
                case EAST:
                    world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, xRandom + iRandom, yRandom, zRandom + jRandom, 0.0D, 0.0D, 0.0D);
                    world.spawnParticle(EnumParticleTypes.REDSTONE, xRandom + iRandom, yRandom, zRandom + jRandom, 0.0D, 0.0D, 0.0D);
                    break;
                case NORTH:
                    world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, xRandom + jRandom, yRandom, zRandom - iRandom, 0.0D, 0.0D, 0.0D);
                    world.spawnParticle(EnumParticleTypes.REDSTONE, xRandom + jRandom, yRandom, zRandom - iRandom, 0.0D, 0.0D, 0.0D);
                    break;
                case SOUTH:
                    world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, xRandom + jRandom, yRandom, zRandom + iRandom, 0.0D, 0.0D, 0.0D);
                    world.spawnParticle(EnumParticleTypes.REDSTONE, xRandom + jRandom, yRandom, zRandom + iRandom, 0.0D, 0.0D, 0.0D);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (MekanismConfig.current().client.enableAmbientLighting.val()) {
            TileEntity tileEntity = MekanismUtils.getTileEntitySafe(world, pos);
            if (tileEntity instanceof IActiveState && ((IActiveState) tileEntity).lightUpdate() && ((IActiveState) tileEntity).wasActiveRecently()) {
                return MekanismConfig.current().client.ambientLightingLevel.val();
            }
        }
        return 0;
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
                            EnumFacing change = tileEntity.facing.rotateY();
                            tileEntity.setFacing(change);
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
                    entityplayer.openGui(Mekanism.instance, getGuiID(), world, pos.getX(), pos.getY(), pos.getZ());
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
        return new TileEntityOredictionificator();
    }

    @Override
    public TileEntity createNewTileEntity(@Nonnull World world, int metadata) {
        return null;
    }

    @Override
    @Deprecated
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Nonnull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    @Deprecated
    public float getPlayerRelativeBlockHardness(IBlockState state, @Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        return SecurityUtils.canAccess(player, tile) ? super.getPlayerRelativeBlockHardness(state, player, world, pos) : 0.0F;
    }

    @Override
    public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
        //TODO: This is how it was before, but should it be divided by 5 like in Block.java
        return blockResistance;
    }

    @Override
    @Deprecated
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    @Deprecated
    public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IComparatorSupport) {
            return ((IComparatorSupport) tileEntity).getRedstoneLevel();
        }
        return 0;
    }

    @Override
    @Deprecated
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos) {
        if (!world.isRemote) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof TileEntityBasicBlock) {
                ((TileEntityBasicBlock) tileEntity).onNeighborChange(neighborBlock);
            }
        }
    }

    @Nonnull
    @Override
    protected ItemStack getDropItem(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        TileEntityBasicBlock tileEntity = (TileEntityBasicBlock) world.getTileEntity(pos);
        ItemStack itemStack = new ItemStack(this);
        if (itemStack.getTagCompound() == null) {
            itemStack.setTagCompound(new NBTTagCompound());
        }
        if (tileEntity instanceof ISecurityTile) {
            ISecurityItem securityItem = (ISecurityItem) itemStack.getItem();
            securityItem.setOwnerUUID(itemStack, ((ISecurityTile) tileEntity).getSecurity().getOwnerUUID());
            securityItem.setSecurity(itemStack, ((ISecurityTile) tileEntity).getSecurity().getMode());
        }
        if (tileEntity instanceof IUpgradeTile) {
            ((IUpgradeTile) tileEntity).getComponent().write(ItemDataUtils.getDataMap(itemStack));
        }
        if (tileEntity instanceof ISideConfiguration) {
            ISideConfiguration config = (ISideConfiguration) tileEntity;
            config.getConfig().write(ItemDataUtils.getDataMap(itemStack));
            config.getEjector().write(ItemDataUtils.getDataMap(itemStack));
        }
        if (tileEntity instanceof ISustainedData) {
            ((ISustainedData) tileEntity).writeSustainedData(itemStack);
        }
        if (tileEntity instanceof IRedstoneControl) {
            IRedstoneControl control = (IRedstoneControl) tileEntity;
            ItemDataUtils.setInt(itemStack, "controlType", control.getControlType().ordinal());
        }
        if (tileEntity instanceof TileEntityContainerBlock && ((TileEntityContainerBlock) tileEntity).inventory.size() > 0) {
            ISustainedInventory inventory = (ISustainedInventory) itemStack.getItem();
            inventory.setInventory(((ISustainedInventory) tileEntity).getInventory(), itemStack);
        }
        if (((ISustainedTank) itemStack.getItem()).hasTank(itemStack)) {
            if (tileEntity instanceof ISustainedTank) {
                if (((ISustainedTank) tileEntity).getFluidStack() != null) {
                    ((ISustainedTank) itemStack.getItem()).setFluidStack(((ISustainedTank) tileEntity).getFluidStack(), itemStack);
                }
            }
        }
        //this MUST be done after the factory info is saved, as it caps the energy to max, which is based on the recipe type
        if (tileEntity instanceof IStrictEnergyStorage) {
            IEnergizedItem energizedItem = (IEnergizedItem) itemStack.getItem();
            energizedItem.setEnergy(itemStack, ((IStrictEnergyStorage) tileEntity).getEnergy());
        }
        return itemStack;
    }

    @Override
    @Deprecated
    public boolean isFullCube(IBlockState state) {
        return false;
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
    public int getGuiID() {
        return 52;
    }
}
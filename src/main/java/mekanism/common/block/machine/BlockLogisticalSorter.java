package mekanism.common.block.machine;

import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.IMekWrench;
import mekanism.common.Mekanism;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.block.BlockMekanismContainer;
import mekanism.common.block.interfaces.IBlockSound;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.interfaces.IHasInventory;
import mekanism.common.block.interfaces.IHasModel;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.interfaces.ISupportsRedstone;
import mekanism.common.block.interfaces.ISupportsUpgrades;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateActive;
import mekanism.common.block.states.IStateFacing;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.wrenches.Wrenches;
import mekanism.common.network.PacketLogisticalSorterGui.LogisticalSorterGuiMessage;
import mekanism.common.network.PacketLogisticalSorterGui.SorterGuiPacket;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MultipartUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockLogisticalSorter extends BlockMekanismContainer implements IHasModel, IHasGui, ISupportsUpgrades, IStateFacing, IStateActive, IBlockSound, IHasInventory,
      ISupportsRedstone, IHasTileEntity<TileEntityLogisticalSorter> {

    private static final AxisAlignedBB LOGISTICAL_SORTER_BOUNDS = new AxisAlignedBB(0.125F, 0.0F, 0.125F, 0.875F, 1.0F, 0.875F);
    private static final SoundEvent SOUND_EVENT = new SoundEvent(new ResourceLocation(Mekanism.MODID, "tile.machine.logisticalsorter"));

    public BlockLogisticalSorter() {
        super(Material.IRON);
        setHardness(3.5F);
        setResistance(16F);
        setRegistryName(new ResourceLocation(Mekanism.MODID, "logistical_sorter"));
    }

    @Override
    public boolean supportsAll() {
        return true;
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
    public void setTileData(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack, @Nonnull TileEntityMekanism tile) {
        if (tile instanceof TileEntityLogisticalSorter) {
            TileEntityLogisticalSorter transporter = (TileEntityLogisticalSorter) tile;
            if (!transporter.hasConnectedInventory()) {
                for (EnumFacing dir : EnumFacing.values()) {
                    TileEntity tileEntity = Coord4D.get(transporter).offset(dir).getTileEntity(world);
                    if (InventoryUtils.isItemHandler(tileEntity, dir)) {
                        transporter.setFacing(dir.getOpposite());
                        break;
                    }
                }
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random) {
        TileEntityMekanism tileEntity = (TileEntityMekanism) world.getTileEntity(pos);
        if (MekanismUtils.isActive(world, pos) && ((IActiveState) tileEntity).renderUpdate() && MekanismConfig.current().client.machineEffects.val()) {
            float xRandom = (float) pos.getX() + 0.5F;
            float yRandom = (float) pos.getY() + 0.0F + random.nextFloat() * 6.0F / 16.0F;
            float zRandom = (float) pos.getZ() + 0.5F;
            float iRandom = 0.52F;
            float jRandom = random.nextFloat() * 0.6F - 0.3F;
            EnumFacing side = tileEntity.getDirection();

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
        //TODO: Make this be moved into the logistical sorter tile
        TileEntityMekanism tileEntity = (TileEntityMekanism) world.getTileEntity(pos);
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
                            EnumFacing change = tileEntity.getDirection().rotateY();
                            if (tileEntity instanceof TileEntityLogisticalSorter) {
                                if (!((TileEntityLogisticalSorter) tileEntity).hasConnectedInventory()) {
                                    for (EnumFacing dir : EnumFacing.values()) {
                                        TileEntity tile = Coord4D.get(tileEntity).offset(dir).getTileEntity(world);
                                        if (InventoryUtils.isItemHandler(tile, dir)) {
                                            change = dir.getOpposite();
                                            break;
                                        }
                                    }
                                }
                            }
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

        if (!entityplayer.isSneaking()) {
            if (SecurityUtils.canAccess(entityplayer, tileEntity)) {
                LogisticalSorterGuiMessage.openServerGui(SorterGuiPacket.SERVER, 0, world, (EntityPlayerMP) entityplayer, Coord4D.get(tileEntity), -1);
            } else {
                SecurityUtils.displayNoAccess(entityplayer);
            }
            return true;
        }
        return false;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityLogisticalSorter();
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
            if (tileEntity instanceof TileEntityMekanism) {
                ((TileEntityMekanism) tileEntity).onNeighborChange(neighborBlock);
            }
            if (tileEntity instanceof TileEntityLogisticalSorter) {
                TileEntityLogisticalSorter sorter = (TileEntityLogisticalSorter) tileEntity;
                if (!sorter.hasConnectedInventory()) {
                    for (EnumFacing dir : EnumFacing.values()) {
                        TileEntity tile = Coord4D.get(tileEntity).offset(dir).getTileEntity(world);
                        if (InventoryUtils.isItemHandler(tile, dir)) {
                            sorter.setFacing(dir.getOpposite());
                            return;
                        }
                    }
                }
            }
        }
    }

    @Nonnull
    @Override
    @Deprecated
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tile = MekanismUtils.getTileEntitySafe(world, pos);
        if (tile instanceof TileEntityLogisticalSorter) {
            return MultipartUtils.rotate(LOGISTICAL_SORTER_BOUNDS.offset(-0.5, -0.5, -0.5), ((TileEntityLogisticalSorter) tile).getDirection()).offset(0.5, 0.5, 0.5);
        }
        return super.getBoundingBox(state, world, pos);
    }

    @Override
    @Deprecated
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    @Deprecated
    public boolean isSideSolid(IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, EnumFacing side) {
        //TODO: This was true before, but I think it should be false?
        return true;
    }

    @Nonnull
    @Override
    @Deprecated
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public int getGuiID() {
        return 59;
    }

    @Nonnull
    @Override
    public SoundEvent getSoundEvent() {
        return SOUND_EVENT;
    }

    @Override
    public int getInventorySize() {
        return 2;
    }

    @Nullable
    @Override
    public Class<? extends TileEntityLogisticalSorter> getTileClass() {
        return TileEntityLogisticalSorter.class;
    }
}
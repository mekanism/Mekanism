package mekanism.common.block.transmitter;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IMekWrench;
import mekanism.client.render.particle.MekanismParticleHelper;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockTileDrops;
import mekanism.common.block.property.PropertyConnection;
import mekanism.common.block.states.BlockStateTransmitter;
import mekanism.common.integration.multipart.MultipartMekanism;
import mekanism.common.integration.wrenches.Wrenches;
import mekanism.common.tile.transmitter.TileEntitySidedPipe;
import mekanism.common.tile.transmitter.TileEntitySidedPipe.ConnectionType;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.obj.OBJModel.OBJProperty;
import net.minecraftforge.client.model.obj.OBJModel.OBJState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class BlockTransmitter extends BlockTileDrops implements ITileEntityProvider {

    protected final String name;

    protected BlockTransmitter(String name) {
        super(Material.PISTON);
        setCreativeTab(Mekanism.tabMekanism);
        setHardness(1F);
        setResistance(10F);
        this.name = name;
        setTranslationKey(this.name);
        setRegistryName(new ResourceLocation(Mekanism.MODID, this.name));
    }

    protected static TileEntitySidedPipe getTileEntitySidedPipe(IBlockAccess world, BlockPos pos) {
        TileEntity tileEntity = MekanismUtils.getTileEntitySafe(world, pos);
        TileEntitySidedPipe sidedPipe = null;
        if (tileEntity instanceof TileEntitySidedPipe) {
            sidedPipe = (TileEntitySidedPipe) tileEntity;
        } else if (Mekanism.hooks.MCMPLoaded) {
            TileEntity childEntity = MultipartMekanism.unwrapTileEntity(world);
            if (childEntity instanceof TileEntitySidedPipe) {
                sidedPipe = (TileEntitySidedPipe) childEntity;
            }
        }
        return sidedPipe;
    }

    @Nonnull
    @Override
    @Deprecated
    public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        TileEntitySidedPipe tile = getTileEntitySidedPipe(worldIn, pos);
        if (tile != null) {
            state = state.withProperty(BlockStateTransmitter.tierProperty, tile.getBaseTier());
        }
        return state;
    }

    @Nonnull
    @Override
    public BlockStateContainer createBlockState() {
        return new BlockStateTransmitter(this);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        //TODO
        return 0;
    }

    @SideOnly(Side.CLIENT)
    @Nonnull
    @Override
    public IBlockState getExtendedState(@Nonnull IBlockState state, IBlockAccess w, BlockPos pos) {
        TileEntitySidedPipe tile = getTileEntitySidedPipe(w, pos);
        if (tile != null) {
            return tile.getExtendedState(state);
        }
        ConnectionType[] typeArray = new ConnectionType[]{ConnectionType.NORMAL, ConnectionType.NORMAL, ConnectionType.NORMAL,
                                                          ConnectionType.NORMAL, ConnectionType.NORMAL, ConnectionType.NORMAL};
        PropertyConnection connectionProp = new PropertyConnection((byte) 0, (byte) 0, typeArray, true);
        return ((IExtendedBlockState) state).withProperty(OBJProperty.INSTANCE, new OBJState(Collections.emptyList(), true)).withProperty(PropertyConnection.INSTANCE, connectionProp);
    }

    @Override
    @Deprecated
    public void addCollisionBoxToList(IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB entityBox,
          @Nonnull List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean b) {
        TileEntitySidedPipe tile = getTileEntitySidedPipe(world, pos);
        if (tile != null) {
            List<AxisAlignedBB> boxes = tile.getCollisionBoxes(entityBox.offset(-pos.getX(), -pos.getY(), -pos.getZ()));
            for (AxisAlignedBB box : boxes) {
                collidingBoxes.add(box.offset(pos));
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.isEmpty()) {
            return false;
        }
        IMekWrench wrenchHandler = Wrenches.getHandler(stack);
        if (wrenchHandler != null) {
            RayTraceResult raytrace = new RayTraceResult(new Vec3d(hitX, hitY, hitZ), facing, pos);
            if (wrenchHandler.canUseWrench(player, hand, stack, raytrace) && player.isSneaking()) {
                if (!world.isRemote) {
                    MekanismUtils.dismantleBlock(this, state, world, pos);
                }
                return true;
            }
        }
        return false;
    }

    @Nonnull
    @Override
    protected ItemStack getDropItem(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        return new ItemStack(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addHitEffects(IBlockState state, World world, RayTraceResult target, ParticleManager manager) {
        //TODO: This probably isn't needed anymore?
        if (!target.typeOfHit.equals(Type.BLOCK)) {
            return super.addHitEffects(state, world, target, manager);
        }
        return MekanismParticleHelper.addBlockHitEffects(world, target.getBlockPos(), target.sideHit, manager) || super.addHitEffects(state, world, target, manager);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        TileEntitySidedPipe tile = getTileEntitySidedPipe(world, pos);
        if (tile != null) {
            tile.onAdded();
        }
    }

    @Override
    @Deprecated
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos neighbor) {
        TileEntitySidedPipe tile = getTileEntitySidedPipe(world, pos);
        if (tile != null) {
            EnumFacing side = EnumFacing.getFacingFromVector(neighbor.getX() - pos.getX(), neighbor.getY() - pos.getY(), neighbor.getZ() - pos.getZ());
            tile.onNeighborBlockChange(side);
        }
    }

    @Override
    public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
        TileEntitySidedPipe tile = getTileEntitySidedPipe(world, pos);
        if (tile != null) {
            EnumFacing side = EnumFacing.getFacingFromVector(neighbor.getX() - pos.getX(), neighbor.getY() - pos.getY(), neighbor.getZ() - pos.getZ());
            tile.onNeighborTileChange(side);
        }
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT;
    }

    @Override
    @Deprecated
    public boolean isBlockNormalCube(IBlockState state) {
        return false;
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

    @Override
    @Deprecated
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Nonnull
    @Override
    @Deprecated
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }
}
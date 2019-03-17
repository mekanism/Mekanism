package mekanism.common.block;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IMekWrench;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.Tier.BaseTier;
import mekanism.common.base.ITierItem;
import mekanism.common.block.property.PropertyConnection;
import mekanism.common.block.states.BlockStateTransmitter;
import mekanism.common.block.states.BlockStateTransmitter.TransmitterType;
import mekanism.common.block.states.BlockStateTransmitter.TransmitterType.Size;
import mekanism.common.integration.multipart.MultipartMekanism;
import mekanism.common.integration.wrenches.Wrenches;
import mekanism.common.tile.transmitter.TileEntityDiversionTransporter;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporter;
import mekanism.common.tile.transmitter.TileEntityMechanicalPipe;
import mekanism.common.tile.transmitter.TileEntityPressurizedTube;
import mekanism.common.tile.transmitter.TileEntityRestrictiveTransporter;
import mekanism.common.tile.transmitter.TileEntitySidedPipe;
import mekanism.common.tile.transmitter.TileEntitySidedPipe.ConnectionType;
import mekanism.common.tile.transmitter.TileEntityThermodynamicConductor;
import mekanism.common.tile.transmitter.TileEntityUniversalCable;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MultipartUtils;
import mekanism.common.util.MultipartUtils.AdvancedRayTraceResult;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.obj.OBJModel.OBJProperty;
import net.minecraftforge.client.model.obj.OBJModel.OBJState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockTransmitter extends Block implements ITileEntityProvider {

    public static AxisAlignedBB[] smallSides = new AxisAlignedBB[7];
    public static AxisAlignedBB[] largeSides = new AxisAlignedBB[7];

    public static AxisAlignedBB smallDefault;
    public static AxisAlignedBB largeDefault;

    static {
        smallSides[0] = new AxisAlignedBB(0.3, 0.0, 0.3, 0.7, 0.3, 0.7);
        smallSides[1] = new AxisAlignedBB(0.3, 0.7, 0.3, 0.7, 1.0, 0.7);
        smallSides[2] = new AxisAlignedBB(0.3, 0.3, 0.0, 0.7, 0.7, 0.3);
        smallSides[3] = new AxisAlignedBB(0.3, 0.3, 0.7, 0.7, 0.7, 1.0);
        smallSides[4] = new AxisAlignedBB(0.0, 0.3, 0.3, 0.3, 0.7, 0.7);
        smallSides[5] = new AxisAlignedBB(0.7, 0.3, 0.3, 1.0, 0.7, 0.7);
        smallSides[6] = new AxisAlignedBB(0.3, 0.3, 0.3, 0.7, 0.7, 0.7);

        largeSides[0] = new AxisAlignedBB(0.25, 0.0, 0.25, 0.75, 0.25, 0.75);
        largeSides[1] = new AxisAlignedBB(0.25, 0.75, 0.25, 0.75, 1.0, 0.75);
        largeSides[2] = new AxisAlignedBB(0.25, 0.25, 0.0, 0.75, 0.75, 0.25);
        largeSides[3] = new AxisAlignedBB(0.25, 0.25, 0.75, 0.75, 0.75, 1.0);
        largeSides[4] = new AxisAlignedBB(0.0, 0.25, 0.25, 0.25, 0.75, 0.75);
        largeSides[5] = new AxisAlignedBB(0.75, 0.25, 0.25, 1.0, 0.75, 0.75);
        largeSides[6] = new AxisAlignedBB(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);

        smallDefault = smallSides[6];
        largeDefault = largeSides[6];
    }

    public BlockTransmitter() {
        super(Material.PISTON);
        setCreativeTab(Mekanism.tabMekanism);
        setHardness(1F);
        setResistance(10F);
    }

    private static AxisAlignedBB getDefaultForTile(TileEntitySidedPipe tile) {
        if (tile == null || tile.getTransmitterType().getSize() == Size.SMALL) {
            return smallDefault;
        }

        return largeDefault;
    }

    private static void setDefaultForTile(TileEntitySidedPipe tile, AxisAlignedBB box) {
        if (tile == null) {
            return;
        }

        if (box == null) {
            throw new IllegalStateException("box should not be null");
        }

        if (tile.getTransmitterType().getSize() == Size.SMALL) {
            smallDefault = box;
            return;
        }

        largeDefault = box;
    }

    private static TileEntitySidedPipe getTileEntitySidedPipe(IBlockAccess world, BlockPos pos) {
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
    @Deprecated
    public IBlockState getStateFromMeta(int meta) {
        TransmitterType type = TransmitterType.get(meta);
        return getDefaultState().withProperty(BlockStateTransmitter.typeProperty, type);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        TransmitterType type = state.getValue(BlockStateTransmitter.typeProperty);
        return type.ordinal();
    }

    @Nonnull
    @Override
    public BlockStateContainer createBlockState() {
        return new BlockStateTransmitter(this);
    }

    @SideOnly(Side.CLIENT)
    @Nonnull
    @Override
    public IBlockState getExtendedState(@Nonnull IBlockState state, IBlockAccess w, BlockPos pos) {
        TileEntitySidedPipe tile = getTileEntitySidedPipe(w, pos);

        if (tile != null) {
            return tile.getExtendedState(state);
        } else {
            ConnectionType[] typeArray = new ConnectionType[]{ConnectionType.NORMAL, ConnectionType.NORMAL,
                  ConnectionType.NORMAL,
                  ConnectionType.NORMAL, ConnectionType.NORMAL, ConnectionType.NORMAL};
            PropertyConnection connectionProp = new PropertyConnection((byte) 0, (byte) 0, typeArray, true);

            return ((IExtendedBlockState) state)
                  .withProperty(OBJProperty.INSTANCE, new OBJState(Collections.emptyList(), true))
                  .withProperty(PropertyConnection.INSTANCE, connectionProp);
        }
    }

    @Nonnull
    @Override
    @Deprecated
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntitySidedPipe tile = getTileEntitySidedPipe(world, pos);

        if (tile != null && tile.getTransmitterType().getSize() == Size.SMALL) {
            return smallSides[6];
        }

        return largeSides[6];
    }

    @Override
    @Deprecated
    public void addCollisionBoxToList(IBlockState state, @Nonnull World world, @Nonnull BlockPos pos,
          @Nonnull AxisAlignedBB entityBox, @Nonnull List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn,
          boolean b) {
        TileEntitySidedPipe tile = getTileEntitySidedPipe(world, pos);

        if (tile != null) {
            List<AxisAlignedBB> boxes = tile.getCollisionBoxes(entityBox.offset(-pos.getX(), -pos.getY(), -pos.getZ()));

            for (AxisAlignedBB box : boxes) {
                collidingBoxes.add(box.offset(pos));
            }
        }
    }

    @Nonnull
    @Override
    @Deprecated
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, @Nonnull World world, @Nonnull BlockPos pos) {
        return getDefaultForTile(getTileEntitySidedPipe(world, pos)).offset(pos);
    }

    @Override
    @Deprecated
    public RayTraceResult collisionRayTrace(IBlockState blockState, @Nonnull World world, @Nonnull BlockPos pos,
          @Nonnull Vec3d start, @Nonnull Vec3d end) {
        TileEntitySidedPipe tile = getTileEntitySidedPipe(world, pos);

        if (tile == null) {
            return null;
        }

        List<AxisAlignedBB> boxes = tile.getCollisionBoxes();
        AdvancedRayTraceResult result = MultipartUtils.collisionRayTrace(pos, start, end, boxes);

        if (result != null && result.valid()) {
            setDefaultForTile(tile, result.bounds);
        }

        return result != null ? result.hit : null;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
          EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);

        if (stack.isEmpty()) {
            return false;
        }

        IMekWrench wrenchHandler = Wrenches.getHandler(stack);
        if (wrenchHandler != null) {
            RayTraceResult raytrace = new RayTraceResult(new Vec3d(hitX, hitY, hitZ), facing, pos);
            if (wrenchHandler.canUseWrench(player, hand, stack, raytrace) && player.isSneaking()) {
                if (!world.isRemote) {
                    dismantleBlock(state, world, pos, false);
                }

                return true;
            }
        }

        return false;
    }

    @Nonnull
    @Override
    public ItemStack getPickBlock(@Nonnull IBlockState state, RayTraceResult target, @Nonnull World world,
          @Nonnull BlockPos pos, EntityPlayer player) {
        ItemStack itemStack = ItemStack.EMPTY;
        TileEntitySidedPipe tileEntity = getTileEntitySidedPipe(world, pos);
        if (tileEntity != null) {
            itemStack = new ItemStack(MekanismBlocks.Transmitter, 1, tileEntity.getTransmitterType().ordinal());

            if (!itemStack.hasTagCompound()) {
                itemStack.setTagCompound(new NBTTagCompound());
            }

            ITierItem tierItem = (ITierItem) itemStack.getItem();
            tierItem.setBaseTier(itemStack, tileEntity.getBaseTier());
        }

        return itemStack;
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
    public void getSubBlocks(CreativeTabs creativetabs, NonNullList<ItemStack> list) {
        for (TransmitterType type : TransmitterType.values()) {
            if (type.hasTiers()) {
                for (BaseTier tier : BaseTier.values()) {
                    if (tier.isObtainable()) {
                        list.add(MekanismUtils.getTransmitter(type, tier, 1));
                    }
                }
            } else {
                list.add(MekanismUtils.getTransmitter(type, BaseTier.BASIC, 1));
            }
        }
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer,
          ItemStack stack) {
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
            EnumFacing side = EnumFacing.getFacingFromVector(neighbor.getX() - pos.getX(), neighbor.getY() - pos.getY(),
                  neighbor.getZ() - pos.getZ());
            tile.onNeighborBlockChange(side);
        }
    }

    @Override
    public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
        TileEntitySidedPipe tile = getTileEntitySidedPipe(world, pos);
        if (tile != null) {
            EnumFacing side = EnumFacing.getFacingFromVector(neighbor.getX() - pos.getX(), neighbor.getY() - pos.getY(),
                  neighbor.getZ() - pos.getZ());
            tile.onNeighborTileChange(side);
        }
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        TransmitterType type = state.getValue(BlockStateTransmitter.typeProperty);

        if (layer == BlockRenderLayer.TRANSLUCENT && (type == TransmitterType.LOGISTICAL_TRANSPORTER
              || type == TransmitterType.DIVERSION_TRANSPORTER)) {
            return true;
        }

        return layer == BlockRenderLayer.CUTOUT;
    }

    @Nonnull
    @Override
    @Deprecated
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
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

    @Override
    public int quantityDropped(Random random) {
        return 0;
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

        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) {
        TransmitterType type = TransmitterType.get(meta);

        switch (type) {
            case UNIVERSAL_CABLE:
                return new TileEntityUniversalCable();
            case MECHANICAL_PIPE:
                return new TileEntityMechanicalPipe();
            case PRESSURIZED_TUBE:
                return new TileEntityPressurizedTube();
            case LOGISTICAL_TRANSPORTER:
                return new TileEntityLogisticalTransporter();
            case DIVERSION_TRANSPORTER:
                return new TileEntityDiversionTransporter();
            case RESTRICTIVE_TRANSPORTER:
                return new TileEntityRestrictiveTransporter();
            case THERMODYNAMIC_CONDUCTOR:
                return new TileEntityThermodynamicConductor();
        }

        return null;
    }
}

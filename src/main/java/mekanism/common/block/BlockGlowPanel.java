package mekanism.common.block;

import java.util.Random;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.block.property.PropertyColor;
import mekanism.common.block.states.BlockStateFacing;
import mekanism.common.block.states.BlockStateGlowPanel;
import mekanism.common.integration.multipart.MultipartMekanism;
import mekanism.common.tile.TileEntityGlowPanel;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MultipartUtils;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockGlowPanel extends Block implements ITileEntityProvider {

    public static AxisAlignedBB[] bounds = new AxisAlignedBB[6];
    private static Random rand = new Random();

    static {
        AxisAlignedBB cuboid = new AxisAlignedBB(0.25, 0, 0.25, 0.75, 0.125, 0.75);
        Vec3d fromOrigin = new Vec3d(-0.5, -0.5, -0.5);

        for (EnumFacing side : EnumFacing.VALUES) {
            bounds[side.ordinal()] = MultipartUtils
                  .rotate(cuboid.offset(fromOrigin.x, fromOrigin.y, fromOrigin.z), side)
                  .offset(-fromOrigin.x, -fromOrigin.z, -fromOrigin.z);
        }
    }

    public BlockGlowPanel() {
        super(Material.PISTON);
        setCreativeTab(Mekanism.tabMekanism);
        setHardness(1F);
        setResistance(10F);
    }

    public static boolean canStay(IBlockAccess world, BlockPos pos) {
        boolean canStay = false;

        if (Mekanism.hooks.MCMPLoaded) {
            canStay = MultipartMekanism.hasCenterSlot(world, pos);
        }

        if (!canStay) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof TileEntityGlowPanel) {
                TileEntityGlowPanel glowPanel = (TileEntityGlowPanel) tileEntity;
                Coord4D adj = new Coord4D(glowPanel.getPos().offset(glowPanel.side), glowPanel.getWorld());
                canStay = glowPanel.getWorld().isSideSolid(adj.getPos(), glowPanel.side.getOpposite());
            }
        }

        return canStay;
    }

    private static TileEntityGlowPanel getTileEntityGlowPanel(IBlockAccess world, BlockPos pos) {
        TileEntity tileEntity = MekanismUtils.getTileEntitySafe(world, pos);
        TileEntityGlowPanel glowPanel = null;
        if (tileEntity instanceof TileEntityGlowPanel) {
            glowPanel = (TileEntityGlowPanel) tileEntity;
        } else if (Mekanism.hooks.MCMPLoaded) {
            TileEntity childEntity = MultipartMekanism.unwrapTileEntity(world);
            if (childEntity instanceof TileEntityGlowPanel) {
                glowPanel = (TileEntityGlowPanel) childEntity;
            }
        }

        return glowPanel;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Nonnull
    @Override
    public BlockStateContainer createBlockState() {
        return new BlockStateGlowPanel(this);
    }

    @Nonnull
    @Override
    @Deprecated
    public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntityGlowPanel tileEntity = getTileEntityGlowPanel(world, pos);
        return tileEntity != null ? state.withProperty(BlockStateFacing.facingProperty, tileEntity.side) : state;
    }

    @SideOnly(Side.CLIENT)
    @Nonnull
    @Override
    public IBlockState getExtendedState(@Nonnull IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntityGlowPanel tileEntity = getTileEntityGlowPanel(world, pos);

        if (tileEntity != null) {
            state = state.withProperty(BlockStateFacing.facingProperty, tileEntity.side);

            if (state instanceof IExtendedBlockState) {
                return ((IExtendedBlockState) state)
                      .withProperty(PropertyColor.INSTANCE, new PropertyColor(tileEntity.colour));
            }
        }

        return state;
    }

    @Override
    @Deprecated
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos neighbor) {
        TileEntityGlowPanel tileEntity = getTileEntityGlowPanel(world, pos);

        if (tileEntity != null && !world.isRemote && !canStay(world, pos)) {
            float motion = 0.7F;
            double motionX = (rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
            double motionY = (rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
            double motionZ = (rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;

            ItemStack stack = new ItemStack(MekanismBlocks.GlowPanel, 1, tileEntity.colour.getMetaValue());
            EntityItem entityItem = new EntityItem(world, pos.getX() + motionX, pos.getY() + motionY,
                  pos.getZ() + motionZ, stack);

            world.spawnEntity(entityItem);
            world.setBlockToAir(pos);
        }
    }

    @Override
    public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
        TileEntityGlowPanel tileEntity = getTileEntityGlowPanel(world, pos);

        if (tileEntity != null && !tileEntity.getWorld().isRemote && !canStay(world, pos)) {
            float motion = 0.7F;
            double motionX = (rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
            double motionY = (rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
            double motionZ = (rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;

            ItemStack stack = new ItemStack(MekanismBlocks.GlowPanel, 1, tileEntity.colour.getMetaValue());
            EntityItem entityItem = new EntityItem(tileEntity.getWorld(), pos.getX() + motionX, pos.getY() + motionY,
                  pos.getZ() + motionZ, stack);

            tileEntity.getWorld().spawnEntity(entityItem);
            tileEntity.getWorld().setBlockToAir(pos);
        }
    }

    @Nonnull
    @Override
    @Deprecated
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntityGlowPanel tileEntity = getTileEntityGlowPanel(world, pos);

        if (tileEntity != null) {
            return bounds[tileEntity.side.ordinal()];
        }

        return super.getBoundingBox(state, world, pos);
    }

    @Override
    public boolean canPlaceBlockOnSide(@Nonnull World world, @Nonnull BlockPos pos, EnumFacing side) {
        return world.isSideSolid(pos.offset(side.getOpposite()), side);
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return 15;
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Nonnull
    @Override
    public ItemStack getPickBlock(@Nonnull IBlockState state, RayTraceResult target, @Nonnull World world,
          @Nonnull BlockPos pos, EntityPlayer player) {
        TileEntityGlowPanel tileEntity = (TileEntityGlowPanel) world.getTileEntity(pos);
        return new ItemStack(MekanismBlocks.GlowPanel, 1, tileEntity.colour.getMetaValue());
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
        return new TileEntityGlowPanel();
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return true;
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
}

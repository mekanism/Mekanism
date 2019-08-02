package mekanism.common.block;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.block.interfaces.IBlockOreDict;
import mekanism.common.block.interfaces.IColoredBlock;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateFacing;
import mekanism.common.integration.multipart.MultipartMekanism;
import mekanism.common.tile.TileEntityGlowPanel;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MultipartUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockGlowPanel extends BlockTileDrops implements IBlockOreDict, IStateFacing, IColoredBlock {

    public static AxisAlignedBB[] bounds = new AxisAlignedBB[6];

    static {
        AxisAlignedBB cuboid = new AxisAlignedBB(0.25, 0, 0.25, 0.75, 0.125, 0.75);
        Vec3d fromOrigin = new Vec3d(-0.5, -0.5, -0.5);
        for (EnumFacing side : EnumFacing.VALUES) {
            bounds[side.ordinal()] = MultipartUtils.rotate(cuboid.offset(fromOrigin.x, fromOrigin.y, fromOrigin.z), side).offset(-fromOrigin.x, -fromOrigin.z, -fromOrigin.z);
        }
    }

    private final EnumColor color;

    public BlockGlowPanel(EnumColor color) {
        super(Material.PISTON);
        this.color = color;
        setHardness(1F);
        setResistance(10F);
        //It gets multiplied by 15 when being set
        setLightLevel(1);
        setRegistryName(new ResourceLocation(Mekanism.MODID, color.registry_prefix + "_glow_panel"));
    }

    @Override
    public List<String> getOredictEntries() {
        List<String> entries = new ArrayList<>();
        entries.add("glowPanel");
        if (color.dyeName != null) {
            //As of the moment none of the colors used have a null dye name but if the other ones get used this is needed
            entries.add("glowPanel" + color.dyeName);
        }
        return entries;
    }

    @Override
    public boolean supportsAll() {
        return true;
    }

    @Override
    public EnumColor getColor() {
        return color;
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
        return BlockStateHelper.getActualState(this, state, getTileEntityGlowPanel(world, pos));
    }

    @Override
    @Deprecated
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos neighbor) {
        TileEntityGlowPanel tileEntity = getTileEntityGlowPanel(world, pos);
        if (tileEntity != null && !world.isRemote && !canStay(world, pos)) {
            dropBlockAsItem(world, pos, world.getBlockState(pos), 0);
            world.setBlockToAir(pos);
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
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityGlowPanel();
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return true;
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
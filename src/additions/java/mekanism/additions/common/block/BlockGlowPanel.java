package mekanism.additions.common.block;

import javax.annotation.Nonnull;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.tile.AdditionsTileEntityTypes;
import mekanism.additions.common.tile.TileEntityGlowPanel;
import mekanism.api.Coord4D;
import mekanism.api.block.IColoredBlock;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.text.EnumColor;
import mekanism.common.block.BlockTileDrops;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateFacing;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MultipartUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.state.DirectionProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

//TODO: I don't think the Glow Panel needs a tile entity anymore
public class BlockGlowPanel extends BlockTileDrops implements IStateFacing, IColoredBlock, IHasTileEntity<TileEntityGlowPanel> {

    public static VoxelShape[] bounds = new VoxelShape[6];

    static {
        AxisAlignedBB cuboid = new AxisAlignedBB(0.25, 0, 0.25, 0.75, 0.125, 0.75);
        Vec3d fromOrigin = new Vec3d(-0.5, -0.5, -0.5);
        for (Direction side : Direction.values()) {
            bounds[side.ordinal()] = VoxelShapes.create(MultipartUtils.rotate(cuboid.offset(fromOrigin.x, fromOrigin.y, fromOrigin.z), side)
                  .offset(-fromOrigin.x, -fromOrigin.z, -fromOrigin.z));
        }
    }

    private final EnumColor color;

    public BlockGlowPanel(EnumColor color) {
        super(Block.Properties.create(Material.PISTON, color.getMapColor()).hardnessAndResistance(1F, 10F).lightValue(15));
        this.color = color;
        setRegistryName(new ResourceLocation(MekanismAdditions.MODID, color.registry_prefix + "_glow_panel"));
    }

    @Nonnull
    @Override
    public DirectionProperty getFacingProperty() {
        return BlockStateHelper.facingProperty;
    }

    @Override
    public EnumColor getColor() {
        return color;
    }

    public boolean canStay(World world, BlockPos pos, BlockState state) {
        boolean canStay = false;
        //TODO: Multipart
        /*if (Mekanism.hooks.MCMPLoaded) {
            canStay = MultipartMekanism.hasCenterSlot(world, pos);
        }*/
        if (!canStay) {
            Direction side = getDirection(state);
            Coord4D adj = new Coord4D(pos.offset(side), world);
            canStay = Block.hasSolidSide(world.getBlockState(adj.getPos()), world, adj.getPos(), side.getOpposite());
        }
        return canStay;
    }

    private static TileEntityGlowPanel getTileEntityGlowPanel(IBlockReader world, BlockPos pos) {
        TileEntity tileEntity = MekanismUtils.getTileEntitySafe(world, pos);
        TileEntityGlowPanel glowPanel = null;
        if (tileEntity instanceof TileEntityGlowPanel) {
            glowPanel = (TileEntityGlowPanel) tileEntity;
        }
        //TODO: Multipart
        /*else if (Mekanism.hooks.MCMPLoaded) {
            TileEntity childEntity = MultipartMekanism.unwrapTileEntity(world);
            if (childEntity instanceof TileEntityGlowPanel) {
                glowPanel = (TileEntityGlowPanel) childEntity;
            }
        }*/
        return glowPanel;
    }

    @Override
    @Deprecated
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        TileEntityGlowPanel tileEntity = getTileEntityGlowPanel(world, pos);
        if (tileEntity != null && !world.isRemote && !canStay(world, pos, state)) {
            Block.spawnDrops(world.getBlockState(pos), world, pos, tileEntity);
            world.removeBlock(pos, isMoving);
        }
    }

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return bounds[getDirection(state).ordinal()];
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
        //TODO: Check this
        Direction side = getDirection(state);
        BlockPos positionOn = pos.offset(side.getOpposite());
        return Block.hasSolidSide(world.getBlockState(positionOn), world, positionOn, side);
    }

    @Override
    public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
        return true;
    }

    @Override
    public TileEntityType<TileEntityGlowPanel> getTileType() {
        return AdditionsTileEntityTypes.GLOW_PANEL;
    }
}
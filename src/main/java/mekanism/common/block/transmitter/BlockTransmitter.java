package mekanism.common.block.transmitter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IMekWrench;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.block.states.TransmitterType.Size;
import mekanism.common.content.network.transmitter.Transmitter;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MultipartUtils;
import mekanism.common.util.MultipartUtils.AdvancedRayTraceResult;
import mekanism.common.util.VoxelShapeUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

public abstract class BlockTransmitter extends BlockMekanism implements IStateFluidLoggable {

    private static final Map<ConnectionInfo, VoxelShape> cachedShapes = new HashMap<>();

    protected BlockTransmitter() {
        super(AbstractBlock.Properties.create(Material.PISTON).hardnessAndResistance(1, 6));
    }

    @Nonnull
    @Override
    @Deprecated
    public ActionResultType onBlockActivated(@Nonnull BlockState state, @Nonnull World world, @Nonnull BlockPos pos, PlayerEntity player, @Nonnull Hand hand,
          @Nonnull BlockRayTraceResult hit) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.isEmpty()) {
            return ActionResultType.PASS;
        }
        IMekWrench wrenchHandler = MekanismUtils.getWrench(stack);
        if (wrenchHandler != null && wrenchHandler.canUseWrench(stack, player, hit.getPos()) && player.isSneaking()) {
            if (!world.isRemote) {
                WorldUtils.dismantleBlock(state, world, pos);
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    @Override
    public void onBlockPlacedBy(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable LivingEntity placer, @Nonnull ItemStack stack) {
        TileEntityTransmitter tile = WorldUtils.getTileEntity(TileEntityTransmitter.class, world, pos);
        if (tile != null) {
            tile.onAdded();
        }
    }

    @Override
    @Deprecated
    public void neighborChanged(@Nonnull BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull Block neighborBlock, @Nonnull BlockPos neighborPos,
          boolean isMoving) {
        TileEntityTransmitter tile = WorldUtils.getTileEntity(TileEntityTransmitter.class, world, pos);
        if (tile != null) {
            Direction side = Direction.getFacingFromVector(neighborPos.getX() - pos.getX(), neighborPos.getY() - pos.getY(), neighborPos.getZ() - pos.getZ());
            tile.onNeighborBlockChange(side);
        }
    }

    @Override
    public void onNeighborChange(BlockState state, IWorldReader world, BlockPos pos, BlockPos neighbor) {
        TileEntityTransmitter tile = WorldUtils.getTileEntity(TileEntityTransmitter.class, world, pos);
        if (tile != null) {
            Direction side = Direction.getFacingFromVector(neighbor.getX() - pos.getX(), neighbor.getY() - pos.getY(), neighbor.getZ() - pos.getZ());
            tile.onNeighborTileChange(side);
        }
    }

    @Override
    @Deprecated
    public boolean allowsMovement(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos, @Nonnull PathType type) {
        return false;
    }

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos, ISelectionContext context) {
        if (!context.hasItem(MekanismItems.CONFIGURATOR.getItem())) {
            return getRealShape(world, pos);
        }
        //Get the partial selection box if we are holding a configurator
        if (context.getEntity() == null) {
            //If we don't have an entity get the full VoxelShape
            return getRealShape(world, pos);
        }
        TileEntityTransmitter tile = WorldUtils.getTileEntity(TileEntityTransmitter.class, world, pos);
        if (tile == null) {
            //If we failed to get the tile, just give the center shape
            return getCenter();
        }
        //TODO: Try to cache some of this? At the very least the collision boxes
        Pair<Vector3d, Vector3d> vecs = MultipartUtils.getRayTraceVectors(context.getEntity());
        AdvancedRayTraceResult result = MultipartUtils.collisionRayTrace(pos, vecs.getLeft(), vecs.getRight(), tile.getCollisionBoxes());
        if (result != null && result.valid()) {
            return result.bounds;
        }
        //If we failed to figure it out somehow, just fall back to the center. This should never happen
        return getCenter();
    }

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getRenderShape(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos) {
        //Override this so that we ALWAYS have the full collision box, even if a configurator is being held
        return getRealShape(world, pos);
    }

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getCollisionShape(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos, @Nonnull ISelectionContext context) {
        //Override this so that we ALWAYS have the full collision box, even if a configurator is being held
        return getRealShape(world, pos);
    }

    protected abstract VoxelShape getCenter();

    protected abstract VoxelShape getSide(ConnectionType type, Direction side);

    private VoxelShape getRealShape(IBlockReader world, BlockPos pos) {
        TileEntityTransmitter tile = WorldUtils.getTileEntity(TileEntityTransmitter.class, world, pos);
        if (tile == null) {
            //If we failed to get the tile, just give the center shape
            return getCenter();
        }
        Transmitter<?, ?, ?> transmitter = tile.getTransmitter();
        ConnectionType[] connectionTypes = new ConnectionType[transmitter.getConnectionTypesRaw().length];
        for (int i = 0; i < EnumUtils.DIRECTIONS.length; i++) {
            //Get the actual connection types
            connectionTypes[i] = transmitter.getConnectionType(EnumUtils.DIRECTIONS[i]);
        }
        ConnectionInfo info = new ConnectionInfo(tile.getTransmitterType().getSize(), connectionTypes);
        if (cachedShapes.containsKey(info)) {
            return cachedShapes.get(info);
        }
        //If we don't have a cached version of our shape, then we need to calculate it
        List<VoxelShape> shapes = new ArrayList<>();
        for (Direction side : EnumUtils.DIRECTIONS) {
            ConnectionType connectionType = connectionTypes[side.ordinal()];
            if (connectionType != ConnectionType.NONE) {
                shapes.add(getSide(connectionType, side));
            }
        }
        VoxelShape center = getCenter();
        if (shapes.isEmpty()) {
            cachedShapes.put(info, center);
            return center;
        }
        shapes.add(center);
        VoxelShape shape = VoxelShapeUtils.combine(shapes);
        cachedShapes.put(info, shape);
        return shape;
    }

    private static class ConnectionInfo {

        private final Size size;
        private final ConnectionType[] connectionTypes;

        private ConnectionInfo(Size size, ConnectionType[] connectionTypes) {
            this.size = size;
            this.connectionTypes = connectionTypes;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof ConnectionInfo) {
                ConnectionInfo other = (ConnectionInfo) o;
                return size == other.size && Arrays.equals(connectionTypes, other.connectionTypes);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(size);
            result = 31 * result + Arrays.hashCode(connectionTypes);
            return result;
        }
    }
}
package mekanism.common.block.transmitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BlockTransmitter extends BlockMekanism implements IStateFluidLoggable {

    private static final Map<ConnectionInfo, VoxelShape> cachedShapes = new HashMap<>();

    protected BlockTransmitter() {
        super(BlockBehaviour.Properties.of(Material.PISTON).strength(1, 6));
    }

    @Nonnull
    @Override
    @Deprecated
    public InteractionResult use(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, Player player, @Nonnull InteractionHand hand,
          @Nonnull BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (MekanismUtils.canUseAsWrench(stack) && player.isShiftKeyDown()) {
            if (!world.isClientSide) {
                WorldUtils.dismantleBlock(state, world, pos);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void setPlacedBy(@Nonnull Level world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable LivingEntity placer, @Nonnull ItemStack stack) {
        TileEntityTransmitter tile = WorldUtils.getTileEntity(TileEntityTransmitter.class, world, pos);
        if (tile != null) {
            tile.onAdded();
        }
    }

    @Override
    @Deprecated
    public void neighborChanged(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull Block neighborBlock, @Nonnull BlockPos neighborPos,
          boolean isMoving) {
        TileEntityTransmitter tile = WorldUtils.getTileEntity(TileEntityTransmitter.class, world, pos);
        if (tile != null) {
            Direction side = Direction.getNearest(neighborPos.getX() - pos.getX(), neighborPos.getY() - pos.getY(), neighborPos.getZ() - pos.getZ());
            tile.onNeighborBlockChange(side);
        }
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor) {
        TileEntityTransmitter tile = WorldUtils.getTileEntity(TileEntityTransmitter.class, world, pos);
        if (tile != null) {
            Direction side = Direction.getNearest(neighbor.getX() - pos.getX(), neighbor.getY() - pos.getY(), neighbor.getZ() - pos.getZ());
            tile.onNeighborTileChange(side);
        }
    }

    @Override
    @Deprecated
    public boolean isPathfindable(@Nonnull BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos, @Nonnull PathComputationType type) {
        return false;
    }

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos, CollisionContext context) {
        if (!context.isHoldingItem(MekanismItems.CONFIGURATOR.asItem())) {
            return getRealShape(world, pos);
        }
        //Get the partial selection box if we are holding a configurator
        if (!(context instanceof EntityCollisionContext entityContext) || entityContext.getEntity() == null) {
            //If we don't have an entity get the full VoxelShape
            return getRealShape(world, pos);
        }
        TileEntityTransmitter tile = WorldUtils.getTileEntity(TileEntityTransmitter.class, world, pos);
        if (tile == null) {
            //If we failed to get the tile, just give the center shape
            return getCenter();
        }
        //TODO: Try to cache some of this? At the very least the collision boxes
        AdvancedRayTraceResult result = MultipartUtils.collisionRayTrace(entityContext.getEntity(), pos, tile.getCollisionBoxes());
        if (result != null && result.valid()) {
            return result.bounds;
        }
        //If we failed to figure it out somehow, just fall back to the center. This should never happen
        return getCenter();
    }

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getOcclusionShape(@Nonnull BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos) {
        //Override this so that we ALWAYS have the full collision box, even if a configurator is being held
        return getRealShape(world, pos);
    }

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getCollisionShape(@Nonnull BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
        //Override this so that we ALWAYS have the full collision box, even if a configurator is being held
        return getRealShape(world, pos);
    }

    protected abstract VoxelShape getCenter();

    protected abstract VoxelShape getSide(ConnectionType type, Direction side);

    private VoxelShape getRealShape(BlockGetter world, BlockPos pos) {
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

    private record ConnectionInfo(Size size, ConnectionType[] connectionTypes) {
    }
}
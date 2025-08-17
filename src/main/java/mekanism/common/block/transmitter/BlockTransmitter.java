package mekanism.common.block.transmitter;

import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMaps;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import mekanism.common.block.attribute.AttributeTier;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.prefab.BlockBase.BlockBaseModel;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tile.interfaces.ITileRadioactive;
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
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public abstract class BlockTransmitter<TILE extends TileEntityTransmitter> extends BlockBaseModel<BlockTypeTile<TILE>> implements IHasTileEntity<TILE> {

    //Max retained size if we used a HashMap with a key of record(Size, ConnectionType[6]) ~= 1,343,576B
    //Max retained size packing it like this 163,987B
    private static final Short2ObjectMap<VoxelShape> cachedShapes = Short2ObjectMaps.synchronize(new Short2ObjectOpenHashMap<>());

    protected BlockTransmitter(BlockTypeTile<TILE> type) {
        this(type, properties -> {
            AttributeTier<?> attributeTier = type.get(AttributeTier.class);
            return attributeTier == null ? properties : properties.mapColor(attributeTier.tier().getBaseTier().getMapColor());
        });
    }

    protected BlockTransmitter(BlockTypeTile<TILE> type, UnaryOperator<BlockBehaviour.Properties> propertiesModifier) {
        super(type, propertiesModifier.apply(BlockBehaviour.Properties.of().strength(1, 6).pushReaction(PushReaction.BLOCK)));
    }

    @Override
    public final TileEntityTypeRegistryObject<TILE> getTileType() {
        return type.getTileType();
    }

    @Override
    protected void neighborChanged(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Block neighborBlock, @NotNull BlockPos neighborPos,
          boolean isMoving) {
        TileEntityTransmitter tile = WorldUtils.getTileEntity(TileEntityTransmitter.class, world, pos);
        if (tile != null) {
            Direction side = Direction.getNearest(neighborPos.getX() - pos.getX(), neighborPos.getY() - pos.getY(), neighborPos.getZ() - pos.getZ());
            tile.onNeighborBlockChange(side);
        }
    }

    @Override
    protected boolean isPathfindable(@NotNull BlockState state, @NotNull PathComputationType type) {
        return false;
    }

    @NotNull
    @Override
    protected VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        if (!context.isHoldingItem(MekanismItems.CONFIGURATOR.value())) {
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

    @NotNull
    @Override
    protected VoxelShape getOcclusionShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos) {
        //Override this so that we ALWAYS have the full collision box, even if a configurator is being held
        return getRealShape(world, pos);
    }

    @NotNull
    @Override
    protected VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
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
        mekanism.common.content.network.transmitter.Transmitter<?, ?, ?> transmitter = tile.getTransmitter();
        //Created a pack key as follows:
        // first four bits of a short are used to represent size (realistically first three are ignored and fourth represents small or large)
        // last 12 bits are separated into 6 sides each of 2 bits that represent the connection type
        int packedKey = tile.getTransmitterType().getSize().ordinal() << 12;
        for (Direction side : EnumUtils.DIRECTIONS) {
            //Get the actual connection types
            ConnectionType connectionType = transmitter.getConnectionType(side);
            //Bit shift in increments of two based on which side we are on
            packedKey |= connectionType.ordinal() << (side.ordinal() * 2);
        }
        //We can cast this to a short as we don't use more bits than are in a short, we just use an int to simplify bit shifting
        short packed = (short) packedKey;
        VoxelShape shape = cachedShapes.get(packed);
        if (shape == null) {
            //If we don't have a cached version of our shape, then we need to calculate it
            //size = Size.byIndexStatic(packed >> 12);
            List<VoxelShape> shapes = new ArrayList<>(EnumUtils.DIRECTIONS.length);
            for (Direction side : EnumUtils.DIRECTIONS) {
                //Unpack the ordinal of the connection type (shift so that significant bits are the two rightmost
                // and then read those two bits
                int index = (packed >> (side.ordinal() * 2)) & 0b11;
                ConnectionType connectionType = ConnectionType.BY_ID.apply(index);
                if (connectionType != ConnectionType.NONE) {
                    shapes.add(getSide(connectionType, side));
                }
            }
            VoxelShape center = getCenter();
            if (shapes.isEmpty()) {
                shape = center;
            } else {
                //Call batchCombine directly rather than just combine so that we can skip a few checks
                shape = VoxelShapeUtils.batchCombine(center, BooleanOp.OR, true, shapes);
            }
            cachedShapes.put(packed, shape);
        }
        return shape;
    }

    @NotNull
    @Override
    protected ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos,
          @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (player.isShiftKeyDown() && MekanismUtils.canUseAsWrench(stack)) {
            BlockEntity tile = WorldUtils.getTileEntity(world, pos);
            if (tile instanceof ITileRadioactive tileRadioactive && tileRadioactive.getRadiationScale() > 0) {
                return ItemInteractionResult.FAIL;
            }
        }
        return super.useItemOn(stack, state, world, pos, player, hand, hit);
    }
}
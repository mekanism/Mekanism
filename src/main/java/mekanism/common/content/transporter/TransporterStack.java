package mekanism.common.content.transporter;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import java.util.Optional;
import java.util.function.IntFunction;
import mekanism.api.SerializationConstants;
import mekanism.api.resource.LargeResourceStack;
import mekanism.api.text.EnumColor;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.TransporterPathfinder.Destination;
import mekanism.common.content.transporter.TransporterPathfinder.IdlePathData;
import mekanism.common.lib.inventory.IAdvancedTransportEjector;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.lib.inventory.TransitRequest.TransitResponse;
import mekanism.common.util.ValueUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public class TransporterStack {

    //Make sure to call updateForPos before calling this method
    public static StreamCodec<RegistryFriendlyByteBuf, TransporterStack> STREAM_CODEC = StreamCodec.composite(
          EnumColor.OPTIONAL_STREAM_CODEC, stack -> Optional.ofNullable(stack.color),
          ByteBufCodecs.VAR_INT, stack -> stack.progress,
          ByteBufCodecs.VAR_LONG, stack -> stack.originalLocation,
          Path.STREAM_CODEC, TransporterStack::getPathType,
          ByteBufCodecs.optional(ByteBufCodecs.VAR_LONG), stack -> stack.clientNext == Long.MAX_VALUE ? Optional.empty() : Optional.of(stack.clientNext),
          ByteBufCodecs.optional(ByteBufCodecs.VAR_LONG), stack -> stack.clientPrev == Long.MAX_VALUE ? Optional.empty() : Optional.of(stack.clientPrev),
          LargeResourceStack.ITEM_HELPER.streamCodec(), stack -> stack.itemStack,
          (color, progress, originalLocation, pathType, clientNext, clientPrev, itemStack) -> {
              TransporterStack stack = new TransporterStack();
              stack.color = color.orElse(null);
              stack.progress = progress == 0 ? 5 : progress;
              stack.originalLocation = originalLocation;
              stack.pathType = pathType;
              stack.clientNext = clientNext.orElse(Long.MAX_VALUE);
              stack.clientPrev = clientPrev.orElse(Long.MAX_VALUE);
              stack.itemStack = itemStack;
              return stack;
          }
    );

    private LargeResourceStack<ItemResource> itemStack = LargeResourceStack.ITEM_HELPER.empty();

    public int progress;

    @Nullable
    public EnumColor color = null;

    public boolean initiatedPath = false;

    @Nullable
    public Direction idleDir = null;

    //packed BlockPos-es
    public long originalLocation = Long.MAX_VALUE;
    public long homeLocation = Long.MAX_VALUE;
    private long clientNext = Long.MAX_VALUE;
    private long clientPrev = Long.MAX_VALUE;
    //

    @Nullable
    private Path pathType;
    private LongList pathToTarget = new LongArrayList();

    public TransporterStack() {
    }

    private TransporterStack(ValueInput input) {
        this.color = ValueUtils.getEnum(input, SerializationConstants.COLOR, EnumColor.BY_ID);
        this.progress = input.getIntOr(SerializationConstants.PROGRESS, progress);
        this.originalLocation = input.getLongOr(SerializationConstants.ORIGINAL_LOCATION, Long.MAX_VALUE);
        this.pathType = ValueUtils.getEnum(input, SerializationConstants.PATH_TYPE, Path.BY_ID);
        this.itemStack = LargeResourceStack.ITEM_HELPER.readOrEmpty(input, SerializationConstants.ITEM);
    }

    public static TransporterStack read(ValueInput input) {
        TransporterStack stack = new TransporterStack(input);
        stack.idleDir = ValueUtils.getEnum(input, SerializationConstants.IDLE_DIR, Direction::from3DDataValue);
        stack.homeLocation = input.getLongOr(SerializationConstants.HOME_LOCATION, Long.MAX_VALUE);
        return stack;
    }

    public static TransporterStack readFromUpdate(ValueInput input) {
        TransporterStack stack = new TransporterStack(input);
        stack.clientNext = input.getLongOr(SerializationConstants.NEXT, Long.MAX_VALUE);
        stack.clientPrev = input.getLongOr(SerializationConstants.PREVIOUS, Long.MAX_VALUE);
        return stack;
    }

    private void writeCommon(ValueOutput output) {
        if (color != null) {
            ValueUtils.writeEnum(output, SerializationConstants.COLOR, color);
        }
        output.putInt(SerializationConstants.PROGRESS, progress);
        output.putLong(SerializationConstants.ORIGINAL_LOCATION, originalLocation);
        LargeResourceStack.ITEM_HELPER.storeNonEmpty(output, SerializationConstants.ITEM, itemStack);
    }

    public void writeToUpdateTag(LogisticalTransporterBase transporter, ValueOutput output) {
        writeCommon(output);
        ValueUtils.writeEnum(output, SerializationConstants.PATH_TYPE, getPathType());
        long next = getNext(transporter);
        if (next != Long.MAX_VALUE) {
            output.putLong(SerializationConstants.NEXT, next);
        }
        long prev = getPrev(transporter);
        if (prev != Long.MAX_VALUE) {
            output.putLong(SerializationConstants.PREVIOUS, prev);
        }
    }

    public void write(ValueOutput output) {
        writeCommon(output);
        if (pathType != null) {
            //TODO - 26.2: Figure out path type and if we should set it to none when saving to file instead of not saving it
            // given that for syncing we pretend it is none.
            ValueUtils.writeEnum(output, SerializationConstants.PATH_TYPE, pathType);
        }
        if (idleDir != null) {
            ValueUtils.writeEnum(output, SerializationConstants.IDLE_DIR, idleDir);
        }
        if (homeLocation != Long.MAX_VALUE) {
            output.putLong(SerializationConstants.HOME_LOCATION, homeLocation);
        }
    }

    public boolean isEmpty() {
        return itemStack.isEmpty();
    }

    public ItemStack asItemStack() {
        return getItemType().toStack(size());
    }

    public ItemResource getItemType() {
        return itemStack.resource();
    }

    public int size() {
        return itemStack.amountAsInt();
    }

    public void setStack(ItemResource itemType, int amount) {
        this.itemStack = LargeResourceStack.ITEM_HELPER.createStack(itemType, amount);
    }

    private void setPath(Level world, LongList path, Path type, @Nullable TransactionContext transaction) {
        //Make sure old path isn't null
        if (pathType == null || pathType.hasTarget()) {
            //Only update the actual flowing stacks if we want to modify more than our current stack
            TransporterManager.remove(world, this, transaction);
        }
        pathToTarget = path;
        pathType = type;
        if (pathType.hasTarget()) {
            //Only update the actual flowing stacks if we want to modify more than our current stack
            TransporterManager.add(world, this, transaction);
        }
    }

    public boolean hasPath() {
        return pathToTarget.size() >= 2;
    }

    public LongList getPath() {
        return pathToTarget;
    }

    public Path getPathType() {
        return pathType == null ? Path.NONE : pathType;
    }

    public final TransitResponse recalculatePath(Level level, TransitRequest request, @Nullable BlockEntity ignored, LogisticalTransporterBase transporter, int min, @Nullable TransactionContext transaction) {
        return recalculatePath(level, request, transporter, min, transaction);
    }

    public TransitResponse recalculatePath(Level level, TransitRequest request, LogisticalTransporterBase transporter, int min, @Nullable TransactionContext transaction) {
        Destination newPath = TransporterPathfinder.getNewBasePath(transporter, this, request, min, transaction);
        if (newPath == null) {
            return TransitResponse.EMPTY;
        }
        idleDir = null;
        setPath(level, newPath.getPath(), Path.DEST, transaction);
        initiatedPath = true;
        return newPath.getResponseOrEmpty();
    }

    public TransitResponse recalculateRRPath(Level level, TransitRequest request, IAdvancedTransportEjector outputter, LogisticalTransporterBase transporter, int min, @Nullable TransactionContext transaction) {
        Destination newPath = TransporterPathfinder.getNewRRPath(transporter, this, request, outputter, min, transaction);
        if (newPath == null) {
            return TransitResponse.EMPTY;
        }
        idleDir = null;
        setPath(level, newPath.getPath(), Path.DEST, transaction);
        initiatedPath = true;
        return newPath.getResponseOrEmpty();
    }

    public boolean calculateIdle(Level level, LogisticalTransporterBase transporter, @Nullable TransactionContext transaction) {
        IdlePathData newPath = TransporterPathfinder.getIdlePath(transporter, this, transaction);
        if (newPath == null) {
            return false;
        }
        if (newPath.type().isHome()) {
            idleDir = null;
        }
        setPath(level, newPath.path(), newPath.type(), transaction);
        originalLocation = transporter.getWorldPositionLong();
        initiatedPath = true;
        return true;
    }

    public boolean isFinal(LogisticalTransporterBase transporter) {
        return transporter.getWorldPositionLong() == pathToTarget.get(getPathType().hasTarget() ? 1 : 0);
    }

    //TODO - 1.20.5: Re-evaluate this method
    public TransporterStack updateForPos(long pos) {
        clientNext = getNext(pos);
        clientPrev = getPrev(pos);
        return this;
    }

    public long getNext(LogisticalTransporterBase transporter) {
        return transporter.isClientSide() ? clientNext : getNext(transporter.getWorldPositionLong());
    }

    private long getNext(long pos) {
        int index = pathToTarget.indexOf(pos) - 1;
        if (index < 0) {
            return Long.MAX_VALUE;
        }
        return pathToTarget.getLong(index);
    }

    public long getPrev(LogisticalTransporterBase transporter) {
        return transporter.isClientSide() ? clientPrev : getPrev(transporter.getBlockPos().asLong());
    }

    private long getPrev(long pos) {
        int index = pathToTarget.indexOf(pos) + 1;
        if (index < pathToTarget.size()) {
            return pathToTarget.getLong(index);
        }
        return originalLocation;
    }

    public Direction getSide(LogisticalTransporterBase transporter) {
        Direction side = null;
        if (progress < 50) {
            long prev = getPrev(transporter);
            if (prev != Long.MAX_VALUE) {
                side = WorldUtils.sideDifference(transporter.getBlockPos().asLong(), prev);
            }
        } else {
            long next = getNext(transporter);
            if (next != Long.MAX_VALUE) {
                side = WorldUtils.sideDifference(next, transporter.getBlockPos().asLong());
            }
        }
        //sideDifference can return null
        //TODO: Look into implications further about what side should be returned.
        // This is mainly to stop a crash I randomly encountered but was unable to reproduce.
        // (I believe the difference returns null when it is the "same" transporter somehow or something)
        return side == null ? Direction.DOWN : side;
    }

    public Direction getSide(long pos, long target) {
        Direction side = null;
        if (target != Long.MAX_VALUE) {
            side = WorldUtils.sideDifference(target, pos);
        }
        //TODO: See getSide(Transporter) for why we null check and then return down
        return side == null ? Direction.DOWN : side;
    }

    @Contract("null, _, _ -> false")
    public boolean canInsertToTransporter(@Nullable LogisticalTransporterBase transporter, Direction from, @Nullable LogisticalTransporterBase transporterFrom) {
        if (transporter == null) {
            return false;
        }
        //If the color is valid, make sure that the connection is valid
        EnumColor color = transporter.getColor();
        return (color == null || color == this.color) && transporter.canConnectMutual(from.getOpposite(), transporterFrom);
    }

    @Contract("null, _, _ -> false")
    public boolean canInsertToTransporter(@Nullable LogisticalTransporterBase transporter, Direction from, @Nullable BlockEntity tileFrom) {
        if (transporter == null) {
            return false;
        }
        //If the color is valid, make sure that the connection is valid
        EnumColor color = transporter.getColor();
        return (color == null || color == this.color) && transporter.canConnectMutual(from.getOpposite(), tileFrom);
    }

    public long getDest() {
        return pathToTarget.getFirst();
    }

    @Nullable
    public Direction getSideOfDest() {
        if (hasPath()) {
            long lastTransporter = pathToTarget.getLong(1);
            return WorldUtils.sideDifference(lastTransporter, getDest());
        }
        return null;
    }

    public enum Path {
        DEST,
        HOME,
        NONE;

        public static final IntFunction<Path> BY_ID = ByIdMap.continuous(Path::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, Path> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Path::ordinal);

        public boolean hasTarget() {
            return this != NONE;
        }

        public boolean noTarget() {
            return this == NONE;
        }

        public boolean isHome() {
            return this == HOME;
        }
    }
}
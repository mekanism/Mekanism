package mekanism.common.content.transporter;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntFunction;
import mekanism.api.SerializationConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.text.EnumColor;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.TransporterPathfinder.Destination;
import mekanism.common.content.transporter.TransporterPathfinder.IdlePathData;
import mekanism.common.lib.inventory.IAdvancedTransportEjector;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.lib.inventory.TransitRequest.TransitResponse;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TransporterStack {

    //Make sure to call updateForPos before calling this method
    public static StreamCodec<RegistryFriendlyByteBuf, TransporterStack> STREAM_CODEC = NeoForgeStreamCodecs.composite(
          EnumColor.OPTIONAL_STREAM_CODEC, stack -> Optional.ofNullable(stack.color),
          ByteBufCodecs.VAR_INT, stack -> stack.progress,
          ByteBufCodecs.VAR_LONG, stack -> stack.originalLocation,
          Path.STREAM_CODEC, TransporterStack::getPathType,
          ByteBufCodecs.optional(ByteBufCodecs.VAR_LONG), stack -> stack.clientNext == Long.MAX_VALUE ? Optional.empty() : Optional.of(stack.clientNext),
          ByteBufCodecs.optional(ByteBufCodecs.VAR_LONG), stack -> stack.clientPrev == Long.MAX_VALUE ? Optional.empty() : Optional.of(stack.clientPrev),
          ItemStack.OPTIONAL_STREAM_CODEC, stack -> stack.itemStack,
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

    public ItemStack itemStack = ItemStack.EMPTY;

    public int progress;

    public EnumColor color = null;

    public boolean initiatedPath = false;

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

    public static TransporterStack readFromNBT(HolderLookup.Provider provider, CompoundTag nbtTags) {
        TransporterStack stack = new TransporterStack();
        stack.read(provider, nbtTags);
        return stack;
    }

    public static TransporterStack readFromUpdate(HolderLookup.Provider provider, CompoundTag nbtTags) {
        TransporterStack stack = new TransporterStack();
        stack.readFromUpdateTag(provider, nbtTags);
        return stack;
    }

    public void writeToUpdateTag(HolderLookup.Provider provider, LogisticalTransporterBase transporter, CompoundTag updateTag) {
        if (color != null) {
            NBTUtils.writeEnum(updateTag, SerializationConstants.COLOR, color);
        }
        updateTag.putInt(SerializationConstants.PROGRESS, progress);
        updateTag.putLong(SerializationConstants.ORIGINAL_LOCATION, originalLocation);
        NBTUtils.writeEnum(updateTag, SerializationConstants.PATH_TYPE, getPathType());
        long next = getNext(transporter);
        if (next != Long.MAX_VALUE) {
            updateTag.putLong(SerializationConstants.NEXT, next);
        }
        long prev = getPrev(transporter);
        if (prev != Long.MAX_VALUE) {
            updateTag.putLong(SerializationConstants.PREVIOUS, prev);
        }
        if (!itemStack.isEmpty()) {
            updateTag.put(SerializationConstants.ITEM, SerializerHelper.saveOversized(provider, itemStack));
        }
    }

    public void readFromUpdateTag(HolderLookup.Provider provider, CompoundTag updateTag) {
        this.color = NBTUtils.getEnum(updateTag, SerializationConstants.COLOR, EnumColor.BY_ID);
        progress = updateTag.getInt(SerializationConstants.PROGRESS);
        NBTUtils.setLongIfPresent(updateTag, SerializationConstants.ORIGINAL_LOCATION, coord -> originalLocation = coord);
        NBTUtils.setEnumIfPresent(updateTag, SerializationConstants.PATH_TYPE, Path.BY_ID, type -> pathType = type);

        //todo is backcompat needed?
        clientNext = Long.MAX_VALUE;
        NBTUtils.setLongIfPresent(updateTag, SerializationConstants.NEXT, coord -> clientNext = coord);
        NBTUtils.setBlockPosIfPresent(updateTag, SerializationConstants.NEXT, coord -> clientNext = coord.asLong());
        clientPrev = Long.MAX_VALUE;
        NBTUtils.setLongIfPresent(updateTag, SerializationConstants.PREVIOUS, coord -> clientPrev = coord);
        NBTUtils.setBlockPosIfPresent(updateTag, SerializationConstants.PREVIOUS, coord -> clientPrev = coord.asLong());

        Tag itemTag = updateTag.get(SerializationConstants.ITEM);
        if (itemTag != null) {
            itemStack = SerializerHelper.parseOversized(provider, itemTag).orElse(ItemStack.EMPTY);
        }
    }

    public void write(HolderLookup.Provider provider, CompoundTag nbtTags) {
        if (color != null) {
            NBTUtils.writeEnum(nbtTags, SerializationConstants.COLOR, color);
        }

        nbtTags.putInt(SerializationConstants.PROGRESS, progress);
        nbtTags.putLong(SerializationConstants.ORIGINAL_LOCATION, originalLocation);

        if (idleDir != null) {
            NBTUtils.writeEnum(nbtTags, SerializationConstants.IDLE_DIR, idleDir);
        }
        if (homeLocation != Long.MAX_VALUE) {
            nbtTags.putLong(SerializationConstants.HOME_LOCATION, homeLocation);
        }
        if (pathType != null) {
            NBTUtils.writeEnum(nbtTags, SerializationConstants.PATH_TYPE, pathType);
        }
        if (!itemStack.isEmpty()) {
            nbtTags.put(SerializationConstants.ITEM_OVERSIZED, SerializerHelper.saveOversized(provider, itemStack));
        }
    }

    public void read(HolderLookup.Provider provider, CompoundTag nbtTags) {
        this.color = NBTUtils.getEnum(nbtTags, SerializationConstants.COLOR, EnumColor.BY_ID);
        progress = nbtTags.getInt(SerializationConstants.PROGRESS);
        NBTUtils.setBlockPosIfPresent(nbtTags, SerializationConstants.ORIGINAL_LOCATION, coord -> originalLocation = coord.asLong());//TODO - 1.22 remove backcompat
        NBTUtils.setLongIfPresent(nbtTags, SerializationConstants.ORIGINAL_LOCATION, coord -> originalLocation = coord);
        NBTUtils.setEnumIfPresent(nbtTags, SerializationConstants.IDLE_DIR, Direction::from3DDataValue, dir -> idleDir = dir);
        NBTUtils.setBlockPosIfPresent(nbtTags, SerializationConstants.HOME_LOCATION, coord -> homeLocation = coord.asLong());//TODO - 1.22 remove backcompat
        NBTUtils.setLongIfPresent(nbtTags, SerializationConstants.HOME_LOCATION, coord -> homeLocation = coord);
        NBTUtils.setEnumIfPresent(nbtTags, SerializationConstants.PATH_TYPE, Path.BY_ID, type -> pathType = type);
        Tag oversizedTag = nbtTags.get(SerializationConstants.ITEM_OVERSIZED);
        if (oversizedTag != null) {
            itemStack = SerializerHelper.parseOversized(provider, oversizedTag).orElse(ItemStack.EMPTY);
        } else if (nbtTags.contains(SerializationConstants.ITEM, Tag.TAG_COMPOUND)) {//TODO - 1.22: Remove this legacy way of loading data
            itemStack = ItemStack.parseOptional(provider, nbtTags.getCompound(SerializationConstants.ITEM));
        } else {//TODO - 1.22: Remove this legacy way of loading data
            itemStack = ItemStack.parseOptional(provider, nbtTags);
        }
    }

    private void setPath(Level world, @NotNull LongList path, @NotNull Path type, boolean updateFlowing) {
        //Make sure old path isn't null
        if (updateFlowing && (pathType == null || pathType.hasTarget())) {
            //Only update the actual flowing stacks if we want to modify more than our current stack
            TransporterManager.remove(world, this);
        }
        pathToTarget = path;
        pathType = type;
        if (updateFlowing && pathType.hasTarget()) {
            //Only update the actual flowing stacks if we want to modify more than our current stack
            TransporterManager.add(world, this);
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

    public TransitResponse recalculatePath(TransitRequest request, LogisticalTransporterBase transporter, int min) {
        return recalculatePath(request, transporter, min, true);
    }

    public final TransitResponse recalculatePath(TransitRequest request, BlockEntity ignored, LogisticalTransporterBase transporter, int min, boolean updateFlowing) {
        return recalculatePath(request, transporter, min, updateFlowing);
    }

    public TransitResponse recalculatePath(TransitRequest request, LogisticalTransporterBase transporter, int min, boolean updateFlowing) {
        return recalculatePath(request, transporter, min, updateFlowing, Collections.emptyMap());
    }

    public TransitResponse recalculatePath(TransitRequest request, LogisticalTransporterBase transporter, int min,
          Map<GlobalPos, Set<TransporterStack>> additionalFlowingStacks) {
        return recalculatePath(request, transporter, min, false, additionalFlowingStacks);
    }

    private TransitResponse recalculatePath(TransitRequest request, LogisticalTransporterBase transporter, int min, boolean updateFlowing,
          Map<GlobalPos, Set<TransporterStack>> additionalFlowingStacks) {
        Destination newPath = TransporterPathfinder.getNewBasePath(transporter, this, request, min, additionalFlowingStacks);
        if (newPath == null) {
            return request.getEmptyResponse();
        }
        idleDir = null;
        setPath(transporter.getLevel(), newPath.getPath(), Path.DEST, updateFlowing);
        initiatedPath = true;
        return newPath.getResponse();
    }

    public <BE extends BlockEntity & IAdvancedTransportEjector> TransitResponse recalculateRRPath(TransitRequest request, BE outputter, LogisticalTransporterBase transporter, int min) {
        return recalculateRRPath(request, outputter, transporter, min, true);
    }

    public <BE extends BlockEntity & IAdvancedTransportEjector> TransitResponse recalculateRRPath(TransitRequest request, BE outputter, LogisticalTransporterBase transporter, int min, boolean updateFlowing) {
        Destination newPath = TransporterPathfinder.getNewRRPath(transporter, this, request, outputter, min);
        if (newPath == null) {
            return request.getEmptyResponse();
        }
        idleDir = null;
        setPath(transporter.getLevel(), newPath.getPath(), Path.DEST, updateFlowing);
        initiatedPath = true;
        return newPath.getResponse();
    }

    public boolean calculateIdle(LogisticalTransporterBase transporter) {
        IdlePathData newPath = TransporterPathfinder.getIdlePath(transporter, this);
        if (newPath == null) {
            return false;
        }
        if (newPath.type().isHome()) {
            idleDir = null;
        }
        setPath(transporter.getLevel(), newPath.path(), newPath.type(), true);
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
        return transporter.isRemote() ? clientNext : getNext(transporter.getWorldPositionLong());
    }

    private long getNext(long pos) {
        int index = pathToTarget.indexOf(pos) - 1;
        if (index < 0) {
            return Long.MAX_VALUE;
        }
        return pathToTarget.getLong(index);
    }

    public long getPrev(LogisticalTransporterBase transporter) {
        return transporter.isRemote() ? clientPrev : getPrev(transporter.getBlockPos().asLong());
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
    public boolean canInsertToTransporter(@Nullable LogisticalTransporterBase transmitter, Direction from, @Nullable LogisticalTransporterBase transporterFrom) {
        return transmitter != null && canInsertToTransporterNN(transmitter, from, transporterFrom);
    }

    public boolean canInsertToTransporterNN(@NotNull LogisticalTransporterBase transporter, Direction from, @Nullable BlockEntity tileFrom) {
        //If the color is valid, make sure that the connection is valid
        EnumColor color = transporter.getColor();
        return (color == null || color == this.color) && transporter.canConnectMutual(from.getOpposite(), tileFrom);
    }

    public boolean canInsertToTransporterNN(@NotNull LogisticalTransporterBase transporter, Direction from, @Nullable LogisticalTransporterBase transporterFrom) {
        //If the color is valid, make sure that the connection is valid
        EnumColor color = transporter.getColor();
        return (color == null || color == this.color) && transporter.canConnectMutual(from.getOpposite(), transporterFrom);
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
package mekanism.common.content.transporter;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntFunction;
import mekanism.api.NBTConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.TransporterPathfinder.Destination;
import mekanism.common.content.transporter.TransporterPathfinder.IdlePathData;
import mekanism.common.lib.inventory.IAdvancedTransportEjector;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.lib.inventory.TransitRequest.TransitResponse;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
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
          BlockPos.STREAM_CODEC, stack -> stack.originalLocation,
          Path.STREAM_CODEC, TransporterStack::getPathType,
          ByteBufCodecs.optional(BlockPos.STREAM_CODEC), stack -> Optional.ofNullable(stack.clientNext),
          BlockPos.STREAM_CODEC, stack -> stack.clientPrev,
          ItemStack.OPTIONAL_STREAM_CODEC, stack -> stack.itemStack,
          (color, progress, originalLocation, pathType, clientNext, clientPrev, itemStack) -> {
              TransporterStack stack = new TransporterStack();
              stack.color = color.orElse(null);
              stack.progress = progress == 0 ? 5 : progress;
              stack.originalLocation = originalLocation;
              stack.pathType = pathType;
              stack.clientNext = clientNext.orElse(null);
              stack.clientPrev = clientPrev;
              stack.itemStack = itemStack;
              return stack;
          }
    );

    public ItemStack itemStack = ItemStack.EMPTY;

    public int progress;

    public EnumColor color = null;

    public boolean initiatedPath = false;

    public Direction idleDir = null;
    public BlockPos originalLocation;
    public BlockPos homeLocation;
    @Nullable
    private BlockPos clientNext;
    private BlockPos clientPrev;
    @Nullable
    private Path pathType;
    private List<BlockPos> pathToTarget = new ArrayList<>();

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
            NBTUtils.writeEnum(updateTag, NBTConstants.COLOR, color);
        }
        updateTag.putInt(NBTConstants.PROGRESS, progress);
        updateTag.put(NBTConstants.ORIGINAL_LOCATION, NbtUtils.writeBlockPos(originalLocation));
        NBTUtils.writeEnum(updateTag, NBTConstants.PATH_TYPE, getPathType());
        BlockPos next = getNext(transporter);
        if (next != null) {
            updateTag.put(NBTConstants.CLIENT_NEXT, NbtUtils.writeBlockPos(next));
        }
        updateTag.put(NBTConstants.CLIENT_PREVIOUS, NbtUtils.writeBlockPos(getPrev(transporter)));
        if (!itemStack.isEmpty()) {
            itemStack.save(provider, updateTag);
        }
    }

    public void readFromUpdateTag(HolderLookup.Provider provider, CompoundTag updateTag) {
        this.color = NBTUtils.getEnum(updateTag, NBTConstants.COLOR, TransporterUtils::readColor);
        progress = updateTag.getInt(NBTConstants.PROGRESS);
        NBTUtils.setBlockPosIfPresent(updateTag, NBTConstants.ORIGINAL_LOCATION, coord -> originalLocation = coord);
        NBTUtils.setEnumIfPresent(updateTag, NBTConstants.PATH_TYPE, Path.BY_ID, type -> pathType = type);
        NBTUtils.setBlockPosIfPresent(updateTag, NBTConstants.CLIENT_NEXT, coord -> clientNext = coord);
        NBTUtils.setBlockPosIfPresent(updateTag, NBTConstants.CLIENT_PREVIOUS, coord -> clientPrev = coord);
        itemStack = ItemStack.parseOptional(provider, updateTag);
    }

    public void write(HolderLookup.Provider provider, CompoundTag nbtTags) {
        if (color != null) {
            NBTUtils.writeEnum(nbtTags, NBTConstants.COLOR, color);
        }

        nbtTags.putInt(NBTConstants.PROGRESS, progress);
        nbtTags.put(NBTConstants.ORIGINAL_LOCATION, NbtUtils.writeBlockPos(originalLocation));

        if (idleDir != null) {
            NBTUtils.writeEnum(nbtTags, NBTConstants.IDLE_DIR, idleDir);
        }
        if (homeLocation != null) {
            nbtTags.put(NBTConstants.HOME_LOCATION, NbtUtils.writeBlockPos(homeLocation));
        }
        if (pathType != null) {
            NBTUtils.writeEnum(nbtTags, NBTConstants.PATH_TYPE, pathType);
        }
        if (!itemStack.isEmpty()) {
            itemStack.save(provider, nbtTags);
        }
    }

    public void read(HolderLookup.Provider provider, CompoundTag nbtTags) {
        this.color = NBTUtils.getEnum(nbtTags, NBTConstants.COLOR, TransporterUtils::readColor);
        progress = nbtTags.getInt(NBTConstants.PROGRESS);
        NBTUtils.setBlockPosIfPresent(nbtTags, NBTConstants.ORIGINAL_LOCATION, coord -> originalLocation = coord);
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.IDLE_DIR, Direction::from3DDataValue, dir -> idleDir = dir);
        NBTUtils.setBlockPosIfPresent(nbtTags, NBTConstants.HOME_LOCATION, coord -> homeLocation = coord);
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.PATH_TYPE, Path.BY_ID, type -> pathType = type);
        itemStack = ItemStack.parseOptional(provider, nbtTags);
    }

    private void setPath(Level world, @NotNull List<BlockPos> path, @NotNull Path type, boolean updateFlowing) {
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

    public List<BlockPos> getPath() {
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
        originalLocation = transporter.getBlockPos();
        initiatedPath = true;
        return true;
    }

    public boolean isFinal(LogisticalTransporterBase transporter) {
        return pathToTarget.indexOf(transporter.getBlockPos()) == (getPathType().hasTarget() ? 1 : 0);
    }

    //TODO - 1.20.5: Re-evaluate this method
    public TransporterStack updateForPos(BlockPos pos) {
        clientNext = getNext(pos);
        clientPrev = getPrev(pos);
        return this;
    }

    @Nullable
    public BlockPos getNext(LogisticalTransporterBase transporter) {
        return transporter.isRemote() ? clientNext : getNext(transporter.getBlockPos());
    }

    @Nullable
    private BlockPos getNext(BlockPos pos) {
        int index = pathToTarget.indexOf(pos) - 1;
        if (index < 0) {
            return null;
        }
        return pathToTarget.get(index);
    }

    public BlockPos getPrev(LogisticalTransporterBase transporter) {
        return transporter.isRemote() ? clientPrev : getPrev(transporter.getBlockPos());
    }

    private BlockPos getPrev(BlockPos pos) {
        int index = pathToTarget.indexOf(pos) + 1;
        if (index < pathToTarget.size()) {
            return pathToTarget.get(index);
        }
        return originalLocation;
    }

    public Direction getSide(LogisticalTransporterBase transporter) {
        Direction side = null;
        if (progress < 50) {
            BlockPos prev = getPrev(transporter);
            if (prev != null) {
                side = WorldUtils.sideDifference(transporter.getBlockPos(), prev);
            }
        } else {
            BlockPos next = getNext(transporter);
            if (next != null) {
                side = WorldUtils.sideDifference(next, transporter.getBlockPos());
            }
        }
        //sideDifference can return null
        //TODO: Look into implications further about what side should be returned.
        // This is mainly to stop a crash I randomly encountered but was unable to reproduce.
        // (I believe the difference returns null when it is the "same" transporter somehow or something)
        return side == null ? Direction.DOWN : side;
    }

    public Direction getSide(BlockPos pos, @Nullable BlockPos target) {
        Direction side = null;
        if (target != null) {
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

    public BlockPos getDest() {
        return pathToTarget.getFirst();
    }

    @Nullable
    public Direction getSideOfDest() {
        if (hasPath()) {
            BlockPos lastTransporter = pathToTarget.get(1);
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
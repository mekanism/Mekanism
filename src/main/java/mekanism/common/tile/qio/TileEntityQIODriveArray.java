package mekanism.common.tile.qio;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.content.qio.IQIODriveHolder;
import mekanism.common.content.qio.QIODriveData;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.slot.QIODriveSlot;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityQIODriveArray extends TileEntityQIOComponent implements IQIODriveHolder {

    public static final ModelProperty<byte[]> DRIVE_STATUS_PROPERTY = new ModelProperty<>();
    public static final int DRIVE_SLOTS = 12;

    private List<QIODriveSlot> driveSlots;
    private byte[] driveStatus = new byte[DRIVE_SLOTS];
    private int prevDriveHash = -1;

    public TileEntityQIODriveArray(BlockPos pos, BlockState state) {
        super(MekanismBlocks.QIO_DRIVE_ARRAY, pos, state);
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSide(facingSupplier);
        final int xSize = 176;
        driveSlots = new ArrayList<>();
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 6; x++) {
                QIODriveSlot slot = new QIODriveSlot(this, y * 6 + x, this::getLevel, listener, xSize / 2 - (6 * 18 / 2) + x * 18, 70 + y * 18);
                driveSlots.add(slot);
                builder.addSlot(slot);
            }
        }
        return builder.build();
    }

    @Override
    protected boolean onUpdateServer(@Nullable QIOFrequency frequency) {
        boolean needsUpdate = super.onUpdateServer(frequency);
        if (level.getGameTime() % MekanismUtils.TICKS_PER_HALF_SECOND == 0) {
            for (int i = 0; i < DRIVE_SLOTS; i++) {
                QIODriveSlot slot = driveSlots.get(i);
                QIODriveData data = frequency == null ? null : frequency.getDriveData(slot.getKey());
                if (frequency == null || data == null) {
                    setDriveStatus(i, slot.isEmpty() ? DriveStatus.NONE : DriveStatus.OFFLINE);
                    continue;
                }
                if (data.getTotalCount() == data.getCountCapacity()) {
                    //If we are at max item capacity: Full
                    setDriveStatus(i, DriveStatus.FULL);
                } else if (data.getTotalTypes() == data.getTypeCapacity() || data.getTotalCount() >= data.getCountCapacity() * 0.75) {
                    //If we are at max type capacity OR we are at 75% or more capacity: Near full
                    setDriveStatus(i, DriveStatus.NEAR_FULL);
                } else {
                    //Otherwise: Ready
                    setDriveStatus(i, DriveStatus.READY);
                }
            }

            int newHash = Arrays.hashCode(driveStatus);
            if (newHash != prevDriveHash) {
                needsUpdate = true;
                prevDriveHash = newHash;
            }
        }
        return needsUpdate;
    }

    private void setDriveStatus(int slot, DriveStatus status) {
        driveStatus[slot] = status.status();
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag nbtTags, @NotNull HolderLookup.Provider provider) {
        QIOFrequency freq = getQIOFrequency();
        if (freq != null) {
            // save all item data before we save
            freq.saveAll();
        }
        super.saveAdditional(nbtTags, provider);
    }

    @NotNull
    @Override
    public ModelData getModelData() {
        return ModelData.of(DRIVE_STATUS_PROPERTY, driveStatus);
    }

    @NotNull
    @Override
    public CompoundTag getReducedUpdateTag(@NotNull HolderLookup.Provider provider) {
        CompoundTag updateTag = super.getReducedUpdateTag(provider);
        updateTag.putByteArray(SerializationConstants.DRIVES, Arrays.copyOf(driveStatus, driveStatus.length));
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.handleUpdateTag(tag, provider);
        byte[] status = tag.getByteArray(SerializationConstants.DRIVES);
        if (!Arrays.equals(status, driveStatus)) {
            driveStatus = status;
            updateModelData();
        }
    }

    @Override
    public void onDataUpdate() {
        markForSave();
    }

    @Override
    public List<QIODriveSlot> getDriveSlots() {
        return driveSlots;
    }

    //Methods relating to IComputerTile
    @ComputerMethod
    int getSlotCount() {
        return DRIVE_SLOTS;
    }

    private void validateSlot(int slot) throws ComputerException {
        int slots = getSlotCount();
        if (slot < 0 || slot >= slots) {
            throw new ComputerException("Slot: '%d' is out of bounds, as this QIO drive array only has '%d' drive slots (zero indexed).", slot, slots);
        }
    }

    @ComputerMethod
    ItemStack getDrive(int slot) throws ComputerException {
        validateSlot(slot);
        return driveSlots.get(slot).getStack();
    }

    @ComputerMethod
    DriveStatus getDriveStatus(int slot) throws ComputerException {
        validateSlot(slot);
        return DriveStatus.BY_ID.apply(driveStatus[slot]);
    }

    @ComputerMethod(methodDescription = "Requires a frequency to be selected")
    long getFrequencyItemCount() throws ComputerException {
        return computerGetFrequency().getTotalItemCount();
    }

    @ComputerMethod(methodDescription = "Requires a frequency to be selected")
    long getFrequencyItemCapacity() throws ComputerException {
        return computerGetFrequency().getTotalItemCountCapacity();
    }

    @ComputerMethod(methodDescription = "Requires a frequency to be selected")
    double getFrequencyItemPercentage() throws ComputerException {
        QIOFrequency frequency = computerGetFrequency();
        return frequency.getTotalItemCount() / (double) frequency.getTotalItemCountCapacity();
    }

    @ComputerMethod(methodDescription = "Requires a frequency to be selected")
    long getFrequencyItemTypeCount() throws ComputerException {
        return computerGetFrequency().getTotalItemTypes(false);
    }

    @ComputerMethod(methodDescription = "Requires a frequency to be selected")
    long getFrequencyItemTypeCapacity() throws ComputerException {
        return computerGetFrequency().getTotalItemTypeCapacity();
    }

    @ComputerMethod(methodDescription = "Requires a frequency to be selected")
    double getFrequencyItemTypePercentage() throws ComputerException {
        QIOFrequency frequency = computerGetFrequency();
        return frequency.getTotalItemTypes(false) / (double) frequency.getTotalItemTypeCapacity();
    }
    //End methods IComputerTile

    public enum DriveStatus {
        NONE(null),
        OFFLINE(Mekanism.rl("block/qio_drive/qio_drive_offline")),
        READY(Mekanism.rl("block/qio_drive/qio_drive_empty")),
        NEAR_FULL(Mekanism.rl("block/qio_drive/qio_drive_partial")),
        FULL(Mekanism.rl("block/qio_drive/qio_drive_full"));

        public static final IntFunction<DriveStatus> BY_ID = ByIdMap.continuous(DriveStatus::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, DriveStatus> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, DriveStatus::ordinal);

        private final ResourceLocation model;

        DriveStatus(ResourceLocation model) {
            this.model = model;
        }

        public int ledIndex() {
            return ordinal() - READY.ordinal();
        }

        public ResourceLocation getModel() {
            return model;
        }

        public byte status() {
            return (byte) ordinal();
        }
    }
}

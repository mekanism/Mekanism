package mekanism.common.tile.qio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.IContentsListener;
import mekanism.api.NBTConstants;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.MathUtils;
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
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;

public class TileEntityQIODriveArray extends TileEntityQIOComponent implements IQIODriveHolder {

    public static final ModelProperty<byte[]> DRIVE_STATUS_PROPERTY = new ModelProperty<>();
    private static final int DRIVE_SLOTS = 12;

    private List<IInventorySlot> driveSlots;
    private byte[] driveStatus = new byte[DRIVE_SLOTS];
    private int prevDriveHash = -1;

    public TileEntityQIODriveArray(BlockPos pos, BlockState state) {
        super(MekanismBlocks.QIO_DRIVE_ARRAY, pos, state);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        final int xSize = 176;
        driveSlots = new ArrayList<>();
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 6; x++) {
                QIODriveSlot slot = new QIODriveSlot(this, y * 6 + x, listener, xSize / 2 - (6 * 18 / 2) + x * 18, 70 + y * 18);
                driveSlots.add(slot);
                builder.addSlot(slot);
            }
        }
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (level.getGameTime() % 10 == 0) {
            QIOFrequency frequency = getQIOFrequency();
            for (int i = 0; i < DRIVE_SLOTS; i++) {
                QIODriveSlot slot = (QIODriveSlot) driveSlots.get(i);
                QIODriveData data = frequency == null ? null : frequency.getDriveData(slot.getKey());
                if (frequency == null || data == null) {
                    setDriveStatus(i, slot.getStack().isEmpty() ? DriveStatus.NONE : DriveStatus.OFFLINE);
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
                sendUpdatePacket();
                prevDriveHash = newHash;
            }
        }
    }

    private void setDriveStatus(int slot, DriveStatus status) {
        driveStatus[slot] = (byte) status.ordinal();
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag nbtTags) {
        QIOFrequency freq = getQIOFrequency();
        if (freq != null) {
            // save all item data before we save
            freq.saveAll();
        }
        super.saveAdditional(nbtTags);
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        return new ModelDataMap.Builder().withInitial(DRIVE_STATUS_PROPERTY, driveStatus).build();
    }

    @Nonnull
    @Override
    public CompoundTag getReducedUpdateTag() {
        CompoundTag updateTag = super.getReducedUpdateTag();
        updateTag.putByteArray(NBTConstants.DRIVES, driveStatus);
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundTag tag) {
        super.handleUpdateTag(tag);
        driveStatus = tag.getByteArray(NBTConstants.DRIVES);
        requestModelDataUpdate();
        WorldUtils.updateBlock(getLevel(), getBlockPos(), getBlockState());
    }

    @Override
    public void onDataUpdate() {
        markForSave();
    }

    @Override
    public List<IInventorySlot> getDriveSlots() {
        return driveSlots;
    }

    //Methods relating to IComputerTile
    @ComputerMethod
    private int getSlotCount() {
        return DRIVE_SLOTS;
    }

    private void validateSlot(int slot) throws ComputerException {
        int slots = getSlotCount();
        if (slot < 0 || slot >= slots) {
            throw new ComputerException("Slot: '%d' is out of bounds, as this QIO drive array only has '%d' drive slots (zero indexed).", slot, slots);
        }
    }

    @ComputerMethod
    private ItemStack getDrive(int slot) throws ComputerException {
        validateSlot(slot);
        return driveSlots.get(slot).getStack();
    }

    @ComputerMethod
    private DriveStatus getDriveStatus(int slot) throws ComputerException {
        validateSlot(slot);
        return DriveStatus.byIndexStatic(driveStatus[slot]);
    }

    @ComputerMethod
    private long getFrequencyItemCount() throws ComputerException {
        return computerGetFrequency().getTotalItemCount();
    }

    @ComputerMethod
    private long getFrequencyItemCapacity() throws ComputerException {
        return computerGetFrequency().getTotalItemCountCapacity();
    }

    @ComputerMethod
    private double getFrequencyItemPercentage() throws ComputerException {
        QIOFrequency frequency = computerGetFrequency();
        return frequency.getTotalItemCount() / (double) frequency.getTotalItemCountCapacity();
    }

    @ComputerMethod
    private long getFrequencyItemTypeCount() throws ComputerException {
        return computerGetFrequency().getTotalItemTypes(false);
    }

    @ComputerMethod
    private long getFrequencyItemTypeCapacity() throws ComputerException {
        return computerGetFrequency().getTotalItemTypeCapacity();
    }

    @ComputerMethod
    private double getFrequencyItemTypePercentage() throws ComputerException {
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

        private final ResourceLocation model;

        DriveStatus(ResourceLocation model) {
            this.model = model;
        }

        public static final DriveStatus[] STATUSES = values();

        public int ledIndex() {
            return ordinal() - READY.ordinal();
        }

        public ResourceLocation getModel() {
            return model;
        }

        public static DriveStatus byIndexStatic(int index) {
            return MathUtils.getByIndexMod(STATUSES, index);
        }
    }
}
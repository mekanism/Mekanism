package mekanism.common.tile.qio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.content.qio.IQIODriveHolder;
import mekanism.common.content.qio.QIODriveData;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.inventory.slot.QIODriveSlot;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.WorldUtils;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;

public class TileEntityQIODriveArray extends TileEntityQIOComponent implements IQIODriveHolder {

    public static final ModelProperty<byte[]> DRIVE_STATUS_PROPERTY = new ModelProperty<>();
    private static final int DRIVE_SLOTS = 12;

    private List<IInventorySlot> driveSlots;
    private byte[] driveStatus = new byte[DRIVE_SLOTS];
    private int prevDriveHash = -1;

    public TileEntityQIODriveArray() {
        super(MekanismBlocks.QIO_DRIVE_ARRAY);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        final int xSize = 176;
        driveSlots = new ArrayList<>();
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 6; x++) {
                QIODriveSlot slot = new QIODriveSlot(this, y * 6 + x, xSize / 2 - (6 * 18 / 2) + x * 18, 70 + y * 18);
                driveSlots.add(slot);
                builder.addSlot(slot);
            }
        }
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (world.getGameTime() % 10 == 0) {
            QIOFrequency frequency = getQIOFrequency();
            setActive(frequency != null);
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

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT tag) {
        QIOFrequency freq = getFrequency(FrequencyType.QIO);
        if (freq != null) {
            // save all item data before we save
            freq.saveAll();
        }
        super.write(tag);
        return tag;
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        return new ModelDataMap.Builder().withInitial(DRIVE_STATUS_PROPERTY, driveStatus).build();
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        updateTag.putByteArray(NBTConstants.DRIVES, driveStatus);
        return updateTag;
    }

    @Override
    public void handleUpdateTag(BlockState state, @Nonnull CompoundNBT tag) {
        super.handleUpdateTag(state, tag);
        driveStatus = tag.getByteArray(NBTConstants.DRIVES);
        requestModelDataUpdate();
        WorldUtils.updateBlock(getWorld(), getPos());
    }

    @Override
    public void onDataUpdate() {
        markDirty(false);
    }

    @Override
    public List<IInventorySlot> getDriveSlots() {
        return driveSlots;
    }

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
    }
}

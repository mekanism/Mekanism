package mekanism.common.lib.inventory.personalstorage;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.Mekanism;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.lib.MekanismSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
class PersonalStorageData extends MekanismSavedData {

    private final Map<UUID, PersonalStorageItemInventory> inventoriesById = new HashMap<>();

    PersonalStorageItemInventory getOrAddInventory(UUID id) {
        return inventoriesById.computeIfAbsent(id, unused -> createInventory());
    }

    PersonalStorageItemInventory addInventory(UUID id, List<IInventorySlot> contents) {
        return inventoriesById.computeIfAbsent(id, unused -> {
            PersonalStorageItemInventory inventory = createInventory();
            List<IInventorySlot> inventorySlots = inventory.getInventorySlots(null);
            for (int i = 0, slots = contents.size(); i < slots; i++) {
                inventorySlots.get(i).deserializeNBT(contents.get(i).serializeNBT());
            }
            setDirty();
            return inventory;
        });
    }

    void removeInventory(UUID id) {
        if (this.inventoriesById.remove(id) != null) {
            setDirty();
        }
    }

    @NotNull
    private PersonalStorageItemInventory createInventory() {
        return new PersonalStorageItemInventory(this::setDirty);
    }

    /**
     * {
     *     [NBTConstants.DATA]: [
     *          {
     *              [NBTConstants.PERSONAL_STORAGE_ID]: UUID,
     *              [NBTConstants.ITEMS]: PersonalStorageItemInventory
     *          }
     *     ]
     * }
     */
    @Override
    public void load(@NotNull CompoundTag nbt) {
        ListTag entries = nbt.getList(NBTConstants.DATA, Tag.TAG_COMPOUND);
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            PersonalStorageItemInventory inv = createInventory();
            ContainerType.ITEM.readFrom(entry, inv.getInventorySlots(null));
            inventoriesById.put(entry.getUUID(NBTConstants.PERSONAL_STORAGE_ID), inv);
        }
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        ListTag entries = new ListTag();
        inventoriesById.forEach((uuid, inv) -> {
            CompoundTag nbtEntry = new CompoundTag();
            nbtEntry.putUUID(NBTConstants.PERSONAL_STORAGE_ID, uuid);
            ContainerType.ITEM.saveTo(nbtEntry, inv.getInventorySlots(null));
            entries.add(nbtEntry);
        });
        compoundTag.put(NBTConstants.DATA, entries);
        return compoundTag;
    }

    @Override
    public void save(File file) {
        if (isDirty()) {
            File folder = file.getParentFile();
            if (!folder.exists() && !folder.mkdirs()) {
                Mekanism.logger.error("Could not create personal storage directory, saves may fail");
            }
        }
        super.save(file);
    }
}
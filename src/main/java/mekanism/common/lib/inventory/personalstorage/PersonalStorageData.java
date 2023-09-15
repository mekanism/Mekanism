package mekanism.common.lib.inventory.personalstorage;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.Mekanism;
import mekanism.common.lib.MekanismSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
class PersonalStorageData extends MekanismSavedData {
    private final Map<UUID, PersonalStorageItemInventory> inventoriesById = new HashMap<>();

    PersonalStorageItemInventory getOrAddInventory(UUID id) {
        return inventoriesById.computeIfAbsent(id, unused->createInventory());
    }

    //PersonalStorageItemInventory addInventory(UUID id) {
    //    PersonalStorageItemInventory inv = createInventory();
    //    inventoriesById.put(id, inv);
    //    setDirty();
    //    return inv;
    //}

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
     *     "data": [
     *          {
     *              "uuid": UUID,
     *              "inv": PersonalStorageItemInventory
     *          }
     *     ]
     * }
     */
    @Override
    public void load(@NotNull CompoundTag nbt) {
        ListTag entries = nbt.getList("data", Tag.TAG_COMPOUND);
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            PersonalStorageItemInventory inv = createInventory();
            inv.deserializeNBT(entry.getList("inv", Tag.TAG_COMPOUND));
            inventoriesById.put(entry.getUUID("uuid"), inv);
        }
    }

    @Override
    public CompoundTag save(CompoundTag pCompoundTag) {
        ListTag entries = new ListTag();
        inventoriesById.forEach((uuid, inv) -> {
            CompoundTag nbtEntry = new CompoundTag();
            nbtEntry.putUUID("uuid", uuid);
            nbtEntry.put("inv", inv.serializeNBT());
            entries.add(nbtEntry);
        });
        pCompoundTag.put("data", entries);
        return pCompoundTag;
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

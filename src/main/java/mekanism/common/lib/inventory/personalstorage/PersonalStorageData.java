package mekanism.common.lib.inventory.personalstorage;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import mekanism.api.IContentsListener;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.Mekanism;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

class PersonalStorageData extends SavedData {
    
    private static final Codec<Map<UUID, PersonalStorageItemInventory>> MAP_CODEC = Codec.unboundedMap(
          UUIDUtil.STRING_CODEC,
          PersonalStorageItemInventory.CODEC
    ).promotePartial(error -> Mekanism.logger.error("Some Personal Storage items failed: {}", error));

    public static final Codec<PersonalStorageData> CODEC = MAP_CODEC.xmap(PersonalStorageData::new, it -> it.inventoriesById);

    private final Map<UUID, PersonalStorageItemInventory> inventoriesById = new HashMap<>();
    private final IContentsListener contentsListener = this::setDirty;

    PersonalStorageData() {
    }

    private PersonalStorageData(Map<UUID, PersonalStorageItemInventory> loadedMap) {
        for (Map.Entry<UUID, PersonalStorageItemInventory> entry : loadedMap.entrySet()) {
            PersonalStorageItemInventory value = entry.getValue();
            value.setParent(contentsListener);
            inventoriesById.put(entry.getKey(), value);
        }
    }

    PersonalStorageItemInventory getOrAddInventory(UUID id) {
        return inventoriesById.computeIfAbsent(id, unused -> createInventory());
    }

    @CanIgnoreReturnValue
    PersonalStorageItemInventory addInventory(UUID id, List<IInventorySlot> contents, TransactionContext transaction) {
        PersonalStorageItemInventory inventory = inventoriesById.get(id);
        if (inventory == null) {
            inventory = createInventory();
            inventoriesById.put(id, inventory);
            List<IInventorySlot> inventorySlots = inventory.getContainers();
            for (int i = 0, slots = contents.size(); i < slots; i++) {
                inventorySlots.get(i).copyContents(contents.get(i), transaction);
            }
            setDirty();
        }
        return inventory;
    }

    void removeInventory(UUID id) {
        if (this.inventoriesById.remove(id) != null) {
            setDirty();
        }
    }

    private PersonalStorageItemInventory createInventory() {
        return new PersonalStorageItemInventory(contentsListener);
    }


    public static SavedDataType<PersonalStorageData> createType(UUID id) {
        return new SavedDataType<>(Mekanism.rl("personal_storage/" + id), PersonalStorageData::new, CODEC);
    }
}
package mekanism.common.lib.inventory.personalstorage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.Mekanism;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

/// Inventory for Personal Storages when an item. Handled by the Block when placed in world.
public class PersonalStorageItemInventory extends AbstractPersonalStorageItemInventory {

    public static final Codec<PersonalStorageItemInventory> CODEC = SlotData.CODEC
          .listOf()
          .promotePartial(err -> Mekanism.logger.error("Failed to parse some Personal Storage slots: {}", err))
          .xmap(PersonalStorageItemInventory::new, PersonalStorageItemInventory::toSlotData);

    @Nullable
    private IContentsListener parent;

    private PersonalStorageItemInventory(List<SlotData> loadedData) {
        this.parent = null;
        for (SlotData slotData : loadedData) {
            IInventorySlot slot = slots.get(slotData.slot());
            slot.setContents(slotData.itemType(), slotData.amount(), null);
        }
    }

    PersonalStorageItemInventory(IContentsListener parent) {
        this.parent = parent;
    }

    protected void setParent(IContentsListener newParent) {
        this.parent = newParent;
    }

    private List<SlotData> toSlotData() {
        List<SlotData> out = new ArrayList<>();
        for (int i = 0; i < slots.size(); i++) {
            IInventorySlot slot = slots.get(i);
            if (!slot.isEmpty()) {
                out.add(new SlotData(i, slot.resource(), slot.amountAsLong()));
            }
        }
        return out;
    }

    @Override
    public void onContentsChanged() {
        //TODO - 26.1: Switch back to this once we make make it so onContentsChanged isn't called for deserialization?
        //Objects.requireNonNull(parent, "Incorrect deserialisation, setParent not called").onContentsChanged();
        if (parent != null) {
            parent.onContentsChanged();
        }
    }

    record SlotData(int slot, ItemResource itemType, long amount) {

        public static Codec<SlotData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
              Codec.INT.fieldOf(SerializationConstants.SLOT).forGetter(SlotData::slot),
              ItemResource.OPTIONAL_CODEC.fieldOf(SerializationConstants.TYPE).forGetter(SlotData::itemType),
              ExtraCodecs.NON_NEGATIVE_LONG.fieldOf(SerializationConstants.AMOUNT).forGetter(SlotData::amount)
        ).apply(instance, SlotData::new));
    }
}
